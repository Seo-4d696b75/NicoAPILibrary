package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.graphics.drawable.Drawable;
import android.util.Log;

import org.apache.http.client.CookieStore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    /**
     * アプリの名前、使用するＡＰＩによってはこの値が必要<br>
     * Your application name, needed for some API.
     */
    public static final String appName = "yourAppName";

    /**
     * コンストラクタからインスタンスを取得します、全てはここから始まる<br>
     * Gets instance of this constructor.
     */
    public NicoClient (){}

    /*
    /**
     * Intent等で{@link LoginInfo ログイン情報}が手元にある場合を想定したコンストラクタ<br>
     *     constructor in case where {@link LoginInfo login information} is available with Intent or so.
     * @param loginInfo can be {@code null}, but add new {@link LoginInfo login information} with no login
     *//*
    public NicoClient (LoginInfo loginInfo){
        if ( loginInfo == null ){
            loginInfo = new LoginInfo();
        }
        this.loginInfo = loginInfo;
    }*/

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
     * ユーザのアイコンを取得する【ログイン必須】<br>
     * Gets user icon image, be sure to login beforehand.<br>
     * ログインしていないと取得に失敗して例外を投げます。<br>
     * If not login, this fails to get the image and throws exception.
     * @return Returns user icon, not {@code null}
     * @throws NicoAPIException if fail to get image
     */
    public synchronized Drawable getUserIcon() throws NicoAPIException{
        NicoLogin nicoLogin = new NicoLogin(NicoClient.this);
        return nicoLogin.getUserIcon();
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
        RecommendGetter getter = new RecommendGetter();
        return getter.get(videoInfo);
    }

    /**
     * とりあえずマイリストの取得【ログイン必須】<br>
     * Gets temp myList, be sure to login beforehand.<br>
     * ログインしていない、もしくはユーザの公開設定によっては取得に失敗して例外を投げます。<br>
     * If not login or setting of user does not allow the access, this fails to get myList and throws exception.
     * @return Returns empty List if no hit, not {@code null}
     * @throws NicoAPIException if fail to get myList
     */
    public synchronized List<VideoInfo> getTempMyList()throws NicoAPIException{
        if ( isLogin() ) {
            TempMyListGetter getter = new TempMyListGetter();
            return getter.get();
        }else{
            throw new NicoAPIException.NoLoginException("no login > temp myList");
        }
    }

    /**
     * 動画を検索するのに必要な{@link NicoSearch}のインスタンスを取得する、検索にはログインが要らない<br>
     * Gets {@link NicoSearch} instance in order to search videos in NicoNico, you don't have to login.
     * @return use this to search videos, not {@code null}
     */
    public NicoSearch getNicoSearch (){
        return new NicoSearch();
    }

    /**
     * マイリスグループを取得します【ログイン必須】<br>
     *     get myList group , be sure to login beforehand.<br>
     * @return Returns {@code Map<String,String>} of myList name and its ID
     * @throws NicoAPIException if fail to get myList group
     */
    public synchronized Map<String,String> getMyListGroup() throws NicoAPIException{
        if ( !isLogin() ){
            throw new NicoAPIException.NoLoginException("no login > myList group");
        }
        MyListGetter getter = new MyListGetter();
        return getter.getMyListGroup();
    }

    /**
     * 指定したIDのマイリスを取得します【ログイン必須】<br>
     * Gets myList identified with ID.<br>
     * このメソッドでは自身のマイリスのみ取得できます。
     * ただし、マイリスの公開設定によっては取得できません。
     * マイリスIDは{@link #getMyListGroup()}で取得できます。<br>
     *     This can get your own myList only, but may fails due to its accessibility by user.
     *     You can get myList ID from {@link #getMyListGroup()}.
     * @param ID yhe target myList ID, cannot be {@code null}
     * @return Returns empty List if no hit, not {@code null}
     * @throws NicoAPIException if fail to get myList, which my be because of no target myList or invalid myList ID
     */
    public synchronized List<VideoInfo> getMyList (String ID) throws NicoAPIException{
        if ( !isLogin() ){
            throw new NicoAPIException.NoLoginException("no login > myList");
        }
        if ( ID == null ){
            throw new NicoAPIException.InvalidParamsException("myList ID is null");
        }
        MyListGetter getter = new MyListGetter();
        return getter.getMyList(ID);
    }

    /**
     *{@link #getComment(VideoInfo, int)}の引数省略したもの、コメント数は動画長さに応じて適当に設定される<br>
     *{@link #getComment(VideoInfo, int)} with omitted argument, number of comments is set corresponding to video length.
     */
    public synchronized List<CommentInfo> getComment (VideoInfo videoInfo) throws NicoAPIException{
        if ( videoInfo == null ){
            throw new NicoAPIException.InvalidParamsException("target video is null > comment");
        }
        int max = (int)((float)videoInfo.getInt(VideoInfo.LENGTH) * 3.0f);
        return getComment(videoInfo,max);
    }

    /**
     * 対象の動画を渡して、コメントを取得する【ログイン必須】<br>
     * Gets comments of the video passed in argument, be sure to login.
     * @param videoInfo the target video, cannot be {@code null}
     * @param max the limit number comment response from 0 to 1000, if over 1000, fixed to 1000
     * @return Returns List of CommentInfo sorted along time series, not {@code null}
     * @throws NicoAPIException if fail to get comment
     */
    public synchronized List<CommentInfo> getComment (VideoInfo videoInfo, int max) throws NicoAPIException{
        if ( videoInfo == null ){
            throw new NicoAPIException.InvalidParamsException("target video is null > comment");
        }
        if ( !isLogin() ){
            throw new NicoAPIException.NoLoginException("no login > comment");
        }
        CommentGetter getter = new CommentGetter();
        return getter.get(videoInfo,max);
    }

    private class RecommendGetter extends HttpResponseGetter {

        private String recommendUrl = "http://flapi.nicovideo.jp/api/getrelation?page=10&sort=p&order=d&video=";

        protected List<VideoInfo> get (VideoInfo info) throws NicoAPIException{
            String path = recommendUrl + info.getString(VideoInfo.ID);
            tryGet(path);
            return RecommendVideoInfo.parse(super.response);

        }
    }

    private class TempMyListGetter extends HttpResponseGetter {

        private String tempMyListUrl = "http://www.nicovideo.jp/api/deflist/list";

        protected List<VideoInfo> get () throws NicoAPIException {
            String path = tempMyListUrl;
            tryGet(path, getCookieStore());
            return TempMyListVideoInfo.parse(super.response);
        }
    }

    private class CommentGetter extends HttpResponseGetter {

        private String paramFormat = ".json/thread?version=20090904&thread=%s&res_from=-%d";

        protected List<CommentInfo> get(VideoInfo videoInfo, int max) throws NicoAPIException{
            if ( max <= 0 ){
                max = 100;
            }
            if ( max > 1000 ){
                max = 1000;
            }
            String threadID = "";
            String path = "";
            try {
                threadID = videoInfo.getString(VideoInfo.THREAD_ID);
                path = videoInfo.getString(VideoInfo.MESSAGE_SERVER_URL);
            }catch (NicoAPIException e){
                ((VideoInfoManager)videoInfo).getFlv(getCookieStore());
                threadID = videoInfo.getString(VideoInfo.THREAD_ID);
                path = videoInfo.getString(VideoInfo.MESSAGE_SERVER_URL);
            }
            String param = String.format(paramFormat,threadID,max);
            Matcher matcher = Pattern.compile("(.+/api)/?").matcher(path);
            if ( matcher.find() ){
                path = matcher.group(1);
                path = path + param;
                tryGet(path,getCookieStore() );
                return CommentInfo.parse(super.response);
            }else{
                throw new NicoAPIException.APIUnexpectedException("message server URL > " + path);
            }
        }
    }

    private class MyListGetter extends HttpResponseGetter {

        private String myListGroupUrl = "http://www.nicovideo.jp/api/mylistgroup/list";
        private String myListUrl = "http://www.nicovideo.jp/mylist/%d?rss=2.0";

        protected Map<String, String> getMyListGroup () throws NicoAPIException{
            if ( tryGet(myListGroupUrl, getCookieStore()) ){
                try{
                    JSONObject json = new JSONObject(response);
                    if ( !json.optString("status").equals("ok")){
                        String message = "Unexpected API response status > myList group ";
                        try{
                            JSONObject error = json.getJSONObject("error");
                            String code = error.getString("code");
                            String description = error.getString("description");
                            message += (code + ":" +description);
                        }catch (JSONException e){}
                        throw new NicoAPIException.APIUnexpectedException(message);
                    }
                    JSONArray array = json.optJSONArray("mylistgroup");
                    Map<String,String> myListGroup = new HashMap<String,String>();
                    for ( int i=0 ; i<array.length() ; i++){
                        JSONObject item = array.optJSONObject(i);
                        myListGroup.put(item.optString("name"),item.optString("id"));
                    }
                    return myListGroup;
                }catch (JSONException e){
                    throw new NicoAPIException.ParseException(e.getMessage(),response);
                }
            }
            return null;
        }

        protected List<VideoInfo> getMyList(String ID)throws NicoAPIException{
            String path = String.format(myListUrl, ID);
            tryGet(path, getCookieStore());
            return RankingVideoInfo.parse(super.response,null,null,null);
        }
    }

}
