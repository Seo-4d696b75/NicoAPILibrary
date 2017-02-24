package jp.ac.u_tokyo.kyoyo.seo.mylibraryfactory;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.*;

public class TestActivity extends AppCompatActivity {

    private String appName = "NicoAPIDemoApp";

    private TextView textViewLoginStatus;
    private TextView textViewUserName;
    private ImageView imageViewUserIcon;
    private Button buttonRanking;
    private Button buttonSearch;
    private Button buttonMyList;
    private Button buttonTempMyList;

    private NicoClient nicoClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        setTitle("DemoApp");

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
        login("sendakaoru509@gmail.com","testaccount");
    }

    private void showMessage(String message){
        Log.d("DemoApp",message);
        Toast.makeText(TestActivity.this,message,Toast.LENGTH_SHORT).show();
    }

    private void loginDialogShow(){
        AlertDialog.Builder builder = new AlertDialog.Builder(TestActivity.this);
        builder.setTitle("Your Account");
        Context context = TestActivity.this;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup root = (ViewGroup)findViewById(R.id.dialogAccountRoot);
        final View view = inflater.inflate(R.layout.dialog_account, root, true);
        builder.setView(view);
        builder.setMessage("Do you want to Login in NicoNico? You don't always have to, but some function needs login.");
        builder.setPositiveButton("Login", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText editMail = (EditText) view.findViewById(R.id.editTextMail);
                EditText editPass = (EditText) view.findViewById(R.id.editTextPass);
                login(editMail.getText().toString(),editPass.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showMessage("Not login");
            }
        });
        builder.create();
        builder.show();
    }

    private void login (final String mail, final String pass){
        new AsyncTask<String, Void, String>() {
            private ProgressDialog progress;
            private Drawable userIcon;
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
                        imageViewUserIcon.setImageDrawable(userIcon);
                    }
                }else{
                    message = "fail to login \n" + response;
                }
                showMessage(message);
            }
        }.execute();
    }
}
