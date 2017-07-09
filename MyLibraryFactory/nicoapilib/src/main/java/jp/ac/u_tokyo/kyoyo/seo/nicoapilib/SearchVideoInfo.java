package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * ニコ動ＡＰＩからの検索結果をパースします<br>
 * This parses search results in JSON.<br><br>
 *
 * This class extending VideoInfo provides methods to parse JSON
 * from http://api.search.nicovideo.jp/api/v2/snapshot/video/contents/search?q={your query}<br>
 *      details of this API : <a href=http://site.nicovideo.jp/search-api-docs/snapshot.html>[niconico]ニコニコ動画 『スナップショット検索API v2』 ガイド</a><br>
 * @author Seo-4d696b75
 * @version 0.0  on 2017/01/16.
 */

class SearchVideoInfo extends NicoVideoInfo {

    private SearchVideoInfo (CookieGroup cookies, JSONObject item) throws NicoAPIException.ParseException {
        super(cookies);
        initialize(item);
    }

    private synchronized void initialize(JSONObject item) throws NicoAPIException.ParseException {
        try{
            ResourceStore res = ResourceStore.getInstance();
            title = item.getString(res.getString(R.string.key_search_title));
            setID( item.getString(res.getString(R.string.key_search_videoID)) );
            date = convertDate(item.getString(res.getString(R.string.key_search_date)));
            description = item.getString(res.getString(R.string.key_search_description));
            length = item.getInt(res.getString(R.string.key_search_duration));
            viewCounter = item.getInt(res.getString(R.string.key_search_view_counter));
            commentCounter = item.getInt(res.getString(R.string.key_search_comment_counter));
            myListCounter = item.getInt(res.getString(R.string.key_search_myList_counter));
            tags = Arrays.asList(item.getString(res.getString(R.string.key_search_tags)).split("\\s"));
        }catch(JSONException e){
            throw new NicoAPIException.ParseException(e.getMessage(),item.toString());
        }
    }

    private Date convertDate (String date) throws NicoAPIException.ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(ResourceStore.getInstance().getString(R.string.format_search_date), Locale.JAPAN);
        dateFormat.setTimeZone(TimeZone.getTimeZone("Japan"));
        try {
            return dateFormat.parse(date);
        }catch (ParseException e){
            throw new NicoAPIException.ParseException(e.getMessage(),date);
        }
    }

    /**
     * ニコ動ＡＰＩからの検索結果をパースします<br>
     * Parses search results form Nico.<br>
     * @param response the string corresponding to root of result, cannot be {@code null}
     * @return Returns empty List if no hit, not {@code null}
     * @throws NicoAPIException if invalid argument not following required format
     */
    protected static List<VideoInfo> parse (CookieGroup cookies, String response) throws NicoAPIException{
        if ( response == null ){
            throw new NicoAPIException.ParseException("parse target is null",null);
        }
        try {
            JSONObject root = new JSONObject(response);
            return parse(cookies,root);
        }catch ( JSONException e){
            throw new NicoAPIException.ParseException(e.getMessage(),response);
        }
    }

    /**
     * ニコ動ＡＰＩからの検索結果をパースします<br>
     * Parses search results form Nico.<br>
     * ＡＰＩの詳細やレスポンスの形式は{@link SearchVideoInfo ここから参照}してください。<br>
     * 取得できる動画のフィールドは以下の通りです。<br>
     *     動画タイトル<br>
     *     動画ID<br>
     *     動画説明<br>
     *     動画長さ<br>
     *     動画投稿日時<br>
     *     再生数<br>
     *     コメント数<br>
     *     マイリス数<br>
     *     動画タグ<br>
     * More details about API and response format is {@link SearchVideoInfo available here}.<br>
     * You can get these video fields;<br>
     *     title of video<br>
     *     video ID<br>
     *     video description<br>
     *     length<br>
     *     contributed date<br>
     *     number of view<br>
     *     number of comment<br>
     *     number of myList registered<br>
     *     tags of video<br>
     * @param root the JSONObject corresponding to root of result, cannot be {@code null}
     * @return Returns empty List if no hit, not {@code null}
     * @throws NicoAPIException if invalid argument not following required format
     */
    protected static List<VideoInfo> parse (CookieGroup cookies, JSONObject root) throws NicoAPIException{
        try {
            List<VideoInfo> list = new ArrayList<VideoInfo>();
            JSONArray array = root.getJSONArray(ResourceStore.getInstance().getString(R.string.key_search_result_body));
            for ( int i=0 ; i<array.length() ; i++){
                JSONObject item = array.getJSONObject(i);
                list.add( new SearchVideoInfo(cookies, item));
            }
            return list;
        }catch(JSONException e){
            throw new NicoAPIException.ParseException(e.getMessage(),root.toString());
        }
    }
}
