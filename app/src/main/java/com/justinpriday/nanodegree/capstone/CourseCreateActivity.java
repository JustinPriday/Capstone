package com.justinpriday.nanodegree.capstone;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CourseCreateActivity extends AppCompatActivity implements CourseCreateFragment.OnFragmentInteractionListener,CourseCreateFragment.CallBack {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String LOG_TAG = CourseCreateActivity.class.getSimpleName();
    private File mFileName;

    @Override
    public void requestPhoto() {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                mFileName = createImageFile(".tmp");
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mFileName));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } catch (IOException e) {
            Log.d(LOG_TAG,"Got intent exception "+e.toString());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_create);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                this.onBackPressed();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap fullBitmap = processPhoto(data);
            CourseCreateFragment articleFrag = (CourseCreateFragment)getSupportFragmentManager().findFragmentById(R.id.course_create_fragment);
            articleFrag.receivePhoto(fullBitmap);
        }
    }

    protected Bitmap processPhoto(Intent i) {
        //Camera Processing Function with thanks to Mark Fidell on StackOverflow

        int imageExifOrientation = 0;
        try {
            ExifInterface exif;
            exif = new ExifInterface(mFileName.getAbsolutePath());
            imageExifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        int rotationAmount = 0;

        if (imageExifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            rotationAmount = 270;
        }
        if (imageExifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            rotationAmount = 90;
        }
        if (imageExifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            rotationAmount = 180;
        }

        int targetW = 240;
        int targetH = 320;

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mFileName.getAbsolutePath(), bmOptions);
        int photoWidth = bmOptions.outWidth;
        int photoHeight = bmOptions.outHeight;

        int scaleFactor = Math.min(photoWidth/targetW, photoHeight/targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
//        bmOptions.inPurgeable = true;

        Bitmap scaledDownBitmap = BitmapFactory.decodeFile(mFileName.getAbsolutePath(), bmOptions);

        mFileName.delete();

        if (rotationAmount != 0)
        {
            Matrix mat = new Matrix();
            mat.postRotate(rotationAmount);
            scaledDownBitmap = Bitmap.createBitmap(scaledDownBitmap, 0, 0, scaledDownBitmap.getWidth(), scaledDownBitmap.getHeight(), mat, true);
        }

        return scaledDownBitmap;
    }

    private File createImageFile(String fileExtensionToUse) throws IOException {

        File storageDir = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                ),
                "MyImages"
        );

        if (!storageDir.exists()) {
            if (!storageDir.mkdir()) {
                Log.d(LOG_TAG, "was not able to create it");
            }
        }
        if (!storageDir.isDirectory()) {
            Log.d(LOG_TAG, "Don't think there is a dir there.");
        }

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "FOO_" + timeStamp + "_image";

        File image = File.createTempFile(
                imageFileName,
                fileExtensionToUse,
                storageDir
        );

        return image;
    }
}
