package io.github.domi04151309.alwayson;

import android.util.Log;

class Root {

    static void shell(String command) {
        try {
            Process p = Runtime.getRuntime()
                    .exec(new String[]{ "su", "-c", command });
            p.waitFor();
        } catch (Exception ex) {
            Log.e("Superuser", String.valueOf(ex));
        }
    }
}
