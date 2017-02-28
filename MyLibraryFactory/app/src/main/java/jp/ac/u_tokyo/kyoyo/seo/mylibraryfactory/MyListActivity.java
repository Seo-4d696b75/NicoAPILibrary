package jp.ac.u_tokyo.kyoyo.seo.mylibraryfactory;

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

import java.util.List;

import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.MyListGroup;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.MyListVideoGroup;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.MyListVideoInfo;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.NicoAPIException;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.VideoInfo;

/**
 * Created by Seo-4d696b75 on 2017/02/07.
 */

public class MyListActivity extends CustomListActivity {

    private MyListGroup myListGroup;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setTitle("MyList");

        myListDialog();

        buttonGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myListDialog();
            }
        });

    }

    private void myListDialog(){
        if ( dialog != null ){
            dialog.cancel();
            dialog = null;
        }
        new AsyncTask<String, Void, String>() {
            @Override
            protected void onPreExecute() {
                textViewMes.setText("Your My List Group");
            }
            @Override
            protected String doInBackground(String... params) {
                try {
                    myListGroup = null;
                    myListGroup = nicoClient.getMyListGroup();
                    return null;
                } catch (NicoAPIException e) {
                    return e.getMessage();
                }
            }
            @Override
            protected void onPostExecute(String response) {
                showMessage(response);
                if ( myListGroup != null ){
                    List<MyListVideoGroup> videoGroup = myListGroup.getMyListVideoGroup();
                    AlertDialog.Builder builder = new AlertDialog.Builder(MyListActivity.this);
                    builder.setTitle("My List Selecting");
                    Context context = MyListActivity.this;
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    ViewGroup root = (ViewGroup)findViewById(R.id.dialogMyListRoot);
                    View view = inflater.inflate(R.layout.dialog_my_list, root, true);
                    builder.setView(view);
                    ListView listView = (ListView)view.findViewById(R.id.listViewMyList);
                    listView.setAdapter(new MyListAdapter(MyListActivity.this, videoGroup));
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            ArrayAdapter adapter = (ArrayAdapter) parent.getAdapter();
                            MyListVideoGroup item = (MyListVideoGroup) adapter.getItem(position);
                            showMyList(item);
                        }
                    });
                    dialog = builder.create();
                    dialog.show();
                }
            }
        }.execute();
    }

    private class MyListAdapter extends ArrayAdapter<MyListVideoGroup>{
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

    private void showMyList(final MyListVideoGroup target) {
            final int myListID = target.getMyListID();
            new AsyncTask<String, Void, String>() {
                private List<MyListVideoInfo> list;
                private ProgressDialog progress;

                @Override
                protected void onPreExecute() {
                    textViewMes.setText("Your My List : " + myListID);
                    if (dialog != null) {
                        dialog.cancel();
                        dialog = null;
                    }
                    progress = new ProgressDialog(MyListActivity.this);
                    progress.setMessage("Getting my list...");
                    progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progress.show();
                }

                @Override
                protected String doInBackground(String... params) {
                    try {
                        list = nicoClient.getMyList(myListID).getVideos();
                        //list = target.getVideos();
                        return null;
                    } catch (NicoAPIException e) {
                        return e.getMessage();
                    }
                }

                @Override
                protected void onPostExecute(String response) {
                    progress.cancel();
                    progress = null;
                    showMessage(response);
                    setVideos(list);
                }
            }.execute();

    }
}
