package rudiment.alaramapp.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rudiment.alaramapp.R;
import rudiment.alaramapp.receiver.AlarmBroadcastRecevice;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
import static com.yalantis.ucrop.util.BitmapLoadUtils.calculateInSampleSize;

/**
 * Created by RWS 6 on 1/2/2017.
 */

public class Utility {

    public static String ADDED_NEW_REMINDER = "rudiment.alaramapp.util.added_new_reminder";

    public static boolean isEmailAddressValid(String email) {
        boolean isEmailValid = false;
        String strExpression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;
        Pattern objPattern = Pattern.compile(strExpression, Pattern.CASE_INSENSITIVE);
        Matcher objMatcher = objPattern.matcher(inputStr);
        if (objMatcher.matches()) {
            isEmailValid = true;
        }
        return isEmailValid;
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        boolean check = false;
        if (!Pattern.matches("[a-zA-Z]+", phoneNumber)) {
            if (phoneNumber.length() < 6 || phoneNumber.length() > 13) {
                check = false;
            } else {
                check = true;
            }
        } else {
            check = false;
        }
        return check;
    }

    public static String encodeImage(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.NO_WRAP);
        return encImage;
    }

    public static Fragment getVisibleFragment(AppCompatActivity activity) {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null && fragment.isVisible())
                    return fragment;
            }
        }
        return null;
    }

    public static void showMessage(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public static Bitmap handleSamplingAndRotationBitmap(Context context, Uri selectedImage)
            throws IOException {
        int MAX_HEIGHT = 1024;
        int MAX_WIDTH = 1024;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        img = rotateImageIfRequired(img, selectedImage);
        return img;
    }

    private static Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {

        ExifInterface ei = new ExifInterface(selectedImage.getPath());
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    public static void showKeyboard(EditText yourEditText, Activity activity, boolean showKeyboard) {
        try {
            if (showKeyboard) {
                InputMethodManager input = (InputMethodManager) activity
                        .getSystemService(Activity.INPUT_METHOD_SERVICE);
                input.showSoftInput(yourEditText, InputMethodManager.SHOW_IMPLICIT);
            } else {
                InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAppOpened() {
        ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);
        return (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE);
    }

    public static void setAlarm(Context context, long timeInMilis, String reminder_id) {
        AlarmManager am = (AlarmManager) (context.getSystemService(Context.ALARM_SERVICE));
        Intent intent = new Intent(context, AlarmBroadcastRecevice.class);
        intent.putExtra("reminder_id", reminder_id);
        PendingIntent pi = PendingIntent.getBroadcast(context, NotificationID.getNextNotifId(context), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(timeInMilis, pi);
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMilis, pi);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, timeInMilis, pi);
        }
    }

    public static void showToast(Context context, String msg) {
        if (msg == null || msg.isEmpty()) {
            msg = context.getString(R.string.wrong);
        }
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }


    public static void setDialog(Context appContext, String titleStr, String msgStr, String leftStr, String rightStr, final DialogListener dialogListener) {
        final Dialog dialog = new Dialog(appContext);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView title = (TextView) dialog.findViewById(R.id.title);
        TextView msg = (TextView) dialog.findViewById(R.id.msg);

        title.setText(titleStr);
        msg.setText(msgStr);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.cancel_action:
                        dialog.dismiss();
                        dialogListener.onNegative(dialog);
                        break;
                    case R.id.send_action:
                        dialog.dismiss();
                        dialogListener.onPositive(dialog);
                        break;
                }
            }
        };

        Button left = (Button) dialog.findViewById(R.id.cancel_action);
        Button right = (Button) dialog.findViewById(R.id.send_action);

        left.setText(leftStr);
        right.setText(rightStr);

        dialog.findViewById(R.id.cancel_action).setOnClickListener(onClickListener);
        dialog.findViewById(R.id.send_action).setOnClickListener(onClickListener);
        dialog.show();
    }
}
