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
 * this class try to login NicoNico and get userID, userName and userIconImage in background.<br><br>
 *
 * references :<br>
 * how to login with Apache : https://teratail.com/questions/31972<br>
 *                              http://c-loft.com/blog/?p=1196<br>
 * regular expression : java.keicode.com/lang/regexp-split.php<br>
 * get image via http : http://logicalerror.seesaa.net/article/419965567.html<br>
 * how to get user name : http://7cc.hatenadiary.jp/entry/nico-user-id-to-name<br>
 *
 * @author Seo-4d696b75
 * @version 0.0 2016/12/10.
 */

public class NicoLogin extends HttpResponseGetter {

    private LoginInfo loginInfo;
    private String loginUrl = "https://account.nicovideo.jp/api/v1/login?show_button_twitter=1&site=niconico&show_button_facebook=1&next_url=";
    private String myPageUrl = "http://www.nicovideo.jp/my";
    private String userIconUrl = "http://usericon.nimg.jp/usericon/%d/%d";
    private String nicoSeigaUrl = "http://seiga.nicovideo.jp/api/user/info?id=";
    private int userID;
    private String userName;

    /**
     * ログインを行うにはこのコンストラクタでインスタンスを取得する<br>
     *     get instance in order to login.<br>
     *         ログイン情報を格納するための{@link LoginInfo}を引数に渡す必要があります。<br>
     *         you need to pass {@link LoginInfo} to keep login session.
     * @param loginInfo can not be {@code null}
     */
    protected NicoLogin(LoginInfo loginInfo){
        if ( loginInfo != null ) {
            this.loginInfo = loginInfo;
        }else{
            //TODO throw exception
        }
    }

    /**
     * 指定したメールアドレスとパスワードでログインします<br>
     *     try to login with mail address and password passed.<br>
     * ログインに成功すると、{@link NicoLogin コンストラクタ}で渡した{@link LoginInfo}に
     * ログインセッションの情報を含むCookiesとユーザＩＤ、ユーザ名を登録します。
     * これらすべての取得に成功すると{@code true}を返します。<br>
     * when succeed in login, try to get userID and name
     * then resister them in {@link LoginInfo} passed at {@link NicoLogin constructor}.
     * @param mail can not be {@code null}
     * @param pass can not be {@code null}
     * @return Returns {@code true} if succeed in login and in getting userID and name
     */
    protected boolean login( final String mail, final String pass){
        if ( mail == null || pass == null ){
            return false;
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
                if ( getUserID() ){
                    loginInfo.setUserID(userID);
                    if ( getUserName() ){
                        loginInfo.setUserName(userName);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean getUserName(){
        if ( super.response == null || userID <= 0 ){
            //TODO exception
        }
        Matcher matcher = Pattern.compile("<span id=\"siteHeaderUserNickNameContainer\">(.+?)</span>").matcher(super.response);
        if ( matcher.find() ){
            userName =  matcher.group(1);
            return true;
        }
        String path = nicoSeigaUrl + userID;
        if ( tryGet(path) ){
            matcher = Pattern.compile("<nickname>(.+?)</nickname>").matcher(super.response);
            if ( matcher.find() ){
                userName = matcher.group(1);
                return true;
            }
        }
        return false;
    }

    private boolean getUserID(){
        if ( super.response == null ){
            //TODO exception
        }
        Matcher matcher = Pattern.compile("var User = \\{ id: ([0-9]+), age: ([0-9]+), isPremium: (false|true), isOver18: (false|true), isMan: (false|true) \\};").matcher(super.response);
        if ( matcher.find() ){
            userID = Integer.parseInt(matcher.group(1));
            String age = matcher.group(2);
            //following params also can be gotten, but not used in this app
            boolean isPremium = Boolean.valueOf(matcher.group(3));
            boolean isOver18 = Boolean.valueOf(matcher.group(4));
            boolean isMan = Boolean.valueOf(matcher.group(5));
            return true;
        }else{
            userID = 0;
            return false;
        }
    }

    /**
     * supposed to be called from {@link NicoClient} only.
     * @return Returns {@code null} if not login
     */
    protected Drawable getUserIcon(){
        if ( !loginInfo.isLogin() ){
            //TODO exception
            return null;
        }
        userID = loginInfo.getUserID();
        String path = String.format(userIconUrl,userID/10000,userID);
        Drawable image = null;
        try {
            URL url = new URL(path);
            InputStream input = (InputStream)url.getContent();
            image = Drawable.createFromStream(input,"uer_icon");
            input.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return image;
    }



}
