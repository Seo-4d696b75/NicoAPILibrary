package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.graphics.drawable.Drawable;

import org.apache.http.client.CookieStore;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Seo-4d696b75 on 2017/01/21.
 */

public class NicoClient {

    private LoginInfo loginInfo;
    public static final String appName = "yourAppName";

    public NicoClient (){
        loginInfo = new LoginInfo();
    }

    public NicoClient (LoginInfo loginInfo){
        if ( loginInfo == null ){
            loginInfo = new LoginInfo();
        }
        this.loginInfo = loginInfo;
    }

    public void login (String mail, String pass){
        NicoLogin nicoLogin = new NicoLogin(loginInfo);
        nicoLogin.login(mail,pass);
    }

    public Drawable getUserIcon(){
        NicoLogin nicoLogin = new NicoLogin(loginInfo);
        return nicoLogin.getUserIcon();
    }

    public List<VideoInfo> getRanking(String genre, String period, String rankKind){
        RankingGetter getter = new RankingGetter();
        return getter.get(genre,period,rankKind);
    }

    public List<VideoInfo> getRecommend(VideoInfo videoInfo){
        if ( videoInfo == null ){
            //TODO exception
            return null;
        }
        RecommendGetter getter = new RecommendGetter();
        return getter.get(videoInfo);
    }

    public List<VideoInfo> getTempMyList(){
        if ( loginInfo.isLogin() ) {
            TempMyListGetter getter = new TempMyListGetter();
            return getter.get();
        }else{
            //TODO
            return null;
        }
    }

    public NicoSearch getNicoSearch (){
        return new NicoSearch();
    }

    public List<CommentInfo> getComment (VideoInfo videoInfo){
        if ( videoInfo == null ){
            //TODO
            return null;
        }
        int max = (int)((float)videoInfo.getInt(VideoInfo.LENGTH) * 3.0f);
        return getComment(videoInfo,max);
    }
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
