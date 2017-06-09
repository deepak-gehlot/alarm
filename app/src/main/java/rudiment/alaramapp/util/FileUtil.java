package rudiment.alaramapp.util;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by RWS 6 on 2/9/2017.
 */

public class FileUtil {

    private static final String TAG = "FileUtil";

    public static String getFolderName(String name) {
        File mediaStorageDir =
                new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        name);

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return "";
            }
        }
        return mediaStorageDir.getAbsolutePath();
    }

    private static boolean isSDAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static File getNewFile(Context context, String folderName) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");

        String timeStamp = simpleDateFormat.format(new Date());

        String path;
        if (isSDAvailable()) {
            path = getFolderName(folderName) + File.separator + timeStamp + ".jpg";
        } else {
            path = context.getFilesDir().getPath() + File.separator + timeStamp + ".jpg";
        }

        if (TextUtils.isEmpty(path)) {
            return null;
        }

        return new File(path);
    }

    public static void writeFile(Context context, String fileName, String content) throws IOException {
        FileOutputStream fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        fileOutputStream.write(content.getBytes());
        fileOutputStream.close();
    }

    public static String readFile(Context context, String fileName) throws IOException {
        FileInputStream fileInputStream = context.openFileInput(fileName);
        InputStreamReader isr = new InputStreamReader(fileInputStream, "UTF-8");
        BufferedReader bufferedReader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}