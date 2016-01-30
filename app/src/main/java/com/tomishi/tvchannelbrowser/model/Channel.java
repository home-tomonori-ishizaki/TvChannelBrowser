package com.tomishi.tvchannelbrowser.model;

public class Channel {

    private String mChannameName;

    public Channel setChannelName(String channelName) {
        mChannameName = channelName;
        return this;
    }

    public String getChannelName() {
        return mChannameName;
    }
}
