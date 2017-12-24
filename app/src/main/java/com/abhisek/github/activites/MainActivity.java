package com.abhisek.github.activites;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import com.abhisek.github.MyApplication;
import com.abhisek.github.services.MyResultReceiver;
import com.abhisek.github.R;
import com.abhisek.github.model.RepositoryData;
import com.abhisek.github.adapters.RepositoriesAdapter;
import com.abhisek.github.services.ConnectivityReceiver;
import com.abhisek.github.services.DownloadIntentService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity  implements ConnectivityReceiver.ConnectivityReceiverListener,
        SearchView.OnQueryTextListener,MyResultReceiver.Receiver {


    private static final String TAG =MainActivity.class.getSimpleName();
    private RecyclerView repoDisplay;
    private RepositoriesAdapter mAdapter;
    LinearLayoutManager layoutManager ;
    private Context mContext;
    ProgressDialog progress;
    String followers_url;
    String full_name, watchers,name, id, avatar_url, fullNameApi;
    public static List<RepositoryData> repositoryDataList = new ArrayList<>();
    List<String> watchersCount = new ArrayList<>();
    List<String> fullNames = new ArrayList<>();
    List<String> commits = new ArrayList<>();
    int count ,i,j =0;
    String searchString, finalUrl, orderType, sortType;
    public static final String KEY_NAME = "fullName" ;
    public MyResultReceiver mReceiver;
    Boolean searchTask =false;




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
        mReceiver = new MyResultReceiver(new Handler());
        mReceiver.setReceiver((MyResultReceiver.Receiver) this);
        new RetrieveFeedTask().execute("https://api.github.com/repositories?sort=watchers&order=desc");


    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        repositoryDataList = (List<RepositoryData>) resultData.getSerializable("repoList");
        mAdapter = new RepositoriesAdapter(mContext,repositoryDataList);
        repoDisplay.setLayoutManager(layoutManager);
        repoDisplay.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        progress.dismiss();

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
        progress = new ProgressDialog(this);
        progress.setMessage("Loading..Please Wait..");
        progress.setCancelable(true);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();

    }


    class RetrieveFeedTask extends AsyncTask<String, Void, String> {


        protected void onPreExecute() {

               showProgressBar();

        }

        protected String doInBackground(String... params) {


            try {
                URL url = new URL(params[0]);


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
                    for (String name : fullNames) {
                        Intent intent = new Intent(MainActivity.this, DownloadIntentService.class);
                        intent.putExtra(KEY_NAME, name);
                        intent.putExtra("receiverTag", mReceiver);
                        intent.putExtra("ActivityTag", "MainActivity");
                        startService(intent);

                    }

                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    class SearchTask extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
            searchTask =true;
            showProgressBar();

        }

        protected String doInBackground(String... params) {


            try {
                URL url = new URL(params[0]);
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
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray itemsJsonArray = jsonObject.getJSONArray("items");
                    repositoryDataList.clear();
                    fullNames.clear();

                    //displaying top 10 results
                    int length=0;
                    if(itemsJsonArray.length()>=10) {
                        length =10;
                    }
                    else {

                        length =itemsJsonArray.length();
                    }
                    for(int i=0;i<length;i++) {
                        RepositoryData repositoryData = new RepositoryData();
                        JSONObject itemsJsonObject = itemsJsonArray.getJSONObject(i);
                        id  = itemsJsonObject.getString("id");
                        name  = itemsJsonObject.getString("name");
                        full_name =itemsJsonObject.getString("full_name");
                        fullNames.add(full_name);
                        JSONObject owner = itemsJsonObject.getJSONObject("owner");
                        avatar_url = owner.getString("avatar_url");
                        repositoryData.repoName = name;
                        repositoryData.fullRepoName = full_name;
                        repositoryData.logo = avatar_url;
                        repositoryData.setRepoId(id);
                        repositoryDataList.add(repositoryData);
                    }

                    for (String name : fullNames) {
                        Intent intent = new Intent(MainActivity.this, DownloadIntentService.class);
                        intent.putExtra(KEY_NAME, name);
                        intent.putExtra("receiverTag", mReceiver);
                        intent.putExtra("ActivityTag", "MainActivity");
                        startService(intent);
                    }
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
        MenuItem filterItem = menu.findItem(R.id.action_filter);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        //Querying for github repos
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchString = query;
                new SearchTask().execute("https://api.github.com/search/repositories?q="+searchString+"&sort=stars&order=desc");
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });

        filterItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                editMessageAlertDialog();
                return false;
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


    public void editMessageAlertDialog() {
        LayoutInflater li = LayoutInflater.from(this);
        View dialogView = li.inflate(R.layout.filter_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);
        alertDialogBuilder.setView(dialogView);
        alertDialogBuilder.setTitle("Filter");

        final RadioButton stars = (RadioButton)dialogView.findViewById(R.id.radio_stars);
        final RadioButton forks = (RadioButton)dialogView.findViewById(R.id.radio_forks);
        final RadioButton desc = (RadioButton)dialogView.findViewById(R.id.radio_descend);
        final RadioButton ascend = (RadioButton)dialogView.findViewById(R.id.radio_ascend);

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("APPLY",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {

                                if(stars.isChecked()){
                                    sortType = "stars";
                                }
                                else if(forks.isChecked()) {
                                    sortType = "forks";
                                }

                                if(desc.isChecked()) {
                                    orderType = "desc";
                                }
                                else if(ascend.isChecked()) {
                                    orderType = "asc";
                                }

                                if(searchTask) {
                                    finalUrl = "https://api.github.com/search/repositories?q="+searchString+"&sort="+sortType+"&order="+orderType;
                                     new SearchTask().execute(finalUrl);
                                }
                                else {
                                    finalUrl ="https://api.github.com/repositories?sort="+sortType+"&order="+orderType;
                                    new RetrieveFeedTask().execute(finalUrl);
                                }

                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }



}



