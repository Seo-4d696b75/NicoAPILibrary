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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setTitle("TempMyList");

        buttonGet.setEnabled(false);
        textViewMes.setText("Your temp my list");

        new AsyncTask<Void, Void, NicoAPIException>() {
            private ProgressDialog progress;
            @Override
            protected void onPreExecute() {
                progress = new ProgressDialog(TempMyListActivity.this);
                progress.setMessage("Getting temp my list...");
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.show();
            }
            @Override
            protected NicoAPIException doInBackground(Void... params) {
                try {
                    if ( tempMyListVideoGroup == null ) {
                        tempMyListVideoGroup = nicoClient.getTempMyList();
                    }
                    return null;
                } catch (NicoAPIException e) {
                    return e;
                }
            }
            @Override
            protected void onPostExecute(NicoAPIException e) {
                if ( e == null ) {
                    progress.cancel();
                    progress = null;
                    setVideos(tempMyListVideoGroup.getVideos());
                }else{
                    showMessage(e);
                }
            }
        }.execute();

    }

}
