package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * ニコ動のユーザー情報を管理する<br>
 *     This class manages information of user.<br><br>
 *
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

    protected CookieGroup cookieGroup;

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

    /**
     * ログインしているユーザがプレミアムかどうか取得します
     * Gets whether or not the user is premium.
     * @return Returns {@code true} if succeed to login
     * @throws NicoAPIException.NoLoginException
     */
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
            userIcon = ResourceStore.getInstance().getHttpClient().getBitmap(userIconUrl,null);
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
    public synchronized CookieGroup getCookies() throws NicoAPIException.NoLoginException{
        if ( !login || cookieGroup == null ){
            throw new NicoAPIException.NoLoginException(
                    "no login > getCookieStore",
                    NicoAPIException.EXCEPTION_NOT_LOGIN_COOKIE
            );
        }
        return cookieGroup;
    }

    protected synchronized void setCookies(CookieGroup cookies){
        if ( cookies != null ){
            cookieGroup = cookies;
            login = false;
            String key = ResourceStore.getInstance().getString(R.string.key_login_session);
            for ( CookieInfo info : cookies ){
                if ( info.getName().equals(key) ){
                    login = true;
                    break;
                }
            }
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
        out.writeParcelable(cookieGroup,flags);
    }

    public static final Parcelable.Creator<LoginInfo> CREATOR = new Parcelable.Creator<LoginInfo>() {
        public LoginInfo createFromParcel(Parcel in) {
            return new LoginInfo(in);
        }
        public LoginInfo[] newArray(int size) {
            return new LoginInfo[size];
        }
    };

    protected LoginInfo(Parcel in) {
        boolean[] booleanValue = new boolean[1];
        in.readBooleanArray(booleanValue);
        this.login = booleanValue[0];
        this.userName = in.readString();
        this.userID = in.readInt();
        in.readBooleanArray(booleanValue);
        this.isPremium = booleanValue[0];
        this.userIconUrl = in.readString();
        this.userIcon = in.readParcelable(Bitmap.class.getClassLoader());
        this.cookieGroup = in.readParcelable(CookieGroup.class.getClassLoader());
    }

}
