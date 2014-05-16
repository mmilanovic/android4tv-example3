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
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.iwedia.dtv.DVBManager;

/**
 * Parent class off all activities. This class contains connection to dtv
 * service through dtv manager object.
 */
public abstract class DVBActivity extends Activity {
    public static final String FINISH_ACTIVITIES_MESSAGE = "activity_finish";
    /** DTV manager object. */
    protected DVBManager mDVBManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /** Set full screen application. */
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        /** Creates dtv manager object and connects it to service. */
        mDVBManager = new DVBManager(this);
        mDVBManager.InitializeDTVService();
    }

    public void finishActivity() {
        Toast.makeText(this,
                "Error with connection happened, closing application...",
                Toast.LENGTH_LONG).show();
        super.finish();
    }
}
