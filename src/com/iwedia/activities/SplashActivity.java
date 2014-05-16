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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.iwedia.scan.R;

/**
 * SplashActivity - Splash screen is shown when TV service is being initialized.
 */
public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
        /** Start Channel scan activity. */
        Intent intentActionStartTV = new Intent(this, ChannelScanActivity.class);
        intentActionStartTV.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentActionStartTV);
        finish();
    }
}
