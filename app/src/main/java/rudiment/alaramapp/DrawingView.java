package rudiment.alaramapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by RWS 6 on 3/10/2017.
 */

public class DrawingView extends View {
    Paint mPaint;
    //MaskFilter  mEmboss;
    //MaskFilter  mBlur;
    Bitmap mBitmap;
    Canvas mCanvas;
    Path mPath;
    Paint mBitmapPaint;
    private ArrayList<Path> paths = new ArrayList<Path>();
    private ArrayList<Path> undonePaths = new ArrayList<Path>();
    private boolean enable = false;


    //	Annotation Data, each element contains data for a single stroke
    private ArrayList<Float> widths = new ArrayList<Float>();
    private ArrayList<Integer> colors = new ArrayList<Integer>();
    private ArrayList<Integer> undocolors = new ArrayList<Integer>();
    private ArrayList<ArrayList<Point>> strokes = new ArrayList<ArrayList<Point>>();

    //	Current Stroke Data, we assume each stroke to only be a fixed size and color
    //	currentSize and currentColor and available to be set publicly
    private ArrayList<Point> currentStroke;
    private Float currentWidth = 10.0f;
    private int currentColor = Color.BLACK;
    private int currentStrokeIndex = 0;            //	Take note of current point in markerStrokes, used for undo/redo


    public DrawingView(Context context) {
        super(context);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(currentColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(currentWidth);

        mPath = new Path();
        mBitmapPaint = new Paint();
        mBitmapPaint.setColor(Color.RED);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        //    canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        //  canvas.drawPath(mPath, mPaint);

        for (int i = 0; i < paths.size(); i++) {
            Path p = paths.get(i);
            mPaint.setColor(colors.get(i));
            canvas.drawPath(p, mPaint);
        }
        mPaint.setColor(currentColor);
        canvas.drawPath(mPath, mPaint);
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
        undonePaths.clear();
        undocolors.clear();
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, mPaint);
        // kill this so we don't double draw
        paths.add(mPath);
        colors.add(currentColor);
        mPath = new Path();

    }

    public void setEndableDisable(boolean enable) {
        this.enable = enable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (enable) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
            return true;
        } else {
            return false;
        }
    }


   /* //	Method handle touch inputs
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //	Create a new stroke
            currentStroke = new ArrayList<Point>();

            //	Add 3 points to it so we get a dot immediately
            this.addPointAndDraw(new Point((int) event.getX() - 1, (int) event.getY() - 1));
            this.addPointAndDraw(new Point((int) event.getX(), (int) event.getY()));
            this.addPointAndDraw(new Point((int) event.getX() + 1, (int) event.getY() + 1));
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            final int N = event.getHistorySize();
            final int P = event.getPointerCount();
            for (int i = 0; i < N; i++)
                for (int j = 0; j < P; j++)
                    this.addPointAndDraw(new Point((int) event.getHistoricalX(j, i), (int) event.getHistoricalY(j, i)));
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            this.addPointAndDraw(new Point((int) event.getX(), (int) event.getY()));

            //  Clear stored redos after current index
            while (currentStrokeIndex < strokes.size()) {
                strokes.remove(strokes.size() - 1);
                widths.remove(widths.size() - 1);
                colors.remove(colors.size() - 1);
            }

            strokes.add(currentStroke);
            widths.add(currentWidth);
            colors.add(currentColor);

            currentStrokeIndex = strokes.size();
        }

        invalidate();
        return true;
    }*/

    private void addPointAndDraw(Point p) {
        //	Add point
        currentStroke.add(p);

        //	Draw to point, only if there are at least 3 points
        if (currentStroke.size() < 3)
            return;

        //	Draw bezier curve from start to mid to end (3 points)
        this.drawSection(currentStroke.size() - 3);
    }

    //	Draw the ith section in the currentStroke
    private void drawSection(int i) {
        //	Ensure enough points
        if (i > currentStroke.size() - 3)
            return;

        //	Set color and size to paint
        mPaint.setColor(currentColor);
        mPaint.setStrokeWidth(currentWidth);

        Point mid1 = new Point((currentStroke.get(i).x + currentStroke.get(i + 1).x) / 2,
                (currentStroke.get(i).y + currentStroke.get(i + 1).y) / 2);
        Point midmid = currentStroke.get(i + 1);
        Point mid2 = new Point((currentStroke.get(i + 1).x + currentStroke.get(i + 2).x) / 2,
                (currentStroke.get(i + 1).y + currentStroke.get(i + 2).y) / 2);

        Path path = new Path();
        path.moveTo(mid1.x, mid1.y);
        path.quadTo(midmid.x, midmid.y, mid2.x, mid2.y);
        mCanvas.drawPath(path, mPaint);
    }

    //	Method to clear all annotations
    public void clearAnnotation() {
        if (mCanvas != null) {
            mCanvas.drawColor(Color.WHITE);

            //	Reset strokes data
            strokes.clear();
            colors.clear();
            widths.clear();
            currentStrokeIndex = 0;

            invalidate();
        }
    }

    //	Method to Undo annotation
    public void undoAnnotation() {
        if (paths.size() > 0) {
            undonePaths.add(paths.remove(paths.size() - 1));
            undocolors.add(colors.remove(colors.size() - 1));
            invalidate();
        } else {

        }
       /* //	Reset back currentColor and currentWidth after undoing
        Integer originalColor = currentColor;
        Float originalWidth = currentWidth;

        //  Ensure that there are moves to undo
        if (currentStrokeIndex < 1) return;

        //  Reset the image and update currentStrokeIndex
        mCanvas.drawColor(Color.WHITE);
        currentStrokeIndex--;

        //  Redraw the points before
        for (int i = 0; i < currentStrokeIndex; i++) {
            currentStroke = strokes.get(i);
            currentColor = colors.get(i);
            currentWidth = widths.get(i);

            for (int j = 0; j < currentStroke.size(); j++)
                this.drawSection(j);
        }

        //	Reset
        currentColor = originalColor;
        currentWidth = originalWidth;
        invalidate();*/
    }

    //	Method to redo annotation
    public void redoAnnotation() {
      /*  //	Reset back currentColor and currentWidth after undoing
        Integer originalColor = currentColor;
        Float originalWidth = currentWidth;

        //	Ensure that there are moves to redo
        if (currentStrokeIndex >= strokes.size()) return;

        currentStroke = strokes.get(currentStrokeIndex);
        currentColor = colors.get(currentStrokeIndex);
        currentWidth = widths.get(currentStrokeIndex);

        //  Draw each stroke section
        for (int j = 0; j < currentStroke.size(); j++)
            this.drawSection(j);

        //	Update currentStrokeIndex
        currentStrokeIndex++;

        //	Reset
        currentColor = originalColor;
        currentWidth = originalWidth;
        invalidate();*/
        if (undonePaths.size() > 0) {
            paths.add(undonePaths.remove(undonePaths.size() - 1));
            colors.add(undocolors.remove(undocolors.size() - 1));
            invalidate();
        } else {

        }
    }

    public Float getCurrentWidth() {
        return currentWidth;
    }

    public void setCurrentWidth(Float currentWidth) {
        this.currentWidth = currentWidth;
    }

    public int getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(int currentColor) {
        //	Set color and size to paint
        mPaint.setColor(currentColor);
        mPaint.setStrokeWidth(currentWidth);
        this.currentColor = currentColor;
    }
}