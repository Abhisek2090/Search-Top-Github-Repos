package com.abhisek.mapprr_github;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bapu on 12/9/2017.
 */

public class DownloadIntentService extends IntentService {

    public static final String TAG = DownloadIntentService.class.getSimpleName();
    List<String> watchersCount = new ArrayList<>();
    List<String> commitsCount = new ArrayList<>();
    int count=0;


    public DownloadIntentService() {
        super("DownloadIntentService");
        setIntentRedelivery(true);
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

       // String fullName = intent.getStringExtra(MainActivity.KEY_NAME);
        ResultReceiver rec = intent.getParcelableExtra("receiverTag");
       String fullName = intent.getStringExtra(MainActivity.KEY_NAME);
        watchersCount(fullName);
        commitsCount(fullName);
        Log.i(TAG+"watchersCount", String.valueOf(watchersCount.size()));

        for(int j=0;j<watchersCount.size();j++) {
            count++;
            MainActivity.repositoryDataList.get(j).repowatchers = watchersCount.get(j);
            MainActivity.repositoryDataList.get(j).commitsCount = commitsCount.get(j);

        }
            if(watchersCount.size() ==10) {
                if (rec != null) {
                    Bundle b = new Bundle();
                    b.putSerializable("repoList", (Serializable) MainActivity.repositoryDataList);
                    rec.send(0, b);

                }
            }

    }

    private void watchersCount(String fullName) {


        try {

            URL url = new URL("https://api.github.com/repos/"+fullName);

           // Log.i(TAG, String.valueOf(url));

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Authorization", "token af4fa32d5869d0cd57179926af02481ac650a4bb");

            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                String watchers = jsonObject.getString("watchers");
                Log.i(TAG, watchers);
                // Log.i(TAG, String.valueOf(count));
                watchersCount.add(watchers);


            } finally {
                urlConnection.disconnect();
            }


        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void commitsCount(String fullName) {


        try {

            URL url = new URL("https://api.github.com/repos/"+fullName+"/contributors");

           // Log.i(TAG, String.valueOf(url));

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Authorization", "token af4fa32d5869d0cd57179926af02481ac650a4bb");

            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();

                if (stringBuilder.toString() != null) {

                    JSONArray jsonArray = new JSONArray(stringBuilder.toString());


                int total = 0;
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    total = total + jsonObject.getInt("contributions");

                }
                Log.i(TAG, String.valueOf(total));
                commitsCount.add(String.valueOf(total));
            }
            else  {
                    Log.i(TAG, "noValue");
                }


            } finally {
                urlConnection.disconnect();
            }


        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}
