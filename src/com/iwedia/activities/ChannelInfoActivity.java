package com.iwedia.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.iwedia.adapters.ChannelListAdapter;
import com.iwedia.dtv.SignalInformation;
import com.iwedia.dtv.types.InternalException;
import com.iwedia.scan.R;

public class ChannelInfoActivity extends DVBActivity implements
        OnItemClickListener {
    /** For channels. */
    private ListView mListViewChannels;
    private ChannelListAdapter mListAdapter;
    /** Other views. */
    private TextView mTextViewChannelName, mTextViewChannelMultiplex,
            mTextViewChannelNetworkId;
    private ProgressBar mProgressBarSignalStrength, mProgressBarSignalQuality;
    /** Thread fields. */
    private Thread backgroundThread = null;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            SignalInformation info = (SignalInformation) msg.obj;
            refreshViews(info);
            super.handleMessage(msg);
        }
    };;
    private Runnable run = new Runnable() {
        @Override
        public void run() {
            while (true) {
                Thread thisThread = Thread.currentThread();
                if (thisThread.equals(backgroundThread)) {
                    SignalInformation info = mDVBManager.getServiceInfo();
                    Message.obtain(handler, 0, info).sendToTarget();
                    try {
                        // Sleep 500 milliseconds
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channel_info_activity);
        /** Initialize list view. */
        mListAdapter = new ChannelListAdapter(this,
                mDVBManager.getChannelNames());
        mListViewChannels = (ListView) findViewById(R.id.listViewChannels);
        mListViewChannels.setAdapter(mListAdapter);
        mListViewChannels.setOnItemClickListener(this);
        /** Initialize views. */
        mTextViewChannelName = (TextView) findViewById(R.id.textViewChannelName);
        mTextViewChannelMultiplex = (TextView) findViewById(R.id.textViewChannelMultiplex);
        mTextViewChannelNetworkId = (TextView) findViewById(R.id.textViewChannelNetworkId);
        mProgressBarSignalStrength = (ProgressBar) findViewById(R.id.progressBarSignalStrength);
        mProgressBarSignalQuality = (ProgressBar) findViewById(R.id.progressBarSignalQuality);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mListViewChannels.setItemChecked(mDVBManager.getCurrentChannelNumber(),
                true);
        startThread(run);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopThread();
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        try {
            mDVBManager.changeChannelByNumber(arg2);
            mListViewChannels.setItemChecked(arg2, true);
        } catch (InternalException e) {
            e.printStackTrace();
        }
    }

    /**
     * Refresh views with new information.
     * 
     * @param info
     *        Signal information object.
     */
    private void refreshViews(SignalInformation info) {
        mTextViewChannelName.setText(info.getChannelName());
        mTextViewChannelMultiplex.setText(String.valueOf(info.getMultiplex()));
        mTextViewChannelNetworkId.setText(String.valueOf(info.getNetworkId()));
        mProgressBarSignalQuality.setProgress(info.getSignalQuality());
        mProgressBarSignalStrength.setProgress(info.getSignalStrength());
    }

    /**
     * Start background thread.
     * 
     * @param run
     *        Runnable to run in thread
     */
    public void startThread(Runnable run) {
        if (backgroundThread == null) {
            backgroundThread = new Thread(run);
            backgroundThread.setPriority(Thread.MIN_PRIORITY);
            backgroundThread.start();
        }
    }

    /**
     * Stops background thread.
     */
    public void stopThread() {
        if (backgroundThread != null) {
            Thread moribund = backgroundThread;
            backgroundThread = null;
            moribund.interrupt();
        }
    }
}
