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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.NicoAPIException;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.VideoInfo;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.VideoInfoManager;

/**
 * Created by Seo-4d696b75 on 2017/02/07.
 */

public class MyListActivity extends CustomListActivity {

    private Map<String,String> myListMap;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setTitle("Ranking");

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
                    myListMap = null;
                    myListMap = nicoClient.getMyListGroup();
                    return null;
                } catch (NicoAPIException e) {
                    return e.getMessage();
                }
            }
            @Override
            protected void onPostExecute(String response) {
                showMessage(response);
                if ( myListMap != null ){
                    List<String> list = new ArrayList<String>(){
                        {
                            for ( String name : myListMap.keySet() ){
                                add(name);
                            }
                        }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(MyListActivity.this);
                    builder.setTitle("My List Selecting");
                    Context context = MyListActivity.this;
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    ViewGroup root = (ViewGroup)findViewById(R.id.dialogMyListRoot);
                    View view = inflater.inflate(R.layout.dialog_my_list, root, true);
                    builder.setView(view);
                    ListView listView = (ListView)view.findViewById(R.id.listViewMyList);
                    listView.setAdapter(new ArrayAdapter<String>(MyListActivity.this,android.R.layout.simple_list_item_1,list));
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                        @Override
                                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                            ArrayAdapter adapter = (ArrayAdapter) parent.getAdapter();
                                                            String name = (String)adapter.getItem(position);
                                                            showMyList(name);
                                                        }
                                                    });
                    dialog = builder.create();
                    dialog.show();
                }
            }
        }.execute();
    }

    private void showMyList(String name){
        if ( myListMap.containsKey(name) ) {
            final String myListID = myListMap.get(name);
            new AsyncTask<String, Void, String>() {
                private List<VideoInfo> list;
                private ProgressDialog progress;
                @Override
                protected void onPreExecute() {
                    textViewMes.setText("Your My List : " + myListID);
                    if ( dialog != null ){
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
                        list = nicoClient.getMyList(myListID);
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
}
