package com.abhisek.mapprr_github;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ContributorDetails extends AppCompatActivity {

    ImageView ownerImageView;
    ProgressDialog progress;
    String contributorName, contributorLogo;
    public static List<RepositoryData> contributorDataList = new ArrayList<>();
    String full_name, watchers,name, id, avatar_url, fullNameApi;
    private RecyclerView repoDisplay;
    private RepositoriesAdapter mAdapter;
    LinearLayoutManager layoutManager ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contributor_details);

        contributorName = getIntent().getStringExtra("fullName");
        contributorLogo = getIntent().getStringExtra("logo");

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        repoDisplay = (RecyclerView) findViewById(R.id.contributorRepoList);
        repoDisplay.setLayoutManager(layoutManager);

        ownerImageView =(ImageView)findViewById(R.id.ownerImage);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(contributorName);

        Picasso.with(ContributorDetails.this)
                .load(contributorLogo)
                .resize(260, 260)
                .centerCrop()
                .transform(new CircleTransform())
                .into(ownerImageView);

        new RetrieveFeedTask().execute();

    }

    public class CircleTransform implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());

            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
            if (squaredBitmap != source) {
                source.recycle();
            }

            Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(squaredBitmap,
                    BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setAntiAlias(true);

            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);

            squaredBitmap.recycle();
            return bitmap;
        }

        @Override
        public String key() {
            return "circle";
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i== android.R.id.home){
            this.finish();
        }

        return false;
    }
    public void showProgressBar() {

       // Log.i(TAG, "showProgressBar");
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

                URL url = new URL("https://api.github.com/users/"+contributorName+"/repos");

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
                    contributorDataList.clear();
                    for(int i=0;i<10;i++) {
                        RepositoryData repositoryData = new RepositoryData();

                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        id  = jsonObject.getString("id");
                        name  = jsonObject.getString("name");
                        full_name =jsonObject.getString("full_name");
                        JSONObject owner = jsonObject.getJSONObject("owner");
                        avatar_url = owner.getString("avatar_url");
                        repositoryData.repoName = name;
                        repositoryData.fullRepoName = full_name;
                        repositoryData.logo = avatar_url;
                        repositoryData.setRepoId(id);
                        contributorDataList.add(repositoryData);
                    }

                    mAdapter = new RepositoriesAdapter(ContributorDetails.this,contributorDataList);
                    repoDisplay.setLayoutManager(layoutManager);
                    repoDisplay.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();
                    progress.dismiss();
              /*      for (String name : fullNames) {

                        Intent intent = new Intent(ContributorDetails.this, DownloadIntentService.class);
                        intent.putExtra(KEY_NAME, name);
                        intent.putExtra("receiverTag", mReceiver);
                        startService(intent);

                    }*/

                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }




        }
    }
}
