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

public class StepHistorySave {
    private ReactContext mReactContext;
    private GoogleFitManager googleFitManager;
    private static final String TAG = "Step";


    public StepHistorySave(ReactContext reactContext, GoogleFitManager googleFitManager) {
        this.mReactContext = reactContext;
        this.googleFitManager = googleFitManager;
    }

    public void saveStep(long startTime, long endTime, int step, Promise promise) {
        try {
            DataSource dataSource = new DataSource.Builder()
                    .setAppPackageName(mReactContext.getPackageName())
                    .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                    .setType(DataSource.TYPE_RAW)
                    .build();


            DataSet stepCountDeltaDataSet = DataSet.create(dataSource);

            DataPoint stepCountDelta = stepCountDeltaDataSet.createDataPoint().setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
            stepCountDelta.getValue(Field.FIELD_STEPS).setInt(step);
            stepCountDeltaDataSet.add(stepCountDelta);


            // Inserir o conjunto de dados na conta do usuário
            Fitness.getHistoryClient(mReactContext, GoogleSignIn.getLastSignedInAccount(mReactContext))
                    .insertData(stepCountDeltaDataSet)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Dados de passos inseridos com sucesso.");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Erro ao inserir dados de passos.", e);
                        }
                    });


        } catch (Throwable e) {
            HelperUtil.displayMessage(this.getClass().getName());
            Log.error(this.getClass().getName(),e.getMessage());
        }
    }


}
