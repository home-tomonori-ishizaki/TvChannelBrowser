package com.tomishi.tvchannelbrowser.ui;

import android.os.Bundle;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.util.Log;

import com.tomishi.tvchannelbrowser.model.Channel;
import com.tomishi.tvchannelbrowser.presenter.ChannelItemPresenter;

public class MainFragment extends BrowseFragment {
    private static final String TAG = MainFragment.class.getSimpleName();

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

        ArrayObjectAdapter rowAdapter = new ArrayObjectAdapter(selector);

        ArrayObjectAdapter itemAdapter = new ArrayObjectAdapter(new ChannelItemPresenter());
        for (int i = 0; i < 10; i++) {
            Channel channel = new Channel();
            channel.setChannelName("channel " + (i + 1));
            itemAdapter.add(channel);
        }

        rowAdapter.add(new ListRow(new HeaderItem(0, "input"), itemAdapter));

        setAdapter(rowAdapter);
    }

}

