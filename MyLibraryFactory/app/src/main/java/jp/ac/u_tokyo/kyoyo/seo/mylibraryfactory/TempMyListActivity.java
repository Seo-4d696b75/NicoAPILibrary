package jp.ac.u_tokyo.kyoyo.seo.mylibraryfactory;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import java.util.List;

import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.NicoAPIException;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.TempMyListVideoInfo;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.VideoInfo;

/**
 * Created by Seo-4d696b75 on 2017/02/06.
 */

public class TempMyListActivity extends CustomListActivity {

    List<VideoInfo> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setTitle("TempMyList");

        buttonGet.setEnabled(false);
        textViewMes.setText("Your temp my list");

        new AsyncTask<String, Void, String>() {
            private ProgressDialog progress;
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
                    list = nicoClient.getTempMyList();
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
