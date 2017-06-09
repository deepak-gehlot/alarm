package rudiment.alaramapp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by RWS 6 on 3/16/2017.
 */

public class NotificationID {
    private static final String PREFERENCE_LAST_NOTIF_ID = "PREFERENCE_LAST_NOTIF_ID";

    public static int getNextNotifId(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int id = sharedPreferences.getInt(PREFERENCE_LAST_NOTIF_ID, 0) + 1;
        if (id == Integer.MAX_VALUE) {
            id = 0;
        }
        sharedPreferences.edit().putInt(PREFERENCE_LAST_NOTIF_ID, id).apply();
        return id;
    }
}
