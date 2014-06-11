/*
 * Copyright (C) 2014 iWedia S.A. Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.iwedia.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.iwedia.adapters.ChannelListAdapter;
import com.iwedia.dtv.scan.IScanCallback;
import com.iwedia.dtv.scan.ScanInstallStatus;
import com.iwedia.dtv.types.InternalException;
import com.iwedia.scan.R;

import java.util.ArrayList;

public class ChannelProgressActivity extends DVBActivity {
    private static final int MESSAGE_CHANNEL_FOUND = 0,
            MESSAGE_FREQUENCY_CHANGED = 2, MESSAGE_PROGRESS_CHANGED = 3,
            MESSAGE_QUALITY_CHANGED = 4, MESSAGE_STRENGTH_CHANGED = 5;
    /** For scanned channels. */
    private ListView mListViewChannels;
    private ChannelListAdapter mListAdapter;
    /** Progress bars for scan procedure. */
    private ProgressBar mProgressSignalStrength, mProgressSignalQuality,
            mProgressScan;
    private TextView mTextViewFrequency, mTextViewProgress, mTextViewStrength,
            mTextViewQuality;
    /** List of scanned channel names. */
    private ArrayList<String> mChannelNames;
    /** Handler for communication with UI thread from callback thread. */
    private Handler mUiHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MESSAGE_FREQUENCY_CHANGED: {
                    mTextViewFrequency.setText(String.valueOf(msg.obj));
                    break;
                }
                case MESSAGE_CHANNEL_FOUND: {
                    Log.d("HANDLER", "\n\n\nMESSAGE_CHANNEL_FOUND"
                            + (String) msg.obj);
                    addChannelName((String) msg.obj);
                    break;
                }
                case MESSAGE_PROGRESS_CHANGED: {
                    mProgressScan.setProgress((Integer) msg.obj);
                    mTextViewProgress.setText(getString(R.string.progress, ""
                            + (Integer) msg.obj));
                    break;
                }
                case MESSAGE_STRENGTH_CHANGED: {
                    mProgressSignalStrength.setProgress((Integer) msg.obj);
                    mTextViewStrength.setText(getString(R.string.strength, ""
                            + (Integer) msg.obj));
                    break;
                }
                case MESSAGE_QUALITY_CHANGED: {
                    mProgressSignalQuality.setProgress((Integer) msg.obj);
                    mTextViewQuality.setText(getString(R.string.quality, ""
                            + (Integer) msg.obj));
                    break;
                }
                default:
                    break;
            }
        };
    };
    /**
     * Callback for scan procedure.
     */
    private IScanCallback mScanCallback = new IScanCallback() {
        @Override
        public void installServiceDATAName(int arg0, String arg1) {
        }

        @Override
        public void installServiceDATANumber(int arg0, int arg1) {
        }

        @Override
        public void installServiceRADIOName(int arg0, String arg1) {
            Log.d("ChannelProgressActivity", "installServiceRADIOName " + arg1);
            mUiHandler.sendMessage(Message.obtain(mUiHandler,
                    MESSAGE_CHANNEL_FOUND, arg1));
        }

        @Override
        public void installServiceRADIONumber(int arg0, int arg1) {
        }

        @Override
        public void installServiceTVName(int arg0, String arg1) {
            Log.d("ChannelProgressActivity", "installServiceTVName " + arg1);
            mUiHandler.sendMessage(Message.obtain(mUiHandler,
                    MESSAGE_CHANNEL_FOUND, arg1));
        }

        @Override
        public void installServiceTVNumber(int arg0, int arg1) {
        }

        @Override
        public void installStatus(ScanInstallStatus arg0) {
        }

        @Override
        public void sat2ipServerDropped(int arg0) {
        }

        @Override
        public void scanFinished(int arg0) {
            Log.d("ChannelProgressActivity",
                    "\n\n\n-------------------------------scanFinished ");
            try {
                mDVBManager.changeChannelByNumber(0);
            } catch (InternalException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            finish();
        }

        @Override
        public void scanNoServiceSpace(int arg0) {
        }

        @Override
        public void scanProgressChanged(int arg0, int arg1) {
            Message.obtain(mUiHandler, MESSAGE_PROGRESS_CHANGED, arg1)
                    .sendToTarget();
        }

        @Override
        public void scanTunFrequency(int arg0, int frequency) {
            Message.obtain(mUiHandler, MESSAGE_FREQUENCY_CHANGED, frequency)
                    .sendToTarget();
        }

        @Override
        public void signalBer(int arg0, int arg1) {
        }

        @Override
        public void signalQuality(int arg0, int arg1) {
            Message.obtain(mUiHandler, MESSAGE_QUALITY_CHANGED, arg1)
                    .sendToTarget();
        }

        @Override
        public void signalStrength(int arg0, int arg1) {
            Message.obtain(mUiHandler, MESSAGE_STRENGTH_CHANGED, arg1)
                    .sendToTarget();
        }

        @Override
        public void triggerStatus(int arg0) {
        }

        @Override
        public void antennaConnected(int arg0, boolean arg1) {
        }

        @Override
        public void networkChanged(int arg0) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channel_progress_activity);
        mTextViewFrequency = (TextView) findViewById(R.id.textViewFrequency);
        mTextViewProgress = (TextView) findViewById(R.id.textViewProgress);
        mTextViewStrength = (TextView) findViewById(R.id.textViewStrength);
        mTextViewQuality = (TextView) findViewById(R.id.textViewQuality);
        mTextViewProgress.setText(getString(R.string.progress, "0"));
        mTextViewStrength.setText(getString(R.string.strength, "0"));
        mTextViewQuality.setText(getString(R.string.quality, "0"));
        /** Initialize list view. */
        mChannelNames = new ArrayList<String>();
        mListAdapter = new ChannelListAdapter(this, mChannelNames);
        mListViewChannels = (ListView) findViewById(R.id.listViewChannels);
        mListViewChannels.setAdapter(mListAdapter);
        /** Initialize progress bars. */
        mProgressScan = (ProgressBar) findViewById(R.id.progressBarScanProgress);
        mProgressSignalQuality = (ProgressBar) findViewById(R.id.progressBarSignalQuality);
        mProgressSignalStrength = (ProgressBar) findViewById(R.id.progressBarSignalStrength);
        mDVBManager.setScanCallback(mScanCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDVBManager.removeScanCallback(mScanCallback);
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Aborting scan procedure", Toast.LENGTH_LONG)
                .show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mDVBManager.abortScan();
                } catch (InternalException e) {
                    e.printStackTrace();
                    finish();
                }
            }
        }).start();
    }

    /**
     * Add scanned item to list view.
     * 
     * @param newChannelName
     *        Scanned channel name.
     */
    private synchronized void addChannelName(String newChannelName) {
        mChannelNames.add(newChannelName);
        mListAdapter.notifyDataSetChanged();
        /** Scroll to last element. */
        mListViewChannels.smoothScrollToPosition(mListAdapter.getCount() - 1);
        mListViewChannels.setSelection(mListAdapter.getCount() - 1);
    }
}
