package jp.ac.u_tokyo.kyoyo.seo.mylibraryfactory;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import java.util.List;

import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.NicoAPIException;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.NicoSearch;
import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.VideoInfo;

/**
 * Created by Seo-4d696b75 on 2017/02/09.
 */

public class SearchActivity extends CustomListActivity implements CustomDialog.onClickListener{

    private NicoSearch nicoSearch;
    private AlertDialog dialog;
    private final String DIALOG_TAG_SEARCH = "dialogSearch";

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

    public static class SearchDialog extends CustomDialog{
        public static SearchDialog getInstance(){
            return new SearchDialog();
        }
        @Override
        protected void onCreateContentView(View view){
            if ( param != null && param instanceof NicoSearch){
                final NicoSearch nicoSearch = (NicoSearch)param;
                EditText editTextQuery = (EditText)view.findViewById(R.id.editTextSearchQuery);
                CheckBox checkBoxTag = (CheckBox)view.findViewById(R.id.checkBoxSearchTag);
                editTextQuery.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override
                    public void afterTextChanged(Editable s) {
                        nicoSearch.setKeyword(s.toString());
                    }
                });
                checkBoxTag.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        nicoSearch.setTagsSearch(isChecked);
                    }
                });
            }
        }
    }

    @Override
    public void onDialogButtonClicked(String tag, Dialog dialog, int which, Object param){
        super.onDialogButtonClicked(tag,dialog,which,param);
        if ( tag.equals(DIALOG_TAG_SEARCH) && which == DialogInterface.BUTTON_POSITIVE ){
            if ( param != null && param instanceof NicoSearch ){
                final NicoSearch nicoSearch = (NicoSearch)param;
                new AsyncTask<Void, Void, NicoAPIException>(){
                    private ProgressDialog progress = null;
                    private NicoSearch.SearchVideoGroup group;
                    @Override
                    protected void onPreExecute() {
                        progress = new ProgressDialog(SearchActivity.this);
                        progress.setMessage("Getting videos...");
                        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progress.show();
                    }
                    @Override
                    protected NicoAPIException doInBackground(Void... params) {
                        try {
                            group = nicoSearch.search();
                            return null;
                        }catch (NicoAPIException e){
                            return e;
                        }
                    }
                    @Override
                    protected void onPostExecute(NicoAPIException e) {
                        progress.cancel();
                        progress = null;
                        if ( e == null ) {
                            setVideos(group.getVideos());
                            textViewMes.setText("query : " + group.getQuery());
                        }else{
                            showMessage(e);
                        }
                    }
                }.execute();
            }
        }
    }

    private void showSearchDialog(){
        Bundle args = new Bundle();
        args.putInt(CustomDialog.LAYOUT, R.layout.dialog_search);
        args.putString(CustomDialog.TITLE,"Search Setting");
        args.putString(CustomDialog.BUTTON_POSITIVE,"Search");
        args.putString(CustomDialog.BUTTON_NEUTRAL,"Cancel");
        args.putParcelable(CustomDialog.PARAM, nicoClient.getNicoSearch());
        CustomDialog dialog = SearchDialog.getInstance();
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(),DIALOG_TAG_SEARCH);
    }

}
