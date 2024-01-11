package com.reactnative.googlefit;

import android.os.AsyncTask;

import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataSet;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import android.util.Log;

class SaveDataHelper extends AsyncTask<Void, Void, Void> {
  private ArrayList<DataSet> dataSets;
  private GoogleFitManager googleFitManager;

  SaveDataHelper(ArrayList<DataSet> dataSets, GoogleFitManager googleFitManager) {
    this.dataSets = dataSets;
    this.googleFitManager = googleFitManager;
  }

  @Override
  protected Void doInBackground(Void... params) {
    try {
      for (DataSet dataSet : this.dataSets) {
        Fitness.HistoryApi.insertData(googleFitManager.getGoogleApiClient(), dataSet)
                .await(1, TimeUnit.MINUTES);
      }
    }catch (Throwable e){
      Log.error(this.getClass().getName(),e.getMessage());
      HelperUtil.displayMessage(this.getClass().getName());
    }
    return null;
  }
}
