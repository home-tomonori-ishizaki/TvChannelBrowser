package com.tomishi.tvchannelbrowser.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class StorageUtils {
    private final static String TAG = StorageUtils.class.getSimpleName();

    public static boolean storeCursor(Cursor cursor, String filename) {
        //DatabaseUtils.dumpCursor(cursor);
        String csvStr = cursorToCsv(cursor);

        // dump to external storage
        OutputStream outputStream = null;
        try {
            outputStream  = new FileOutputStream(new File(filename));
            outputStream.write(csvStr.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    return false;
                }
            }
        }

        return true;
    }

    private static String cursorToCsv(Cursor cursor) {
        StringBuffer sb = new StringBuffer();

        // dump header
        String[] cols = cursor.getColumnNames();
        for (int i = 0; i < cols.length; i++) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append(cols[i]);
        }
        sb.append("\n");

        // dump data
        while (cursor.moveToNext()) {
            for (int i = 0; i < cols.length; i++) {
                if (i != 0) {
                    sb.append(",");
                }
                try {
                    String value = cursor.getString(i);
                    if (value != null) {
                        // each value will be surround by ["], so replace ["] to [']
                        value = value.replaceAll("\"", "'");
                    }
                    sb.append("\"");
                    sb.append(value);
                    sb.append("\"");
                } catch (SQLiteException e) {
                    // case of blob data
                    sb.append("\"---\"");
                } finally {
                }
            }
            sb.append("\n");
        }
        //Log.i(TAG, sb.toString());
        return sb.toString();
    }

    public static String getRemovableStoragePath(Context context) {
        try {
            StorageManager sm = (StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
            Method getVolumeList = sm.getClass().getDeclaredMethod("getVolumeList");
            Object[] volumeList = (Object[])getVolumeList.invoke(sm);
            for (Object volume : volumeList) {
                Method getPath = volume.getClass().getDeclaredMethod("getPath");
                Method isRemovable = volume.getClass().getDeclaredMethod("isRemovable");
                Method getState = volume.getClass().getDeclaredMethod("getState");
                String path = (String)getPath.invoke(volume);
                boolean removable = (Boolean)isRemovable.invoke(volume);
                String state = (String)getState.invoke(volume);
                //Log.i(TAG, "path:" + path + " removable:" + removable + " state:" + state);
                if (removable && TextUtils.equals(state, Environment.MEDIA_MOUNTED)) {
                    return path;
                }
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
