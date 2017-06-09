package rudiment.alaramapp.services;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;

/**
 * Created by RWS 6 on 4/12/2017.
 */

public class ContactObserverService extends Service implements ContactObserver.OnUpdate {

    private ContactObserver contactObserver;

    @Override
    public void onCreate() {
        super.onCreate();
        contactObserver = new ContactObserver(this);
        this.getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, false, contactObserver);
    }

    @Override
    public void onDestroy() {
        if (contactObserver != null) {
            this.getContentResolver().unregisterContentObserver(contactObserver);
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        return START_STICKY;
    }

    @Override
    public void onUpdate(Uri uri) {
     /*   NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setContentTitle("Contact Update Notifier")
                .setContentText(uri.getQuery())
                .setSmallIcon(R.mipmap.ic_launcher);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());

        // Create a new dispatcher using the Google Play driver.
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(ContactObserverService.this));
        Job myJob = dispatcher.newJobBuilder()
                .setService(UpdateContactJobService.class) // the JobService that will be called
                .setTag("update_contact")        // uniquely identifies the job
                .build();
        dispatcher.mustSchedule(myJob);*/
    }
}
