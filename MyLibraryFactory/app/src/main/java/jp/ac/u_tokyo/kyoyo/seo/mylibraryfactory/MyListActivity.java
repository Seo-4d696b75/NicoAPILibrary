package jp.ac.u_tokyo.kyoyo.seo.mylibraryfactory;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.MyListGroup;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.MyListVideoGroup;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.MyListVideoInfo;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.NicoAPIException;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.VideoInfo;

/**
 * Created by Seo-4d696b75 on 2017/02/07.
 */

public class MyListActivity extends CustomListActivity implements CustomDialog.OnItemClickListener {

    private final String DIALOG_TAG_MYLIST = "dialogMyList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setTitle("MyList");

        buttonGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMyListPikerDialog(DIALOG_TAG_MYLIST,null);
            }
        });
        textViewMes.setText("Your My List Group");

        showMyListPikerDialog(DIALOG_TAG_MYLIST,null);
    }


    public static class MyListPickerDialog extends CustomDialog {

        public static final String GROUP_LIST = "groups";
        private OnItemClickListener listener;

        public static MyListPickerDialog getInstance (){
            return new MyListPickerDialog();
        }

        @Override
        protected void onCreateContentView (View view){
            if ( args.containsKey(GROUP_LIST)) {
                ListView listView = (ListView) view.findViewById(R.id.listViewMyList);
                MyListGroup group = args.getParcelable(GROUP_LIST);
                listView.setAdapter(new MyListAdapter(context,group.getMyListVideoGroup()));
                if ( context instanceof OnItemClickListener ){
                    listener = (OnItemClickListener) context;
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
        }


        private class MyListAdapter extends ArrayAdapter<MyListVideoGroup> {
            private LayoutInflater inflater;
            protected MyListAdapter(Context context, List<MyListVideoGroup> list){
                super(context,0,list);
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            @Override
            public View getView(final int position, View convertView, final ViewGroup parent) {
                View view = convertView;
                MyListVideoGroup item = this.getItem(position);
                if ( view == null ){
                    view = inflater.inflate(android.R.layout.simple_list_item_1,null);
                }
                if ( item != null ){
                    TextView text = (TextView)view.findViewById(android.R.id.text1);
                    text.setText(item.getName());
                }
                return view;
            }

        }

        @Override
        public void onDestroy(){
            super.onDestroy();
            this.listener = null;
        }

    }

    @Override
    public void onItemClick(String tag, Object item, Object param){
        super.onItemClick(tag,item,param);
        if ( tag.equals(DIALOG_TAG_MYLIST) && item instanceof MyListVideoGroup) {
            showMyList((MyListVideoGroup)item);
        }
    }


    private void showMyList(final MyListVideoGroup target) {
            final int myListID = target.getMyListID();
            new AsyncTask<Void, Void, NicoAPIException>() {
                private List<MyListVideoInfo> list;
                private ProgressDialog progress;
                @Override
                protected void onPreExecute() {
                    textViewMes.setText(
                            String.format(
                                    "MyList \"%s\" (ID:%d)\ndescription:%s\npublic:%b\ncreateDate:%s\nupdateDate:%s",
                                    target.getName(),
                                    target.getMyListID(),
                                    target.getDescription(),
                                    target.isPublic(),
                                    target.getCreateDate(),
                                    target.getUpdateDate()
                            )
                    );
                    progress = new ProgressDialog(MyListActivity.this);
                    progress.setMessage("Getting my list...");
                    progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progress.show();
                }

                @Override
                protected NicoAPIException doInBackground(Void... params) {
                    try {
                        list = null;
                        //list = nicoClient.getMyList(myListID).getVideos();
                        target.loadVideos();
                        list = target.getVideos();
                        return null;
                    } catch (NicoAPIException e) {
                        return e;
                    }
                }

                @Override
                protected void onPostExecute(NicoAPIException e) {
                    progress.cancel();
                    progress = null;
                    if ( e == null ){
                        setVideos(list);
                    }else {
                        showMessage(e);
                    }
                }
            }.execute();

    }
}
