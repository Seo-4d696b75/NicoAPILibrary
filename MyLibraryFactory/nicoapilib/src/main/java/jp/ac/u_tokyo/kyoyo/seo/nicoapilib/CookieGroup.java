package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.os.Parcelable;

import java.util.Iterator;
import java.util.List;

/**
 * Cookieの集合を扱うオブジェクトを定義します。
 * @author Seo-4d696b75
 * @version 0.0 on 2017/06/26.
 */

public interface CookieGroup extends Iterable<CookieInfo>, Parcelable {
    List<CookieInfo> getCookies();
    Iterator<CookieInfo> iterator();
}
