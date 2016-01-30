package com.tomishi.tvchannelbrowser.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.tv.TvContentRating;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import com.tomishi.tvchannelbrowser.model.Channel;
import com.tomishi.tvchannelbrowser.presenter.ChannelItemPresenter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.Manifest;

public class MainFragment extends BrowseFragment {
    private static final String TAG = MainFragment.class.getSimpleName();
    private static final int REQUEEST_ID_PERMISSION = 0;

    private static final String PERMISSION_READ_TV_LISTING = "android.permission.READ_TV_LISTINGS";

    private ArrayObjectAdapter mRowAdapter;

    public MainFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        setupUIElements();

        loadRows();
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
        setAdapter(mRowAdapter);

        if (ContextCompat.checkSelfPermission(getActivity(), PERMISSION_READ_TV_LISTING)
                != PackageManager.PERMISSION_GRANTED) {
            // check SDK level, becuase requestPermissions can be called above 23
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                        new String[]{PERMISSION_READ_TV_LISTING},
                        REQUEEST_ID_PERMISSION
                );
            }
            return;
        }
        loadChannels();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != REQUEEST_ID_PERMISSION) {
            return;
        }

        for (int i = 0; i < permissions.length; i++) {
            if (TextUtils.equals(permissions[i], PERMISSION_READ_TV_LISTING)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadChannels();
                }
            }
        }
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
                        .setChannelNumber(cursor.getString(idxChannelNumber));
                channels.add(channel);
            }
        }

        return channels;
    }
}

