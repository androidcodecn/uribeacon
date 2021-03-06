/*
 * Copyright 2015 Google Inc. All rights reserved.
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

package org.uribeacon.validator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import org.uribeacon.validator.TestRunner.DataCallback;

import java.util.ArrayList;


public class TestActivity extends Activity {

  private static final String TAG = TestActivity.class.getCanonicalName();
  private TestRunner mTestRunner;
  private final DataCallback mDataCallback = new DataCallback() {
    ProgressDialog progress;

    @Override
    public void dataUpdated() {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (progress != null) {
            progress.dismiss();
          }
          mAdapter.notifyDataSetChanged();
        }
      });
    }

    @Override
    public void waitingForConfigMode() {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          progress = new ProgressDialog(TestActivity.this);
          progress.setMessage(getString(R.string.put_beacon_in_config_mode));
          progress.show();
          progress.setCanceledOnTouchOutside(false);
          progress.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
              mTestRunner.stop();
            }
          });
        }
      });
    }

    @Override
    public void connectedToBeacon() {
      progress.dismiss();
    }

    @Override
    public void testsCompleted(final boolean failed) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          int message;
          if (failed) {
            message = R.string.test_failed;
          } else {
            message = R.string.test_success;
          }
          Toast.makeText(TestActivity.this, message, Toast.LENGTH_SHORT).show();
        }
      });
    }
  };
  private RecyclerView.Adapter mAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test);
    boolean optionalImplemented = getIntent().getBooleanExtra(MainActivity.LOCK_IMPLEMENTED, false);
    String testType = getIntent().getStringExtra(MainActivity.TEST_TYPE);
    mTestRunner = new TestRunner(this, mDataCallback, testType, optionalImplemented);
    ArrayList<TestHelper> mUriBeaconTests = mTestRunner.getUriBeaconTests();
    RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView_tests);
    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
    mRecyclerView.setHasFixedSize(true);
    mRecyclerView.setLayoutManager(mLayoutManager);
    mAdapter = new TestsAdapter(mUriBeaconTests);
    mRecyclerView.setAdapter(mAdapter);
  }

  @Override
  protected void onResume() {
    super.onResume();
    mTestRunner.start(null, null);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mTestRunner.stop();
  }
}

