package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.http.cookie.Cookie;

/**
 * Apache HTTP client の{@link Cookie Cookieインターフェイス}で定義されたオブジェクトを
 * {@link CookieInfo}のオブジェクトに適用させるためのクラスです。
 * @author Seo-4d696b75
 * @version 0.0 on 2017/06/26.
 */

public class ApacheCookieInfo implements CookieInfo {

    public ApacheCookieInfo(Cookie cookie){
        this.name = cookie.getName();
        this.value = cookie.getValue();
        this.version = cookie.getVersion();
        this.domain = cookie.getDomain();
        this.path = cookie.getPath();
    }

    private String name;
    private String value;
    private int version;
    private String domain;
    private String path;

    @Override
    public String getName(){
        return name;
    }

    @Override
    public String getValue(){
        return value;
    }

    @Override
    public int getVersion(){
        return version;
    }

    @Override
    public String getDomain(){
        return domain;
    }

    @Override
    public String getPath(){
        return path;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags){
        out.writeString(name);
        out.writeString(value);
        out.writeInt(version);
        out.writeString(domain);
        out.writeString(path);
    }

    private ApacheCookieInfo (Parcel in){
        this.name = in.readString();
        this.value = in.readString();
        this.version = in.readInt();
        this.domain = in.readString();
        this.path = in.readString();
    }

    public static final Parcelable.Creator<ApacheCookieInfo> CREATOR = new Parcelable.Creator<ApacheCookieInfo>() {
        public ApacheCookieInfo createFromParcel(Parcel in) {
            return new ApacheCookieInfo(in);
        }
        public ApacheCookieInfo[] newArray(int size) {
            return new ApacheCookieInfo[size];
        }
    };

}
