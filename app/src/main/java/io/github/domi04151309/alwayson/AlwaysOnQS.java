package io.github.domi04151309.alwayson;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.preference.PreferenceManager;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.N)
public class AlwaysOnQS extends TileService{

    @Override
    public void onClick() {
       updateTile(getPref());
    }

    private void updateTile(boolean isActive){
        Tile tile = this.getQsTile();
        Icon newIcon;
        int newState;

        if (isActive) {
            newIcon = Icon.createWithResource(getApplicationContext(), R.drawable.ic_qs_alwayson);
            newState = Tile.STATE_ACTIVE;
            this.startService(new Intent(this,MainService.class));
        } else {
            newIcon = Icon.createWithResource(getApplicationContext(), R.drawable.ic_qs_alwayson);
            newState = Tile.STATE_INACTIVE;
        }
        tile.setIcon(newIcon);
        tile.setState(newState);
        tile.updateTile();
    }

    private boolean getPref() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isActive = !prefs.getBoolean("always_on", false);
        prefs.edit().putBoolean("always_on", isActive).apply();
        Toast.makeText(this, String.valueOf(isActive), Toast.LENGTH_LONG).show();
        return isActive;
    }
}