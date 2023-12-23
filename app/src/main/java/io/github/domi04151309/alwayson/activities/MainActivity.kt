package io.github.domi04151309.alwayson.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.service.quicksettings.TileService
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.helpers.Global
import io.github.domi04151309.alwayson.helpers.P
import io.github.domi04151309.alwayson.helpers.Permissions
import io.github.domi04151309.alwayson.helpers.Theme
import io.github.domi04151309.alwayson.receivers.AlwaysOnAppWidgetProvider
import io.github.domi04151309.alwayson.services.AlwaysOnTileService
import io.github.domi04151309.alwayson.services.ForegroundService

class MainActivity : AppCompatActivity() {
    enum class Dialogs {
        DEVICE_ADMIN,
        NOTIFICATION_ACCESS,
        DISPLAY_OVER_OTHER_APPS,
        PHONE_STATE,
        CALENDAR,
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val actionBar = supportActionBar ?: return
        actionBar.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        actionBar.setDisplayShowCustomEnabled(true)
        actionBar.setCustomView(R.layout.action_bar)
        actionBar.elevation = 0f

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

        if (Permissions.needsNotificationPermissions(this)) buildDialog(Dialogs.NOTIFICATION_ACCESS)
        if (Permissions.needsDeviceAdminOrRoot(this)) buildDialog(Dialogs.DEVICE_ADMIN)
        if (Permissions.needsCalendarPermission(this)) buildDialog(Dialogs.CALENDAR)

        if (!Permissions.hasPhoneStatePermission(this)) buildDialog(Dialogs.PHONE_STATE)
        if (!Settings.canDrawOverlays(this)) buildDialog(Dialogs.DISPLAY_OVER_OTHER_APPS)
    }

    @SuppressLint("InflateParams")
    private fun buildDialog(dialogType: Dialogs) {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_permission, null, false)
        val icon = view.findViewById<ImageView>(R.id.icon)
        val title = view.findViewById<TextView>(R.id.title)
        val message = view.findViewById<TextView>(R.id.message)

        when (dialogType) {
            Dialogs.DEVICE_ADMIN -> {
                icon.setImageResource(R.drawable.ic_color_mode)
                title.setText(R.string.device_admin)
                message.setText(R.string.device_admin_summary)
                builder.setPositiveButton(resources.getString(android.R.string.ok)) { _, _ ->
                    startActivity(Intent(this, PermissionsActivity::class.java))
                }
            }
            Dialogs.DISPLAY_OVER_OTHER_APPS -> {
                icon.setImageResource(R.drawable.ic_color_draw_over_other_apps)
                title.setText(R.string.setup_draw_over_other_apps)
                message.setText(R.string.setup_draw_over_other_apps_summary)
                builder.setPositiveButton(resources.getString(android.R.string.ok)) { _, _ ->
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                }
            }
            Dialogs.NOTIFICATION_ACCESS -> {
                icon.setImageResource(R.drawable.ic_color_notification)
                title.setText(R.string.notification_listener_service)
                message.setText(R.string.notification_listener_service_summary)
                builder.setPositiveButton(resources.getString(android.R.string.ok)) { _, _ ->
                    startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                }
            }
            Dialogs.PHONE_STATE -> {
                icon.setImageResource(R.drawable.ic_color_phone_state)
                title.setText(R.string.setup_phone_state)
                message.setText(R.string.setup_phone_state_summary)
                builder.setPositiveButton(resources.getString(android.R.string.ok)) { _, _ ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_PHONE_STATE),
                        0,
                    )
                }
            }
            Dialogs.CALENDAR -> {
                icon.setImageResource(R.drawable.ic_color_calendar)
                title.setText(R.string.setup_calendar)
                message.setText(R.string.setup_calendar_summary)
                builder.setPositiveButton(resources.getString(android.R.string.ok)) { _, _ ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_CALENDAR),
                        0,
                    )
                }
            }
        }
        builder.setView(view)
        builder.setNegativeButton(resources.getString(android.R.string.cancel)) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    class GeneralPreferenceFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(
            savedInstanceState: Bundle?,
            rootKey: String?,
        ) {
            addPreferencesFromResource(R.xml.pref_main)

            if (!Permissions.isDeviceAdminOrRoot(requireContext())) {
                var currentPref: Preference?
                var currentPrefAsSwitch: SwitchPreference?
                Permissions.DEVICE_ADMIN_OR_ROOT_PERMISSION_PREFS.forEach {
                    currentPref = findPreference(it)
                    if (currentPref != null) {
                        currentPref?.isEnabled = false
                        currentPref?.setSummary(R.string.permissions_device_admin_or_root)
                        currentPrefAsSwitch = currentPref as? SwitchPreference
                        if (currentPrefAsSwitch != null) {
                            currentPrefAsSwitch?.setSummaryOff(
                                R.string.permissions_device_admin_or_root,
                            )
                            currentPrefAsSwitch?.setSummaryOn(
                                R.string.permissions_device_admin_or_root,
                            )
                        }
                    }
                }
            }

            findPreference<Preference>("always_on")?.setOnPreferenceClickListener {
                TileService.requestListeningState(
                    context,
                    ComponentName(requireContext(), AlwaysOnTileService::class.java),
                )
                requireContext().sendBroadcast(
                    Intent(context, AlwaysOnAppWidgetProvider::class.java)
                        .setAction(Global.ALWAYS_ON_STATE_CHANGED),
                )
                true
            }
            findPreference<Preference>("pref_watch_face")?.setOnPreferenceClickListener {
                startActivity(Intent(context, LAFWatchFaceActivity::class.java))
                true
            }
            findPreference<Preference>("pref_background")?.setOnPreferenceClickListener {
                startActivity(Intent(context, LAFBackgroundActivity::class.java))
                true
            }
            findPreference<Preference>("rules")?.setOnPreferenceClickListener {
                startActivity(Intent(context, LAFRulesActivity::class.java))
                true
            }
            findPreference<Preference>("pref_behavior")?.setOnPreferenceClickListener {
                startActivity(Intent(context, LAFBehaviorActivity::class.java))
                true
            }
            findPreference<Preference>(P.CHARGING_STYLE)?.setOnPreferenceClickListener {
                startActivity(Intent(context, LAFChargingLookActivity::class.java))
                true
            }
            findPreference<Preference>("dark_mode")?.setOnPreferenceClickListener {
                activity?.recreate()
                true
            }
            findPreference<Preference>("pref_permissions")?.setOnPreferenceClickListener {
                startActivity(Intent(context, PermissionsActivity::class.java))
                true
            }
            findPreference<Preference>("pref_help")?.setOnPreferenceClickListener {
                startActivity(Intent(context, HelpActivity::class.java))
                true
            }
            findPreference<Preference>("pref_about")?.setOnPreferenceClickListener {
                startActivity(Intent(context, AboutActivity::class.java))
                true
            }
        }
    }
}
