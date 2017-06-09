package rudiment.alaramapp;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class TestDrawaing extends Activity {
    //	Reference
    AnnotationView annotationView;
    SeekBar widthSeekBar;
    TextView widthText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DrawingView mDrawingView = new DrawingView(this);
        setContentView(R.layout.activity_test_drawaing);
        LinearLayout mDrawingPad = (LinearLayout) findViewById(R.id.view_drawing_pad);
        mDrawingPad.addView(mDrawingView);

       /* annotationView = (AnnotationView)findViewById(R.id.AnnotationView);
        widthText = (TextView)findViewById(R.id.widthText);
        widthSeekBar = (SeekBar)findViewById(R.id.widthSeekBar);
        widthSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                annotationView.setCurrentWidth(progress+1.0f);
                widthText.setText("Width: "+(progress+1));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });*/
    }

    public void undo(View v) {
        annotationView.undoAnnotation();
    }

    public void redo(View v) {
        annotationView.redoAnnotation();
    }

    public void blackColor(View v) {
        annotationView.setCurrentColor(Color.BLACK);
    }

    public void redColor(View v) {
        annotationView.setCurrentColor(Color.RED);
    }

    public void clear(View v) {
        annotationView.clearAnnotation();
    }
}
