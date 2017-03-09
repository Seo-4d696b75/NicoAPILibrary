package jp.ac.u_tokyo.kyoyo.seo.mylibraryfactory;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.Map;

import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.NicoAPIException;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.NicoRanking;

/**
 * Created by Seo-4d696b75 on 2017/02/04.
 */

public class RankingActivity extends CustomListActivity implements CustomDialog.onClickListener{

    private final String DIALOG_TAG_RANKING = "dialogRanking";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setTitle("Ranking");


        rankingDialog();

        buttonGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rankingDialog();
            }
        });

    }

    public static class RankingSearchDialog extends CustomDialog{
        private Map<String,String> genreMap;
        private Map<String,String> kindMap;
        private Map<String,String> periodMap;
        public static RankingSearchDialog getInstance(){
            return new RankingSearchDialog();
        }
        @Override
        protected void onCreateContentView (View view){
            if ( param != null && param instanceof NicoRanking){
                final NicoRanking nicoRanking = (NicoRanking)param;
                genreMap = nicoRanking.getGenreMap();
                kindMap = nicoRanking.getKindMap();
                periodMap = nicoRanking.getPeriodMap();
                Spinner spinnerGenre = (Spinner)view.findViewById(R.id.spinnerRankingGenre);
                Spinner spinnerKind = (Spinner)view.findViewById(R.id.spinnerRankingKind);
                Spinner spinnerPeriod = (Spinner)view.findViewById(R.id.spinnerRankingPeriod);
                spinnerGenre.setAdapter(new ArrayAdapter(context, android.R.layout.simple_spinner_item){
                    {
                        for ( String name : genreMap.keySet() ){
                            add(name);
                        }
                    }
                });
                spinnerKind.setAdapter(new ArrayAdapter(context, android.R.layout.simple_spinner_item){
                    {
                        for ( String name : kindMap.keySet() ){
                            add(name);
                        }
                    }
                });
                spinnerPeriod.setAdapter(new ArrayAdapter(context, android.R.layout.simple_spinner_item){
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
            }
        }
    }

    @Override
    public void onDialogButtonClicked(String tag, Dialog dialog, int which, Object param){
        super.onDialogButtonClicked(tag,dialog,which,param);
        if ( tag.equals(DIALOG_TAG_RANKING)){
            switch ( which ){
                case DialogInterface.BUTTON_POSITIVE:
                    if ( param != null && param instanceof NicoRanking) {
                        final NicoRanking nicoRanking = (NicoRanking)param;
                        new AsyncTask<Void, Void, NicoAPIException>() {
                            private ProgressDialog progress = null;
                            private NicoRanking.RankingVideoGroup group;

                            @Override
                            protected void onPreExecute() {
                                progress = new ProgressDialog(RankingActivity.this);
                                progress.setMessage("Getting ranking...");
                                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                progress.show();
                            }

                            @Override
                            protected NicoAPIException doInBackground(Void... params) {
                                try {
                                    group = nicoRanking.get();
                                    return null;
                                } catch (NicoAPIException e) {
                                    return e;
                                }
                            }

                            @Override
                            protected void onPostExecute(NicoAPIException e) {
                                if ( e == null ) {
                                    setVideos(group.getVideoList());
                                    progress.cancel();
                                    String genre = group.getGenre();
                                    String kind = group.getKind();
                                    String period = group.getPeriod();
                                    textViewMes.setText(String.format("genre:%s kind:%s \n period:%s", genre, kind, period));
                                }else{
                                    showMessage(e);
                                }
                            }
                        }.execute();
                    }
                    break;
                default:
            }
        }

    }

    private void rankingDialog(){
        Bundle args = new Bundle();
        args.putInt(CustomDialog.LAYOUT, R.layout.dialog_ranking);
        args.putString(CustomDialog.TITLE,"Ranking Setting");
        args.putString(CustomDialog.MESSAGE,"You set ranking params");
        args.putString(CustomDialog.BUTTON_POSITIVE,"Get");
        args.putString(CustomDialog.BUTTON_NEUTRAL,"Cancel");
        args.putParcelable(CustomDialog.PARAM,nicoClient.getNicoRanking());
        CustomDialog dialog = RankingSearchDialog.getInstance();
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(),DIALOG_TAG_RANKING);
    }

}
