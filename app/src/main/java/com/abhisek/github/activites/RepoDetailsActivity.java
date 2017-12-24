package com.abhisek.github.activites;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abhisek.github.R;
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

public class RepoDetailsActivity extends AppCompatActivity {

    private static final String TAG =RepoDetailsActivity.class.getSimpleName() ;
    String url,description, fullName, ownerImage;
    TextView textUrl, textDescription;
    ImageView ownerImageView;
    ProgressDialog progress;
    List<String> images = new ArrayList<>();
    LinearLayout profileImgParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repo_details);
        fullName = getIntent().getStringExtra("fullName");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(fullName);
        textUrl = (TextView)findViewById(R.id.linkTextView);
        textDescription = (TextView)findViewById(R.id.descriptionTextView);
        profileImgParent = (LinearLayout)findViewById(R.id.profileImagell);
        ownerImageView =(ImageView)findViewById(R.id.ownerImage);

        new RetrieveFeedTask().execute();
        new ContributorsTask().execute();



    }
    public void showProgressBar() {

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

                URL url = new URL("https://api.github.com/repos/"+fullName);
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
                    JSONObject owner = jsonObject.getJSONObject("owner");
                    ownerImage = owner.getString("avatar_url");
                    url = jsonObject.getString("html_url");
                    description = jsonObject.getString("description");
                    textUrl.setText(url);
                    textDescription.setText(description);

                    Picasso.with(RepoDetailsActivity.this)
                            .load(ownerImage)
                            .resize(260, 260)
                            .centerCrop()
                            .transform(new CircleTransform())
                            .into(ownerImageView);

                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class ContributorsTask extends AsyncTask<Void, Void, String> {


        protected void onPreExecute() {

        }

        protected String doInBackground(Void... urls) {


            try {

                URL url = new URL("https://api.github.com/repos/"+fullName+"/contributors");

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
            progress.dismiss();


            if (response == null) {
                response = "THERE WAS AN ERROR";
            }
            else {

                try {
                    JSONArray jsonArray = new JSONArray(response);
                    for(int i=0;i<jsonArray.length();i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        final String contributorImageUrl = jsonObject.getString("avatar_url");
                        final String contributorName = jsonObject.getString("login");
                        images.add(contributorImageUrl);
                        LayoutInflater inflater = LayoutInflater.from(RepoDetailsActivity.this);
                        final View inflatedLayout = inflater.inflate(R.layout.addprofilepic, null, false);
                        profileImgParent.addView(inflatedLayout);
                        ImageView contributorImage = (ImageView)inflatedLayout.findViewById(R.id.contributorImageView);

                        TextView contributorTextView  = (TextView) inflatedLayout.findViewById(R.id.contributorNameTextView);
                                contributorTextView.setText(contributorName);

                        Picasso.with(RepoDetailsActivity.this)
                                .load(contributorImageUrl)
                                .resize(140, 140)
                                .centerCrop()
                                .transform(new CircleTransform())
                                .into(contributorImage);

                        inflatedLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(RepoDetailsActivity.this, ContributorDetails.class);
                                intent.putExtra("fullName", contributorName);
                                intent.putExtra("logo", contributorImageUrl);
                                startActivity(intent);
                            }
                        });

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
        int i = item.getItemId();
        if (i== android.R.id.home){
            this.finish();
        }

        return false;
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

}
