package com.streambow;

import android.util.Log;

import com.streambow.xperience.xperience.TestCallback;
import com.streambow.xperience.xperience.TestProgress;
import com.streambow.xperience.xperience.TestStatus;
import com.streambow.xperience.xperience.Xperience;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class Streambow extends CordovaPlugin {
    private static final String TAG = "StreambowPlugin";
    private Xperience xperience;
    private CallbackContext callbackContext;

    final TestCallback testCallback = new TestCallback() {

        @Override
        public void progressUpdate(TestProgress testProgress, TestStatus testStatus) {
			Log.i(TAG, ">>> Test Progress: " + testProgress.getProgress() + "% <<<");
			Log.i(TAG, ">>> Test Message: " + testProgress.getMessage() + " <<<");
			switch (testStatus) {
				case TEST_PROGRESS:
					// Preserve callback and send progress
					PluginResult pluginResult = new  PluginResult(PluginResult.Status.OK, testProgress.getProgress() + "%");
					pluginResult.setKeepCallback(true);
					callbackContext.sendPluginResult(pluginResult);

					if (testProgress.getProgress() == 100) {
						Log.i(TAG, ">>>Test FINISHED <<<");
						String results = "";
						if (testProgress.getResult() != null) {
							Log.i(TAG, ">>> RESULT STRING JSON: " + testProgress.getResult().getJSON() + " <<<");
							results = testProgress.getResult().toString();
							callbackContext.success(results);
						} else {
							Log.i(TAG, ">>> GET RESULT IS NULL <<<");
							callbackContext.error("Error: Test results are null");
						}
					}
				break;
				case TEST_STARTED:
					Log.i(TAG, ">>> Test STARTED <<<");
					break;
				case TEST_FINISHED:
					Log.i(TAG, ">>>Test FINISHED <<<");
					break;
				case TEST_CANCELLED:
					Log.i(TAG, ">>>Test cancelled <<<");
					callbackContext.error("Error: Test Cancelled");
					break;
			}
        }
    };

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
		if(action.equals("requestPermissions")) {
			Log.i(TAG, ">>> requesting permissions <<<");

			xperience = Xperience.getInstance(cordova.getContext());
			Xperience.requestPermission(this.cordova.getActivity());
			return true;
		}
        else if (action.equals("performTest")) {
            String testID = args.getString(0);
            Log.i(TAG, ">>> testID: " + testID + " <<<");
            
			this.performTest(testID, callbackContext);
            return true;
        }
        return false;
    }

    private void performTest(String testID, CallbackContext callbackContext) {
		//request permissions
		xperience = Xperience.getInstance(cordova.getContext());
		
		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				if (xperience.startTest(testCallback, testID)){
					Log.i(TAG, ">>> Service Requested <<<");
				} else {
					Log.i(TAG, ">>> Couldn't start service <<<");
				}
			}
		});
    }
}
