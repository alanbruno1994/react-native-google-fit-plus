package com.reactnative.googlefit;

import android.util.Log;

import com.facebook.react.bridge.*;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.fitness.*;
import com.google.android.gms.fitness.data.*;
import com.google.android.gms.fitness.request.*;
import com.google.android.gms.tasks.*;


import java.util.*;

import java.util.concurrent.TimeUnit;

import static com.google.android.gms.fitness.data.Device.TYPE_WATCH;

public class HeartHistory {
    private ReactContext mReactContext;
    private GoogleFitManager googleFitManager;
    private static final String TAG = "RNGoogleFit";
    private static final String HEARTBEAT_FIELD_NAME = "heartbeat";

    public HeartHistory(ReactContext reactContext, GoogleFitManager googleFitManager){
        this.mReactContext = reactContext;
        this.googleFitManager = googleFitManager;
    }

    public void saveHeart(long startTime,long endTime, float value, Promise promise) {
        try {
            DataSource dataSource = new DataSource.Builder()
                    .setAppPackageName(this.mReactContext.getPackageName())
                    .setDataType(DataType.TYPE_HEART_RATE_BPM)
                    .setType(DataSource.TYPE_RAW)
                    .build();

            DataSet dataSet = DataSet.create(dataSource);
            DataPoint dataPoint = dataSet.createDataPoint().setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
            dataPoint.getValue(Field.FIELD_BPM).setFloat(value);
            dataSet.add(dataPoint);

            Fitness.getHistoryClient(this.mReactContext, GoogleSignIn.getLastSignedInAccount(this.mReactContext))
                    .insertData(dataSet)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            promise.resolve(true);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    promise.resolve(false);
                }
            });
        }catch (Throwable e){
            Log.e(this.getClass().getName(),e.getMessage());
            HelperUtil.displayMessage(this.getClass().getName());
        }
    }


}

