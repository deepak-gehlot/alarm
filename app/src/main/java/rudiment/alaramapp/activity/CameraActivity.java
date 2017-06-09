package rudiment.alaramapp.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.jksiezni.permissive.PermissionsGrantedListener;
import com.github.jksiezni.permissive.PermissionsRefusedListener;
import com.github.jksiezni.permissive.Permissive;
import com.jrummyapps.android.colorpicker.ColorPickerDialog;
import com.jrummyapps.android.colorpicker.ColorPickerDialogListener;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cn.iwgang.countdownview.CountdownView;
import rudiment.alaramapp.R;
import rudiment.alaramapp.adapter.NavigationItemAdapter;
import rudiment.alaramapp.fragment.CameraFragment;
import rudiment.alaramapp.fragment.SetReminderFragment;
import rudiment.alaramapp.fragment.StickerFragment;
import rudiment.alaramapp.fragment.TextFragment;
import rudiment.alaramapp.util.Constant;
import rudiment.alaramapp.util.PreferenceConnector;

import static rudiment.alaramapp.activity.HomeActivity.currentPos;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener, CameraFragment.OnCameraFragmentInteractionListener, ColorPickerDialogListener, StickerFragment.OnClickColorPickerButton, TextFragment.OnClickColorPickerButton {

    private static FragmentManager fragmentManager;
    public static File imageFile;
    private ProgressDialog progressDialog;
    private ImageView mCameraSwithcImg, mTextSwitchImg;
    private static LinearLayout bottomPanel;
    private static RelativeLayout bottomPanelMain;
    private DrawerLayout drawer;
    private ImageView mMenuIcon;

    private static final String CAMERA = "camera";
    private static final String TEXT = "text";
    private static final String STICKER = "sticker";
    private static final String REMINDER = "reminder";

    private static String currentFragment = "";
    public static String mImageBase64Str = "";
    public static String mRemindeStr = "";
    private String FACE = "back";
    private CountdownView countdownView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        init();

        new Permissive.Request(Manifest.permission.CAMERA)
                .whenPermissionsGranted(new PermissionsGrantedListener() {
                    @Override
                    public void onPermissionsGranted(String[] permissions) throws SecurityException {
                        // given permissions are granted
                        final ProgressDialog progressDialog = ProgressDialog.show(CameraActivity.this, "", "Loading camera", false, false);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setFirstView();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                    }
                                }, 500);
                            }
                        }, 1000);
                    }
                })
                .whenPermissionsRefused(new PermissionsRefusedListener() {
                    @Override
                    public void onPermissionsRefused(String[] permissions) {
                        // given permissions are refused
                        Toast.makeText(CameraActivity.this, "You need to allow camera permission.", Toast.LENGTH_SHORT).show();
                    }
                }).execute(CameraActivity.this);

        setNavigationMenu();
    }

    private void setFirstView() {
        if (PreferenceConnector.readString(CameraActivity.this, PreferenceConnector.REMINDER_TYPE, "").equals(Constant.TYPE_TEXT)) {
            bottomPanel.setVisibility(View.VISIBLE);
            bottomPanelMain.setVisibility(View.GONE);
            mCameraSwithcImg.setImageResource(R.drawable.camera_unactive);
            mTextSwitchImg.setImageResource(R.drawable.abc_active);
            addTextView();
        } else if (PreferenceConnector.readString(CameraActivity.this, PreferenceConnector.REMINDER_TYPE, "").equals(Constant.TYPE_CAMERA)) {
            bottomPanel.setVisibility(View.GONE);
            bottomPanelMain.setVisibility(View.VISIBLE);
            mCameraSwithcImg.setImageResource(R.drawable.camera);
            mTextSwitchImg.setImageResource(R.drawable.abc);
            addCameraView("back");
        } else {
            bottomPanel.setVisibility(View.GONE);
            bottomPanelMain.setVisibility(View.VISIBLE);
            mCameraSwithcImg.setImageResource(R.drawable.camera);
            mTextSwitchImg.setImageResource(R.drawable.abc);
            addCameraView("back");
        }
    }

    private void init() {
        mMenuIcon = (ImageView) findViewById(R.id.menuIcon);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        findViewById(R.id.setReminderImgLayout).setOnClickListener(CameraActivity.this);
        findViewById(R.id.linear_layout_back).setOnClickListener(CameraActivity.this);
        findViewById(R.id.take_picture).setOnClickListener(CameraActivity.this);
        fragmentManager = getSupportFragmentManager();

        mCameraSwithcImg = (ImageView) findViewById(R.id.cameraSwitchImg);
        mTextSwitchImg = (ImageView) findViewById(R.id.textSwitchImg);
        bottomPanel = (LinearLayout) findViewById(R.id.bottomPanel);
        bottomPanelMain = (RelativeLayout) findViewById(R.id.bottomPanelMain);
        countdownView = (CountdownView) findViewById(R.id.countdownView);

        mCameraSwithcImg.setOnClickListener(this);
        mTextSwitchImg.setOnClickListener(this);
        mCameraSwithcImg.setImageResource(R.drawable.camera);
        mTextSwitchImg.setImageResource(R.drawable.abc);


        //countdownView.start(995550000);
        //countdownView.start(getTimeInMilliSeconds("2017-04-30 18:50"));
    }

    public long getTimeInMilliSeconds(String oldDateTime) {
        Long timeDiff = 0l;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date oldDate = dateFormat.parse(oldDateTime);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String currentDateandTime = sdf.format(new Date());
            Date cDate = sdf.parse(currentDateandTime);
            //Date cDate = new Date();
            timeDiff = oldDate.getTime() - cDate.getTime();
            return timeDiff;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeDiff;
    }

    private void changeSwitch(String switchTo, String face) {
        switch (switchTo) {
            case CAMERA:
                mCameraSwithcImg.setImageResource(R.drawable.camera);
                mTextSwitchImg.setImageResource(R.drawable.abc);
                PreferenceConnector.writeString(CameraActivity.this, PreferenceConnector.REMINDER_TYPE, Constant.TYPE_CAMERA);
                addCameraView(face);
                bottomPanel.setVisibility(View.GONE);
                break;
            case TEXT:
                bottomPanel.setVisibility(View.VISIBLE);
                mCameraSwithcImg.setImageResource(R.drawable.camera_unactive);
                mTextSwitchImg.setImageResource(R.drawable.abc_active);
                PreferenceConnector.writeString(CameraActivity.this, PreferenceConnector.REMINDER_TYPE, Constant.TYPE_TEXT);
                addTextView();
                break;
        }
    }

    @Override
    public void onCameraFragmentInteraction() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                bottomPanel.setVisibility(View.VISIBLE);
                addStickerView(CameraActivity.this);
            }
        }, 300);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.setReminderImgLayout:
                if (currentFragment.equals(STICKER)) {
                    nextStepWithImage();
                } else if (currentFragment.equals(TEXT)) {
                    nextStepWithText();
                }
                break;
            case R.id.linear_layout_back:
                finish();
                startActivity(new Intent(CameraActivity.this, CameraActivity.class));
                break;
            case R.id.take_picture:
                onTakePicktureBtnClick();
                break;
            case R.id.cameraSwitchImg:
                if (currentFragment.equals(TEXT)) {

                } else if (FACE.equalsIgnoreCase("front")) {
                    FACE = "back";
                } else {
                    FACE = "front";
                }
                changeSwitch(CAMERA, FACE);
                break;
            case R.id.textSwitchImg:
                changeSwitch(TEXT, "");
                break;
        }
    }

    private void onTakePicktureBtnClick() {
        switch (currentFragment) {
            case CAMERA:
                progressDialog = ProgressDialog.show(CameraActivity.this, "", "Taking picture", true, true);
                CameraFragment.takePicture();
                break;
            case STICKER:
                //nextStepWithImage();
                break;
            case TEXT:
                nextStepWithText();
                break;
        }
    }

    public void setBottomButtonVisibility(boolean flag) {
        if (flag) {
            findViewById(R.id.setReminderImgLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.linear_layout_back).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.setReminderImgLayout).setVisibility(View.GONE);
            findViewById(R.id.linear_layout_back).setVisibility(View.GONE);
        }

    }

    private void nextStepWithText() {
        final ProgressDialog progressDialog = ProgressDialog.show(CameraActivity.this, "", "Saving text...", false, false);

        TextFragment.save(CameraActivity.this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                //  finish();
                // startActivity(new Intent(CameraActivity.this, SetReminderActivity.class));
                addReminderViewFinal();
            }
        }, 700);
    }

    private void nextStepWithImage() {
        new Permissive.Request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .whenPermissionsGranted(new PermissionsGrantedListener() {
                    @Override
                    public void onPermissionsGranted(String[] permissions) throws SecurityException {
                        // given permissions are granted
                        final ProgressDialog progressDialog = ProgressDialog.show(CameraActivity.this, "", "Saving image...", false, false);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                StickerFragment.save(CameraActivity.this);
                            }
                        }).start();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                //finish();
                                // startActivity(new Intent(CameraActivity.this, SetReminderActivity.class));
                                addReminderViewFinal();
                            }
                        }, 700);
                    }
                })
                .whenPermissionsRefused(new PermissionsRefusedListener() {
                    @Override
                    public void onPermissionsRefused(String[] permissions) {
                        // given permissions are refused
                        Toast.makeText(CameraActivity.this, "You need to allow permission.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).execute(CameraActivity.this);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public static void addCameraView(String cameraFace) {
        currentFragment = CAMERA;
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putString("face", cameraFace);
        CameraFragment cameraFragment = CameraFragment.newInstance();
        cameraFragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.root_frame, cameraFragment, "CameraFragment");
        fragmentTransaction.commit();
    }

    public static void addStickerView(Context context) {
        bottomPanelMain.setVisibility(View.VISIBLE);
        currentFragment = STICKER;
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.root_frame, StickerFragment.newInstance(), "StickerFragment");
        fragmentTransaction.commit();
    }

    public static void addReminderViewFinal() {
        bottomPanelMain.setVisibility(View.GONE);
        bottomPanel.setVisibility(View.GONE);
        currentFragment = REMINDER;
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.root_frame, SetReminderFragment.newInstance(), "SetReminderFragment");
        fragmentTransaction.commit();
    }

    public static void addTextView() {
        bottomPanelMain.setVisibility(View.VISIBLE);
        currentFragment = TEXT;
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.root_frame, TextFragment.newInstance(), "TextFragment");
        fragmentTransaction.commit();
    }

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.aspect_ratio:
                if (mCameraView != null) {
                    final Set<AspectRatio> ratios = mCameraView.getSupportedAspectRatios();
                    final AspectRatio currentRatio = mCameraView.getAspectRatio();
                    AspectRatioFragment.newInstance(ratios, currentRatio)
                            .show(getSupportFragmentManager(), FRAGMENT_DIALOG);
                }
                break;
            case R.id.switch_flash:
                if (mCameraView != null) {
                    mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.length;
                    item.setTitle(FLASH_TITLES[mCurrentFlash]);
                    item.setIcon(FLASH_ICONS[mCurrentFlash]);
                    mCameraView.setFlash(FLASH_OPTIONS[mCurrentFlash]);
                }
                break;
            case R.id.switch_camera:
                if (mCameraView != null) {
                    int facing = mCameraView.getFacing();
                    mCameraView.setFacing(facing == CameraView.FACING_FRONT ?
                            CameraView.FACING_BACK : CameraView.FACING_FRONT);
                }
                break;
        }
        return false;
    }*/

    @Override
    public void openColorPicker() {
        ColorPickerDialog.newBuilder()
                .setDialogType(ColorPickerDialog.TYPE_PRESETS)
                .setAllowPresets(true)
                .setDialogId(0)
                .setColor(Color.BLACK)
                .setShowAlphaSlider(true)
                .show(CameraActivity.this);

    }

    @Override
    public void onColorSelected(int dialogId, @ColorInt int color) {
        StickerFragment fragment = (StickerFragment) getSupportFragmentManager().findFragmentByTag("StickerFragment");
        TextFragment textFragment = (TextFragment) getSupportFragmentManager().findFragmentByTag("TextFragment");
        if (fragment != null) {
            fragment.onColorSelected(dialogId, color);
        }
        if (textFragment != null) {
            textFragment.onColorSelected(dialogId, color);
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }

    @Override
    public void onBackPressed() {
        if (currentFragment == REMINDER) {
            setFirstView();
        } else {
            super.onBackPressed();
        }
    }

    private void setNavigationMenu() {
        RecyclerView list = (RecyclerView) findViewById(R.id.navigationList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CameraActivity.this);
        list.setLayoutManager(linearLayoutManager);
        ArrayList<String> items = new ArrayList<>();
        ArrayList<Integer> itemIcons = new ArrayList<>();
        items.add(getString(R.string.text_groups));
        items.add(getString(R.string.text_setting));
        items.add(getString(R.string.text_my_to_do));
        items.add(getString(R.string.text_contact));
        items.add(getString(R.string.text_contact));

        itemIcons.add(R.drawable.group);
        itemIcons.add(R.drawable.setting);
        itemIcons.add(R.drawable.to_do);
        itemIcons.add(R.drawable.contact);
        itemIcons.add(R.drawable.contact);

        NavigationItemAdapter navigationItemAdapter = new NavigationItemAdapter(items, itemIcons);
        list.setAdapter(navigationItemAdapter);
        navigationItemAdapter.setOnNavigationClickListener(new NavigationItemAdapter.OnNavigationClick() {
            @Override
            public void onClick(int position) {
                drawer.closeDrawer(GravityCompat.START);
                switch (position) {
                    case 0:
                        currentPos = 0;
                        startActivity(new Intent(CameraActivity.this, HomeActivity.class));
                        break;
                    case 1:
                        currentPos = 1;
                        startActivity(new Intent(CameraActivity.this, HomeActivity.class));
                        break;
                    case 2:
                        currentPos = 2;
                        startActivity(new Intent(CameraActivity.this, HomeActivity.class));
                        break;
                    case 3:
                        currentPos = 3;
                        startActivity(new Intent(CameraActivity.this, HomeActivity.class));
                        break;
                    case 4:

                        break;
                }
                finish();
            }
        });

        mMenuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(GravityCompat.START);
            }
        });
    }

}
