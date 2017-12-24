package com.abhisek.github.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.abhisek.github.R;
import com.abhisek.github.activites.RepoDetailsActivity;
import com.abhisek.github.model.RepositoryData;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by bapu on 2/11/2017.
 */

public class RepositoriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    List<RepositoryData> data = Collections.emptyList();
    List<RepositoryData> filterList = new ArrayList<RepositoryData>();
    ProgressDialog progress;



    // create constructor to innitilize context and data sent from MainActivity
    public RepositoriesAdapter(Context context, List<RepositoryData> data) {
            this.context = context;
            inflater = LayoutInflater.from(context);
            this.data = data;
            filterList.addAll(this.data);

    }

    // Inflate the layout when viewholder created
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        View view = inflater.inflate(R.layout.repository_container,parent,false);
        MyHolder holder = new MyHolder(view);
        return holder;
    }
    // Bind data
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        // Get current position of item in recyclerview to bind data and assign values from list

        final MyHolder myHolder= (MyHolder) holder;
        RepositoryData current = filterList.get(position);
        myHolder.textRepoName.setText(current.repoName);
        myHolder.textFullRepoName.setText(current.fullRepoName);
        myHolder.textWatchersTextView.setText(current.repowatchers);
        myHolder.textCommitsCount.setText(current.commitsCount);

        // load image into imageview using glide

      DownloadBitmap task = new DownloadBitmap();
       String bitmap= String.valueOf(task.execute(current.logo));

        Glide.with(context).load(current.logo)
                .asBitmap()
                .placeholder(R.mipmap.ic_launcher_round)
                .error(R.mipmap.ic_launcher_round)
                .centerCrop()
                .into(new BitmapImageViewTarget(myHolder.logoImg) {
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        myHolder.logoImg.setImageDrawable(circularBitmapDrawable);
                    }


        });


    }
    // return total item from List
    @Override
    public int getItemCount() {
        return (null != filterList ? filterList.size() : 0);
    }

    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textRepoName, textrepoFullNameView,textFullRepoName,textWatchersTextView,
        textCommitsCount;
        ImageView logoImg;


        public MyHolder(View itemView) {
            super(itemView);

            textRepoName =(TextView)itemView.findViewById(R.id.repoName);
            textFullRepoName =(TextView)itemView.findViewById(R.id.fullRepoName);
            logoImg =(ImageView)itemView.findViewById(R.id.ownerImageView);
            textWatchersTextView = (TextView)itemView.findViewById(R.id.watchersTextView);
            textCommitsCount =(TextView)itemView.findViewById(R.id.commitTextView);


            itemView.setOnClickListener(this);

        }


        @Override
        public void onClick(View view) {
           //Implement method for further details
            String fullName = textFullRepoName.getText().toString();
            Intent intent = new Intent(context, RepoDetailsActivity.class);
            intent.putExtra("fullName", fullName);
            context.startActivity(intent);

        }
    }

    public void showProgressBar() {
        progress = new ProgressDialog(context);
        progress.setMessage("Loading..Please Wait..");
        progress.setCancelable(true);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();

    }



    public class DownloadBitmap extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params) {

            try {
                URL url = new URL((String) params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return String.valueOf(myBitmap);
            } catch (IOException e) {
                // Log exception
                return null;
            }

        }

        @Override
        protected  void onPostExecute(String result) {
            super.onPostExecute(result);


        }
    }



}
