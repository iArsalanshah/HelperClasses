package com.citemenu.mystash.home.upload_bill_camera;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.citemenu.mystash.R;
import com.commonsware.cwac.camera.CameraHost;
import com.commonsware.cwac.camera.CameraHostProvider;
import com.commonsware.cwac.camera.CameraView;
import com.commonsware.cwac.camera.PictureTransaction;
import com.commonsware.cwac.camera.SimpleCameraHost;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Created by Dev.Arsalan on 10/31/2016.
 */

public class TakePhoto extends AppCompatActivity implements View.OnClickListener, CameraHostProvider {
    public static ArrayList<String> arrFile;

    CameraView cameraView;

    private Button btnCapture, btnDone, btnCancel;

    TextView txtMsgTop, tvImageCount;
    LinearLayout layoutMsgTop, layoutPreviewTop;

    ImageView imgPreview, imgPreviewTop;

    Bitmap currentBitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.take_photo_preview_activity);
        arrFile = new ArrayList<>();

        cameraView = (CameraView) findViewById(R.id.cameraView);

        layoutMsgTop = (LinearLayout) findViewById(R.id.layoutMsgTop);
        layoutPreviewTop = (LinearLayout) findViewById(R.id.layoutPreviewTop);

        btnCapture = (Button) findViewById(R.id.btnCapture);
        btnDone = (Button) findViewById(R.id.btnDone);
        btnCancel = (Button) findViewById(R.id.btnCancel);

        tvImageCount = (TextView) findViewById(R.id.tvImageCount);
        imgPreview = (ImageView) findViewById(R.id.imgPreview);
        imgPreviewTop = (ImageView) findViewById(R.id.imgPreviewTop);

        txtMsgTop = (TextView) findViewById(R.id.txtMsgTop);

        tvImageCount = (TextView) findViewById(R.id.tvImageCount);
        tvImageCount.setText("0");

        btnCapture.setOnClickListener(this);
        btnDone.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCancel:
                if (((Button) v).getText().equals(getString(R.string.btn_cancel))) {
                    arrFile.clear();
                    onBackPressed();
                } else {
                    imgPreview.setVisibility(View.GONE);
                    updateActions(false);
                }
                break;
            case R.id.btnCapture:
                if (((Button) v).getText().equals("Capture")) {
                    txtMsgTop.setText(getString(R.string.msg_retake));
                    cameraView.takePicture(true, true);
                } else {
                    selectImage();
                }
                break;
            case R.id.btnDone:
                uploadImages();
                break;
        }
    }

    @Override
    public CameraHost getCameraHost() {
        return new MyCameraHost(this);
    }

    class MyCameraHost extends SimpleCameraHost {
        private Camera.Size previewSize;

        MyCameraHost(Context ctxt) {
            super(ctxt);
        }

        @Override
        public boolean useFullBleedPreview() {
            return true;
        }

        @Override
        public Camera.Size getPictureSize(PictureTransaction xact, Camera.Parameters parameters) {
            return previewSize;
        }

        @Override
        public Camera.Parameters adjustPreviewParameters(Camera.Parameters parameters) {
            Camera.Parameters parameters1 = super.adjustPreviewParameters(parameters);
            previewSize = parameters1.getPreviewSize();
            return parameters1;
        }

        @Override
        public void saveImage(PictureTransaction xact, final Bitmap bitmap) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    currentBitmap = bitmap;
                    imgPreview.setVisibility(View.VISIBLE);
                    imgPreview.setImageBitmap(bitmap);

                    updateActions(true);
                }
            });
        }
    }

    private void updateActions(boolean isCaptured) {
        if (isCaptured) {
            btnCapture.setText("Add Section");
            btnCancel.setText("Retake");

            btnCapture.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_add, 0, 0);
            btnCancel.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_retake, 0, 0);
        } else {
            btnCapture.setText("Capture");
            btnCancel.setText("Cancel");

            btnCapture.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_camera, 0, 0);
            btnCancel.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_cancel, 0, 0);
        }
    }

    private void selectImage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                writeBitmapToFile(currentBitmap);
            }
        }).start();

        layoutMsgTop.setVisibility(View.GONE);
        imgPreviewTop.setImageBitmap(currentBitmap);

        imgPreview.setVisibility(View.GONE);
        layoutPreviewTop.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                layoutPreviewTop.animate()
                        .translationY(-layoutPreviewTop.getHeight() + 160)
                        .alpha(1.0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                }).setDuration(450);
            }
        }, 450);

        currentBitmap = null;
        updateActions(false);
    }

    public void writeBitmapToFile(Bitmap bitmap) {

        File file = null, f = null;
        if (android.os.Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED)) {
            file = new File(android.os.Environment.getExternalStorageDirectory(),
                    "My Stash");
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        FileOutputStream outStream = null;

        try {
            String fullPath = file.getAbsolutePath() + File.separator + "BillImage_" + arrFile.size() + ".jpg";
            outStream = new FileOutputStream(fullPath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outStream);
            outStream.close();

            arrFile.add(fullPath);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvImageCount.setText(String.valueOf(arrFile.size()));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadImages() {
        if (currentBitmap != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    writeBitmapToFile(currentBitmap);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            doPost();
                        }
                    });
                }
            }).start();
        } else {
            doPost();
        }
    }

    private void doPost() {
        if (arrFile.size() > 0) {
            setResult(RESULT_OK);
            onBackPressed();
        } else {
            Toast.makeText(TakePhoto.this, getString(R.string.msg_select_image), Toast.LENGTH_LONG).show();
        }
    }
}