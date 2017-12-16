package com.abhisek.mapprr_github;

import android.app.ProgressDialog;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity  implements ConnectivityReceiver.ConnectivityReceiverListener, SearchView.OnQueryTextListener {


    private static final String TAG =MainActivity.class.getSimpleName() ;

    private RecyclerView repoDisplay;
    private RepositoriesAdapter mAdapter;
    LinearLayoutManager layoutManager ;
    private Context mContext;
    ProgressDialog progress;
    String followers_url;
    String full_name, watchers,name, id, avatar_url, fullNameApi;
    List<RepositoryData> repositoryDataList = new ArrayList<>();;
    List<String> watchersCount = new ArrayList<>();;
    List<String> fullNames = new ArrayList<>();
    List<String> commits = new ArrayList<>();
    int count ,i,j =0;
    String searchString;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        checkConnection();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);
        repoDisplay = (RecyclerView) findViewById(R.id.notiList);
        repoDisplay.setLayoutManager(layoutManager);
        new RetrieveFeedTask().execute();


    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.getInstance().setConnectivityListener(this);
    }


    // Method to manually check connection status
    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showSnack(isConnected);
    }

    // Showing the status in Snackbar
    private void showSnack(boolean isConnected) {
        String message;
        int color;
        if (isConnected) {
            //  message = "Good! Connected to Internet";
          //  new RetrieveFeedTask().execute();
            color = Color.WHITE;
        } else {
            message = "Sorry! Not connected to internet";
            color = Color.RED;
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }

    }


    /**
     * Callback will be triggered when there is change in
     * network connection
     */
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    public void showProgressBar() {

        Log.i(TAG, "showProgressBar");
        progress = new ProgressDialog(this);
        progress.setMessage("Loading..Please Wait..");
        progress.setCancelable(true);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();

    }


    class RetrieveFeedTask extends AsyncTask<Void, Void, String> {


        protected void onPreExecute() {

               showProgressBar();

        }

        protected String doInBackground(Void... urls) {


            try {

                URL url = new URL("https://api.github.com/repositories?sort=watchers&order=desc");

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
                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }

        }

        protected void onPostExecute(String response) {


            if (response == null) {
                response = "THERE WAS AN ERROR";
            }
            else {

              //  Log.i(TAG, response);
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    repositoryDataList.clear();
                    fullNames.clear();

                    for(int i=0;i<10;i++) {
                       RepositoryData repositoryData = new RepositoryData();

                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        id  = jsonObject.getString("id");
                        name  = jsonObject.getString("name");
                        full_name =jsonObject.getString("full_name");
                        fullNames.add(full_name);
                       // new FollowersTask().execute();
                        JSONObject owner = jsonObject.getJSONObject("owner");
                        avatar_url = owner.getString("avatar_url");
                        repositoryData.repoName = name;
                        repositoryData.fullRepoName = full_name;
                        repositoryData.logo = avatar_url;
                        repositoryData.setRepoId(id);
                         repositoryDataList.add(repositoryData);
                    }
                        new FollowersTask().execute();
                  //  new CommitsTask().execute();

                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }




        }
    }


    class FollowersTask extends AsyncTask<Void, Void, String> {


        protected void onPreExecute() {

        }

        protected String doInBackground(Void... urls) {

            try {
                watchersCount.clear();


                for (String name : fullNames) {
                    fullNameApi = name;
                    Log.i(TAG, fullNameApi);
                    Log.i(TAG, String.valueOf(fullNames.size()));


                    URL url = new URL("https://api.github.com/repos/"+fullNameApi);

                    Log.i(TAG, String.valueOf(url));

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
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
            return String.valueOf(watchersCount);
        }






        protected void onPostExecute(String response) {
            progress.dismiss();


            if (response == null) {
                response = "THERE WAS AN ERROR";
            }
            else {
              //  count++;

                Log.i("response2", response);
/*
                try {
                *//*    JSONObject jsonObject = new JSONObject(response);
                   String watchers = jsonObject.getString("watchers");
                    Log.i(TAG, watchers);
                  // Log.i(TAG, String.valueOf(count));
                    watchersCount.add(watchers);*//*

                } catch (JSONException e) {
                    e.printStackTrace();
                }*/


            }

                Log.i("watchersCount", String.valueOf(watchersCount.size()));

                for(int j=0;j<watchersCount.size();j++) {
                    repositoryDataList.get(j).repowatchers = watchersCount.get(j);
                }

                mAdapter = new RepositoriesAdapter(mContext,repositoryDataList);
                repoDisplay.setLayoutManager(layoutManager);
                repoDisplay.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
            }

        }


    class SearchTask extends AsyncTask<Void, Void, String> {



        protected void onPreExecute() {
            Log.i(TAG, "SearchTask");

            showProgressBar();

        }

        protected String doInBackground(Void... urls) {


            try {

                URL url = new URL("https://api.github.com/repositories?q="+searchString+"&sort=watchers&order=desc");
                Log.i(TAG, String.valueOf(url));

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
                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }

        }

        protected void onPostExecute(String response) {


            if (response == null) {
                response = "THERE WAS AN ERROR";
            }
            else {

                try {
                    JSONArray jsonArray = new JSONArray(response);
                    repositoryDataList.clear();
                    fullNames.clear();
                    //displaying top 10 results
                    for(int i=0;i<10;i++) {
                        RepositoryData repositoryData = new RepositoryData();

                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        id  = jsonObject.getString("id");
                        name  = jsonObject.getString("name");
                        full_name =jsonObject.getString("full_name");
                        fullNames.add(full_name);
                        JSONObject owner = jsonObject.getJSONObject("owner");
                        avatar_url = owner.getString("avatar_url");
                        repositoryData.repoName = name;
                        repositoryData.fullRepoName = full_name;
                        repositoryData.logo = avatar_url;
                        repositoryData.setRepoId(id);
                        repositoryDataList.add(repositoryData);
                    }
                    new FollowersTask().execute();
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // API 5+ solution
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_github, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        //Querying for github repos
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchString = query;
                new SearchTask().execute();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }


    class CommitsTask extends AsyncTask<Void, Void, String> {


        protected void onPreExecute() {

        }

        protected String doInBackground(Void... urls) {


            try {

                for (String name : fullNames) {
                    fullNameApi = name;


                    URL url = new URL("https://api.github.com/repos/" + fullNameApi+"/stats/commit_activity");


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
                        Log.i(TAG +"commits",stringBuilder.toString());
                        JSONArray jsonArray = new JSONArray(stringBuilder.toString());

                        int total =0;
                        for(int i=0;i<jsonArray.length();i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            total = total+ jsonObject.getInt("total");

                        }
                        commits.add(String.valueOf(total));

                    } finally {
                        urlConnection.disconnect();
                    }
                }
            } catch (Exception e) {
                Log.i("ERROR", e.getMessage(), e);
                return null;
            }
            return String.valueOf(commits);
        }






        protected void onPostExecute(String response) {
            progress.dismiss();
            if (response == null) {
                response = "THERE WAS AN ERROR";
            }
            else {
                progress.dismiss();            }

            for(int j=0;j<commits.size();j++) {
                repositoryDataList.get(j).commitsCount = commits.get(j);
            }
            mAdapter = new RepositoriesAdapter(mContext,repositoryDataList);
            repoDisplay.setLayoutManager(layoutManager);
            repoDisplay.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        }

    }

}



