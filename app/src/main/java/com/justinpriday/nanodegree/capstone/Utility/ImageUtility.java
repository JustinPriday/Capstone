package com.justinpriday.nanodegree.capstone.Utility;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

/**
 * Created by justinpriday on 2016/10/04.
 */

public class ImageUtility {
    private static final String LOG_TAG = ImageUtility.class.getSimpleName();

    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
            return stream.toByteArray();
        }
        return new byte[0];
    }
}
