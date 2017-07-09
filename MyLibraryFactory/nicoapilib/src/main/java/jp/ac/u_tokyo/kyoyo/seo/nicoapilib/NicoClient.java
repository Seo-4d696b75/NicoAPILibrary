package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NicoAPIの中心となるクラスです<br>
 * this class the center of NicoAPI, all the functions are called from here.<br><br>
 *
 * このライブラリの全機能はここから呼ぶことができます。このクラスは{@code Parcelable}ですのでアプリ起動時に
 * 一度インスタンス化したらIntentでActivity間を渡して使いまわせます。
 *
 * @author Seo-4d696b75
 * @version 0.0 on 2017/01/21.
 */

public class NicoClient extends LoginInfo implements Parcelable{

    public static final String INTENT = "intent";

    /**
     * アプリの名前、使用するＡＰＩによってはこの値が必要<br>
     * Your application name, needed for some API.<br>
     *  動画検索に用いる『スナップショット検索API v2』 に必要です。
     */
    protected final String appName;
    /**
     * 使用するデバイスの名前 <br>
     * コメントを投稿するときに必要です。<br>
     * This value is needed when a comment is posted.
     */
    protected final String deviceName;

    /**
     * コンストラクタからインスタンスを取得します<br>
     * Gets instance of this constructor.<br>
     * 使用するアプリ名とデバイス名を渡してください。{@code null}と空文字以外の文字列が必要です。
     * @param appName your application name, cannot be {@code null}
     * @param deviceName your device name, cannot be {@code null}
     */
    public NicoClient (String appName, String deviceName){
        this.appName = appName;
        this.deviceName = deviceName;
    }

    /**
     * ニコ動にログインします<br>
     * Tries to login to NicoNico. <br>
     * ニコ動にアカウント登録された有効なメールアドレスとパスワードを渡してください。
     * {@code null}または無効な値を渡すとログインに失敗します。
     * ライブラリでは機能によってはログインが必須なので、必ず事前にログインしてください。
     * 各種ログイン情報は{@link LoginInfo 親クラス}の適当なメソッドを呼び参照できます。<br>
     *  Pass valid mail address and pass word registered in NicoNico.
     *  If invalid value or {@code null} is passed, you fail to login.
     *  In this library, some function require login session, so be sure to login.
     *  You can access various relevant information by calling methods of {@link LoginInfo super class}.
     * <strong>ＵＩスレッド禁止</strong>  HTTP通信を行うのでバックグランドで処理して下さい。<br>
     * <strong>No UI thread</strong>:  HTTP communication is done<br>
     * @param mail cannot be {@code null}
     * @param pass cannot be {@code null}
     * @throws NicoAPIException if fail to login because of invalid params
     */
    public synchronized void login (String mail, String pass) throws NicoAPIException {
        NicoLogin nicoLogin = new NicoLogin(this);
        nicoLogin.login(mail, pass);
    }

    /**
     * ランキング検索に必要な{@link NicoRanking}のインスタンスを取得します<br>
     * Gets instance of {@link NicoRanking}, in order to get ranking from Nico.<br>
     * ニコ動のランキング検索を行うには、このメソッドから{@link NicoRanking}オブジェクトを取得して
     * このオブジェクトに対して各種検索パラメータをセットしてから{@link NicoRanking#getRanking()} ()}を呼びます。
     * <strong>注意　</strong>動画のランキング検索にはログインが要りませんが、非ログインで状態で取得した動画への操作には制約が生じるので
     * ログイン状態でランキング検索するよう推奨されます。<br>
     * <strong>Warning </strong>Getting video ranking does not require login, but there is some limitation
     * on operations to videos which are gotten without login. It is recommended to get videos ranking with login.
     * @return Returns the object to get ranking, not {@code null}
     */
    public synchronized NicoRanking getNicoRanking (){
        if ( isLogin() ){
            try {
                return new NicoRanking(getCookies());
            }catch (NicoAPIException e){}
        }
        return new NicoRanking(null);
    }

    /**
     * 動画のおすすめ動画を取得します<br>
     * Gets videos recommended about the target video.<br>
     * <strong>ＵＩスレッド禁止</strong> HTTP通信を行うのでバックグランドで処理して下さい。<br>
     * <strong>注意　</strong>おすすめ動画の取得にはログインが要りませんが、非ログインで状態で取得した動画への操作には制約が生じるので
     * ログイン状態で取得するよう推奨されます。<br>
     * <strong>No UI thread</strong>: HTTP communication is done<br>
     * <strong>Warning </strong>Getting recommended videos does not require login, but there is some limitation
     * on operations to videos which are gotten without login. It is better to get recommended videos woth login.
     * @param videoInfo target video, cannot be {@code null}
     * @return Returns empty List if no hit, not {@code null}
     * @throws NicoAPIException if fail to get recommended video
     */
    public List<VideoInfo> getRecommend(VideoInfo videoInfo) throws NicoAPIException {
        if ( videoInfo == null ){
            throw new NicoAPIException.InvalidParamsException("target video is null > recommend");
        }
        ResourceStore res = ResourceStore.getInstance();
        String path = String.format(
                Locale.US,
                res.getURL(R.string.url_recommend),
                videoInfo.getID()
        );
        HttpClient client = res.getHttpClient();
        if ( client.get(path,null) ) {
            try{
                return RecommendVideoInfo.parse(getCookies(),client.getResponse());
            }catch (NicoAPIException.NoLoginException e){
                return RecommendVideoInfo.parse(null,client.getResponse());
            }
        }else{
            throw new NicoAPIException.HttpException(
                    "Http failure > recommend", NicoAPIException.EXCEPTION_HTTP_RECOMMEND,
                    client.getStatusCode(),path,"GET"
            );
        }
    }

    /**
     * とりあえずマイリストを取得します  Gets temp myList.<br>
     * <strong>ログイン必須 </strong>ログインしていない、もしくはユーザの公開設定によっては取得に失敗して例外を投げます。<br>
     * <strong>ＵＩスレッド禁止</strong> HTTP通信を行うのでバックグランドで処理して下さい。<br>
     * <strong>Login Needed </strong>If not login or setting of user does not allow the access, this fails to get myList and throws exception.
     * <strong>No UI thread</strong>: HTTP communication is done<br>
     * @return Returns {@link TempMyListVideoGroup} object with empty List, not {@code null}
     * @throws NicoAPIException if fail to get myList
     */
    public synchronized TempMyListVideoGroup getTempMyList()throws NicoAPIException{
        if ( isLogin() ){
            return new TempMyListVideoGroup(getCookies(),getUserID());
        }else{
            throw new NicoAPIException.NoLoginException(
                    "no login > temp myList",
                    NicoAPIException.EXCEPTION_NOT_LOGIN_TEMP_MYLIST
            );
        }
    }

    /**
     * 動画を検索するのに必要な{@link NicoSearch}のインスタンスを取得します<br>
     * Gets {@link NicoSearch} instance in order to search videos in NicoNico.<br>
     * 動画を検索するにはこのメソッドから{@link NicoSearch}オブジェクトを取得してこのオブジェクトに対して
     * 各種検索パラメータをセットして{@link NicoSearch#search()}を呼びます。
     * <strong>注意　</strong>動画検索にはログインが要りませんが、非ログインで状態で取得した動画への操作には制約が生じるので
     * ログイン状態で検索するよう推奨されます。<br>
     * In order to search videos, first, you get {@link NicoSearch} object and set various params to it.
     * Then you can get results by calling {@link NicoSearch#search()}.
     * <strong>Warning </strong>Searching for videos does not require login, but there is some limitation
     * on operations to videos which are gotten without login. It is recommended to search and get videos with login.
     * @return Returns the object to search videos, not {@code null}
     */
    public synchronized NicoSearch getNicoSearch (){
        if ( isLogin() ){
            try{
                return new NicoSearch(appName,null,getCookies());
            }catch (NicoAPIException e){}
        }
        return new NicoSearch(appName,null,null);
    }

    /**
     * マイリスグループを取得します【ログイン必須】<br>
     * Gets myList group , be sure to login beforehand.<br>
     * <strong>ＵＩスレッド禁止</strong> HTTP通信を行うのでバックグランドで処理して下さい。<br>
     * <strong>No UI thread</strong>: HTTP communication is done<br>
     * @return Returns {@link MyListGroup} instance with {@code List} of {@link MyListVideoGroup}
     * @throws NicoAPIException if fail to get myList group
     */
    public synchronized MyListGroup getMyListGroup() throws NicoAPIException{
        if ( !isLogin() ){
            throw new NicoAPIException.NoLoginException(
                    "no login > myList group",
                    NicoAPIException.EXCEPTION_NOT_LOGIN_MYLIST_GROUP
            );
        }
        return new MyListGroup(getCookies());
    }

    /**
     * 指定したIDのマイリスを取得します<br>
     * Gets myList identified with ID.<br>
     * 公開マイリスはログインしていなくても他ユーザのマイリスでもマイリスＩＤを正しく指定することで取得できます。
     * 非公開マイリスはログイン状態で自身のマイリスのみ取得できます。
     * ただし、取得可能な値の関係で自身のマイリスは{@link MyListGroup#getMyListVideoGroup()}からの取得が推奨されます。
     * マイリスIDは{@link #getMyListGroup()}で取得できます。<br>
     * Getting public myList does not require login. One user's public myList is available to another user.
     * However, non-public myList is available only for that user who created it.
     * You can get myList ID from {@link #getMyListGroup()}.
     * <strong>ＵＩスレッド禁止</strong> HTTP通信を行うのでバックグランドで処理して下さい。<br>
     * <strong>No UI thread</strong>: HTTP communication is done<br>
     * @param ID the target myList ID, cannot be {@code null}
     * @return Returns empty List if no hit, not {@code null}
     * @throws NicoAPIException if fail to get myList, which my be because of no target myList or invalid myList ID
     */
    public synchronized MyListVideoGroup getMyList (int ID) throws NicoAPIException{
        /*if ( !loginInfo.isLogin() ){
            throw new NicoAPIException.NoLoginException(
                    "no login > myList",
                    NicoAPIException.EXCEPTION_NOT_LOGIN_MYLIST
            );
        }*/
        return MyListVideoGroup.getMyListGroup(isLogin() ? getCookies() : null ,ID);
    }

    /**
     * 対象の動画を渡して、コメントを取得する【ログイン必須】<br>
     * Gets comments of the video passed in argument, be sure to login.<br>
     * 【ＵＩスレッド禁止】HTTP通信を行うのでバックグランド処理してください。
     * 取得するコメント数は動画長さに応じて適当に設定されます。
     * 一度取得すれば{@link VideoInfo#getComment()}からいつでも取得できます。<br>
     *【No UI thread】HTTP communication is done.
     * The number of comments is set corresponding to video length.
     * Once the comment is gotten, it can be accessed from {@link VideoInfo#getComment()}
     * @param videoInfo the target video, cannot be {@code null}
     * @return Returns List of CommentInfo sorted along time series, not {@code null}
     * @throws NicoAPIException NicoAPIException if fail to get comment
     */
    public synchronized CommentInfo.CommentGroup getComment (VideoInfo videoInfo) throws NicoAPIException{
        if ( videoInfo == null ){
            throw new NicoAPIException.InvalidParamsException(
                    "target video is null > comment",
                    NicoAPIException.EXCEPTION_PARAM_COMMENT_TARGET
            );
        }
        try{
            videoInfo.getMessageServerURL();
            videoInfo.getThreadID();
        }catch (NicoAPIException e){
            videoInfo.getFlv(getCookies());
        }
        return videoInfo.getComment();
    }

    /**
     * 対象の動画を渡して、コメントを取得する【ログイン必須】<br>
     * Gets comments of the video passed in argument, be sure to login.
     * 【ＵＩスレッド禁止】HTTP通信を行うのでバックグランド処理してください。
     * 一度取得すれば{@link VideoInfo#getComment()}からいつでも取得できます。<br>
     *【No UI thread】HTTP communication is done.
     * Once the comment is gotten, it can be accessed from {@link VideoInfo#getComment()}
     * @param videoInfo the target video, cannot be {@code null}
     * @param max the limit number comment response from 0 to 1000, if over 1000, fixed to 1000
     * @return Returns List of CommentInfo sorted along time series, not {@code null}
     * @throws NicoAPIException if fail to get comment
     */
    public synchronized CommentInfo.CommentGroup getComment (VideoInfo videoInfo, int max) throws NicoAPIException{
        if ( videoInfo == null ){
            throw new NicoAPIException.InvalidParamsException(
                    "target video is null > comment",
                    NicoAPIException.EXCEPTION_PARAM_COMMENT_TARGET
            );
        }
        if ( !isLogin() ){
            throw new NicoAPIException.NoLoginException(
                    "no login > comment",
                    NicoAPIException.EXCEPTION_NOT_LOGIN_COMMENT
            );
        }
        try{
            videoInfo.getMessageServerURL();
            videoInfo.getThreadID();
        }catch (NicoAPIException e){
            videoInfo.getFlv(getCookies());
        }
        return videoInfo.getComment(max);
        //return videoInfo.loadCommentByJson(max);
    }

    /**
     * コメントの投稿に必要な{@link NicoCommentPost}のインスタンスを取得します<br>
     * Gets instance of {@link NicoCommentPost} in order to post a comment to Nico.
     * <strong>ログイン必須　</strong>コメントの投稿にはログインが必要です。
     * 非ログイン状態で呼ばれると例外を投げますので注意してください。
     * @param info the target video to which new comment is posted, cannot be {@code null}
     * @return not {@code null}
     * @throws NicoAPIException if not login or target video is {@code null}
     */
    public NicoCommentPost getNicoCommentPost(VideoInfo info) throws NicoAPIException{
        if ( isLogin() ){
            return new NicoCommentPost(info,this,deviceName);
        }else{
            throw new NicoAPIException.NoLoginException(
                    "no login > posting comment",
                    NicoAPIException.EXCEPTION_NOT_LOGIN_COMMENT_POST
            );
        }
    }

    public boolean downloadFlv (VideoInfo info, File dir) throws Exception{
        if ( isLogin() ){
            if ( info == null ){

            }else{
                try{
                    info.getFlvURL();
                }catch (NicoAPIException e){
                    if ( ! info.getFlv(getCookies())){
                        return false;
                    }
                }
                File output = new File(dir,info.getTitle());
                ResourceStore res = ResourceStore.getInstance();
                HttpClient client = res.getHttpClient();
                String path = String.format(
                        Locale.US,
                        res.getURL(R.string.url_watch),
                        info.getID()
                );
                client.get(path, getCookies());
                return client.download(info.getFlvURL(),output,client.getCookies());
            }
        }
        return false;
    }

    /*implementation of parcelable*/

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out,flags);
        out.writeString(appName);
        out.writeString(deviceName);
    }

    public static final Parcelable.Creator<NicoClient> CREATOR = new Parcelable.Creator<NicoClient>() {
        public NicoClient createFromParcel(Parcel in) {
            return new NicoClient(in);
        }
        public NicoClient[] newArray(int size) {
            return new NicoClient[size];
        }
    };

    private NicoClient(Parcel in) {
        super(in);
        this.appName = in.readString();
        this.deviceName = in.readString();
    }


}
