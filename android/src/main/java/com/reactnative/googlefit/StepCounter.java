/**
 * Copyright (c) 2017-present, Stanislav Doskalenko - doskalenko.s@gmail.com
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 * <p>
 * Based on Asim Malik android source code, copyright (c) 2015
 **/

package com.reactnative.googlefit;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataSourcesResult;

import java.util.concurrent.TimeUnit;

public class StepCounter implements OnDataPointListener {

    private ReactContext mReactContext;
    private GoogleFitManager googleFitManager;
    private Activity activity;

    private static final String TAG = "StepCounter";


    public StepCounter(ReactContext reactContext, GoogleFitManager googleFitManager, Activity activity) {
        this.mReactContext = reactContext;
        this.googleFitManager = googleFitManager;
        this.activity = activity;
    }

    public void findFitnessDataSources() {
        try {

            DataSourcesRequest dataSourceRequest = new DataSourcesRequest.Builder()
                    .setDataTypes(DataType.TYPE_STEP_COUNT_DELTA, DataType.TYPE_STEP_COUNT_CUMULATIVE)
                    .setDataSourceTypes(DataSource.TYPE_DERIVED)
                    .build();

            ResultCallback<DataSourcesResult> dataSourcesResultCallback = new ResultCallback<DataSourcesResult>() {
                @Override
                public void onResult(DataSourcesResult dataSourcesResult) {
                    try {
                        for (DataSource dataSource : dataSourcesResult.getDataSources()) {
                            DataType type = dataSource.getDataType();

                            if (DataType.TYPE_STEP_COUNT_DELTA.equals(type)
                                    || DataType.TYPE_STEP_COUNT_CUMULATIVE.equals(type)) {
                                Log.i(TAG, "Register Fitness Listener: " + type);
                                registerFitnessDataListener(dataSource, type);//DataType.TYPE_STEP_COUNT_DELTA);
                            }
                        }
                    } catch (Throwable e) {
                        HelperUtil.displayMessage(this.getClass().getName());
                        Log.e(this.getClass().getName(), e.getMessage());
                    }
                }
            };

            Fitness.SensorsApi.findDataSources(googleFitManager.getGoogleApiClient(), dataSourceRequest)
                    .setResultCallback(dataSourcesResultCallback);
        } catch (Throwable e) {
            HelperUtil.displayMessage(this.getClass().getName());
            Log.e(this.getClass().getName(), e.getMessage());
        }
    }

    private void registerFitnessDataListener(DataSource dataSource, DataType dataType) {
        try {


            SensorRequest request = new SensorRequest.Builder()
                    .setDataSource(dataSource)
                    .setDataType(dataType)
                    .setSamplingRate(3, TimeUnit.SECONDS)
                    .build();

            Fitness.SensorsApi.add(googleFitManager.getGoogleApiClient(), request, this)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                Log.i(TAG, "SensorApi successfully added");
                            }
                        }
                    });
        } catch (Throwable e) {
            HelperUtil.displayMessage(this.getClass().getName());
            Log.e(this.getClass().getName(), e.getMessage());
        }
    }


    @Override
    public void onDataPoint(DataPoint dataPoint) {
        try {
            DataType type = dataPoint.getDataType();
            Log.i(TAG, "Detected DataPoint type: " + type);

            for (final Field field : type.getFields()) {
                final Value value = dataPoint.getValue(field);
                Log.i(TAG, "Detected DataPoint field: " + field.getName());
                Log.i(TAG, "Detected DataPoint value: " + value);


       /*     activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mReactContext.getApplicationContext(), "Field: " + field.getName() + " Value: " + value, Toast.LENGTH_SHORT).show();
                }
            });*/

                if (type.equals(DataType.TYPE_STEP_COUNT_DELTA)) {
                    WritableMap map = Arguments.createMap();
                    map.putDouble("steps", value.asInt());
                    sendEvent(this.mReactContext, "StepChangedEvent", map);
                }

            }
        } catch (Throwable e) {
            HelperUtil.displayMessage(this.getClass().getName());
            Log.e(this.getClass().getName(), e.getMessage());
        }
    }


    private void sendEvent(ReactContext reactContext,
                           String eventName,
                           @Nullable WritableMap params) {
        try {


            reactContext
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(eventName, params);
        } catch (Throwable e) {
            HelperUtil.displayMessage(this.getClass().getName());
            Log.e(this.getClass().getName(), e.getMessage());
        }
    }
}
