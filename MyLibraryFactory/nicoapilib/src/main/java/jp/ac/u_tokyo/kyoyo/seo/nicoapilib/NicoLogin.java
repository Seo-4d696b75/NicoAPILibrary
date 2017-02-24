package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;



import android.graphics.drawable.Drawable;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ニコ動へのログインおよびユーザＩＤ・ニックネームを取得する<br>
 * This class try to login NicoNico and get userID, userName and userIconImage in background.<br><br>
 *
 * references;<br>
 * how to login with Apache : <a href=https://teratail.com/questions/31972>[teratail]Java_ニコニコ動画へのログイン</a><br>
 * <a href=http://c-loft.com/blog/?p=1196>[夏研ブログ]HTTP POST/GET クッキー認証によるWebサイトへのログイン (Android, Java)</a><br>
 * regular expression : <a href=java.keicode.com/lang/regexp-split.php>[Java 入門]正規表現による文字列のスプリット</a><br>
 * get image via http : <a href=http://logicalerror.seesaa.net/article/419965567.html>[SQLの窓]Android Studio : インターネット上の画像を取得して、Bitmap か Drawable で ImageView に表示する</a><br>
 * how to get user name : <a href=http://7cc.hatenadiary.jp/entry/nico-user-id-to-name>[Hatena Blog]ニコニコ動画で、ユーザーIDからニックネーム（ユーザーネーム）を取得する</a><br>
 *
 * @author Seo-4d696b75
 * @version 0.0 2016/12/10.
 */

public class NicoLogin extends HttpResponseGetter {

    private LoginInfo loginInfo;
    private String loginUrl = "https://account.nicovideo.jp/api/v1/login?show_button_twitter=1&site=niconico&show_button_facebook=1&next_url=";
    private String myPageUrl = "http://www.nicovideo.jp/my";
    private String userIconUrl = "http://usericon.nimg.jp/usericon/%d/%d.jpg";
    private String nicoSeigaUrl = "http://seiga.nicovideo.jp/api/user/info?id=";
    private int userID;
    private String userName;

    /**
     * ログインを行うにはこのコンストラクタでインスタンスを取得する<br>
     *  Gets instance in order to login.<br>
     *         ログイン情報を格納するための{@link LoginInfo}を引数に渡す必要があります。<br>
     *        You need to pass {@link LoginInfo} to keep login session.
     * @param loginInfo cannot be {@code null}
     */
    protected NicoLogin(LoginInfo loginInfo){
        if ( loginInfo != null ) {
            this.loginInfo = loginInfo;
        }
    }

    /**
     * 指定したメールアドレスとパスワードでログインします<br>
     *  Tries to login with mail address and password passed.<br>
     * ログインに成功すると、{@link NicoLogin コンストラクタ}で渡した{@link LoginInfo}に
     * ログインセッションの情報を含むCookiesとユーザＩＤ、ユーザ名を登録します。
     * いずれかの取得に失敗すると例外を投げます。<br>
     * When succeed in login, this tries to get userID and its name,
     * then resisters them in {@link LoginInfo} passed at {@link NicoLogin constructor}.
     * @param mail the mail address, cannot be {@code null}
     * @param pass the password, cannot be {@code null}
     * @throws NicoAPIException if fail to login
     */
    protected void login( final String mail, final String pass) throws NicoAPIException{
        if ( mail == null || pass == null ){
            throw new NicoAPIException.InvalidParamsException("mail and pass cannot be null > login");
        }
        String path = loginUrl + myPageUrl;
        Map<String,String> params = new HashMap<String,String>(){
            {
                put("mail_tel", mail);
                put("password", pass);
            }
        };
        if ( tryPost(path,params) ){
            loginInfo.setCookieStore(super.cookieStore);
            if ( loginInfo.isLogin() ){
                getUserID();
                getUserName();
                return;
            }
        }
        throw new NicoAPIException.InvalidParamsException("mail and pass are invalid > login");
    }

    private void getUserName() throws NicoAPIException.ParseException{
        if ( super.response == null || userID <= 0 ){
            throw new NicoAPIException.ParseException("parse target is null",null);
        }
        Matcher matcher = Pattern.compile("<span id=\"siteHeaderUserNickNameContainer\">(.+?)</span>").matcher(super.response);
        if ( matcher.find() ){
            userName =  matcher.group(1);
            loginInfo.setUserName(userName);
            return;
        }
        String path = nicoSeigaUrl + userID;
        if ( tryGet(path) ){
            matcher = Pattern.compile("<nickname>(.+?)</nickname>").matcher(super.response);
            if ( matcher.find() ){
                userName = matcher.group(1);
                loginInfo.setUserName(userName);
                return;
            }else{
                throw new NicoAPIException.ParseException("cannot find user name ",super.response);
            }
        }
    }

    private void getUserID() throws NicoAPIException.ParseException{
        if ( super.response == null ){
            throw new NicoAPIException.ParseException("parse target is null",null);
        }
        Matcher matcher = Pattern.compile("var User = \\{ id: ([0-9]+), age: ([0-9]+), isPremium: (false|true), isOver18: (false|true), isMan: (false|true) \\};").matcher(super.response);
        if ( matcher.find() ){
            userID = Integer.parseInt(matcher.group(1));
            String age = matcher.group(2);
            //following params also can be gotten, but not used in this app
            boolean isPremium = Boolean.valueOf(matcher.group(3));
            boolean isOver18 = Boolean.valueOf(matcher.group(4));
            boolean isMan = Boolean.valueOf(matcher.group(5));
            loginInfo.setUserID(userID);
            loginInfo.setPremium(isPremium);
        }else{
            userID = 0;
            throw new NicoAPIException.ParseException("cannot find userID ",super.response);
        }
    }

    /**
     * supposed to be called from {@link NicoClient} only.
     * @return Returns user icon image, not {@code null}
     * @throws NicoAPIException if fail to get image
     */
    protected Drawable getUserIcon() throws NicoAPIException{
        try {
            userID = loginInfo.getUserID();
            String path = String.format(userIconUrl,userID/10000,userID);
            Drawable image = null;
            URL url = new URL(path);
            InputStream input = (InputStream)url.getContent();
            image = Drawable.createFromStream(input,"uer_icon");
            input.close();
            return image;
        }catch (NicoAPIException.NoLoginException e){
            throw e;
        }catch (Exception e){
            throw new NicoAPIException.DrawableFailureException("fail to get user icon");
        }
    }



}
