package io.github.domi04151309.alwayson.preferences

import android.Manifest
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
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import io.github.domi04151309.alwayson.*
import io.github.domi04151309.alwayson.objects.Theme
import io.github.domi04151309.alwayson.alwayson.AlwaysOnQS
import io.github.domi04151309.alwayson.objects.Global
import io.github.domi04151309.alwayson.receivers.AdminReceiver
import io.github.domi04151309.alwayson.services.ForegroundService

class Preferences : AppCompatActivity(),
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    companion object {
        private const val DEVICE_ADMIN_DIALOG: Byte = 0
        private const val NOTIFICATION_ACCESS_DIALOG: Byte = 1
        private const val DISPLAY_OVER_OTHER_APPS_DIALOG: Byte = 2
    }
    
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
        if (!isDeviceAdminOrRoot) buildDialog(DEVICE_ADMIN_DIALOG)
        if (!isNotificationServiceEnabled) buildDialog(NOTIFICATION_ACCESS_DIALOG)
        if (!Settings.canDrawOverlays(this)) buildDialog(DISPLAY_OVER_OTHER_APPS_DIALOG)
    }


    private fun buildDialog(dialogType: Byte) {
        val builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.DialogTheme))

        when (dialogType) {
            DEVICE_ADMIN_DIALOG -> {
                builder.setTitle(R.string.device_admin)
                builder.setMessage(R.string.device_admin_summary)
                builder.setPositiveButton(resources.getString(android.R.string.ok)) { _, _ ->
                    startActivity(Intent(this, PermissionPreferences::class.java))
                }
            }
            NOTIFICATION_ACCESS_DIALOG -> {
                builder.setTitle(R.string.notification_listener_service)
                builder.setMessage(R.string.notification_listener_service_summary)
                builder.setPositiveButton(resources.getString(android.R.string.ok)) { _, _ ->
                    startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                }
            }
            DISPLAY_OVER_OTHER_APPS_DIALOG -> {
                builder.setTitle(R.string.setup_draw_over_other_apps)
                builder.setMessage(R.string.setup_draw_over_other_apps_summary)
                builder.setPositiveButton(resources.getString(android.R.string.ok)) { _, _ ->
                    startActivityForResult(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION), 1)
                }
            } else -> return
        }

        builder.setNegativeButton(resources.getString(android.R.string.cancel)) { dialog, _ -> dialog.cancel() }
        builder.show()
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

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
                classLoader,
                pref.fragment)
        fragment.arguments = pref.extras
        fragment.setTargetFragment(caller, 0)
        supportFragmentManager.beginTransaction()
                .replace(R.id.settings, fragment)
                .addToBackStack(null)
                .commit()
        return true
    }

    class GeneralPreferenceFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_general)
            findPreference<Preference>("always_on")?.setOnPreferenceClickListener {
                TileService.requestListeningState(context, ComponentName(requireContext(), AlwaysOnQS::class.java))
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(Intent().setAction(Global.ALWAYS_ON_STATE_CHANGED))
                true
            }
            findPreference<Preference>("pref_look_and_feel")?.setOnPreferenceClickListener {
                startActivity(Intent(context, LookAndFeelPreferences::class.java))
                true
            }
            findPreference<Preference>("pref_permissions")?.setOnPreferenceClickListener {
                startActivity(Intent(context, PermissionPreferences::class.java))
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
