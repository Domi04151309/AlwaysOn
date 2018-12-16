package io.github.domi04151309.alwayson;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;

public class Preferences extends AppCompatPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Theme.set(this);
        super.onCreate(savedInstanceState);
        setupActionBar();
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new GeneralPreferenceFragment())
                .commit();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);
            findPreference( "pref_ao" ).setOnPreferenceClickListener( new Preference.OnPreferenceClickListener() {
                @Override public boolean onPreferenceClick( Preference preference ) {
                    getFragmentManager().beginTransaction().replace( android.R.id.content, new PreferenceAlwaysOn() ).addToBackStack( PreferenceAlwaysOn.class.getSimpleName() ).commit();
                    return true;
                }
            } );
            findPreference( "light_mode" ).setOnPreferenceClickListener( new Preference.OnPreferenceClickListener() {
                @Override public boolean onPreferenceClick( Preference preference ) {
                    startActivity(new Intent(getContext(), MainActivity.class));
                    return true;
                }
            } );
            findPreference( "request_root" ).setOnPreferenceClickListener( new Preference.OnPreferenceClickListener() {
                @Override public boolean onPreferenceClick( Preference preference ) {
                    Process p;
                    try {
                        p = Runtime.getRuntime().exec("su");
                        DataOutputStream os = new DataOutputStream(p.getOutputStream());
                        os.writeBytes("echo access granted\n");
                        os.writeBytes("exit\n");
                        os.flush();
                        Toast.makeText(getContext(), "Success!", Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Request failed!", Toast.LENGTH_LONG).show();
                    }
                    return true;
                }
            } );
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), Preferences.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    public static class PreferenceAlwaysOn extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_ao);
            setHasOptionsMenu(true);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), Preferences.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
