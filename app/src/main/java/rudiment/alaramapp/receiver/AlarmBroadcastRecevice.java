package rudiment.alaramapp.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.WakefulBroadcastReceiver;

import rudiment.alaramapp.R;
import rudiment.alaramapp.activity.HomeActivity;
import rudiment.alaramapp.activity.ShowReminderActivity;
import rudiment.alaramapp.util.NotificationID;
import rudiment.alaramapp.util.Utility;

public class AlarmBroadcastRecevice extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        String reminder_id = bundle.getString("reminder_id");

        if (Utility.isAppOpened()) {
            context.startActivity(new Intent(context, ShowReminderActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("reminder_id", reminder_id));
        } else {
            showNotification(context, "" + reminder_id);
        }
    }

    private void showNotification(Context context, String msg) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.app_name) + " Reminder")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentText(msg);
        //mBuilder.setNumber();
// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, ShowReminderActivity.class);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(HomeActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(NotificationID.getNextNotifId(context), mBuilder.build());
    }
}
