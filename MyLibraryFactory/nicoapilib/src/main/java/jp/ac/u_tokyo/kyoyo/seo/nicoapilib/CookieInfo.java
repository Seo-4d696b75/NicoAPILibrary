package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.os.Parcelable;

/**
 * Cookieの属性を取得できるオブジェクトを定義します。
 * @author Seo-4d696b75
 * @version 0.0 on 2017/06/26.
 */

public interface CookieInfo extends Parcelable {
    String getName();
    String getValue();
    int getVersion();
    String getDomain();
    String getPath();
}
