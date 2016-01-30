package com.tomishi.tvchannelbrowser.model;

public class Channel {

    private String mChannelName;
    private String mChannnelNumber;

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
}
