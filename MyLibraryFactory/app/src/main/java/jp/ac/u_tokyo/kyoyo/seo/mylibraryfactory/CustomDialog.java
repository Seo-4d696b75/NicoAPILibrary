package jp.ac.u_tokyo.kyoyo.seo.mylibraryfactory;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.VideoInfo;

/**
 * Created by Seo-4d696b75 on 2017/03/06.
 */

public class CustomDialog extends DialogFragment {

    public static final String LAYOUT = "layout";
    public static final String TITLE = "title";
    public static final String MESSAGE = "message";
    public static final String PARAM = "param";
    public static final String BUTTON_POSITIVE = "buttonPositive";
    public static final String BUTTON_NEGATIVE = "buttonNegative";
    public static final String BUTTON_NEUTRAL = "buttonNeutral";

    public static CustomDialog getInstance (){
        return new CustomDialog();
    }

    public interface onClickListener {
        void onDialogButtonClicked(String tag, Dialog dialog, int which, Object param);
    }
    public interface OnItemClickListener{
        void onItemClick(String tag, Object item, Object param);
    }

    private onClickListener listener;
    protected Dialog dialog;
    protected Bundle args;
    protected FragmentActivity context;
    protected int layout;
    protected Object param;

    @Override
    public Dialog onCreateDialog(Bundle b){
        context = getActivity();
        args = getArguments();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if ( context instanceof onClickListener){
            listener = (onClickListener)context;
        }else{
            listener = null;
        }
        if ( args.containsKey(PARAM) ){
            param = args.getParcelable(PARAM);
        }
        if ( args.containsKey(TITLE)) {
            builder.setTitle(args.getString(TITLE));
        }
        if ( args.containsKey(MESSAGE)) {
            builder.setMessage(args.getString(MESSAGE));
        }
        if ( args.containsKey(LAYOUT)) {
            //builder.setView(args.getInt(LAYOUT));
            LayoutInflater inflater = context.getLayoutInflater();
            View view = inflater.inflate(args.getInt(LAYOUT),null,false);
            onCreateContentView(view);
            builder.setView(view);
        }
        if ( args.containsKey(BUTTON_NEUTRAL)) {
            builder.setNeutralButton(args.getString(BUTTON_NEUTRAL), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    if (listener != null) {
                        listener.onDialogButtonClicked(getTag(), dialog, which, param);
                    }
                }
            });
        }
        if ( args.containsKey(BUTTON_POSITIVE)) {
            builder.setPositiveButton(args.getString(BUTTON_POSITIVE), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    if (listener != null) {
                        listener.onDialogButtonClicked(getTag(), dialog, which, param);
                    }
                }
            });
        }
        if ( args.containsKey(BUTTON_NEGATIVE)){
            builder.setNegativeButton(args.getString(BUTTON_NEGATIVE), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    if (listener != null) {
                        listener.onDialogButtonClicked(getTag(), dialog, which, param);
                    }
                }
            });
        }
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    /*
    Override to customize the content of this alertDialog
     */
    protected void onCreateContentView (View view){}

    @Override
    public void onDestroy(){
        super.onDestroy();
        listener = null;
        dialog = null;
    }

}
