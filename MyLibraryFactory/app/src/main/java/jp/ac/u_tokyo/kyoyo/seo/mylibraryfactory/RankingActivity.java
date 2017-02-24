package jp.ac.u_tokyo.kyoyo.seo.mylibraryfactory;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;
import java.util.Map;

import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.NicoAPIException;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.NicoRanking;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.VideoInfo;

/**
 * Created by Seo-4d696b75 on 2017/02/04.
 */

public class RankingActivity extends CustomListActivity {

    private NicoRanking nicoRanking;
    private Map<String,String> genreMap;
    private Map<String,String> kindMap;
    private Map<String,String> periodMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setTitle("Ranking");

        nicoRanking = nicoClient.getNicoRanking();
        genreMap = nicoRanking.getGenreMap();
        kindMap = nicoRanking.getKindMap();
        periodMap = nicoRanking.getPeriodMap();

        rankingDialog();

        buttonGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nicoRanking = nicoClient.getNicoRanking();
                rankingDialog();
            }
        });

    }

    private void rankingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(RankingActivity.this);
        builder.setTitle("Ranking Setting");
        Context context = RankingActivity.this;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup root = (ViewGroup)findViewById(R.id.dialogRankingRoot);
        View view = inflater.inflate(R.layout.dialog_ranking, root, true);
        Spinner spinnerGenre = (Spinner)view.findViewById(R.id.spinnerRankingGenre);
        Spinner spinnerKind = (Spinner)view.findViewById(R.id.spinnerRankingKind);
        Spinner spinnerPeriod = (Spinner)view.findViewById(R.id.spinnerRankingPeriod);
        spinnerGenre.setAdapter(new ArrayAdapter(RankingActivity.this, android.R.layout.simple_spinner_item){
            {
                for ( String name : genreMap.keySet() ){
                    add(name);
                }
            }
        });
        spinnerKind.setAdapter(new ArrayAdapter(RankingActivity.this, android.R.layout.simple_spinner_item){
            {
                for ( String name : kindMap.keySet() ){
                    add(name);
                }
            }
        });
        spinnerPeriod.setAdapter(new ArrayAdapter(RankingActivity.this, android.R.layout.simple_spinner_item){
            {
                for ( String name : periodMap.keySet() ){
                    add(name);
                }
            }
        });
        spinnerGenre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String name = (String) parent.getSelectedItem();
                String value = genreMap.get(name);
                nicoRanking.setGenre(value);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        spinnerKind.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String name = (String) parent.getSelectedItem();
                String value = kindMap.get(name);
                nicoRanking.setKind(value);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        spinnerPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String name = (String) parent.getSelectedItem();
                String value = periodMap.get(name);
                nicoRanking.setPeriod(value);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        builder.setView(view);
        builder.setMessage("You set ranking params");
        builder.setPositiveButton("GET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new AsyncTask<String, Void, String> (){
                    private ProgressDialog progress = null;
                    private List<VideoInfo> list;
                    @Override
                    protected void onPreExecute() {
                        progress = new ProgressDialog(RankingActivity.this);
                        progress.setMessage("Getting ranking...");
                        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progress.show();
                    }
                    @Override
                    protected String doInBackground(String... params) {
                        try {
                            list = nicoRanking.get();
                            return null;
                        }catch (NicoAPIException e){
                            return e.getMessage();
                        }
                    }
                    @Override
                    protected void onPostExecute(String response) {
                        setVideos(list);
                        showMessage(response);
                        progress.cancel();
                        String genre = nicoRanking.getGenre();
                        String kind = nicoRanking.getKind();
                        String period = nicoRanking.getPeriod();
                        textViewMes.setText(String.format("genre:%s kind:%s \n period:%s",genre,kind,period));
                    }
                }.execute();
            }
        });
        builder.create();
        builder.show();
    }

}
