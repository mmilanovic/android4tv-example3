package com.iwedia.dtv;

/**
 * Class that contains information about active channel.
 */
public class SignalInformation {
    private String mChannelName;
    private int mNetworkId, mSignalQuality, mSignalStrength, mMultiplex;

    public SignalInformation(String mChannelName, int mNetworkId,
            int mSignalQuality, int mSignalStrength, int mMultiplex) {
        super();
        this.mChannelName = mChannelName;
        this.mNetworkId = mNetworkId;
        this.mSignalQuality = mSignalQuality;
        this.mSignalStrength = mSignalStrength;
        this.mMultiplex = mMultiplex;
    }

    public String getChannelName() {
        return mChannelName;
    }

    public int getNetworkId() {
        return mNetworkId;
    }

    public int getSignalQuality() {
        return mSignalQuality;
    }

    public int getSignalStrength() {
        return mSignalStrength;
    }

    public int getMultiplex() {
        return mMultiplex;
    }
}
