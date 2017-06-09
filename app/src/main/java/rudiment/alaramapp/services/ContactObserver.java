package rudiment.alaramapp.services;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

/**
 * Created by RWS 6 on 4/12/2017.
 */

public class ContactObserver extends ContentObserver {

    private OnUpdate onUpdate;

    public interface OnUpdate {
        void onUpdate(Uri uri);
    }

    public ContactObserver(OnUpdate onUpdate) {
        super(new Handler());
        this.onUpdate = onUpdate;
    }

    @Override
    public void onChange(boolean selfChange) {
        this.onChange(selfChange, null);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        // this is NOT UI thread, this is a BACKGROUND thread
        uri.getQueryParameterNames();
        onUpdate.onUpdate(uri);
    }
}
