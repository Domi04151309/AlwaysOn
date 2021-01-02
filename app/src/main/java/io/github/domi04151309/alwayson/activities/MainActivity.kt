package io.github.domi04151309.alwayson.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.service.quicksettings.TileService
import android.text.TextUtils
import android.view.KeyEvent
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.services.AlwaysOnTileService
import io.github.domi04151309.alwayson.helpers.Global
import io.github.domi04151309.alwayson.helpers.Theme
import io.github.domi04151309.alwayson.receivers.AdminReceiver
import io.github.domi04151309.alwayson.services.ForegroundService

class MainActivity : AppCompatActivity() {

    companion object {
        private const val DEVICE_ADMIN_DIALOG: Byte = 0
        private const val NOTIFICATION_ACCESS_DIALOG: Byte = 1
        private const val DISPLAY_OVER_OTHER_APPS_DIALOG: Byte = 2
    }

    private var showsDialog = false

    private val isNotificationServiceEnabled: Boolean
        get() {
            val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            if (!TextUtils.isEmpty(flat)) {
                val names = flat.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (name in names) {
                    val cn = ComponentName.unflattenFromString(name)
                    if (cn != null) {
                        if (TextUtils.equals(packageName, cn.packageName)) {
                            return true
                        }
                    }
                }
            }
            return false
        }

    private val isDeviceAdminOrRoot: Boolean
        get() {
            return if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("root_mode", false)) {
                true
            } else {
                (getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager)
                        .isAdminActive(ComponentName(this, AdminReceiver::class.java))
            }
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

        if (!isNotificationServiceEnabled) buildDialog(NOTIFICATION_ACCESS_DIALOG)
        if (applicationContext.checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_PHONE_STATE),
                    0)
        }

        ContextCompat.startForegroundService(this, Intent(this, ForegroundService::class.java))
    }

    override fun onStart() {
        super.onStart()
        if (!Settings.canDrawOverlays(this)) buildDialog(DISPLAY_OVER_OTHER_APPS_DIALOG)
        if (!isDeviceAdminOrRoot) buildDialog(DEVICE_ADMIN_DIALOG)
    }

    @SuppressLint("InflateParams")
    private fun buildDialog(dialogType: Byte) {
        if (!showsDialog) {
            val builder = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.dialog_permission, null, false)
            val icon = view.findViewById<ImageView>(R.id.icon)
            val title = view.findViewById<TextView>(R.id.title)
            val message = view.findViewById<TextView>(R.id.message)

            when (dialogType) {
                DEVICE_ADMIN_DIALOG -> {
                    icon.setImageResource(R.drawable.ic_color_mode)
                    title.setText(R.string.device_admin)
                    message.setText(R.string.device_admin_summary)
                    builder.setPositiveButton(resources.getString(android.R.string.ok)) { _, _ ->
                        startActivity(Intent(this, PermissionsActivity::class.java))
                    }
                }
                DISPLAY_OVER_OTHER_APPS_DIALOG -> {
                    icon.setImageResource(R.drawable.ic_color_draw_over_other_apps)
                    title.setText(R.string.setup_draw_over_other_apps)
                    message.setText(R.string.setup_draw_over_other_apps_summary)
                    builder.setPositiveButton(resources.getString(android.R.string.ok)) { _, _ ->
                        startActivityForResult(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION), 1)
                    }
                }
                NOTIFICATION_ACCESS_DIALOG -> {
                    icon.setImageResource(R.drawable.ic_color_notification)
                    title.setText(R.string.notification_listener_service)
                    message.setText(R.string.notification_listener_service_summary)
                    builder.setPositiveButton(resources.getString(android.R.string.ok)) { _, _ ->
                        startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                    }
                }
                else -> return
            }
            builder.setView(view)
            builder.setNegativeButton(resources.getString(android.R.string.cancel)) { dialog, _ -> dialog.cancel() }
            builder.setOnDismissListener {
                showsDialog = false
            }
            builder.show()
            showsDialog = true
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    class GeneralPreferenceFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_main)
            findPreference<Preference>("always_on")?.setOnPreferenceClickListener {
                TileService.requestListeningState(context, ComponentName(requireContext(), AlwaysOnTileService::class.java))
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(Intent().setAction(Global.ALWAYS_ON_STATE_CHANGED))
                true
            }
            findPreference<Preference>("pref_look_and_feel")?.setOnPreferenceClickListener {
                startActivity(Intent(context, LookAndFeelActivity::class.java))
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
