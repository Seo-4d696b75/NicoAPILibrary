package jp.ac.u_tokyo.kyoyo.seo.mylibraryfactory;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.List;

import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.NicoAPIException;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.NicoSearch;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.VideoInfo;

/**
 * Created by Seo-4d696b75 on 2017/02/09.
 */

public class SearchActivity extends CustomListActivity {

    private NicoSearch nicoSearch;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setTitle("Search");

        nicoSearch = nicoClient.getNicoSearch();

        showSearchDialog();

        buttonGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearchDialog();
            }
        });
    }

    private void showSearchDialog(){
        if ( dialog != null ){
            dialog.cancel();
            dialog = null;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
        builder.setTitle("Search Setting");
        Context context = SearchActivity.this;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup root = (ViewGroup)findViewById(R.id.dialogSearchRoot);
        View view = inflater.inflate(R.layout.dialog_search, root, true);
        final EditText editTextQuery = (EditText)view.findViewById(R.id.editTextSearchQuery);
        final CheckBox checkBoxTag = (CheckBox)view.findViewById(R.id.checkBoxSearchTag);
        builder.setView(view);
        builder.setPositiveButton("GET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String query = editTextQuery.getText().toString();
                if ( query.isEmpty() ){
                    showMessage("You have to set query");
                    return;
                }
                new AsyncTask<String, Void, String>(){
                    private ProgressDialog progress = null;
                    private List<VideoInfo> list;
                    @Override
                    protected void onPreExecute() {
                        progress = new ProgressDialog(SearchActivity.this);
                        progress.setMessage("Getting videos...");
                        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progress.show();
                        String[] queryArray = query.split("[\\s]+");
                        boolean isTagSearch = checkBoxTag.isChecked();
                        for ( String target : queryArray ){
                            nicoSearch.addQuery(target);
                        }
                        nicoSearch.setTagsSearch(isTagSearch);
                    }
                    @Override
                    protected String doInBackground(String... params) {
                        try {
                            list = nicoSearch.search();
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
                        textViewMes.setText("query : " + query);
                    }
                }.execute();
            }
        });
        dialog = builder.create();
        dialog.show();
    }

}
