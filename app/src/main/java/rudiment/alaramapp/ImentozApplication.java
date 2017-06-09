package rudiment.alaramapp;

import android.content.Context;
import android.support.multidex.MultiDex;

import com.inscripts.activities.CCApplication;
import com.inscripts.cometchat.sdk.CometChat;

/**
 * Created by RWS 6 on 3/28/2017.
 */

public class ImentozApplication extends CCApplication {

    static CometChat cometChat;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);


    }

    public static CometChat getCometChatInstance() {
        return cometChat;
    }
}
