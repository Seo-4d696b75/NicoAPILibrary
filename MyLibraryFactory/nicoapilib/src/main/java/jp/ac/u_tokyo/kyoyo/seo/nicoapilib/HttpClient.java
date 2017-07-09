package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.graphics.Bitmap;

import java.io.File;
import java.util.Map;

/**
 * HTTP通信を行うオブジェクトを定義します。
 * @author Seo-4d696b75
 * @version 0.0 on 2017/06/26.
 */

public interface HttpClient {
    /**
     * 指定したパスにPOSTします。
     * @param path the path to which some entity is post
     * @param entity the content to post
     * @param cookies the cookie to post with entity together, may be {@code null}
     * @return Returns {@code true}, if succeed
     */
    boolean post(String path, String entity, CookieGroup cookies);

    /**
     * 指定したパスにPOSTします。
     * @param path the path to which some entity is post
     * @param entity the list of key-value pairs to post
     * @param cookies the cookie to post with entity together, may be {@code null}
     * @return Returns {@code true}, if succeed
     */
    boolean post(String path, Map<String,String> entity, CookieGroup cookies);

    /**
     * 指定したパスにGETします。
     * @param path the path to which this get
     * @param cookies the cookie to send with entity together, may be {@code null}
     * @return Returns {@code true}, if succeed
     */
    boolean get(String path, CookieGroup cookies);

    /**
     * 画像を取得します。
     * @param path the path from which the target image is gotten
     * @param cookies the cookie to send with entity together, may be {@code null}
     * @return Returns {@code null} if fail to get
     */
    Bitmap getBitmap(String path, CookieGroup cookies);

    /**
     * 指定したパス先のファイルをダウンロードします。
     * @param path the path from which the target file is downloaded
     * @param download the file path to which the target file is downloaded
     * @param cookies the cookie to send with entity together, may be {@code null}
     * @return Returns {@code true}, if succeed
     */
    boolean download(String path, File download, CookieGroup cookies);

    /**
     * 通信で返されたCookieを取得します。
     * @return Returns {@code null} if not get or post vis http yet
     */
    CookieGroup getCookies();

    /**
     * 通信で返されたテキストを取得します。
     * @return Returns {@code null} if not get or post vis http yet
     */
    String getResponse();

    /**
     * 通信で得られたステータスコードを取得します。
     * 通信成功時は200です
     * @return Returns negative if not get or post vis http yet
     */
    int getStatusCode();
}
