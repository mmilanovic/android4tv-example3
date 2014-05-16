/*
 * Copyright (C) 2014 iWedia S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iwedia.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.iwedia.dtv.DVBManager;
import com.iwedia.dtv.scan.TunerType;
import com.iwedia.dtv.service.IServiceCallback;
import com.iwedia.dtv.service.ServiceListUpdateData;
import com.iwedia.dtv.types.InternalException;
import com.iwedia.scan.R;

/**
 * Class that starts scan procedure (auto or manual).
 */
public class ChannelScanActivity extends DVBActivity {
    private final static String TAG = "ChannelScanActivity";
    private EditText mManualScanFrequencyEditText;
    private ToggleButton mKeepCurrentListButton;
    private Spinner mTunerTypeSpinner;
    /**
     * Callback for channels.
     */
    private IServiceCallback mChannelInstallCallback = new IServiceCallback() {
        @Override
        public void channelChangeStatus(int arg0, boolean arg1) {
            if (arg1) {
                DVBManager.setScanStarted(false);
            }
        }

        @Override
        public void safeToUnblank(int arg0) {
        }

        @Override
        public void serviceScrambledStatus(int arg0, boolean arg1) {
        }

        @Override
        public void serviceStopped(int arg0, boolean arg1) {
            if (arg1 && DVBManager.isScanStarted()) {
                DVBManager.setScanStarted(false);
                if (DVBManager.isAutoScan()) {
                    startAutoScan(null);
                } else {
                    startManualScan(null);
                }
            }
        }

        @Override
        public void signalStatus(int arg0, boolean arg1) {
        }

        @Override
        public void updateServiceList(ServiceListUpdateData arg0) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channel_scan_activity);
        mKeepCurrentListButton = (ToggleButton) findViewById(R.id.toggleButtonKeepList);
        mManualScanFrequencyEditText = (EditText) findViewById(R.id.editTextFrequency);
        mTunerTypeSpinner = (Spinner) findViewById(R.id.spinnerTunerType);
        try {
            mDVBManager.startDTV(0);
        } catch (InternalException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDVBManager.setChannelCallback(mChannelInstallCallback);
    }

    /**
     * Start auto scan button click listener.
     */
    public void startAutoScan(View v) {
        TunerType tunerType = convertSpinnerChoiceToFrontendType();
        if (tunerType == null) {
            return;
        }
        try {
            if (mDVBManager.autoScan(tunerType,
                    mKeepCurrentListButton.isChecked())) {
                Intent intent = new Intent(this, ChannelProgressActivity.class);
                startActivity(intent);
            }
        } catch (InternalException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start manual scan button click listener.
     */
    public void startManualScan(View v) {
        String lEnteredText = mManualScanFrequencyEditText.getText().toString();
        if (lEnteredText.length() == 0) {
            Toast.makeText(this, "Frequency field is empty!",
                    Toast.LENGTH_SHORT).show();
        } else {
            TunerType frontendType = convertSpinnerChoiceToFrontendType();
            if (frontendType == null) {
                return;
            }
            try {
                mDVBManager.manualScan(frontendType,
                        Integer.valueOf(lEnteredText),
                        mKeepCurrentListButton.isChecked());
                Intent intent = new Intent(this, ChannelProgressActivity.class);
                startActivity(intent);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (InternalException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Converts selected tuner type from spinner to appropriate frontend type.
     * @return Frontend type.
     */
    private TunerType convertSpinnerChoiceToFrontendType() {
        String tunerType = (String) mTunerTypeSpinner
                .getItemAtPosition(mTunerTypeSpinner.getSelectedItemPosition());
        if (tunerType.equalsIgnoreCase("DVB-T")) {
            return TunerType.TERRESTRIAL;
        } else {
            return null;
        }
    }
}
