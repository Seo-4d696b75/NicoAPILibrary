package jp.ac.u_tokyo.kyoyo.seo.mylibraryfactory;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.CommentInfo;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.MyListGroup;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.MyListVideoGroup;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.MyListVideoInfo;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.NicoAPIException;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.NicoClient;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.NicoCommentPost;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.TempMyListVideoGroup;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.VideoInfo;

/**
 * Created by Seo-4d696b75 on 2017/02/04.
 */

public abstract class CustomListActivity extends AppCompatActivity implements CustomDialog.onClickListener, CustomDialog.OnItemClickListener {

    protected NicoClient nicoClient;
    protected MyListGroup myListGroup;
    protected TempMyListVideoGroup tempMyListVideoGroup;

    protected TextView textViewMes;
    protected Button buttonGet;
    protected ListView listViewVideos;
    protected Resources resources;


    private final String DIALOG_TAG_DETAILS = "dialogDetails";
    private final String DIALOG_TAG_MENU = "dialogMenu";
    private final String DIALOG_TAG_COMMENT = "dialogComment";
    private final String DIALOG_TAG_COMMENT_POST = "dialogCommentPost";
    private final String DIALOG_TAG_MYLIST_PICK = "dialogMyListPick";

    protected static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm";

    protected static String formatDate(Date date){
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        return format.format(date);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        textViewMes = (TextView)findViewById(R.id.textViewMes);
        buttonGet = (Button)findViewById(R.id.buttonGet);
        listViewVideos = (ListView)findViewById(R.id.listViewVideos);

        Intent intent = getIntent();
        if ( intent != null ){
            Bundle bundle = intent.getExtras();
            if ( bundle != null ) {
                nicoClient = (NicoClient) bundle.getParcelable(NicoClient.INTENT);
            }
        }
        resources = getResources();
        if ( nicoClient == null || resources == null ){
            showMessage("fail to get intent and resource");
            finish();
        }
    }

    protected void showMessage(NicoAPIException e){
        if ( e != null ){
            showMessage(String.format("%s\ncode : %d",e.getMessage(),e.getCode()));
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
        new AsyncTask<Void, Void, NicoAPIException>() {
            private ProgressDialog progress;
            @Override
            protected void onPreExecute() {
                progress = new ProgressDialog(CustomListActivity.this);
                progress.setMessage("Getting details...");
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.show();
            }
            @Override
            protected NicoAPIException doInBackground(Void... params) {
                try {
                    info.complete();
                    info.getFlv(nicoClient.getCookies());
                }catch(NicoAPIException e){
                    return e;
                }
                return null;
            }
            @Override
            protected void onPostExecute(NicoAPIException e) {
                progress.cancel();
                progress = null;
                if ( e == null ){
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
                    showMessage(e);
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
                ((TextView) view.findViewById(R.id.textViewDetailsThumbnailURL)).setText(info.getThumbnailURL());
                ((TextView) view.findViewById(R.id.textViewDetailsDate)).setText(formatDate(info.getDate()));
                List<String> tags = info.getTags();
                StringBuilder stringBuilder = new StringBuilder();
                for (String tag : tags) {
                    stringBuilder.append(tag);
                    stringBuilder.append(" ");
                }
                ((TextView) view.findViewById(R.id.textViewDetailsTags)).setText(stringBuilder.toString());
                ((TextView) view.findViewById(R.id.textViewDetailsContributorID)).setText(String.valueOf(info.getContributorID()));
                ((TextView) view.findViewById(R.id.textViewDetailsContributorName)).setText(info.getContributorName());
                ((TextView) view.findViewById(R.id.textViewDetailsContributorIconUrl)).setText(info.getContributorIconURL());
                ((TextView) view.findViewById(R.id.textViewDetailsThreadID)).setText(String.valueOf(info.getThreadID()));
                ((TextView) view.findViewById(R.id.textViewDetailsMesServer)).setText(info.getMessageServerURL());
                ((TextView) view.findViewById(R.id.textViewDetailsFlvURL)).setText(info.getFlvURL());
                if ( info instanceof MyListVideoInfo ){
                    MyListVideoInfo myListVideoInfo = (MyListVideoInfo)info;
                    ViewGroup container = (LinearLayout)view.findViewById(R.id.linearLayoutMyListDetailsContainer);
                    LayoutInflater inflater = context.getLayoutInflater();
                    View myListDetails = inflater.inflate(R.layout.video_mylist_cell,null,false);
                    ((TextView)myListDetails.findViewById(R.id.textViewDetailsMyListAdd)).setText(formatDate(myListVideoInfo.getAddDate()));
                    ((TextView)myListDetails.findViewById(R.id.textViewDetailsMyListUpdate)).setText(formatDate(myListVideoInfo.getUpdateDate()));
                    ((TextView)myListDetails.findViewById(R.id.textViewDetailsMyListDescription)).setText(myListVideoInfo.getMyListItemDescription());
                    container.addView(myListDetails);
                }
            }catch (Exception e){

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
                            showMyListPikerDialog(DIALOG_TAG_MYLIST_PICK,info);
                            break;
                        case VideoMenuDialog.MENU_TEMP_MYLIST_ADD:
                            addVideoToTemp(info);
                            break;
                    }
                }
                break;
            case DIALOG_TAG_MYLIST_PICK:
                info = (VideoInfo)param;
                MyListVideoGroup group = (MyListVideoGroup)item;
                addVideoToMyList(info,group);
                break;
            default:
        }
    }

    private void showComments(final VideoInfo info, final int max){
        new AsyncTask<Void, Void, NicoAPIException>() {
            private ProgressDialog progress;

            @Override
            protected void onPreExecute() {
                progress = new ProgressDialog(CustomListActivity.this);
                progress.setMessage("Getting comments...");
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.show();
            }

            @Override
            protected NicoAPIException doInBackground(Void... params) {
                try {
                    if ( max > 0 ) {
                        nicoClient.getComment(info, max);
                    }else{
                        nicoClient.getComment(info);
                    }
                    return null;
                } catch (NicoAPIException e) {
                    return e;
                }
            }

            @Override
            protected void onPostExecute(NicoAPIException e) {
                progress.cancel();
                progress = null;
                if (e == null) {
                    Bundle args = new Bundle();
                    args.putInt(CustomDialog.LAYOUT, R.layout.dialog_comment);
                    args.putString(CustomDialog.TITLE,"Video Comments");
                    args.putString(CustomDialog.BUTTON_NEUTRAL,"OK");
                    args.putString(CustomDialog.BUTTON_POSITIVE, "Post comment");
                    args.putParcelable(CustomDialog.PARAM,info);
                    CustomDialog dialog = CommentDialog.getInstance();
                    dialog.setArguments(args);
                    dialog.show(getSupportFragmentManager(),DIALOG_TAG_COMMENT);
                }else{
                    showMessage(e);
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
                    commentPost.setColor(name);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            spinnerBack.setBackgroundColor(colorMap.get(CommentInfo.COLOR_WHITE));
            pickerMin.setValue(0);
            pickerSec.setValue(0);
            pickerDecimalSec.setValue(0);
        }
    }

    private void postComment(final NicoCommentPost commentPost){
        new AsyncTask<Void, Void, NicoAPIException>() {
            private ProgressDialog progress;
            @Override
            protected void onPreExecute() {
                progress = new ProgressDialog(CustomListActivity.this);
                String comment = commentPost.getComment();
                int time = commentPost.getTime();
                int min = time / 6000;
                float sec = (time % 6000) / 100f;
                progress.setMessage(String.format("Posting comment :\n \"%s\" at %02d:%04.2f", comment, min, sec));
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.show();
            }

            @Override
            protected NicoAPIException doInBackground(Void... params) {
                try {
                    commentPost.post();
                    return null;
                } catch (NicoAPIException e) {
                    return e;
                }
            }

            @Override
            protected void onPostExecute(NicoAPIException e) {
                progress.cancel();
                progress = null;
                if ( e == null ) {
                    showComments(commentPost.getTargetVideo(),100);
                }else{
                    showMessage(e);
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
                TextView length = (TextView) view.findViewById(R.id.textViewLength);
                title.setText(item.getTitle());
                length.setText(item.formatLength());
                viewCount.setText("views:" + item.formatViewCounter());
                mylistCount.setText("myList:" + item.formatMyListCounter());
                new AsyncTask<Void, Void, String>() {
                    private Bitmap image;

                    @Override
                    protected void onPreExecute() {
                        thumbnail.setImageDrawable(resources.getDrawable(R.drawable.temp_thumbnail));
                    }

                    @Override
                    protected String doInBackground(Void... params) {
                        try {
                            image = item.getThumbnail();
                            return null;
                        } catch (NicoAPIException e) {
                            return e.getMessage();
                        }
                    }

                    @Override
                    protected void onPostExecute(String response) {
                        showMessage(response);
                        if (viewMap.containsValue(item.getID()) && image != null) {
                            thumbnail.setImageBitmap(image);
                        }
                    }
                }.execute();
            }
            return view;
        }
    }

    private void addVideoToTemp (final VideoInfo info){
        new AsyncTask<Void, Void, NicoAPIException>() {
            private ProgressDialog progress;
            @Override
            protected void onPreExecute() {
                progress = new ProgressDialog(CustomListActivity.this);
                progress.setMessage("Adding video...");
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.show();
            }

            @Override
            protected NicoAPIException doInBackground(Void... params) {
                try {
                    if ( tempMyListVideoGroup == null ) {
                        tempMyListVideoGroup = nicoClient.getTempMyList();
                    }
                    tempMyListVideoGroup.add(info,"DemoApp");
                    return null;
                } catch (NicoAPIException e) {
                    return e;
                }
            }

            @Override
            protected void onPostExecute(NicoAPIException e) {
                progress.cancel();
                progress = null;
                if ( e == null ) {
                    showMessage(String.format("Succeed in adding video\nVideoID : %s\nVideoTitle : %s",info.getID(),info.getTitle()));
                }else{
                    showMessage(e);
                }
            }
        }.execute();
    }

    private void addVideoToMyList (final VideoInfo info, final MyListVideoGroup group){
        new AsyncTask<Void, Void, NicoAPIException>() {
            private ProgressDialog progress;
            @Override
            protected void onPreExecute() {
                progress = new ProgressDialog(CustomListActivity.this);
                progress.setMessage("Adding video...");
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.show();
            }

            @Override
            protected NicoAPIException doInBackground(Void... params) {
                try {
                    group.add(info,"DemoApp");
                    return null;
                } catch (NicoAPIException e) {
                    return e;
                }
            }

            @Override
            protected void onPostExecute(NicoAPIException e) {
                progress.cancel();
                progress = null;
                if ( e == null ) {
                    showMessage(String.format("Succeed in adding video\nMyListName : %s\nVideoID : %s\nVideoTitle : %s",group.getName(),info.getID(),info.getTitle()));
                }else{
                    showMessage(e);
                }
            }
        }.execute();
    }

    protected void showMyListPikerDialog(final String tag, final VideoInfo info){
        new AsyncTask<Void, Void, NicoAPIException>() {
            @Override
            protected NicoAPIException doInBackground(Void... params) {
                try {
                    if ( myListGroup == null ) {
                        myListGroup = nicoClient.getMyListGroup();
                    }
                    return null;
                } catch (NicoAPIException e) {
                    return e;
                }
            }
            @Override
            protected void onPostExecute(NicoAPIException e) {
                if ( e == null ){
                    Bundle args = new Bundle();
                    args.putInt(CustomDialog.LAYOUT, R.layout.dialog_my_list);
                    args.putString(CustomDialog.TITLE,"My List Selecting");
                    args.putString(CustomDialog.BUTTON_NEUTRAL,"Cancel");
                    args.putParcelable(CustomDialog.PARAM,info);
                    args.putParcelable(MyListActivity.MyListPickerDialog.GROUP_LIST, myListGroup);
                    CustomDialog dialog = MyListActivity.MyListPickerDialog.getInstance();
                    dialog.setArguments(args);
                    dialog.show(getSupportFragmentManager(),tag);
                }else{
                    showMessage(e);
                }
            }
        }.execute();
    }

}
