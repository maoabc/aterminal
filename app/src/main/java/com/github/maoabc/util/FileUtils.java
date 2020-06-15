package com.github.maoabc.util;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.github.maoabc.BaseApp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    public static String getFileFromUri(Uri uri) {

        String path;
        if (uri == null || (path = uri.getPath()) == null) {
            return null;
        }

        String scheme = uri.getScheme();
        if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            return path;
        }
        Cursor cursor = null;
        try {
            if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
                ContentResolver contentResolver = BaseApp.get().getContentResolver();
                String authority = uri.getAuthority();
                if (MediaStore.AUTHORITY.equals(authority) || "downloads".equals(authority))
                    cursor = contentResolver.query(uri, new String[]{MediaStore.MediaColumns.DATA}, null, null, null);
                if (cursor != null && cursor.moveToFirst() && !cursor.isNull(0)) {
                    return cursor.getString(0);
                }
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        if (path.startsWith("/root")) {//adm
            path = path.substring(5);
        }

        if (new File(path).exists()) {
            return path;
        }
        return "";
    }

    public static void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buf = new byte[64 * 1024];
        int len;
        while ((len = input.read(buf, 0, buf.length)) != -1) {
            output.write(buf, 0, len);
        }
    }

    public static void copyStreamAndClose(InputStream input, OutputStream output) {
        try {
            copyStream(input, output);
        } catch (IOException e) {

        } finally {
            try {
                input.close();
            } catch (IOException ignored) {
            }
            try {
                output.close();
            } catch (IOException ignored) {
            }
        }
    }
}
