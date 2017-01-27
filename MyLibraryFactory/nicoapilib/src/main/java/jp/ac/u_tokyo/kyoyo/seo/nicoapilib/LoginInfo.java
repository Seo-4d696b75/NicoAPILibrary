package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.util.Log;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.io.Serializable;
import java.util.List;

/**
 * ニコ動のユーザー情報を管理する。Intentで渡せるようにSerializableにした<br>
 *     this class manages information of user, and is Serializable so that it can be passed with Intent.
 *
 * @author Seo-4d696b75
 * @version 0.0 on 2017/01/21.
 */

public class LoginInfo implements Serializable{

    private boolean login;
    private String userName;
    private int userID;

    private int cookieNum;
    private int[] cookieVersion;
    private String[] cookieName;
    private String[] cookieValue;
    private String[] cookiePath;
    private String[] cookieDomain;

    public LoginInfo (){
        login = false;
    }

    /**
     * ログイン状態を返す<br>
     *     check login status in boolean.
     * @return Returns {@code true} if login, otherwise {@code false}
     */
    public boolean isLogin (){
        return login;
    }
    /**
     * ニックネームとか言われるあのユーザ名を取得、必ずログインしてからにしよう。<br>
     *     get user name in String.
     * @return can be {@code null} if not login
     */
    public String getUserName(){
        return userName;
    }
    /**
     * ユーザＩＤを取得する、必ずログインしてから取得すること。<br>
     *     get userID, be sure to get after login.
     * @return can be {@code null} if not login
     */
    public int getUserID(){
        return userID;
    }
    /**
     * @deprecated supposed to be called by NicoLogin only.
     */
    public void setUserName (String userName){
        this.userName = userName;
    }
    /**
     * @deprecated supposed to be called by NicoLogin only.
     */
    public void setUserID (int userID){
        this.userID = userID;
    }
    /**
     * @deprecated supposed to be called within NicoAPI only.
     */
    public CookieStore getCookieStore(){
        CookieStore cookieStore = new DefaultHttpClient().getCookieStore();
        for (int i = 0; i < cookieNum; i++) {
            BasicClientCookie cookie = new BasicClientCookie(cookieName[i], cookieValue[i]);
            cookie.setVersion(cookieVersion[i]);
            cookie.setPath(cookiePath[i]);
            cookie.setDomain(cookieDomain[i]);
            cookieStore.addCookie(cookie);
        }
        return cookieStore;
    }
    /**
     * @deprecated supposed to be called within NicoAPI only.
     */
    public void setCookieStore(CookieStore cookieStore){
        if ( cookieStore == null ){
            login = false;
            cookieNum = 0;
            cookieVersion = new int[0];
            cookieName = new String[0];
            cookieValue = new String[0];
            cookieDomain = new String[0];
            cookiePath = new String[0];
            userName = "";
            userID = 0;
            return;
        }
        List<Cookie> list = cookieStore.getCookies();
        cookieNum = list.size();
        cookieVersion = new int[cookieNum];
        cookieName = new String[cookieNum];
        cookieValue = new String[cookieNum];
        cookieDomain = new String[cookieNum];
        cookiePath = new String[cookieNum];
        for ( int i=0 ; i<cookieNum ; i++){
            Cookie cookie = list.get(i);
            cookieVersion[i] = cookie.getVersion();
            cookieName[i] = cookie.getName();
            cookieValue[i] = cookie.getValue();
            cookiePath[i] = cookie.getPath();
            cookieDomain[i] = cookie.getDomain();
            Log.d("login-cookies",cookie.getName() +" :"  +cookie.getValue());
        }
        //TODO checking cookie name is better
        if ( cookieNum > 1 ){
            login = true;
        }
    }
}
