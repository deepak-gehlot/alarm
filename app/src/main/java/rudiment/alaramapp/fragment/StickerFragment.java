package rudiment.alaramapp.fragment;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
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
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;

import java.io.File;
import java.io.IOException;

import rudiment.alaramapp.DrawingView;
import rudiment.alaramapp.R;
import rudiment.alaramapp.activity.CameraActivity;
import rudiment.alaramapp.util.FileUtil;
import rudiment.alaramapp.util.Utility;

import static rudiment.alaramapp.activity.CameraActivity.imageFile;

//TextFragment
public class StickerFragment extends Fragment implements View.OnClickListener {

    public StickerFragment() {
        // Required empty public constructor
    }

    OnClickColorPickerButton mListener;
    private String stickerText = "";
    private ImageView takenImg;
    private static RelativeLayout sticker_view;
    private RelativeLayout sticker_view1;
    DrawingView mDrawingView;
    private boolean isDrawing = false;
    private static boolean isMenuVisible = false;
    int tagEdit = 0;

    public String[] MATERIAL_COLORS = {
            "#000000", // BLACK
            "#F44336", // RED 500
            "#E91E63", // PINK 500
            "#9C27B0", // PURPLE 500
            "#673AB7", // DEEP PURPLE 500
            "#3F51B5", // INDIGO 500
            "#2196F3", // BLUE 500
            "#03A9F4", // LIGHT BLUE 500
            "#00BCD4", // CYAN 500
            "#009688", // TEAL 500
            "#4CAF50", // GREEN 500
            "#8BC34A", // LIGHT GREEN 500
            "#CDDC39", // LIME 500
            "#FFEB3B", // YELLOW 500
            "#FFC107", // AMBER 500
            "#FF9800", // ORANGE 500
    };

    private static FloatingActionButton mColorBtn, mTypeBtn, mDrawBtn, mRedoBtn, mUndoBtn, mMenuBtn;

    public static StickerFragment newInstance() {
        StickerFragment fragment = new StickerFragment();
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


        takenImg = (ImageView) view.findViewById(R.id.takenImg);
        sticker_view = (RelativeLayout) view.findViewById(R.id.sticker_view);
        sticker_view1 = (RelativeLayout) view.findViewById(R.id.sticker_view1);
        LinearLayout mDrawingPad = (LinearLayout) view.findViewById(R.id.view_drawing_pad);
        mDrawingPad.addView(mDrawingView);
        addActionClickListener(view);

        aQuery = new AQuery(getActivity());
        if (imageFile != null) {
            try {
                aQuery.id(takenImg).image(Utility.handleSamplingAndRotationBitmap(getActivity(), Uri.fromFile(imageFile)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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

        ((CameraActivity)getActivity()).setBottomButtonVisibility(true);
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
                showHideMenu(getActivity());
                showColorPickerDialog();
                //mListener.openColorPicker();
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
                showHideMenu(getActivity());
                break;
        }
    }

    private void showColorPickerDialog() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_color_picker);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                switch (view.getId()) {
                    case R.id.view_one:
                        setColorForDrawing(Color.parseColor(MATERIAL_COLORS[0]));
                        break;
                    case R.id.view_two:
                        setColorForDrawing(Color.parseColor(MATERIAL_COLORS[1]));
                        break;
                    case R.id.view_three:
                        setColorForDrawing(Color.parseColor(MATERIAL_COLORS[2]));
                        break;
                    case R.id.view_four:
                        setColorForDrawing(Color.parseColor(MATERIAL_COLORS[3]));
                        break;
                    case R.id.view_five:
                        setColorForDrawing(Color.parseColor(MATERIAL_COLORS[4]));
                        break;
                    case R.id.view_six:
                        setColorForDrawing(Color.parseColor(MATERIAL_COLORS[5]));
                        break;
                    case R.id.view_seven:
                        setColorForDrawing(Color.parseColor(MATERIAL_COLORS[6]));
                        break;
                    case R.id.view_eight:
                        setColorForDrawing(Color.parseColor(MATERIAL_COLORS[7]));
                        break;
                    case R.id.view_nine:
                        setColorForDrawing(Color.parseColor(MATERIAL_COLORS[8]));
                        break;
                    case R.id.view_ten:
                        setColorForDrawing(Color.parseColor(MATERIAL_COLORS[9]));
                        break;
                    case R.id.view_eleven:
                        setColorForDrawing(Color.parseColor(MATERIAL_COLORS[10]));
                        break;
                    case R.id.view_twelve:
                        setColorForDrawing(Color.parseColor(MATERIAL_COLORS[11]));
                        break;
                    case R.id.view_thirteen:
                        setColorForDrawing(Color.parseColor(MATERIAL_COLORS[12]));
                        break;
                    case R.id.view_fourteen:
                        setColorForDrawing(Color.parseColor(MATERIAL_COLORS[13]));
                        break;
                    case R.id.view_fifteen:
                        setColorForDrawing(Color.parseColor(MATERIAL_COLORS[14]));
                        break;
                    case R.id.view_sixteen:
                        setColorForDrawing(Color.parseColor(MATERIAL_COLORS[15]));
                        break;
                }
            }
        };

        dialog.findViewById(R.id.view_one).setOnClickListener(onClickListener);
        dialog.findViewById(R.id.view_two).setOnClickListener(onClickListener);
        dialog.findViewById(R.id.view_three).setOnClickListener(onClickListener);
        dialog.findViewById(R.id.view_four).setOnClickListener(onClickListener);
        dialog.findViewById(R.id.view_five).setOnClickListener(onClickListener);
        dialog.findViewById(R.id.view_six).setOnClickListener(onClickListener);
        dialog.findViewById(R.id.view_seven).setOnClickListener(onClickListener);
        dialog.findViewById(R.id.view_eight).setOnClickListener(onClickListener);
        dialog.findViewById(R.id.view_nine).setOnClickListener(onClickListener);
        dialog.findViewById(R.id.view_ten).setOnClickListener(onClickListener);
        dialog.findViewById(R.id.view_eleven).setOnClickListener(onClickListener);
        dialog.findViewById(R.id.view_twelve).setOnClickListener(onClickListener);
        dialog.findViewById(R.id.view_thirteen).setOnClickListener(onClickListener);
        dialog.findViewById(R.id.view_fourteen).setOnClickListener(onClickListener);
        dialog.findViewById(R.id.view_fifteen).setOnClickListener(onClickListener);
        dialog.findViewById(R.id.view_sixteen).setOnClickListener(onClickListener);

        dialog.show();
    }

    private void setColorForDrawing(int color) {
        StickerFragment fragment = (StickerFragment) getActivity().getSupportFragmentManager().findFragmentByTag("StickerFragment");
        TextFragment textFragment = (TextFragment) getActivity().getSupportFragmentManager().findFragmentByTag("TextFragment");
        if (fragment != null) {
            fragment.onColorSelected(0, color);
        }
        if (textFragment != null) {
            textFragment.onColorSelected(0, color);
        }
    }

    private static void showHideMenu(Context context) {
        if (isMenuVisible) {
            mTypeBtn.hide();
            mDrawBtn.hide();
            mRedoBtn.hide();
            mUndoBtn.hide();
            mColorBtn.hide();
            isMenuVisible = false;
            mMenuBtn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_menu_black_24dp));
        } else {
            mTypeBtn.show();
            mDrawBtn.show();
            mRedoBtn.show();
            mUndoBtn.show();
            mColorBtn.show();
            isMenuVisible = true;
            mMenuBtn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_close_black_24dp));
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
    }

    public interface OnClickColorPickerButton {
        public void openColorPicker();
    }
}
