package io.github.domi04151309.alwayson.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.service.quicksettings.TileService
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import io.github.domi04151309.alwayson.BuildConfig
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.custom.BasePreferenceFragment
import io.github.domi04151309.alwayson.helpers.Global
import io.github.domi04151309.alwayson.helpers.P
import io.github.domi04151309.alwayson.helpers.Permissions
import io.github.domi04151309.alwayson.helpers.PreferenceScreenHelper
import io.github.domi04151309.alwayson.receivers.AlwaysOnAppWidgetProvider
import io.github.domi04151309.alwayson.services.AlwaysOnTileService
import io.github.domi04151309.alwayson.services.ForegroundService

class MainActivity : BaseActivity() {
    enum class Dialog {
        DEVICE_ADMIN,
        NOTIFICATION_ACCESS,
        DISPLAY_OVER_OTHER_APPS,
        PHONE_STATE,
        CALENDAR,
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.statusBarColor = SurfaceColors.SURFACE_0.getColor(this)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, GeneralPreferenceFragment())
            .commit()

        ContextCompat.startForegroundService(this, Intent(this, ForegroundService::class.java))

        onBackPressedDispatcher.addCallback {
            startActivity(
                Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }

    override fun onStart() {
        super.onStart()

        if (Permissions.needsNotificationPermissions(this)) buildDialog(Dialog.NOTIFICATION_ACCESS)
        if (Permissions.needsDeviceAdminOrRoot(this)) buildDialog(Dialog.DEVICE_ADMIN)
        if (Permissions.needsCalendarPermission(this)) buildDialog(Dialog.CALENDAR)

        if (!Permissions.hasPhoneStatePermission(this)) buildDialog(Dialog.PHONE_STATE)
        if (!Settings.canDrawOverlays(this)) buildDialog(Dialog.DISPLAY_OVER_OTHER_APPS)
    }

    @SuppressLint("InflateParams")
    private fun showDialog(
        icon: Int,
        title: Int,
        message: Int,
        onOk: DialogInterface.OnClickListener,
    ) {
        val builder = MaterialAlertDialogBuilder(this)
        val view = layoutInflater.inflate(R.layout.dialog_permission, null, false)
        view.findViewById<ImageView>(R.id.icon).setImageResource(icon)
        view.findViewById<TextView>(R.id.title).setText(title)
        view.findViewById<TextView>(R.id.message).setText(message)
        builder.setPositiveButton(resources.getString(android.R.string.ok), onOk)
        builder.setView(view)
        builder.setNegativeButton(resources.getString(android.R.string.cancel)) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    @SuppressLint("InflateParams")
    private fun buildDialog(dialogType: Dialog) {
        when (dialogType) {
            Dialog.DEVICE_ADMIN ->
                showDialog(
                    R.drawable.ic_color_mode,
                    R.string.device_admin,
                    R.string.device_admin_summary,
                ) { _, _ ->
                    startActivity(Intent(this, PermissionsActivity::class.java))
                }

            Dialog.DISPLAY_OVER_OTHER_APPS ->
                showDialog(
                    R.drawable.ic_color_draw_over_other_apps,
                    R.string.setup_draw_over_other_apps,
                    R.string.setup_draw_over_other_apps_summary,
                ) { _, _ ->
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                }

            Dialog.NOTIFICATION_ACCESS ->
                showDialog(
                    R.drawable.ic_color_notification,
                    R.string.notification_listener_service,
                    R.string.notification_listener_service_summary,
                ) { _, _ ->
                    startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                }

            Dialog.PHONE_STATE ->
                showDialog(
                    R.drawable.ic_color_phone_state,
                    R.string.setup_phone_state,
                    R.string.setup_phone_state_summary,
                ) { _, _ ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_PHONE_STATE),
                        0,
                    )
                }

            Dialog.CALENDAR ->
                showDialog(
                    R.drawable.ic_color_calendar,
                    R.string.setup_calendar,
                    R.string.setup_calendar_summary,
                ) { _, _ ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_CALENDAR),
                        0,
                    )
                }
        }
    }

    class GeneralPreferenceFragment : BasePreferenceFragment() {
        private var debugClicker = 0

        @Suppress("SameReturnValue")
        private fun onAlwaysOnClicked(): Boolean {
            TileService.requestListeningState(
                context,
                ComponentName(requireContext(), AlwaysOnTileService::class.java),
            )
            requireContext().sendBroadcast(
                Intent(context, AlwaysOnAppWidgetProvider::class.java)
                    .setAction(Global.ALWAYS_ON_STATE_CHANGED),
            )
            if (BuildConfig.DEBUG) {
                debugClicker++
                if (debugClicker == CLICKS_TIL_TEST) {
                    debugClicker = 0
                    startActivity(Intent(requireContext(), AODTestActivity::class.java))
                }
            }
            return true
        }

        override fun onCreatePreferences(
            savedInstanceState: Bundle?,
            rootKey: String?,
        ) {
            addPreferencesFromResource(R.xml.pref_main)
            checkPermissions()

            findPreference<Preference>(P.ALWAYS_ON)?.setOnPreferenceClickListener {
                onAlwaysOnClicked()
            }
            PreferenceScreenHelper.linkPreferenceToActivity(
                this,
                "pref_watch_face",
                Intent(requireContext(), LAFWatchFaceActivity::class.java),
            )
            PreferenceScreenHelper.linkPreferenceToActivity(
                this,
                "pref_background",
                Intent(requireContext(), LAFBackgroundActivity::class.java),
            )
            PreferenceScreenHelper.linkPreferenceToActivity(
                this,
                "rules",
                Intent(requireContext(), LAFRulesActivity::class.java),
            )
            PreferenceScreenHelper.linkPreferenceToActivity(
                this,
                "pref_behavior",
                Intent(requireContext(), LAFBehaviorActivity::class.java),
            )
            PreferenceScreenHelper.linkPreferenceToActivity(
                this,
                P.CHARGING_STYLE,
                Intent(requireContext(), LAFChargingLookActivity::class.java),
            )
            findPreference<Preference>("dark_mode")?.setOnPreferenceClickListener {
                activity?.recreate()
                true
            }
            PreferenceScreenHelper.linkPreferenceToActivity(
                this,
                "pref_permissions",
                Intent(requireContext(), PermissionsActivity::class.java),
            )
            PreferenceScreenHelper.linkPreferenceToActivity(
                this,
                "pref_help",
                Intent(requireContext(), HelpActivity::class.java),
            )
            PreferenceScreenHelper.linkPreferenceToActivity(
                this,
                "pref_about",
                Intent(requireContext(), AboutActivity::class.java),
            )
        }

        companion object {
            private const val CLICKS_TIL_TEST = 4
        }
    }
}
