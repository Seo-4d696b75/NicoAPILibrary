package jp.ac.u_tokyo.kyoyo.seo.mylibraryfactory;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;

import java.util.List;

import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.MyListVideoGroup;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.MyListVideoInfo;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.NicoAPIException;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.TempMyListVideoGroup;

/**
 * Created by Seo-4d696b75 on 2017/02/06.
 */

public class TempMyListActivity extends CustomListActivity {

    private TempMyListVideoGroup group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setTitle("TempMyList");

        buttonGet.setEnabled(false);
        textViewMes.setText("Your temp my list");

        new AsyncTask<String, Void, String>() {
            private ProgressDialog progress;
            private List<MyListVideoInfo> list;
            @Override
            protected void onPreExecute() {
                progress = new ProgressDialog(TempMyListActivity.this);
                progress.setMessage("Getting temp my list...");
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.show();
            }
            @Override
            protected String doInBackground(String... params) {
                try {
                    group = nicoClient.getTempMyList();
                    list = group.getVideos();
                    return null;
                } catch (NicoAPIException e) {
                    return e.getMessage();
                }
            }
            @Override
            protected void onPostExecute(String response) {
                if ( response == null ) {
                    progress.cancel();
                    progress = null;
                    showMessage(response);
                    setVideos(list);
                }else{
                    showMessage(response);
                }
            }
        }.execute();

    }

}
