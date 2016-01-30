package com.tomishi.tvchannelbrowser.presenter;

import android.content.res.Resources;
import android.graphics.Color;
import android.support.v17.leanback.widget.Presenter;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tomishi.tvchannelbrowser.R;

public class StringItemPresenter extends Presenter{
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        TextView view = new TextView(parent.getContext());
        Resources res = parent.getContext().getResources();
        int width = res.getDimensionPixelSize(R.dimen.string_item_width);
        int height = res.getDimensionPixelSize(R.dimen.string_item_height);
        view.setLayoutParams(new ViewGroup.LayoutParams(width, height));
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.setGravity(Gravity.CENTER);
        view.setBackgroundColor(res.getColor(R.color.lb_basic_card_bg_color));
        view.setTextColor(res.getColor(R.color.lb_browse_title_color));
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        ((TextView) viewHolder.view).setText((String) item);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {

    }
}
