package com.tomishi.tvchannelbrowser.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class StorageUtils {
    private final static String TAG = StorageUtils.class.getSimpleName();

    public static boolean storeCursor(Cursor cursor, String filename) {
        //DatabaseUtils.dumpCursor(cursor);
        String csvStr = cursorToCsv(cursor);

        // dump to external storage
        File file = new File(Environment.getExternalStorageDirectory().toString()
                + File.separator + filename);
        Log.i(TAG, "store path : " + file.toString());
        OutputStream outputStream = null;
        try {
            outputStream  = new FileOutputStream(file);
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
}
