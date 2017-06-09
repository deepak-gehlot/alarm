package rudiment.alaramapp.dao;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by RWS 6 on 1/2/2017.
 */

public class QueryManager {


    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.MINUTES)
            .writeTimeout(3, TimeUnit.MINUTES)
            .readTimeout(3, TimeUnit.MINUTES)
            .build();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static volatile QueryManager instance = null;

    // private constructor
    private QueryManager() {
    }

    public static QueryManager getInstance() {
        if (instance == null) {
            synchronized (QueryManager.class) {
                // Double check
                if (instance == null) {
                    instance = new QueryManager();
                }
            }
        }
        return instance;
    }

    public void postRequest(String url, String json, final Callback callback) {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public void postRequest(String url, String json, final rudiment.alaramapp.util.Callback callback) {
        RequestBody body = RequestBody.create(JSON, json);
        final Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResult("");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResult(result);
                    }
                });
            }
        });
    }
}
