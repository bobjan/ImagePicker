package com.mindmatchapp.imagepicker;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener{
    private static final String TAG = "ImagePicker";
    ImageView imageView;

    Button btnClock, btnCounter, btnSave;

    Button btnSelect, btnGallery, btnCamera, btnTest;

    // for image upload - opcija SAVE
    String UPLOAD_URL = "http://www.logotet.com/mindmatch/imgupload.php";
    private String KEY_IMAGE = "image";
    private String KEY_NAME = "name";
    //

    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;
    private static final int REQUEST_IMAGE = 3;


    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSelect = (Button) findViewById(R.id.btnSelectImage);
        btnGallery = (Button) findViewById(R.id.btnSelectGallery);
        btnCamera = (Button) findViewById(R.id.btnCamera);
        imageView = (ImageView) findViewById(R.id.imgMatrix); // radni image

        btnClock = (Button) findViewById(R.id.btnClock);
        btnCounter = (Button) findViewById(R.id.btnCounter);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnTest = (Button) findViewById(R.id.btnTest);

        if (!checkCameraHardware(this))
            btnCamera.setVisibility(View.GONE);

        gestureDetector = new GestureDetector(this, this);


        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takeCameraPicture();
            }
        });
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectGalleryPicture();
            }
        });
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectGalleryAnotherOption();
            }
        });

        btnClock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rotateImage(90);
            }
        });
        btnCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rotateImage(-90);
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToserver();
            }
        });


        imageView.setOnTouchListener(new View.OnTouchListener() {
            float lastTouchX, lastTouchY;


            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                gestureDetector.onTouchEvent(motionEvent);
                return true;

//                final int action = motionEvent.getAction();
//                Matrix matrix = imageView.getImageMatrix();
//                switch (action) {
//                    case MotionEvent.ACTION_DOWN: {
//                        final float x = motionEvent.getX();
//                        final float y = motionEvent.getY();
////                         Remember where we started
//                        lastTouchX = x;
//                        lastTouchY = y;
//                        Log.w(TAG, "action down - x=" + x + " y=" + y);
//                        break;
//                    }
//
//                    case MotionEvent.ACTION_MOVE: {
//                        final float x = motionEvent.getX();
//                        final float y = motionEvent.getY();
////                         Calculate the distance moved
//                        final float dx = x - lastTouchX;
//                        final float dy = y - lastTouchY;
//
//                        Log.w(TAG, "action move - dx=" + dx + " dy=" + dy);
//
//                         Move the object
//                        mPosX += dx;
//                        mPosY += dy;
//
//                        matrix.postTranslate(dx, dy);
//
//  //                       Remember this touch position for the next move event
//                        lastTouchX = x;
//                        lastTouchY = y;
//
////                         Invalidate to request a redraw
//                        imageView.setImageMatrix(matrix);
//                        imageView.invalidate();
//                        break;
    //                }
             //   }

//                return true;
            }
        });


        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        logSizes();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_GALLERY) {
            if (resultCode == RESULT_OK) {
                Uri targetUri = data.getData();
                Bitmap bitmap;
                try {
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                    imageView.setImageBitmap(bitmap);
                    logSizes();
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                //Getting the Bitmap from Gallery
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                //Setting the Bitmap to ImageView
                imageView.setImageBitmap(bitmap);
                logSizes();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
            logSizes();
        }
    }


    private void sendToserver() {
        Log.w(TAG, "Sending image to Server ");

//        @todo preuzeti sliku iz imageview , dodeliti joj ime i pozvati upload to server
// ovo je testiranje
        Matrix matrix = new Matrix();
        float scale = 1.0f / getDpSize();
        matrix.setScale(scale, scale);
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        int cropSize = bitmap.getHeight() / 1;
        Log.w("CROP SIZE ", cropSize + " scale=" + scale + " DP:" + getDpSize());
        Bitmap cropped = Bitmap.createBitmap(bitmap, 0, 0, cropSize, cropSize, matrix, false);
//                Log.w("cropde size " , cropped.getHeight() + "x" + cropped.getWidth());
        uploadImage(cropped, "imgWholeScaled");
//                bitmap.recycle();


    }

    private void rotateImage(int i) {
        Log.w(TAG, "Rotating image for " + i);

        Matrix matrix = imageView.getImageMatrix();
        matrix.postRotate((float) i, 450F, 450F); // @ todo centar rotacije pazljivo odrediti

        imageView.setImageMatrix(matrix);
        imageView.invalidate();
        logSizes();
    }

    private void selectGalleryPicture() {
        Log.w(TAG, "Selecting image from gallery ");

        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);

    }


    private void selectGalleryAnotherOption() {
        Log.w(TAG, "Selecting image second option ");

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_IMAGE);
    }

    private void takeCameraPicture() {
        Log.w(TAG, "Taking camera picture");
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, REQUEST_CAMERA);
        }
    }

    // ova funkcija uploaduje image
    private void uploadImage(final Bitmap bitmap, final String imageName) {
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(this, "Uploading...", "Please wait...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        //Disimissing the progress dialog
                        loading.dismiss();
                        Log.w("response SUCSESS", s);
                        //Showing toast message of the response
//                        Toast.makeText(LoadImagesActivity.this, s , Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();
                        Log.w("response failed", volleyError.getMessage());
                        //Showing toast
//                        Toast.makeText(LoadImagesActivity.this, volleyError.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String
                String image = getStringImage(bitmap);
                Map<String, String> params = new Hashtable<String, String>();
                params.put(KEY_IMAGE, image);
                params.put(KEY_NAME, imageName);
                return params;
            }
        };
        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean uspeh = bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    /**
     * Check if this device has a camera
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }


    private void logSizes() {
        int ivH = imageView.getWidth();
        int ivMH = imageView.getMeasuredHeight();
        int ivW = imageView.getWidth();
        int ivMW = imageView.getMeasuredWidth();

        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        int dMH = drawable.getMinimumHeight();
        int dIH = drawable.getIntrinsicHeight();
        int dMW = drawable.getMinimumWidth();
        int dIW = drawable.getIntrinsicWidth();

        Bitmap bitmap = drawable.getBitmap();
        int bH = bitmap.getHeight();
        int bW = bitmap.getWidth();

        Log.w(TAG, "iv Height=" + ivH + "  iv MeasuredHeight=" + ivMH + " bitmapHeight=" + bH + " DrawableMinHeight=" + dMH + " DrawableIntrinsicHeight=" + dIH);
        Log.w(TAG, "iv Width=" + ivW + "  iv MeasuredWidth=" + ivMW + " bitmapWidth=" + bW + " DrawableMinWidth=" + dMW + " DrawableIntrinsicWidth=" + dIW);
    }


    private float getDpSize() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        double dens = (double) dm.densityDpi;
        return (float) (dens / 160.0);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }
}
