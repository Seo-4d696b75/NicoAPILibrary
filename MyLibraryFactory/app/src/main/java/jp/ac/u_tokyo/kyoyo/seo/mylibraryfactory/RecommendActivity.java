package jp.ac.u_tokyo.kyoyo.seo.mylibraryfactory;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import java.util.List;

import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.NicoAPIException;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.VideoInfo;

/**
 * Created by Seo-4d696b75 on 2017/02/06.
 */

public class RecommendActivity extends CustomListActivity {

    private VideoInfo target;
    private List<VideoInfo> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setTitle("Recommend");
        buttonGet.setEnabled(false);

        Intent intent = getIntent();
        if ( intent != null ){
            target = intent.getParcelableExtra(VideoInfo.VIDEO_KEY);
        }
        if ( target == null ){
            showMessage("fail to get target video");
            finish();
        }

        new AsyncTask<String, Void, String>() {
            @Override
            protected void onPreExecute() {}
            @Override
            protected String doInBackground(String... params) {
                try {
                    list = nicoClient.getRecommend(target);
                    return null;
                } catch (NicoAPIException e) {
                    return e.getMessage();
                }
            }
            @Override
            protected void onPostExecute(String response) {
                showMessage(response);
                setVideos(list);
                textViewMes.setText("recommended videos from;\n" + target.getTitle());
            }
        }.execute();
    }
}
