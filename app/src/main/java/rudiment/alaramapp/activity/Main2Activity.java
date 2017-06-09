package rudiment.alaramapp.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;

import com.xiaopo.flying.sticker.BitmapStickerIcon;
import com.xiaopo.flying.sticker.DeleteIconEvent;
import com.xiaopo.flying.sticker.FlipHorizontallyEvent;
import com.xiaopo.flying.sticker.Sticker;
import com.xiaopo.flying.sticker.StickerView;
import com.xiaopo.flying.sticker.TextSticker;
import com.xiaopo.flying.sticker.ZoomIconEvent;

import java.util.Arrays;

import rudiment.alaramapp.R;

public class Main2Activity extends AppCompatActivity {
    private static final String TAG = Main2Activity.class.getSimpleName();
    private StickerView stickerView;
    private TextSticker sticker;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        stickerView = (StickerView) findViewById(R.id.sticker_view);


        //currently you can config your own icons and icon event
        //the event you can custom
        BitmapStickerIcon deleteIcon = new BitmapStickerIcon(ContextCompat.getDrawable(this,
                com.xiaopo.flying.sticker.R.drawable.sticker_ic_close_white_18dp),
                BitmapStickerIcon.LEFT_TOP);
        deleteIcon.setIconEvent(new DeleteIconEvent());

        BitmapStickerIcon zoomIcon = new BitmapStickerIcon(ContextCompat.getDrawable(this,
                com.xiaopo.flying.sticker.R.drawable.sticker_ic_scale_white_18dp),
                BitmapStickerIcon.RIGHT_BOTOM);
        zoomIcon.setIconEvent(new ZoomIconEvent());

        BitmapStickerIcon flipIcon = new BitmapStickerIcon(ContextCompat.getDrawable(this,
                com.xiaopo.flying.sticker.R.drawable.sticker_ic_flip_white_18dp),
                BitmapStickerIcon.RIGHT_TOP);
        flipIcon.setIconEvent(new FlipHorizontallyEvent());

        BitmapStickerIcon heartIcon =
                new BitmapStickerIcon(ContextCompat.getDrawable(this,  com.xiaopo.flying.sticker.R.drawable.sticker_ic_close_white_18dp),
                        BitmapStickerIcon.LEFT_BOTTOM);

        stickerView.setIcons(Arrays.asList(deleteIcon, zoomIcon, flipIcon, heartIcon));

        //default icon layout
        //stickerView.configDefaultIcons();

        stickerView.setBackgroundColor(Color.WHITE);
        stickerView.setLocked(false);
        stickerView.setConstrained(true);

        sticker = new TextSticker(this);

        sticker.setDrawable(ContextCompat.getDrawable(getApplicationContext(),
                R.drawable.sticker_transparent_background));
        sticker.setText("Hello, world!");
        sticker.setTextColor(Color.BLACK);
        sticker.setTextAlign(Layout.Alignment.ALIGN_CENTER);
        sticker.resizeText();

        stickerView.setOnStickerOperationListener(new StickerView.OnStickerOperationListener() {
            @Override public void onStickerClicked(Sticker sticker) {
                //stickerView.removeAllSticker();
                if (sticker instanceof TextSticker) {
                    stickerView.replace(sticker);
                    stickerView.invalidate();
                }
                Log.d(TAG, "onStickerClicked");
            }

            @Override public void onStickerDeleted(Sticker sticker) {
                Log.d(TAG, "onStickerDeleted");
            }

            @Override public void onStickerDragFinished(Sticker sticker) {
                Log.d(TAG, "onStickerDragFinished");
            }

            @Override public void onStickerZoomFinished(Sticker sticker) {
                Log.d(TAG, "onStickerZoomFinished");
            }

            @Override public void onStickerFlipped(Sticker sticker) {
                Log.d(TAG, "onStickerFlipped");
            }

            @Override public void onStickerDoubleTapped(Sticker sticker) {
                Log.d(TAG, "onDoubleTapped: double tap will be with two click");
            }
        });

            loadSticker();

    }

    private void loadSticker() {
        final TextSticker textSticker = new TextSticker(getApplicationContext());
        textSticker.setText("Sticker\n");
        textSticker.setMaxTextSize(14);
        textSticker.resizeText();
        stickerView.addSticker(textSticker);
    }


}
