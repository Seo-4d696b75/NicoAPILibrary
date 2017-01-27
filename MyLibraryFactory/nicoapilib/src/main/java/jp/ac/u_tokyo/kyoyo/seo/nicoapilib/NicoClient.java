package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.graphics.drawable.Drawable;

import org.apache.http.client.CookieStore;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NicoAPIの中心となるクラス,全ての機能はここから呼ぶ<br>
 *     this class the center of NicoAPI, all the functions are called from here.
 *
 * @author Seo-4d696b75
 * @version 0.0 on 2017/01/21.
 */

public class NicoClient {

    private LoginInfo loginInfo;
    /**
     * アプリの名前、使用するＡＰＩによってはこの値が必要<br>
     *     your application name, needed for some API.
     */
    public static final String appName = "yourAppName";

    /**
     * コンストラクタからインスタンスを取得します,全てはここから始まる<br>
     *     get instance in this constructor.
     */
    public NicoClient (){
        loginInfo = new LoginInfo();
    }

    /**
     * Intent等で{@link LoginInfo ログイン情報}が手元にある場合を想定したコンストラクタ<br>
     *     constructor in case where {@link LoginInfo login information} is available with Intent or so.
     * @param loginInfo can be {@code null}, but add new {@link LoginInfo login information} with no login
     */
    public NicoClient (LoginInfo loginInfo){
        if ( loginInfo == null ){
            loginInfo = new LoginInfo();
        }
        this.loginInfo = loginInfo;
    }

    /**
     * ニコ動にログインします、各種ログイン情報は{@link #loginInfo}から参照されたし<br>
     *     try to login in NicoNico, you can access various relevant information from {@link #loginInfo}.
     * @param mail should not be {@code null}, or fail
     * @param pass should not be {@code null}, or fail
     */
    public void login (String mail, String pass){
        NicoLogin nicoLogin = new NicoLogin(loginInfo);
        nicoLogin.login(mail,pass);
    }

    /**
     * ユーザのアイコンを取得する,ログインが必須<br>
     *     get user icon image, be sure to login beforehand.
     * @return Returns {@code null} if not login or fail in HTTP
     */
    public Drawable getUserIcon(){
        NicoLogin nicoLogin = new NicoLogin(loginInfo);
        return nicoLogin.getUserIcon();
    }

    /**
     * ランキングを取得 {@link RankingVideoInfo パラメータの詳細}、ログインが必須<br>
     *     get ranking, see {@link RankingVideoInfo details about params} and be sure to login beforehand.
     * @param genre should not be {@code null}, or return {@code null}
     * @param period should not be {@code null}, or return {@code null}
     * @param rankKind should not be {@code null}, or return {@code null}
     * @return Returns {@code null} if above params is invalid, not login or fail in HTTP
     */
    public List<VideoInfo> getRanking(String genre, String period, String rankKind){
        RankingGetter getter = new RankingGetter();
        return getter.get(genre,period,rankKind);
    }

    /**
     * 対象の動画を引数に渡し、その動画に関するおすすめ動画を取得する<br>
     *     get videos recommended about the target video passed in argument.
     * @param videoInfo target video, should not be {@code null}, or return {@code null}
     * @return Returns {@code null} if no target video or fail in HTTP
     */
    public List<VideoInfo> getRecommend(VideoInfo videoInfo){
        if ( videoInfo == null ){
            //TODO exception
            return null;
        }
        RecommendGetter getter = new RecommendGetter();
        return getter.get(videoInfo);
    }

    /**
     * とりあえずマイリストの取得、ログイン必須<br>
     *     get temp my list, be sure to login beforehand.
     * @return Returns {@code null} if not login or fail in HTTP
     */
    public List<VideoInfo> getTempMyList(){
        if ( loginInfo.isLogin() ) {
            TempMyListGetter getter = new TempMyListGetter();
            return getter.get();
        }else{
            //TODO
            return null;
        }
    }

    /**
     * 動画を検索するのに必要な{@link NicoSearch}のインスタンスを取得する、検索にはログインが要らない<br>
     *     get {@link NicoSearch} instance in order to search videos in NicoNico, you don't have to login.
     * @return use this to search videos
     */
    public NicoSearch getNicoSearch (){
        return new NicoSearch();
    }

    /**
     *{@link #getComment(VideoInfo, int)}の引数省略したもの、コメント数は動画長さに応じて適当に設定される<br>
     *     {@link #getComment(VideoInfo, int)} with omitted argument, number of comments is set corresponding to video length.
     */
    public List<CommentInfo> getComment (VideoInfo videoInfo){
        if ( videoInfo == null ){
            //TODO
            return null;
        }
        int max = (int)((float)videoInfo.getInt(VideoInfo.LENGTH) * 3.0f);
        return getComment(videoInfo,max);
    }

    /**
     * 対象の動画を渡して、コメントを取得する、ログイン必須<br>
     *     get comments of the video passed in argument, be sure to login.
     * @param videoInfo should not be {@code null}, or return {@code null}
     * @param max limit number comment response from 0 to 1000, if over 1000, fixed to 1000
     * @return Returns {@code null} if no target video, {@code max}<=0 or fail in HTTP
     */
    public List<CommentInfo> getComment (VideoInfo videoInfo, int max){
        if ( videoInfo == null ){
            //TODO
            return null;
        }
        CommentGetter getter = new CommentGetter();
        return getter.get(videoInfo,max);
    }

    private class RankingGetter extends HttpResponseGetter {

        private String rankingUrl = "http://www.nicovideo.jp/ranking/%s/%s/%s?rss=2.0";

        public List<VideoInfo> get (String genre, String period, String rankKind){
            if ( genre == null || period == null || rankKind == null ){
                //TODO exception
                return null;
            }else {
                if ( loginInfo.isLogin() ) {
                    String path = String.format(rankingUrl, rankKind, period, genre);
                    if (tryGet(path, loginInfo.getCookieStore())) {
                        return RankingVideoInfo.parse(super.response, genre, period, rankKind);
                    }
                }else{
                    //TODO exception
                }
            }
            return null;
        }

    }

    private class RecommendGetter extends HttpResponseGetter {

        private String recommendUrl = "http://flapi.nicovideo.jp/api/getrelation?page=10&sort=p&order=d&video=";

        public List<VideoInfo> get (VideoInfo info){
                String path = recommendUrl + info.getString(VideoInfo.ID);
                if ( tryGet(path) ){
                    return RecommendVideoInfo.parse(super.response);
                }
            return null;
        }
    }

    private class TempMyListGetter extends HttpResponseGetter {

        private String tempMyListUrl = "http://www.nicovideo.jp/api/deflist/list";

        public List<VideoInfo> get (){
            String path = tempMyListUrl;
            if ( tryGet(path,loginInfo.getCookieStore()) ){
                return TempMyListVideoInfo.parse(super.response);
            }
            return null;
        }
    }

    private class CommentGetter extends HttpResponseGetter {

        private String paramFormat = ".json/thread?version=20090904&thread=%s&res_from=-%d";

        public List<CommentInfo> get(VideoInfo videoInfo, int max){
            if ( max <= 0 ){
                return null;
            }
            if ( max > 1000 ){
                max = 1000;
            }
            String threadID = videoInfo.getString(VideoInfo.THREAD_ID);
            String path = videoInfo.getString(VideoInfo.MESSAGE_SERVER_URL);
            if ( threadID == null || path == null ){
                if ( loginInfo.isLogin() ){
                    if ( ((VideoInfoManager)videoInfo).getFlv(loginInfo.getCookieStore()) ){
                        threadID = videoInfo.getString(VideoInfo.THREAD_ID);
                        path = videoInfo.getString(VideoInfo.MESSAGE_SERVER_URL);
                    }else{
                        return null;
                    }
                }else{
                    //TODO
                    return null;
                }
            }
            String param = String.format(paramFormat,threadID,max);
            Matcher matcher = Pattern.compile("(.+/api)/?").matcher(path);
            if ( matcher.find() ){
                path = matcher.group(1);
                path = path + param;
                if ( tryGet(path,loginInfo.getCookieStore() )){
                    return CommentInfo.parse(super.response);
                }
            }
            return null;
        }
    }

}
