package com.tomishi.tvchannelbrowser.presenter;

import android.content.Context;
import android.support.v17.leanback.widget.BaseCardView;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.tomishi.tvchannelbrowser.R;
import com.tomishi.tvchannelbrowser.model.Channel;

public class ChannelItemPresenter extends Presenter {
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Context context = parent.getContext();
        ImageCardView cardView = new ImageCardView(context);
        cardView.setCardType(BaseCardView.CARD_TYPE_INFO_UNDER_WITH_EXTRA);
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        int width = context.getResources().getDimensionPixelSize(R.dimen.channel_item_width);
        int height = context.getResources().getDimensionPixelSize(R.dimen.channel_item_height);
        cardView.setMainImageDimensions(width, height);
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        Channel channel = (Channel)item;
        ImageCardView cardView = (ImageCardView)viewHolder.view;

        cardView.setTitleText(channel.getChannelName());
        cardView.setContentText(channel.getChannelNumber());

        Picasso.with(cardView.getContext())
                .load(channel.getChannelLogo())
                .into(cardView.getMainImageView());
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {

    }
}
