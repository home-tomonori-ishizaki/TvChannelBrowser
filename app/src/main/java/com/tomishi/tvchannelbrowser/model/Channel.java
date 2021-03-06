package com.tomishi.tvchannelbrowser.model;

import android.net.Uri;

public class Channel {

    private String mChannelName;
    private String mChannnelNumber;
    private Uri mLogo;

    public Channel setChannelName(String channelName) {
        mChannelName = channelName;
        return this;
    }

    public String getChannelName() {
        return mChannelName;
    }

    public Channel setChannelNumber(String channelNumber) {
        mChannnelNumber = channelNumber;
        return this;
    }

    public String getChannelNumber() {
        return mChannnelNumber;
    }

    public Channel setChannelLogo(Uri logo) {
        mLogo = logo;
        return this;
    }

    public Uri getChannelLogo() {
        return mLogo;
    }
}
