package jp.ac.u_tokyo.kyoyo.seo.mylibraryfactory;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.NicoAPIException;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.NicoClient;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.VideoInfo;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.VideoInfoManager;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.VideoInfoPackage;

/**
 * Created by Seo-4d696b75 on 2017/02/04.
 */

public abstract class CustomListActivity extends AppCompatActivity {

    protected NicoClient nicoClient;

    protected TextView textViewMes;
    protected Button buttonGet;
    protected ListView listViewVideos;
    protected Resources resources;

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        textViewMes = (TextView)findViewById(R.id.textViewMes);
        buttonGet = (Button)findViewById(R.id.buttonGet);
        listViewVideos = (ListView)findViewById(R.id.listViewVideos);

        Intent intent = getIntent();
        if ( intent != null ){
               nicoClient = (NicoClient)intent.getSerializableExtra(NicoClient.INTENT);
        }
        resources = getResources();
        if ( nicoClient == null || resources == null ){
            showMessage("fail to get intent and resource");
            finish();
        }
    }

    protected void showMessage(String message){
        if ( message != null ) {
            Toast.makeText(CustomListActivity.this, message, Toast.LENGTH_SHORT).show();
            Log.d("DemoApp",message);
        }
    }

    protected void setVideos(final List<VideoInfo> list){
        if ( list != null ){
            listViewVideos.setAdapter(new CustomListAdapter(CustomListActivity.this, list));
            listViewVideos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    CustomListAdapter adapter = (CustomListAdapter)parent.getAdapter();
                    VideoInfo info = adapter.getItem(position);
                    shoeVideoDetails((VideoInfoManager)info);
                }
            });
        }
    }

    private void shoeVideoDetails (final VideoInfoManager info){
        if ( info == null ){
            return;
        }
        new AsyncTask<String, Void, String>() {
            private ProgressDialog progress;
            private final String success = "success";
            @Override
            protected void onPreExecute() {
                progress = new ProgressDialog(CustomListActivity.this);
                progress.setMessage("Getting details...");
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.show();
            }
            @Override
            protected String doInBackground(String... params) {
                try {
                    if ( nicoClient.isLogin() ) {
                        if (info.complete() && info.getFlv(nicoClient.getCookieStore())) {
                            return success;
                        }
                    }else{
                        if (info.complete() ) {
                            return success;
                        }
                    }
                }catch(NicoAPIException e){
                    return e.getMessage();
                }
                return "fail to get details";
            }
            @Override
            protected void onPostExecute(String response) {
                progress.cancel();
                progress = null;
                if ( response.equals(success) ){
                    if ( dialog != null ){
                        dialog.cancel();
                        dialog = null;
                    }
                    try {
                        AlertDialog.Builder builder = new AlertDialog.Builder(CustomListActivity.this);
                        builder.setTitle("Video Details");
                        Context context = CustomListActivity.this;
                        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        ViewGroup root = (ViewGroup) findViewById(R.id.dialogDetailsRoot);
                        View view = inflater.inflate(R.layout.dialog_details, root, true);
                        ((ImageView)view.findViewById(R.id.imageViewDetailsThumbnail)).setImageDrawable(info.getThumbnail());
                        ((TextView)view.findViewById(R.id.textViewDetailsTitle)).setText(info.getTitle());
                        ((TextView)view.findViewById(R.id.textViewDetailsID)).setText(info.getID());
                        ((TextView)view.findViewById(R.id.textViewDetailsDescription)).setText(info.getDescription());
                        ((TextView)view.findViewById(R.id.textViewDetailsLength)).setText(info.formatLength());
                        ((TextView)view.findViewById(R.id.textViewDetailsView)).setText(info.formatViewCounter());
                        ((TextView)view.findViewById(R.id.textViewDetailsMyList)).setText(info.formatMyListCounter());
                        ((TextView)view.findViewById(R.id.textViewDetailsComment)).setText(info.formatCommentCounter());
                        ((TextView)view.findViewById(R.id.textViewDetailsThumbnailURL)).setText(info.getThumbnailUrl());
                        ((TextView)view.findViewById(R.id.textViewDetailsDate)).setText(info.getDate());
                        List<String> tags = info.getTagsList();
                        StringBuilder stringBuilder = new StringBuilder();
                        for ( String tag : tags){
                            stringBuilder.append(tag);
                            stringBuilder.append(" ");
                        }
                        ((TextView)view.findViewById(R.id.textViewDetailsTags)).setText(stringBuilder.toString());
                        ((TextView)view.findViewById(R.id.textViewDetailsContributorID)).setText(String.valueOf(info.getContributorID()));
                        ((TextView)view.findViewById(R.id.textViewDetailsContributorName)).setText(info.getContributorName());
                        ((TextView)view.findViewById(R.id.textViewDetailsContributorIconUrl)).setText(info.getContributorIconUrl());
                        if ( nicoClient.isLogin() ) {
                            ((TextView) view.findViewById(R.id.textViewDetailsThreadID)).setText(info.getThreadID());
                            ((TextView) view.findViewById(R.id.textViewDetailsMesServer)).setText(info.getMessageServerUrl());
                            ((TextView) view.findViewById(R.id.textViewDetailsFlvURL)).setText(info.getFlvUrl());
                        }
                        builder.setView(view);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                dialog.cancel();
                                dialog = null;
                            }
                        });
                        builder.setNegativeButton("Get Recommend", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try{
                                    Intent intent = new Intent(CustomListActivity.this, RecommendActivity.class);
                                    intent.putExtra(VideoInfoPackage.INTENT_KEY,info.pack());
                                    intent.putExtra(NicoClient.INTENT,nicoClient);
                                    startActivity(intent);
                                }catch (NicoAPIException e){
                                    showMessage(e.getMessage());
                                }
                            }
                        });
                        dialog = builder.create();
                        dialog.show();
                    }catch (NicoAPIException e){
                        showMessage(e.getMessage());
                    }
                }else{
                    showMessage(response);
                }
            }
        }.execute();
    }

    private class CustomListAdapter extends ArrayAdapter<VideoInfo>{
        private LayoutInflater inflater;
        private int index = 0;
        private Map<Integer,String> viewMap;
        public CustomListAdapter(Context context, List<VideoInfo> list){
            super(context, 0, list);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            viewMap = new HashMap<Integer, String>();
        }
        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            View view = convertView;
            final VideoInfoManager item = (VideoInfoManager)this.getItem(position);
            if (view == null) {
                view = inflater.inflate(R.layout.video_cell, null);
                view.setTag(index);
                viewMap.put(index,item.getID());
                index++;
            }else{
                int index = (int)view.getTag();
                if ( viewMap.containsKey(index) ){
                    viewMap.remove(index);
                    viewMap.put(index,item.getID());
                }
            }
            if (item != null) {
                TextView title = (TextView)view.findViewById(R.id.textViewTitle);
                TextView viewCount = (TextView)view.findViewById(R.id.textViewViewCounter);
                TextView mylistCount = (TextView)view.findViewById(R.id.textViewMylistCounter);
                final ImageView thumbnail = (ImageView)view.findViewById(R.id.imageViewThumbnail);
                TextView length = (TextView)view.findViewById(R.id.textViewLength);
                try{
                    title.setText(item.getString(VideoInfo.TITLE));
                    length.setText(item.formatLength());
                    viewCount.setText("views:" + item.formatCounter(VideoInfo.VIEW_COUNTER));
                    mylistCount.setText("myList:" + item.formatCounter(VideoInfo.MY_LIST_COUNTER));
                    new AsyncTask<String, Void, String> (){
                        private Drawable thumbnailImage;
                        @Override
                        protected void onPreExecute() {
                            thumbnail.setImageDrawable(resources.getDrawable(R.drawable.temp_thumbnail));
                        }
                        @Override
                        protected String doInBackground(String... params) {
                            try {
                                thumbnailImage = item.getThumbnail();
                                return null;
                            }catch (NicoAPIException e){
                                return e.getMessage();
                            }
                        }
                        @Override
                        protected void onPostExecute(String response) {
                            showMessage(response);
                            if ( thumbnailImage != null ){
                                if ( viewMap.containsValue(item.getID()) ){
                                    thumbnail.setImageDrawable(thumbnailImage);
                                }
                            }
                        }
                    }.execute();
                }catch (NicoAPIException e){
                    showMessage(e.getMessage());
                }
            }
            return view;
        }
    }

}
