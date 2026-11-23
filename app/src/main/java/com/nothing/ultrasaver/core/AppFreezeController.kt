package com.nothing.ultrasaver.core

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.inputmethod.InputMethodManager
import com.nothing.ultrasaver.data.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppFreezeController(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    // Packages that must NEVER be frozen to keep the device functioning safely
    private val hardcodedProtectedPrefixes = setOf(
        "com.android.systemui",
        "android",
        "com.nothing.ultrasaver",
        "moe.shizuku.privileged.api",
        "com.google.android.gms",
        "com.android.settings"
    )

    /**
     * Resolves currently active Input Method Editor (Keyboard) package
     * so user can always type in emergency apps.
     */
    private fun getActiveKeyboardPackage(): String? {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        return imm?.enabledInputMethodList?.firstOrNull()?.packageName
    }

    /**
     * Resolves default Phone / Dialer package.
     */
    fun getDefaultDialerPackage(): String? {
        val intent = Intent(Intent.ACTION_DIAL)
        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo?.activityInfo?.packageName
    }

    /**
     * Returns true if a package is a system essential that should never be frozen.
     */
    fun isProtectedPackage(packageName: String): Boolean {
        if (hardcodedProtectedPrefixes.any { packageName.startsWith(it) }) return true
        if (packageName == getActiveKeyboardPackage()) return true
        if (packageName == getDefaultDialerPackage()) return true
        return false
    }

    /**
     * Scans installed launchable applications for user whitelisting.
     */
    suspend fun getInstalledUserApps(currentWhitelist: Set<String>): List<AppInfo> = withContext(Dispatchers.IO) {
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = packageManager.queryIntentActivities(launcherIntent, 0)

        val dialer = getDefaultDialerPackage()

        val list = mutableListOf<AppInfo>()
        val seen = mutableSetOf<String>()

        for (resolveInfo in resolveInfos) {
            val pkg = resolveInfo.activityInfo.packageName
            if (pkg == context.packageName || seen.contains(pkg)) continue
            seen.add(pkg)

            val appName = resolveInfo.loadLabel(packageManager).toString()
            val icon = resolveInfo.loadIcon(packageManager)
            val isProtected = isProtectedPackage(pkg) || pkg == dialer
            val isWhitelisted = isProtected || currentWhitelist.contains(pkg)

            list.add(
                AppInfo(
                    packageName = pkg,
                    appName = appName,
                    icon = icon,
                    isSystemProtected = isProtected,
                    isWhitelisted = isWhitelisted
                )
            )
        }

        list.sortedWith(
            compareByDescending<AppInfo> { it.isSystemProtected }
                .thenByDescending { it.isWhitelisted }
                .thenBy { it.appName.lowercase() }
        )
    }

    /**
     * Suspends all non-whitelisted apps and force-stops them.
     * Returns the set of packages successfully suspended.
     */
    suspend fun freezeNonWhitelisted(whitelistedPackages: Set<String>): Set<String> = withContext(Dispatchers.IO) {
        val appsToFreeze = mutableSetOf<String>()
        val dialer = getDefaultDialerPackage()

        val installed = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        for (app in installed) {
            val pkg = app.packageName
            // Skip protected and whitelisted
            if (isProtectedPackage(pkg) || pkg == dialer || whitelistedPackages.contains(pkg)) {
                continue
            }
            // Freeze user apps or third-party background services
            val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isUpdatedSystem = (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

            if (!isSystem || isUpdatedSystem) {
                appsToFreeze.add(pkg)
            }
        }

        if (appsToFreeze.isEmpty()) return@withContext emptySet()

        val actuallySuspended = mutableSetOf<String>()
        // Execute fast bulk suspend and force-stop in chunks
        val chunks = appsToFreeze.chunked(30)
        for (chunk in chunks) {
            val pkgList = chunk.joinToString(" ")
            // 1. Bulk suspend all packages in this chunk in a single 0.1s command
            val res = ShizukuManager.execShell("pm suspend $pkgList")
            if (res.isSuccess) {
                actuallySuspended.addAll(chunk)
            }
            // 2. Bulk force-stop in one chained shell command
            val forceStopCmd = chunk.joinToString("; ") { "am force-stop $it" }
            ShizukuManager.execShell(forceStopCmd)
        }

        actuallySuspended
    }

    /**
     * Restores all suspended apps back to normal in fast bulk operations.
     */
    suspend fun unfreezeAll(packagesToUnfreeze: Set<String>) = withContext(Dispatchers.IO) {
        if (packagesToUnfreeze.isEmpty()) return@withContext
        val chunks = packagesToUnfreeze.chunked(40)
        for (chunk in chunks) {
            val pkgList = chunk.joinToString(" ")
            ShizukuManager.execShell("pm unsuspend $pkgList")
        }
    }
}
