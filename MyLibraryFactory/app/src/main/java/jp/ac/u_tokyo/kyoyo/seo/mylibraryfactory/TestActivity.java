package jp.ac.u_tokyo.kyoyo.seo.mylibraryfactory;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.*;

public class TestActivity extends AppCompatActivity implements CustomDialog.onClickListener{

    private String appName = "NicoAPIDemoApp";

    private TextView textViewLoginStatus;
    private TextView textViewUserName;
    private ImageView imageViewUserIcon;
    private Button buttonRanking;
    private Button buttonSearch;
    private Button buttonMyList;
    private Button buttonTempMyList;

    private final String DIALOG_TAG_LOGIN = "loginDialog";

    private NicoClient nicoClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        setTitle("DemoApp");

        Intent intent = getIntent();
        if ( intent != null ){
            Bundle bundle = intent.getExtras();
            if ( bundle != null ) {
                nicoClient = (NicoClient)bundle.getParcelable(NicoClient.INTENT);
            }
        }

        textViewLoginStatus = (TextView)findViewById(R.id.textViewLoginStatus);
        textViewUserName = (TextView)findViewById(R.id.textViewUserName);
        imageViewUserIcon = (ImageView)findViewById(R.id.imageViewUser);
        buttonRanking = (Button)findViewById(R.id.buttonRanking);
        buttonSearch = (Button)findViewById(R.id.buttonSearch);
        buttonMyList = (Button)findViewById(R.id.buttonMyList);
        buttonTempMyList = (Button)findViewById(R.id.buttonTempMyList);

        nicoClient = new NicoClient(appName, Build.DEVICE);
        //loginDialogShow();

        textViewLoginStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginDialogShow();
            }
        });

        buttonRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TestActivity.this, RankingActivity.class);
                intent.putExtra(NicoClient.INTENT,nicoClient);
                startActivity(intent);
            }
        });
        buttonTempMyList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( nicoClient.isLogin() ) {
                    Intent intent = new Intent(TestActivity.this, TempMyListActivity.class);
                    intent.putExtra(NicoClient.INTENT, nicoClient);
                    startActivity(intent);
                }else{
                    showMessage("You have to login");
                }
            }
        });
        buttonMyList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( nicoClient.isLogin() ) {
                    Intent intent = new Intent(TestActivity.this, MyListActivity.class);
                    intent.putExtra(NicoClient.INTENT, nicoClient);
                    startActivity(intent);
                }else{
                    showMessage("You have to login");
                }
            }
        });
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TestActivity.this, SearchActivity.class);
                intent.putExtra(NicoClient.INTENT,nicoClient);
                startActivity(intent);
            }
        });

        if ( !nicoClient.isLogin()) {
            login("sendakaoru509@gmail.com", "testaccount");
        }
    }

    private void showMessage(String message){
        Log.d("DemoApp",message);
        Toast.makeText(TestActivity.this,message,Toast.LENGTH_SHORT).show();
    }

    private void loginDialogShow(){
        Bundle args = new Bundle();
        args.putInt(CustomDialog.LAYOUT, R.layout.dialog_account);
        args.putString(CustomDialog.TITLE,"Your Account");
        args.putString(CustomDialog.MESSAGE,"Do you want to Login in NicoNico? You don't always have to, but some function needs login.");
        args.putString(CustomDialog.BUTTON_POSITIVE,"Login");
        args.putString(CustomDialog.BUTTON_NEUTRAL,"Cancel");
        CustomDialog dialog = CustomDialog.getInstance();
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(),DIALOG_TAG_LOGIN);
    }

    @Override
    public void onDialogButtonClicked(String tag, Dialog dialog, int which, Object param){
        if ( tag.equals(DIALOG_TAG_LOGIN)){
            switch ( which ){
                case DialogInterface.BUTTON_POSITIVE:
                    EditText editMail = (EditText) dialog.findViewById(R.id.editTextMail);
                    EditText editPass = (EditText) dialog.findViewById(R.id.editTextPass);
                    login(editMail.getText().toString(),editPass.getText().toString());
                    break;
                case DialogInterface.BUTTON_NEUTRAL:
                    showMessage("Not login");
                    break;
                default:
            }
        }
    }


    private void login (final String mail, final String pass){
        new AsyncTask<String, Void, String>() {
            private ProgressDialog progress;
            private Bitmap userIcon;
            @Override
            protected void onPreExecute() {
                progress = new ProgressDialog(TestActivity.this);
                progress.setMessage("Try to login...");
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.show();
            }
            @Override
            protected String doInBackground(String... params) {
                try {
                    nicoClient.login(mail, pass);
                    userIcon = nicoClient.getUserIcon();
                    return "success";
                }catch (NicoAPIException e){
                    return e.getMessage();
                }
            }
            @Override
            protected void onPostExecute(String response) {
                progress.cancel();
                progress = null;
                String message = "";
                if ( nicoClient.isLogin() ){
                    message = "succeed in login";
                    textViewLoginStatus.setText("Login");
                    try {
                        message += ( "\nUserName : " + nicoClient.getUserName());
                        message += ( "\nUserID   : " + nicoClient.getUserID());
                        textViewUserName.setText(nicoClient.getUserName());
                    }catch (NicoAPIException e){
                        e.printStackTrace();
                    }
                    if ( userIcon != null ){
                        imageViewUserIcon.setImageBitmap(userIcon);
                    }
                }else{
                    message = "fail to login \n" + response;
                }
                showMessage(message);
            }
        }.execute();
    }
}
