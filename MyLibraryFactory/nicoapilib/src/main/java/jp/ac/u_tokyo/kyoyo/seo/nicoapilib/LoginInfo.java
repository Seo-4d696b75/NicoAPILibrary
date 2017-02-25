package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.util.Log;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.io.Serializable;
import java.util.List;

/**
 * ニコ動のユーザー情報を管理する<br>
 *     This class manages information of user.<br><br>
 *
 *     Intentで渡す場合を想定してSerializableにしてある<br>
 *     This is Serializable so that it can be passed with Intent.
 *
 * @author Seo-4d696b75
 * @version 0.0 on 2017/01/21.
 */

public class LoginInfo implements Serializable{

    private boolean login;
    private String userName;
    private int userID;
    private boolean isPremium;

    private int cookieNum;
    private int[] cookieVersion;
    private String[] cookieName;
    private String[] cookieValue;
    private String[] cookiePath;
    private String[] cookieDomain;

    protected LoginInfo (){
        login = false;
    }

    /**
     * ログイン状態を返す<br>
     * Checks login status in boolean.
     * @return Returns {@code true} if login, otherwise {@code false}
     */
    public synchronized boolean isLogin (){
        return login;
    }
    /**
     * ニックネームとか言われるあのユーザ名を取得、必ずログインしてから<br>
     * Gets user name in String.
     * @return user name in String
     * @throws NicoAPIException if not login
     */
    public synchronized String getUserName() throws NicoAPIException.NoLoginException{
        if ( !login ){
            throw new NicoAPIException.NoLoginException("no login > user name", NicoAPIException.EXCEPTION_NOT_LOGIN_USER_NAME);
        }
        return userName;
    }
    /**
     * ユーザＩＤを取得する、必ずログインしてから取得すること。<br>
     * Gets userID, be sure to get after login.
     * @return user ID
     * @throws NicoAPIException if not login
     */
    public synchronized int getUserID() throws NicoAPIException.NoLoginException{
        if ( !login ){
            throw new NicoAPIException.NoLoginException("no login > user ID",NicoAPIException.EXCEPTION_NOT_LOGIN_USER_ID);
        }
        return userID;
    }
    public synchronized boolean isPremium () throws NicoAPIException.NoLoginException {
        if ( !login ){
            throw new NicoAPIException.NoLoginException("no login > isPremium",NicoAPIException.EXCEPTION_NOT_LOGIN_USER_PREMIUM);
        }
        return isPremium;
    }
    protected synchronized void setUserName (String userName){
        this.userName = userName;
    }
    protected synchronized void setUserID (int userID){
        this.userID = userID;
    }
    protected synchronized void setPremium (boolean isPremium){
        this.isPremium = isPremium;
    }

    /**
     * ログインセッションを含むCookieを取得する<br>
     * Gets Cookie containing login session.
     * @return CookieStore contains login session
     * @throws NicoAPIException.NoLoginException if not login
     */
    public synchronized CookieStore getCookieStore() throws NicoAPIException.NoLoginException{
        if ( !login ){
            throw new NicoAPIException.NoLoginException("no login > getCookieStore",NicoAPIException.EXCEPTION_NOT_LOGIN_COOKIE);
        }
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
     * supposed to be called within NicoAPI only.
     */
    protected synchronized void setCookieStore(CookieStore cookieStore){
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
