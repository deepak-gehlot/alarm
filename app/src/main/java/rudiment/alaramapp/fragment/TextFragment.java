package rudiment.alaramapp.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.androidquery.AQuery;

import java.io.File;

import rudiment.alaramapp.DrawingView;
import rudiment.alaramapp.R;
import rudiment.alaramapp.activity.CameraActivity;
import rudiment.alaramapp.util.FileUtil;
import rudiment.alaramapp.util.Utility;

//TextFragment
public class TextFragment extends Fragment implements View.OnClickListener {

    public TextFragment() {
        // Required empty public constructor
    }

    OnClickColorPickerButton mListener;
    private String stickerText = "";
    private ImageView takenImg;
    private static View view;
    private static RelativeLayout sticker_view;
    private RelativeLayout sticker_view1;
    DrawingView mDrawingView;
    private boolean isDrawing = false;
    private boolean isMenuVisible = false;
    int tagEdit = 0;

    private FloatingActionButton mColorBtn, mTypeBtn, mDrawBtn, mRedoBtn, mUndoBtn, mMenuBtn;

    public static TextFragment newInstance() {
        TextFragment fragment = new TextFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDrawingView = new DrawingView(getActivity());
        mDrawingView.setEnabled(isDrawing);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sticker, container, false);
    }

    private AQuery aQuery;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;

        takenImg = (ImageView) view.findViewById(R.id.takenImg);
        sticker_view = (RelativeLayout) view.findViewById(R.id.sticker_view);
        sticker_view1 = (RelativeLayout) view.findViewById(R.id.sticker_view1);
        LinearLayout mDrawingPad = (LinearLayout) view.findViewById(R.id.view_drawing_pad);
        mDrawingPad.addView(mDrawingView);
        addActionClickListener(view);

        aQuery = new AQuery(getActivity());
        aQuery.id(takenImg).image(R.drawable.edit_bg);

        sticker_view1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (isDrawing) {
                    return false;
                } else {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            EditText yourEditText = (EditText) getView().findViewById(R.id.edit);
                            RelativeLayout parent = (RelativeLayout) getView().findViewById(R.id.sticker_parent);
                            yourEditText.setTextSize(18);
                            yourEditText.setVisibility(View.VISIBLE);
                            yourEditText.setTypeface(null, Typeface.BOLD);
                            yourEditText.setTextColor(Color.WHITE);
                            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            layoutParams.setMargins((int) motionEvent.getX(), (int) motionEvent.getY(), 16, 5);
                            parent.removeAllViews();
                            parent.addView(yourEditText, layoutParams);
                            yourEditText.setFocusable(true);
                            yourEditText.requestFocus();
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(yourEditText, InputMethodManager.SHOW_IMPLICIT);
                            break;
                    }
                    return true;
                }
            }
        });

       /* view.findViewById(R.id.click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText yourEditText = (EditText) getView().findViewById(R.id.edit);
                if (isDrawing) {
                    isDrawing = false;
                    mDrawingView.setEndableDisable(isDrawing);
                    yourEditText.setEnabled(true);
                } else {
                    isDrawing = true;
                    mDrawingView.setEndableDisable(isDrawing);
                    yourEditText.setEnabled(false);
                }
            }
        });*/
    }

    private void addActionClickListener(View view) {
        mColorBtn = (FloatingActionButton) view.findViewById(R.id.colorBtn);
        mDrawBtn = (FloatingActionButton) view.findViewById(R.id.drawBtn);
        mRedoBtn = (FloatingActionButton) view.findViewById(R.id.redoBtn);
        mUndoBtn = (FloatingActionButton) view.findViewById(R.id.undoBtn);
        mTypeBtn = (FloatingActionButton) view.findViewById(R.id.typeBtn);
        mMenuBtn = (FloatingActionButton) view.findViewById(R.id.menuBtn);

        mColorBtn.setOnClickListener(this);
        mDrawBtn.setOnClickListener(this);
        mRedoBtn.setOnClickListener(this);
        mUndoBtn.setOnClickListener(this);
        mTypeBtn.setOnClickListener(this);
        mMenuBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.colorBtn:
                showHideMenu();
                mListener.openColorPicker();
                break;
            case R.id.typeBtn:
                if (tagEdit == 0) {
                    tagEdit = 1;
                    Toast.makeText(getActivity(), "Touch anywhere in screen and start typing.", Toast.LENGTH_SHORT).show();
                }
                mTypeBtn.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.blue));
                mDrawBtn.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.error_color));
                //   showHideMenu();
                switchBetweenTypeDrawing(true);
                break;
            case R.id.drawBtn:
                //showHideMenu();
                mDrawBtn.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.blue));
                mTypeBtn.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.error_color));
                switchBetweenTypeDrawing(false);
                break;
            case R.id.redoBtn:
                mDrawingView.redoAnnotation();
                break;
            case R.id.undoBtn:
                mDrawingView.undoAnnotation();
                break;
            case R.id.menuBtn:
                showHideMenu();
                break;
        }
    }

    private void showHideMenu() {
        if (isMenuVisible) {
            mTypeBtn.hide();
            mDrawBtn.hide();
            mRedoBtn.hide();
            mUndoBtn.hide();
            mColorBtn.hide();
            isMenuVisible = false;
            mMenuBtn.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_menu_black_24dp));
        } else {
            mTypeBtn.show();
            mDrawBtn.show();
            mRedoBtn.show();
            mUndoBtn.show();
            mColorBtn.show();
            isMenuVisible = true;
            mMenuBtn.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_close_black_24dp));
        }
    }

    private void switchBetweenTypeDrawing(boolean editable) {
        EditText yourEditText = (EditText) getView().findViewById(R.id.edit);
        if (editable) {
            isDrawing = false;
            mDrawingView.setEndableDisable(isDrawing);
            yourEditText.setEnabled(true);
        } else {
            isDrawing = true;
            mDrawingView.setEndableDisable(isDrawing);
            yourEditText.setEnabled(false);
        }
        Utility.showKeyboard(yourEditText, getActivity(), editable);
    }

    public void onColorSelected(int dialogId, @ColorInt int color) {
        mDrawingView.setCurrentColor(color);
    }

    public static void save(Context context) {
        File file = FileUtil.getNewFile(context, "Imentoz_Reminder");
        if (file != null) {
            sticker_view.setDrawingCacheEnabled(true);
            sticker_view.buildDrawingCache();
            Bitmap bm = sticker_view.getDrawingCache();
            CameraActivity.mImageBase64Str = Utility.encodeImage(bm);
        } else {
            Toast.makeText(context, "The file is null", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnClickColorPickerButton) {
            mListener = (OnClickColorPickerButton) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        takenImg.setVisibility(View.GONE);
    }

    public interface OnClickColorPickerButton {
        public void openColorPicker();
    }


}
