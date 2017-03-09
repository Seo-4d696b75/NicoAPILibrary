package jp.ac.u_tokyo.kyoyo.seo.mylibraryfactory;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.CommentInfo;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.NicoAPIException;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.NicoClient;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.NicoCommentPost;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.VideoInfo;

/**
 * Created by Seo-4d696b75 on 2017/02/04.
 */

public abstract class CustomListActivity extends AppCompatActivity implements CustomDialog.onClickListener, CustomDialog.OnItemClickListener {

    protected NicoClient nicoClient;

    protected TextView textViewMes;
    protected Button buttonGet;
    protected ListView listViewVideos;
    protected Resources resources;

    private AlertDialog dialog;

    private final String DIALOG_TAG_DETAILS = "dialogDetails";
    private final String DIALOG_TAG_MENU = "dialogMenu";
    private final String DIALOG_TAG_COMMENT = "dialogComment";
    private final String DIALOG_TAG_COMMENT_POST = "dialogCommentPost";

    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        textViewMes = (TextView)findViewById(R.id.textViewMes);
        buttonGet = (Button)findViewById(R.id.buttonGet);
        listViewVideos = (ListView)findViewById(R.id.listViewVideos);

        Intent intent = getIntent();
        if ( intent != null ){
               nicoClient = (NicoClient)intent.getExtras().getParcelable(NicoClient.INTENT);
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

    protected void setVideos(final List<? extends VideoInfo> list){
        if ( list != null ){
            List<VideoInfo> newList = new ArrayList<VideoInfo>();
            for ( VideoInfo item : list){
                newList.add(item);
            }
            listViewVideos.setAdapter(new CustomListAdapter(CustomListActivity.this, newList));
            listViewVideos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    CustomListAdapter adapter = (CustomListAdapter)parent.getAdapter();
                    VideoInfo info = adapter.getItem(position);
                    showMenuDialog(info);
                }
            });
        }
    }

    private void showMenuDialog (VideoInfo info){
        if ( info != null ){
            Bundle args = new Bundle();
            args.putInt(CustomDialog.LAYOUT, R.layout.dialog_menu);
            args.putString(CustomDialog.TITLE,"Video Menu");
            args.putString(CustomDialog.BUTTON_POSITIVE,"Cancel");
            args.putParcelable(CustomDialog.PARAM, info);
            CustomDialog dialog = VideoMenuDialog.getInstance();
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(),DIALOG_TAG_MENU);
        }
    }

    public static class VideoMenuDialog extends CustomDialog {
        private OnItemClickListener listener;
        public static final String MENU_DETAILS ="Details";
        public static final String MENU_COMMENT = "Comment";
        public static final String MENU_COMMENT_POST = "Post comment";
        public static final String MENU_RECOMMEND = "Recommend";
        public static final String MENU_MYLIST_ADD = "Add in myList";
        public static final String MENU_TEMP_MYLIST_ADD = "Add in tempMyList";
        public static VideoMenuDialog getInstance(){
            return new VideoMenuDialog();
        }
        @Override
        protected void onCreateContentView(View view){
            if ( context instanceof OnItemClickListener) {
                this.listener = (OnItemClickListener) context;
                ListView listView = (ListView) view.findViewById(R.id.listViewVideoMenu);
                listView.setAdapter(new ArrayAdapter<String>(context, R.layout.simple_cell){
                    {
                        add(MENU_DETAILS);
                        add(MENU_COMMENT);
                        add(MENU_COMMENT_POST);
                        add(MENU_RECOMMEND);
                        add(MENU_MYLIST_ADD);
                        add(MENU_TEMP_MYLIST_ADD);
                    }
                });
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        ArrayAdapter adapter = (ArrayAdapter) parent.getAdapter();
                        Object item = adapter.getItem(position);
                        listener.onItemClick(getTag(),item,param);
                        dialog.dismiss();
                        dialog = null;
                    }
                });
            }
        }
        @Override
        public void onDestroy(){
            super.onDestroy();
            listener = null;
        }
    }

    private void showVideoDetails (final VideoInfo info){
        if ( info == null ){
            return;
        }
        new AsyncTask<Void, Void, String>() {
            private ProgressDialog progress;
            private final String SUCCESS = "success";
            @Override
            protected void onPreExecute() {
                progress = new ProgressDialog(CustomListActivity.this);
                progress.setMessage("Getting details...");
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.show();
            }
            @Override
            protected String doInBackground(Void... params) {
                try {
                    if ( nicoClient.isLogin() ) {
                        if ( !info.isOfficial() ) {
                            if (info.complete() && info.getFlv(nicoClient.getCookieStore())) {
                                return SUCCESS;
                            }else{
                                return "fail to get details";
                            }
                        }
                    }
                    if (info.complete() ) {
                        return SUCCESS;
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
                if ( response.equals(SUCCESS) ){
                    Bundle args = new Bundle();
                    args.putInt(CustomDialog.LAYOUT, R.layout.dialog_details);
                    args.putString(CustomDialog.TITLE,"Video Details");
                    args.putString(CustomDialog.BUTTON_NEUTRAL,"OK");
                    args.putString(CustomDialog.BUTTON_POSITIVE,"recommend");
                    args.putString(CustomDialog.BUTTON_NEGATIVE,"comment");
                    args.putParcelable(CustomDialog.PARAM, info);
                    args.putBoolean(VideoDetailsDialog.IS_LOGIN, nicoClient.isLogin());
                    CustomDialog dialog = VideoDetailsDialog.getInstance();
                    dialog.setArguments(args);
                    dialog.show(getSupportFragmentManager(),DIALOG_TAG_DETAILS);
                }else{
                    showMessage(response);
                }
            }
        }.execute();
    }

    public static class VideoDetailsDialog extends CustomDialog {

        public static final String IS_LOGIN = "login";

        public static VideoDetailsDialog getInstance (){
            return new VideoDetailsDialog();
        }

        @Override
        protected void onCreateContentView(View view){
            boolean isLogin = args.getBoolean(IS_LOGIN);
            VideoInfo info = (VideoInfo)param;
            try {
                ((ImageView) view.findViewById(R.id.imageViewDetailsThumbnail)).setImageBitmap(info.getThumbnail());
                ((TextView) view.findViewById(R.id.textViewDetailsTitle)).setText(info.getTitle());
                ((TextView) view.findViewById(R.id.textViewDetailsID)).setText(info.getID());
                ((TextView) view.findViewById(R.id.textViewDetailsDescription)).setText(info.getDescription());
                ((TextView) view.findViewById(R.id.textViewDetailsLength)).setText(info.formatLength());
                ((TextView) view.findViewById(R.id.textViewDetailsView)).setText(info.formatViewCounter());
                ((TextView) view.findViewById(R.id.textViewDetailsMyList)).setText(info.formatMyListCounter());
                ((TextView) view.findViewById(R.id.textViewDetailsComment)).setText(info.formatCommentCounter());
                ((TextView) view.findViewById(R.id.textViewDetailsThumbnailURL)).setText(info.getThumbnailUrl());
                ((TextView) view.findViewById(R.id.textViewDetailsDate)).setText(info.getDate());
                List<String> tags = info.getTagsList();
                StringBuilder stringBuilder = new StringBuilder();
                for (String tag : tags) {
                    stringBuilder.append(tag);
                    stringBuilder.append(" ");
                }
                ((TextView) view.findViewById(R.id.textViewDetailsTags)).setText(stringBuilder.toString());
                ((TextView) view.findViewById(R.id.textViewDetailsContributorID)).setText(String.valueOf(info.getContributorID()));
                ((TextView) view.findViewById(R.id.textViewDetailsContributorName)).setText(info.getContributorName());
                ((TextView) view.findViewById(R.id.textViewDetailsContributorIconUrl)).setText(info.getContributorIconUrl());
                if ( isLogin && !info.isOfficial()) {
                    ((TextView) view.findViewById(R.id.textViewDetailsThreadID)).setText(String.valueOf(info.getThreadID()));
                    ((TextView) view.findViewById(R.id.textViewDetailsMesServer)).setText(info.getMessageServerUrl());
                    ((TextView) view.findViewById(R.id.textViewDetailsFlvURL)).setText(info.getFlvUrl());
                }
            }catch (NicoAPIException e){

            }
        }
    }

    @Override
    public void onDialogButtonClicked(String tag, Dialog dialog, int which, Object param){
        switch ( tag ){
            case DIALOG_TAG_DETAILS:
                VideoInfo info = (VideoInfo)param;
                switch ( which ){
                    case DialogInterface.BUTTON_POSITIVE:
                        Intent intent = new Intent(CustomListActivity.this, RecommendActivity.class);
                        intent.putExtra(VideoInfo.VIDEO_KEY,info);
                        intent.putExtra(NicoClient.INTENT,nicoClient);
                        startActivity(intent);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        showComments(info,0);
                        break;
                    default:
                }
                break;
            case DIALOG_TAG_MENU:
                break;
            case DIALOG_TAG_COMMENT:
                info = (VideoInfo)param;
                switch ( which ){
                    case DialogInterface.BUTTON_POSITIVE:
                        showCommentPost(info);
                        break;
                    default:
                }
                break;
            case DIALOG_TAG_COMMENT_POST:
                if ( which == DialogInterface.BUTTON_POSITIVE){
                    postComment((NicoCommentPost)param);
                }
                break;
            default:
        }
    }

    @Override
    public void onItemClick(String tag, Object item, Object param){
        switch ( tag ){
            case DIALOG_TAG_MENU:
                VideoInfo info = (VideoInfo)param;
                if ( item instanceof String ){
                    switch ( (String)item ){
                        case VideoMenuDialog.MENU_DETAILS:
                            showVideoDetails(info);
                            break;
                        case VideoMenuDialog.MENU_COMMENT:
                            showComments(info,0);
                            break;
                        case VideoMenuDialog.MENU_COMMENT_POST:
                            showCommentPost(info);
                            break;
                        case VideoMenuDialog.MENU_RECOMMEND:
                            Intent intent = new Intent(CustomListActivity.this, RecommendActivity.class);
                            intent.putExtra(VideoInfo.VIDEO_KEY,info);
                            intent.putExtra(NicoClient.INTENT,nicoClient);
                            startActivity(intent);
                            break;
                        case VideoMenuDialog.MENU_MYLIST_ADD:
                            break;
                        case VideoMenuDialog.MENU_TEMP_MYLIST_ADD:
                            break;
                    }
                }
                break;
            default:
        }
    }

    private void showComments(final VideoInfo info, final int max){
        new AsyncTask<Void, Void, String>() {
            private ProgressDialog progress;
            private final String SUCCESS = "success";

            @Override
            protected void onPreExecute() {
                progress = new ProgressDialog(CustomListActivity.this);
                progress.setMessage("Getting comments...");
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                try {
                    if ( max > 0 ) {
                        nicoClient.getComment(info, max);
                    }else{
                        nicoClient.getComment(info);
                    }
                    return SUCCESS;
                } catch (NicoAPIException e) {
                    return e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String response) {
                progress.cancel();
                progress = null;
                if (response.equals(SUCCESS)) {
                    Bundle args = new Bundle();
                    args.putInt(CustomDialog.LAYOUT, R.layout.dialog_comment);
                    args.putString(CustomDialog.TITLE,"Video Comments");
                    args.putString(CustomDialog.BUTTON_NEUTRAL,"OK");
                    args.putString(CustomDialog.BUTTON_POSITIVE, "Post comment");
                    args.putParcelable(CustomDialog.PARAM,info);
                    CustomDialog dialog = CommentDialog.getInstance();
                    dialog.setArguments(args);
                    dialog.show(getSupportFragmentManager(),DIALOG_TAG_COMMENT);
                }
            }
        }.execute();
    }

    public static class CommentDialog extends CustomDialog {
        public static CommentDialog getInstance(){
            return new CommentDialog();
        }
        @Override
        protected void onCreateContentView(View view){
            if ( param != null ) {
                VideoInfo info = (VideoInfo)param;
                try {
                    CommentInfo.CommentGroup group = info.getComment();
                    ((ListView) view.findViewById(R.id.listViewComment)).setAdapter(new CommentAdapter(context, group.getComments()));
                }catch (NicoAPIException e){}
            }
        }
        private class CommentAdapter extends ArrayAdapter<CommentInfo>{
            private LayoutInflater inflater;
            protected CommentAdapter(Context context, List<CommentInfo> list){
                super(context,0,list);
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            @Override
            public View getView(final int position, View convertView, final ViewGroup parent) {
                View view = convertView;
                CommentInfo item = this.getItem(position);
                if ( view == null ){
                    view = inflater.inflate(R.layout.comment_cell,null);
                }
                if ( item != null ){
                    TextView textTime = (TextView)view.findViewById(R.id.textViewCommentTime);
                    TextView textComment = (TextView)view.findViewById(R.id.textViewComment);
                    String startTime = String.format("%02d:%02d %d",(int)item.getStart()/60000,((int)item.getStart()%60000)/1000,item.getNgLevel());
                    textTime.setText(startTime);
                    textComment.setShadowLayer(5f,5f,5f,Color.BLACK);
                    textComment.setTextColor(item.getColor());
                    textComment.setText(item.getContent());
                }
                return view;
            }
        }
    }

    private void showCommentPost(final VideoInfo info){
        try {
            Bundle args = new Bundle();
            args.putInt(CustomDialog.LAYOUT, R.layout.dialog_comment_post);
            args.putString(CustomDialog.TITLE, "Posting comment");
            args.putString(CustomDialog.BUTTON_NEUTRAL, "Cancel");
            args.putString(CustomDialog.BUTTON_POSITIVE, "Post comment");
            args.putParcelable(CustomDialog.PARAM, nicoClient.getNicoCommentPost(info));
            CustomDialog dialog = CommentPostDialog.getInstance();
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), DIALOG_TAG_COMMENT_POST);
        }catch (NicoAPIException e){

        }
    }

    public static class CommentPostDialog extends CustomDialog{
        public static CommentPostDialog getInstance(){
            return new CommentPostDialog();
        }
        @Override
        protected void onCreateContentView(View view){
            final NicoCommentPost commentPost = (NicoCommentPost)param;
            final TextView textTime = (TextView)view.findViewById(R.id.textViewCommentPostTime);
            final EditText textComment = (EditText)view.findViewById(R.id.editTextCommentPost);
            final View spinnerBack = view.findViewById(R.id.spinnerBackCommentPost);
            final Spinner spinnerColor = (Spinner)view.findViewById(R.id.spinnerCommentPost);
            final NumberPicker pickerMin = (NumberPicker)view.findViewById(R.id.numberPickerCommentPostMin);
            final NumberPicker pickerSec = (NumberPicker)view.findViewById(R.id.numberPickerCommentPostSec);
            final NumberPicker pickerDecimalSec = (NumberPicker)view.findViewById(R.id.numberPickerCommentPostDecimalSec);
            pickerMin.setMinValue(0);
            pickerSec.setMinValue(0);
            pickerDecimalSec.setMinValue(0);
            pickerSec.setMaxValue(59);
            pickerDecimalSec.setMaxValue(99);
            final int length = commentPost.getTargetVideo().getLength();
            pickerMin.setMaxValue(length / 60);
            pickerSec.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    int min = pickerMin.getValue();
                    int decimalSec = pickerDecimalSec.getValue();
                    if (min * 60 + newVal >= length) {
                        picker.setValue(oldVal);
                    }
                    int sec = picker.getValue();
                    textTime.setText(String.format("%02d:%02d.%02d", min, sec, decimalSec));
                    commentPost.setTime(6000 * min + 100 * sec + decimalSec);
                }
            });
            pickerMin.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    int sec = pickerSec.getValue();
                    int decimalSec = pickerDecimalSec.getValue();
                    textTime.setText(String.format("%02d:%02d.%02d", newVal, sec, decimalSec));
                    commentPost.setTime(6000 * newVal + 100 * sec + decimalSec);
                }
            });
            pickerDecimalSec.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    int min = pickerMin.getValue();
                    int sec = pickerSec.getValue();
                    textTime.setText(String.format("%02d:%02d.%02d", min, sec, newVal));
                    commentPost.setTime(6000 * min + 100 * sec + newVal);
                }
            });
            final Map<String, Integer> colorMap = commentPost.getColorMap();
            spinnerColor.setAdapter(new ArrayAdapter(context, android.R.layout.simple_spinner_item) {
                {
                    for (String name : colorMap.keySet()) {
                        add(name);
                    }
                }
            });
            textComment.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    commentPost.setComment(s.toString());
                }
            });
            spinnerColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String name = (String) parent.getSelectedItem();
                    int color = colorMap.get(name);
                    spinnerBack.setBackgroundColor(color);
                    commentPost.setColor(color);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            spinnerBack.setBackgroundColor(NicoCommentPost.COLOR_WHITE);
            pickerMin.setValue(0);
            pickerSec.setValue(0);
            pickerDecimalSec.setValue(0);
        }
    }

    private void postComment(final NicoCommentPost commentPost){
        new AsyncTask<Void, Void, String>() {
            private ProgressDialog progress;
            private final String SUCCESS = "success";
            private CommentInfo.CommentGroup group;

            @Override
            protected void onPreExecute() {
                progress = new ProgressDialog(CustomListActivity.this);
                String comment = commentPost.getComment();
                int time = commentPost.getStartTime();
                int min = time / 6000;
                float sec = (time % 6000) / 100f;
                progress.setMessage(String.format("Posting comment :\n \"%s\" at %02d:%04.2f", comment, min, sec));
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                try {
                    commentPost.post();
                    return SUCCESS;
                } catch (NicoAPIException e) {
                    return e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String response) {
                progress.cancel();
                progress = null;
                if (response.equals(SUCCESS)) {
                    showComments(commentPost.getTargetVideo(),100);
                    /*
                    AlertDialog.Builder builder = new AlertDialog.Builder(CustomListActivity.this);
                    builder.setTitle("Video Comments");
                    Context context = CustomListActivity.this;
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    ViewGroup root = (ViewGroup) findViewById(R.id.dialogCommentRoot);
                    View view = inflater.inflate(R.layout.dialog_comment, root, true);
                    ((ListView)view.findViewById(R.id.listViewComment)).setAdapter(new CommentAdapter(CustomListActivity.this,list));
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            if ( dialog != null ){
                                dialog.show();
                            }
                        }
                    });
                    builder.setView(view);
                    AlertDialog commentDialog = builder.create();
                    commentDialog.setCanceledOnTouchOutside(false);
                    commentDialog.show();*/
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
            final VideoInfo item = (VideoInfo)this.getItem(position);
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
                    new AsyncTask<Void, Void, String> (){
                        @Override
                        protected void onPreExecute() {
                            thumbnail.setImageDrawable(resources.getDrawable(R.drawable.temp_thumbnail));
                        }
                        @Override
                        protected String doInBackground(Void... params) {
                            try {
                                item.loadThumbnail();
                                return null;
                            }catch (NicoAPIException e){
                                return e.getMessage();
                            }
                        }
                        @Override
                        protected void onPostExecute(String response) {
                            showMessage(response);
                            try{
                                if ( viewMap.containsValue(item.getID()) ){
                                    thumbnail.setImageBitmap(item.getThumbnail());
                                }
                            }catch (NicoAPIException e){}
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
