package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
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

public class LoginInfo implements Parcelable{

    protected boolean login;
    protected String userName;
    protected int userID;
    protected boolean isPremium;
    protected String userIconUrl;
    protected Bitmap userIcon;

    protected int cookieNum;
    protected int[] cookieVersion;
    protected String[] cookieName;
    protected String[] cookieValue;
    protected String[] cookiePath;
    protected String[] cookieDomain;

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
            throw new NicoAPIException.NoLoginException(
                    "no login > user name",
                    NicoAPIException.EXCEPTION_NOT_LOGIN_USER_NAME
            );
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
            throw new NicoAPIException.NoLoginException(
                    "no login > user ID",
                    NicoAPIException.EXCEPTION_NOT_LOGIN_USER_ID
            );
        }
        return userID;
    }
    public synchronized boolean isPremium () throws NicoAPIException.NoLoginException {
        if ( !login ){
            throw new NicoAPIException.NoLoginException(
                    "no login > isPremium",
                    NicoAPIException.EXCEPTION_NOT_LOGIN_USER_PREMIUM
            );
        }
        return isPremium;
    }

    private final Object iconGetLock = new Object();
    /**
     * ユーザのアイコンを取得する【ログイン必須】<br>
     * Gets user icon image, be sure to login beforehand.<br>
     * ログインしていないと取得に失敗して例外を投げます。<br>
     * If not login, this fails to get the image and throws exception.
     * @return Returns user icon, not {@code null}
     * @throws NicoAPIException if fail to get image
     */
    public Bitmap getUserIcon () throws NicoAPIException{
        synchronized (iconGetLock) {
            synchronized (this) {
                if (login && userIconUrl != null) {
                    if (userIcon != null) {
                        return userIcon;
                    }
                } else {
                    throw new NicoAPIException.NoLoginException(
                            "no login > user Icon",
                            NicoAPIException.EXCEPTION_NOT_LOGIN_USER_ICON
                    );
                }
            }
            userIcon = new HttpResponseGetter().getBitmap(userIconUrl);
            if (userIcon == null) {
                throw new NicoAPIException.DrawableFailureException(
                        "fail to get user icon > ",
                        NicoAPIException.EXCEPTION_DRAWABLE_USER_ICON
                );
            } else {
                return userIcon;
            }
        }
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
    protected synchronized void setUserIconUrl(String userIconUrl){
        this.userIconUrl = userIconUrl;
    }

    /**
     * ログインセッションを含むCookieを取得する<br>
     * Gets Cookie containing login session.
     * @return CookieStore contains login session
     * @throws NicoAPIException.NoLoginException if not login
     */
    public synchronized CookieStore getCookieStore() throws NicoAPIException.NoLoginException{
        if ( !login ){
            throw new NicoAPIException.NoLoginException(
                    "no login > getCookieStore",
                    NicoAPIException.EXCEPTION_NOT_LOGIN_COOKIE
            );
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

    /*implementation of parcelable*/

    public int describeContents() {
        return 0;
    }

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
    }

    public static final Parcelable.Creator<LoginInfo> CREATOR = new Parcelable.Creator<LoginInfo>() {
        public LoginInfo createFromParcel(Parcel in) {
            return new LoginInfo(in);
        }
        public LoginInfo[] newArray(int size) {
            return new LoginInfo[size];
        }
    };

    private LoginInfo(Parcel in) {
        boolean[] booleanValue = new boolean[1];
        in.readBooleanArray(booleanValue);
        this.login = booleanValue[0];
        this.userName = in.readString();
        this.userID = in.readInt();
        in.readBooleanArray(booleanValue);
        this.isPremium = booleanValue[0];
        this.userIconUrl = in.readString();
        this.userIcon = in.readParcelable(Bitmap.class.getClassLoader());
        this.cookieNum = in.readInt();
        this.cookieVersion = new int[cookieNum];
        in.readIntArray(this.cookieVersion);
        this.cookieName = new String[cookieNum];
        in.readStringArray(this.cookieName);
        this.cookieValue = new String[cookieNum];
        in.readStringArray(this.cookieValue);
        this.cookiePath = new String[cookieNum];
        in.readStringArray(this.cookiePath);
        this.cookieDomain = new String[cookieNum];
        in.readStringArray(this.cookieDomain);
    }

}
