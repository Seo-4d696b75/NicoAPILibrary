package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Apache HTTP client {@link CookieStore} で定義されたオブジェクトを
 * {@link CookieGroup} に適用させるためのクラスです。
 * @author Seo-4d696b75
 * @version 0.0 on 2017/06/26.
 */

public class ApacheCookieGroup implements CookieGroup {

    public ApacheCookieGroup(CookieStore cookieStore){
        list = new ArrayList<>();
        for ( Cookie item : cookieStore.getCookies() ){
            list.add(new ApacheCookieInfo(item));
        }
    }

    private List<CookieInfo> list;

    @Override
    public List<CookieInfo> getCookies(){
        return new ArrayList<>(list);
    }

    @Override
    public Iterator<CookieInfo> iterator(){
        return list.iterator();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags){
        out.writeTypedList(list);
    }

    private ApacheCookieGroup (Parcel in){
        List<ApacheCookieInfo> list = new ArrayList<>();
        in.readTypedList(list,ApacheCookieInfo.CREATOR);
        this.list = new ArrayList<>();
        for ( ApacheCookieInfo info : list ){
            this.list.add(info);
        }
    }

    public static final Parcelable.Creator<ApacheCookieGroup> CREATOR = new Parcelable.Creator<ApacheCookieGroup>() {
        public ApacheCookieGroup createFromParcel(Parcel in) {
            return new ApacheCookieGroup(in);
        }
        public ApacheCookieGroup[] newArray(int size) {
            return new ApacheCookieGroup[size];
        }
    };
}
