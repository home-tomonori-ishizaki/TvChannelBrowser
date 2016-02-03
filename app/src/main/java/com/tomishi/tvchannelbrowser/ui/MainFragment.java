package com.tomishi.tvchannelbrowser.ui;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.tomishi.tvchannelbrowser.R;
import com.tomishi.tvchannelbrowser.model.Channel;
import com.tomishi.tvchannelbrowser.presenter.ChannelItemPresenter;
import com.tomishi.tvchannelbrowser.presenter.StringItemPresenter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.Manifest;

public class MainFragment extends BrowseFragment {
    private static final String TAG = MainFragment.class.getSimpleName();
    private static final int REQUEEST_ID_PERMISSION_LOAD_CHANNELS = 0;
    private static final int REQUEEST_ID_PERMISSION_DUMP_CHANNELS = 1;

    private static final String PERMISSION_READ_TV_LISTING = "android.permission.READ_TV_LISTINGS";
    private static final String PERMISSION_ACCESS_EPG_DATA = "com.android.providers.tv.permission.ACCESS_ALL_EPG_DATA";

    private ArrayObjectAdapter mRowAdapter;

    public MainFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        setupUIElements();

        loadRows();

        setupEventListeners();
    }

    private void setupUIElements() {
        setTitle("TV Channel Browser");

        // over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);
    }

    private void loadRows() {
        ClassPresenterSelector selector = new ClassPresenterSelector();
        selector.addClassPresenter(ListRow.class, new ListRowPresenter());

        mRowAdapter = new ArrayObjectAdapter(selector);

        // load setting row
        ArrayObjectAdapter settings = new ArrayObjectAdapter(new StringItemPresenter());
        settings.add(getActivity().getString(R.string.get_channel));
        mRowAdapter.add(new ListRow(new HeaderItem(0, "Settings"), settings));

        setAdapter(mRowAdapter);

        loadChannelsIfGranted(true);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEEST_ID_PERMISSION_LOAD_CHANNELS:
                loadChannelsIfGranted(false);
                break;

            case REQUEEST_ID_PERMISSION_DUMP_CHANNELS:
                dumpChannelsIfGranted(false);
                break;

            default:
                break;
        }
    }

    private void loadChannelsIfGranted(boolean show) {
        // if ACCESS_ALL_EPG_DATA(signatureOrSystem) is granted, can load channels
        if (ContextCompat.checkSelfPermission(getActivity(), PERMISSION_ACCESS_EPG_DATA)
                == PackageManager.PERMISSION_GRANTED) {
            loadChannels();
            return;
        }

        // check SDK level, because requestPermissions can be called above 23
        if (ContextCompat.checkSelfPermission(getActivity(), PERMISSION_READ_TV_LISTING)
                != PackageManager.PERMISSION_GRANTED) {

            if (show == false) {
                return;
            }

            // check SDK level, because requestPermissions can be called above 23
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                        new String[]{PERMISSION_READ_TV_LISTING},
                        REQUEEST_ID_PERMISSION_LOAD_CHANNELS
                );
            }
            return;
        }
        loadChannels();
    }

    private void loadChannels() {
        Map<String, String> inputs = getInputList();
        for (Map.Entry<String, String> entry : inputs.entrySet()) {
            ArrayObjectAdapter itemAdapter = new ArrayObjectAdapter(new ChannelItemPresenter());
            itemAdapter.addAll(0, getChannels(entry.getKey()));
            mRowAdapter.add(new ListRow(new HeaderItem(0, entry.getValue()), itemAdapter));
        }

    }

    private Map<String, String> getInputList() {
        Map<String, String> inputs = new HashMap<>();

        TvInputManager tvInputManager = (TvInputManager)getActivity().getSystemService(Context.TV_INPUT_SERVICE);
        for (TvInputInfo inputInfo : tvInputManager.getTvInputList()) {
            if (inputInfo.getType() != TvInputInfo.TYPE_TUNER) {
                continue;
            }
            inputs.put(inputInfo.getId(), inputInfo.loadLabel(getActivity()).toString());
        }

        return inputs;
    }

    private List<Channel> getChannels(String inputId) {
        List<Channel> channels = new LinkedList<>();

        try (Cursor cursor = getActivity().getContentResolver().query(
                TvContract.Channels.CONTENT_URI,
                null,
                /* need ACCESS_ALL_EPG_DATA permission(signatureOrSystem) to select */
                /*
                TvContract.Channels.COLUMN_INPUT_ID + " = ? ",
                new String[] {
                        inputId
                },
                */
                null,
                null,
                null)) {

            int idxInputId = cursor.getColumnIndexOrThrow(TvContract.Channels.COLUMN_INPUT_ID);
            int idxId = cursor.getColumnIndexOrThrow(TvContract.Channels._ID);
            int idxChannelName = cursor.getColumnIndexOrThrow(TvContract.Channels.COLUMN_DISPLAY_NAME);
            int idxChannelNumber = cursor.getColumnIndexOrThrow(TvContract.Channels.COLUMN_DISPLAY_NUMBER);

            while (cursor.moveToNext()) {
                // check input id
                String id = cursor.getString(idxInputId);
                if (!TextUtils.equals(id, inputId)) {
                    continue;
                }

                Channel channel = new Channel()
                        .setChannelName(cursor.getString(idxChannelName))
                        .setChannelNumber(cursor.getString(idxChannelNumber))
                        .setChannelLogo(TvContract.buildChannelLogoUri(cursor.getLong(idxId)));
                channels.add(channel);
            }
        }

        return channels;
    }

    private void dumpChannelsIfGranted(boolean show) {

        List<String> permissions = new LinkedList<>();

        if ((ContextCompat.checkSelfPermission(getActivity(), PERMISSION_ACCESS_EPG_DATA)
                != PackageManager.PERMISSION_GRANTED)
            && (ContextCompat.checkSelfPermission(getActivity(), PERMISSION_READ_TV_LISTING)
                != PackageManager.PERMISSION_GRANTED)) {
            permissions.add(PERMISSION_READ_TV_LISTING);
        }

        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!permissions.isEmpty()) {
            if (show == false) {
                return;
            }

            // check SDK level, because requestPermissions can be called above 23
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                        permissions.toArray(new String[permissions.size()]),
                        REQUEEST_ID_PERMISSION_DUMP_CHANNELS
                );
            }
            return;
        }

        dumpChannels();
    }

    private void dumpChannels() {
        StringBuffer sb = new StringBuffer();

        try (Cursor cursor = getActivity().getContentResolver().query(
                TvContract.Channels.CONTENT_URI,
                null,
                null,
                null,
                null)) {
            //DatabaseUtils.dumpCursor(cursor);

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
        }

        // dump to external storage
        Log.i(TAG, Environment.getExternalStorageDirectory().toString());
        File file = new File(Environment.getExternalStorageDirectory().toString() + "/channels.csv");
        OutputStream outputStream = null;
        try {
            outputStream  = new FileOutputStream(file);
            outputStream.write(sb.toString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {

                }
            }
        }

        Toast.makeText(getActivity(), "success", Toast.LENGTH_SHORT).show();
    }

    private void setupEventListeners() {
        setOnItemViewClickedListener(new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                      RowPresenter.ViewHolder rowViewHolder, Row row) {
                if (item instanceof String) {
                    dumpChannelsIfGranted(true);
                }
            }
        });
    }
}

