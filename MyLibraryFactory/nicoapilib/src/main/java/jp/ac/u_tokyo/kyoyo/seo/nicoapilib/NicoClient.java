package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NicoAPIの中心となるクラスです<br>
 * this class the center of NicoAPI, all the functions are called from here.<br><br>
 *
 * このライブラリの全機能はここから呼ぶことができます。このクラスは直列化可能ですのでアプリ起動時に
 * 一度インスタンス化したらIntentでActivity間を渡して使いまわせます。
 *
 * @author Seo-4d696b75
 * @version 0.0 on 2017/01/21.
 */

public class NicoClient extends LoginInfo{

    public static final String INTENT = "intent";

    /**
     * アプリの名前、使用するＡＰＩによってはこの値が必要<br>
     * Your application name, needed for some API.<br>
     *  動画検索に用いる『スナップショット検索API v2』 に必要です。
     */
    protected String appName;
    /**
     * 使用するデバイスの名前 <br>
     * コメントを投稿するときに必要です。<br>
     * This value is needed when a comment is posted.
     */
    protected String deviceName;

    /**
     * コンストラクタからインスタンスを取得します、全てはここから始まる<br>
     * Gets instance of this constructor.
     * @param appName your application name, cannot be {@code null}
     * @param deviceName your device name, cannot be {@code null}
     */
    public NicoClient (String appName,String deviceName){
        this.appName = appName;
        this.deviceName = deviceName;
    }

    /**
     * ニコ動にログインします<br>
     * Tries to login in NicoNico. <br>
     * ニコ動にアカウント登録された有効なメールアドレスとパスワードを渡してください。
     * {@code null}または無効な値を渡すとログインに失敗します。
     * ライブラリでは機能によってはログインが必須なので、必ずログインしてください。
     * 各種ログイン情報は{@link LoginInfo 親クラス}の適当なメソッドを呼び参照できます。<br>
     *  Pass valid mail address and pass word registered in NicoNico.
     *  If invalid value or {@code null} is passed, you fail to login.
     *  In this library, some function require login session, so be sure to login.
     *  You can access various relevant information by calling methods of {@link LoginInfo super class}.
     * @param mail cannot be {@code null}
     * @param pass cannot be {@code null}
     * @throws NicoAPIException if fail to login because of invalid params
     */
    public synchronized void login (String mail, String pass) throws NicoAPIException {
        NicoLogin nicoLogin = new NicoLogin(NicoClient.this);
        nicoLogin.login(mail, pass);
    }

    /**
     * ランキング検索に必要な{@link NicoRanking}のインスタンスを取得する<br>
     * Gets instance of {@link NicoRanking}, in order to get ranking from Nico.
     * @return not {@code null}
     */
    public NicoRanking getNicoRanking (){
        return new NicoRanking();
    }

    /**
     * 対象の動画を引数に渡し、その動画に関するおすすめ動画を取得する<br>
     * Gets videos recommended about the target video passed in argument.
     * @param videoInfo target video, cannot be {@code null}
     * @return Returns empty List if no hit, not {@code null}
     * @throws NicoAPIException if fail to get recommend video
     */
    public List<VideoInfo> getRecommend(VideoInfo videoInfo) throws NicoAPIException {
        if ( videoInfo == null ){
            throw new NicoAPIException.InvalidParamsException("target video is null > recommend");
        }
        String recommendUrl = "http://flapi.nicovideo.jp/api/getrelation?page=10&sort=p&order=d&video=";
        String path = recommendUrl + videoInfo.getID();
        HttpResponseGetter getter = new HttpResponseGetter();
        getter.tryGet(path);
        return RecommendVideoInfo.parse(getter.response);
    }

    /**
     * とりあえずマイリストの取得【ログイン必須】<br>
     * Gets temp myList, be sure to login beforehand.<br>
     * ログインしていない、もしくはユーザの公開設定によっては取得に失敗して例外を投げます。<br>
     * If not login or setting of user does not allow the access, this fails to get myList and throws exception.
     * @return Returns {@link TempMyListVideoGroup instance with empty List, not {@code null}
     * @throws NicoAPIException if fail to get myList
     */
    public synchronized TempMyListVideoGroup getTempMyList()throws NicoAPIException{
        if ( isLogin() ) {
            return new TempMyListVideoGroup(this);
        }else{
            throw new NicoAPIException.NoLoginException(
                    "no login > temp myList",
                    NicoAPIException.EXCEPTION_NOT_LOGIN_TEMP_MYLIST
            );
        }
    }

    /**
     * 動画を検索するのに必要な{@link NicoSearch}のインスタンスを取得する、検索にはログインが要らない<br>
     * Gets {@link NicoSearch} instance in order to search videos in NicoNico, you don't have to login.
     * @return use this to search videos, not {@code null}
     */
    public NicoSearch getNicoSearch (){
        return new NicoSearch(appName);
    }

    /**
     * マイリスグループを取得します【ログイン必須】<br>
     *     get myList group , be sure to login beforehand.<br>
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
        return new MyListGroup(this);
    }

    /**
     * 指定したIDのマイリスを取得します【ログイン必須】<br>
     * Gets myList identified with ID.<br>
     * このメソッドでは自身のマイリスのみ取得できます。
     * ただし、マイリスの公開設定によっては取得できません。
     * マイリスIDは{@link #getMyListGroup()}で取得できます。<br>
     *     This can get your own myList only, but may fails due to its accessibility by user.
     *     You can get myList ID from {@link #getMyListGroup()}.
     * @param ID the target myList ID, cannot be {@code null}
     * @return Returns empty List if no hit, not {@code null}
     * @throws NicoAPIException if fail to get myList, which my be because of no target myList or invalid myList ID
     */
    public synchronized MyListVideoGroup getMyList (int ID) throws NicoAPIException{
        if ( !isLogin() ){
            throw new NicoAPIException.NoLoginException(
                    "no login > myList",
                    NicoAPIException.EXCEPTION_NOT_LOGIN_MYLIST
            );
        }
        return MyListVideoGroup.getMyListGroup(ID,this);
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
            videoInfo.getMessageServerUrl();
            videoInfo.getThreadID();
        }catch (NicoAPIException e){
            videoInfo.getFlv(getCookieStore());
        }
        return videoInfo.loadComment();
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
            videoInfo.getMessageServerUrl();
            videoInfo.getThreadID();
        }catch (NicoAPIException e){
            videoInfo.getFlv(getCookieStore());
        }
        return videoInfo.loadComment(max);
        //return videoInfo.loadCommentByJson(max);
    }

    /**
     * コメントの投稿に必要な{@link NicoCommentPost}のインスタンスを取得する<br>
     * Gets instance of {@link NicoCommentPost} in order to post a comment to Nico.
     * @param info the target video to which new comment is posted, cannot be {@code null}
     * @return not {@code null}
     * @throws NicoAPIException if not login or target video is {@code null}
     */
    public NicoCommentPost getNicoCommentPost(VideoInfo info) throws NicoAPIException{
        if ( isLogin() ){
            return new NicoCommentPost(info,this);
        }else{
            throw new NicoAPIException.NoLoginException(
                    "no login > posting comment",
                    NicoAPIException.EXCEPTION_NOT_LOGIN_COMMENT_POST
            );
        }
    }

    /*implementation of parcelable*/

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeBooleanArray(new boolean[]{login});
        out.writeString(userName);
        out.writeInt(userID);
        out.writeBooleanArray(new boolean[]{isPremium});
        out.writeString(userIconUrl);
        out.writeParcelable(userIcon,flags);
        out.writeInt(cookieNum);
        out.writeIntArray(cookieVersion);
        out.writeStringArray(cookieName);
        out.writeStringArray(cookieValue);
        out.writeStringArray(cookiePath);
        out.writeStringArray(cookieDomain);
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
        boolean[] booleanValue = new boolean[1];
        in.readBooleanArray(booleanValue);
        super.login = booleanValue[0];
        super.userName = in.readString();
        super.userID = in.readInt();
        in.readBooleanArray(booleanValue);
        super.isPremium = booleanValue[0];
        super.userIconUrl = in.readString();
        super.userIcon = in.readParcelable(Bitmap.class.getClassLoader());
        super.cookieNum = in.readInt();
        super.cookieVersion = new int[cookieNum];
        in.readIntArray(super.cookieVersion);
        super.cookieName = new String[cookieNum];
        in.readStringArray(super.cookieName);
        super.cookieValue = new String[cookieNum];
        in.readStringArray(super.cookieValue);
        super.cookiePath = new String[cookieNum];
        in.readStringArray(super.cookiePath);
        super.cookieDomain = new String[cookieNum];
        in.readStringArray(super.cookieDomain);
        this.appName = in.readString();
        this.deviceName = in.readString();
    }




}
