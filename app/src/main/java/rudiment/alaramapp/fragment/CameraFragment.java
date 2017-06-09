package rudiment.alaramapp.fragment;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.cameraview.AspectRatio;
import com.google.android.cameraview.CameraView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import rudiment.alaramapp.AspectRatioFragment;
import rudiment.alaramapp.R;
import rudiment.alaramapp.activity.CameraActivity;

import static rudiment.alaramapp.activity.CameraActivity.imageFile;

public class CameraFragment extends Fragment implements
        ActivityCompat.OnRequestPermissionsResultCallback,
        AspectRatioFragment.Listener {


    private static final String TAG = "MainActivity";

    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private static final String FRAGMENT_DIALOG = "dialog";

    private static final int[] FLASH_OPTIONS = {
            CameraView.FLASH_AUTO,
            CameraView.FLASH_OFF,
            CameraView.FLASH_ON,
    };

    private static final int[] FLASH_ICONS = {
            R.drawable.ic_flash_auto,
            R.drawable.ic_flash_off,
            R.drawable.ic_flash_on,
    };

    private static final int[] FLASH_TITLES = {
            R.string.flash_auto,
            R.string.flash_off,
            R.string.flash_on,
    };

    private int mCurrentFlash;
    private static CameraView mCameraView;

    private Handler mBackgroundHandler;
    private OnCameraFragmentInteractionListener mListener;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.take_picture:
                    if (mCameraView != null && mCameraView.isShown()) {
                        mCameraView.takePicture();
                    }
                    break;
            }
        }
    };

    public static void takePicture() {
        mCameraView.takePicture();
    }

    public CameraFragment() {
        // Required empty public constructor
    }

    public static CameraFragment newInstance() {
        CameraFragment fragment = new CameraFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCameraView = (CameraView) view.findViewById(R.id.camera);
        mCameraView.setVisibility(View.VISIBLE);
        if (mCameraView != null) {
            setCameraFacing();
            mCameraView.addCallback(mCallback);
        }
        ((CameraActivity) getActivity()).setBottomButtonVisibility(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        startCamera();
    }

    private void startCamera() {
        mCameraView.setVisibility(View.VISIBLE);
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                mCameraView.start();
                enableZoom();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.CAMERA)) {
            ConfirmationDialogFragment.newInstance(R.string.camera_permission_confirmation,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION,
                    R.string.camera_permission_not_granted)
                    .show(getActivity().getSupportFragmentManager(), FRAGMENT_DIALOG);
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    private void stopCamera() {
        mCameraView.stop();
        mCameraView.setVisibility(View.GONE);
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            mCameraView.stop();
            mCameraView.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBackgroundHandler != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBackgroundHandler.getLooper().quitSafely();
            } else {
                mBackgroundHandler.getLooper().quit();
            }
            mBackgroundHandler = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (permissions.length != 1 || grantResults.length != 1) {
                    throw new RuntimeException("Error on requesting camera permission.");
                }
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(), R.string.camera_permission_not_granted,
                            Toast.LENGTH_SHORT).show();
                }
                // No need to start camera here; it is handled by onResume
                break;
        }
    }

    @Override
    public void onAspectRatioSelected(@NonNull AspectRatio ratio) {
        if (mCameraView != null) {
            Toast.makeText(getActivity(), ratio.toString(), Toast.LENGTH_SHORT).show();
            mCameraView.setAspectRatio(ratio);
        }
    }

    //private ZoomControls zoomControls;
    private void enableZoom() {
        mCameraView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                // Get the pointer ID
                Camera.Parameters params = mCameraView.getCamera().getParameters();
                int action = event.getAction();

                if (event.getPointerCount() > 1) {
                    // handle multi-touch events
                    if (action == MotionEvent.ACTION_POINTER_DOWN) {
                        mDist = getFingerSpacing(event);
                    } else if (action == MotionEvent.ACTION_MOVE && params.isZoomSupported()) {
                        mCameraView.getCamera().cancelAutoFocus();
                        handleZoom(event, params);
                    }
                } else {
                    // handle single touch events
                    if (action == MotionEvent.ACTION_UP) {
                        handleFocus(event, params);
                    }
                }
                return true;
            }
        });
    }

    float mDist = 0;
    private void handleZoom(MotionEvent event, Camera.Parameters params) {
        int maxZoom = params.getMaxZoom();
        int zoom = params.getZoom();
        float newDist = getFingerSpacing(event);
        if (newDist > mDist) {
            //zoom in
            if (zoom < maxZoom)
                zoom++;
        } else if (newDist < mDist) {
            //zoom out
            if (zoom > 0)
                zoom--;
        }
        mDist = newDist;
        params.setZoom(zoom);
        mCameraView.getCamera().setParameters(params);
    }

    /**
     * Determine the space between the first two fingers
     */
    private float getFingerSpacing(MotionEvent event) {
        // ...
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    public void handleFocus(MotionEvent event, Camera.Parameters params) {
        int pointerId = event.getPointerId(0);
        int pointerIndex = event.findPointerIndex(pointerId);
        // Get the pointer's current position
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        List<String> supportedFocusModes = params.getSupportedFocusModes();
        if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            mCameraView.getCamera().autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean b, Camera camera) {
                    // currently set to auto-focus on single touch
                }
            });
        }
    }

    /**
     * Enables zoom feature in native camera .  Called from listener of the view
     * used for zoom in  and zoom out.
     *
     * @param zoomInOrOut "false" for zoom in and "true" for zoom out
     */
    public void zoomCamera(boolean zoomInOrOut) {
        if (mCameraView != null) {
            if (mCameraView.getCamera() != null) {
                Camera.Parameters parameter = mCameraView.getCamera().getParameters();

                if (parameter.isZoomSupported()) {
                    int MAX_ZOOM = parameter.getMaxZoom();
                    int currnetZoom = parameter.getZoom();
                    if (zoomInOrOut && (currnetZoom < MAX_ZOOM && currnetZoom >= 0)) {
                        currnetZoom += 5;
                        parameter.setZoom(currnetZoom);
                    } else if (!zoomInOrOut && (currnetZoom <= MAX_ZOOM && currnetZoom > 0)) {
                        currnetZoom -= 5;
                        parameter.setZoom(currnetZoom);
                    }
                } else
                    Toast.makeText(getActivity(), "Zoom Not Available", Toast.LENGTH_LONG).show();

                mCameraView.getCamera().setParameters(parameter);
            }
        }
    }

    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

    private CameraView.Callback mCallback = new CameraView.Callback() {

        @Override
        public void onCameraOpened(CameraView cameraView) {


            Log.d(TAG, "onCameraOpened");
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            Log.d(TAG, "onCameraClosed");
        }

        @Override
        public void onPictureTaken(CameraView cameraView, final byte[] data) {
            //Log.d(TAG, "onPictureTaken " + data.length);
            /*Toast.makeText(getActivity(), R.string.picture_taken, Toast.LENGTH_SHORT)
                    .show();*/
            getBackgroundHandler().post(new Runnable() {
                @Override
                public void run() {
                    imageFile = new File(getActivity().getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                            "picture.jpg");
                    OutputStream os = null;
                    try {
                        os = new FileOutputStream(imageFile);
                        os.write(data);
                        os.close();
                    } catch (IOException e) {
                        Log.w(TAG, "Cannot write to " + imageFile, e);
                    } finally {
                        if (os != null) {
                            try {
                                os.close();
                            } catch (IOException e) {
                                // Ignore9
                            }
                        }
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                           /* loadSticker();
                            mTakenImg.setImageURI(Uri.fromFile(file));
                            stopCamera();*/
                        }
                    });
                    ////..bbbbbbbbb
                }
            });

            mListener.onCameraFragmentInteraction();
        }
    };

    private void setCameraFacing() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            String face = bundle.getString("face");
            if (face != null && !face.isEmpty()) {
                if (face.equalsIgnoreCase("front")) {
                    mCameraView.setFacing(CameraView.FACING_FRONT);
                } else {
                    mCameraView.setFacing(CameraView.FACING_BACK);
                }

            } else {
                mCameraView.setFacing(CameraView.FACING_BACK);
            }
        } else {
            mCameraView.setFacing(CameraView.FACING_BACK);
        }
    }

    public static class ConfirmationDialogFragment extends DialogFragment {

        private static final String ARG_MESSAGE = "message";
        private static final String ARG_PERMISSIONS = "permissions";
        private static final String ARG_REQUEST_CODE = "request_code";
        private static final String ARG_NOT_GRANTED_MESSAGE = "not_granted_message";

        public static ConfirmationDialogFragment newInstance(@StringRes int message,
                                                             String[] permissions, int requestCode, @StringRes int notGrantedMessage) {
            ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_MESSAGE, message);
            args.putStringArray(ARG_PERMISSIONS, permissions);
            args.putInt(ARG_REQUEST_CODE, requestCode);
            args.putInt(ARG_NOT_GRANTED_MESSAGE, notGrantedMessage);
            fragment.setArguments(args);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Bundle args = getArguments();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(args.getInt(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String[] permissions = args.getStringArray(ARG_PERMISSIONS);
                                    if (permissions == null) {
                                        throw new IllegalArgumentException();
                                    }
                                    ActivityCompat.requestPermissions(getActivity(),
                                            permissions, args.getInt(ARG_REQUEST_CODE));
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getActivity(),
                                            args.getInt(ARG_NOT_GRANTED_MESSAGE),
                                            Toast.LENGTH_SHORT).show();
                                }
                            })
                    .create();
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCameraFragmentInteractionListener) {
            mListener = (OnCameraFragmentInteractionListener) context;
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

    public void onButtonPressed() {
        if (mListener != null) {
            mListener.onCameraFragmentInteraction();
        }
    }

    public interface OnCameraFragmentInteractionListener {
        void onCameraFragmentInteraction();
    }
}
