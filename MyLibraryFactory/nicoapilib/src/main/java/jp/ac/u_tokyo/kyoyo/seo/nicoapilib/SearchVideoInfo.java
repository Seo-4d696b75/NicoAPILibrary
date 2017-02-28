package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
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

public class SearchVideoInfo extends VideoInfo {

    private SearchVideoInfo (JSONObject item) throws NicoAPIException.ParseException {
        initialize(item);
    }

    private synchronized void initialize(JSONObject item) throws NicoAPIException.ParseException {
        try{
            title = item.getString("title");
            setID( item.getString("contentId") );
            date = convertDate(item.getString("startTime"));
            description = item.getString("description");
            length = item.getInt("lengthSeconds");
            viewCounter = item.getInt("viewCounter");
            commentCounter = item.getInt("commentCounter");
            myListCounter = item.getInt("mylistCounter");
            setTags( item.getString("tags").split("\\s") );
        }catch(JSONException e){
            throw new NicoAPIException.ParseException(e.getMessage(),item.toString());
        }
    }

    private String convertDate (String date) throws NicoAPIException.ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+09:00'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Japan"));
        try {
            return dateFormatBase.format(dateFormat.parse(date));
        }catch (ParseException e){
            throw new NicoAPIException.ParseException(e.getMessage(),date);
        }
    }

    /**
     * ニコ動ＡＰＩからの検索結果をパースします<br>
     * Parses search results form Nico.<br>
     * {@link #parse(JSONObject)}と基本的に同じですが、JSONに対応した内容のStringで渡せます。<br>
     * Basically, this is as same as {@link #parse(JSONObject)}, but String can be passed.
     * @param response the string corresponding to root of result, cannot be {@code null}
     * @return Returns empty List if no hit, not {@code null}
     * @throws NicoAPIException if invalid argument not following required format
     */
    protected static List<VideoInfo> parse (String response) throws NicoAPIException{
        if ( response == null ){
            throw new NicoAPIException.ParseException("parse target is null",null);
        }
        try {
            JSONObject root = new JSONObject(response);
            return parse(root);
        }catch ( JSONException e){
            throw new NicoAPIException.ParseException(e.getMessage(),response);
        }
    }

    /**
     * ニコ動ＡＰＩからの検索結果をパースします<br>
     * Parses search results form Nico.<br>
     * ＡＰＩの詳細やレスポンスの形式は{@link SearchVideoInfo ここから参照}してください。<br>
     * 取得できる動画のフィールドは以下の通りです。<br>
     *     {@link VideoInfo#title 動画タイトル}<br>
     *     {@link VideoInfo#id 動画ID}<br>
     *     {@link VideoInfo#description 動画説明}<br>
     *     {@link VideoInfo#length 動画長さ}<br>
     *     {@link VideoInfo#date 動画投稿日時}<br>
     *     {@link VideoInfo#viewCounter 再生数}<br>
     *     {@link VideoInfo#commentCounter コメント数}<br>
     *     {@link VideoInfo#myListCounter マイリス数}<br>
     *     {@link VideoInfo#tags 動画タグ}<br>
     * More details about API and response format is {@link SearchVideoInfo available here}.<br>
     * You can get these video fields;<br>
     *     {@link VideoInfo#title title of video}<br>
     *     {@link VideoInfo#id video ID}<br>
     *     {@link VideoInfo#description video description}<br>
     *     {@link VideoInfo#length length}<br>
     *     {@link VideoInfo#date contributed date}<br>
     *     {@link VideoInfo#viewCounter number of view}<br>
     *     {@link VideoInfo#commentCounter number of comment}<br>
     *     {@link VideoInfo#myListCounter number of myList registered}<br>
     *     {@link VideoInfo#tags tags of video}<br>
     * @param root the JSONObject corresponding to root of result, cannot be {@code null}
     * @return Returns empty List if no hit, not {@code null}
     * @throws NicoAPIException if invalid argument not following required format
     */
    protected static List<VideoInfo> parse (JSONObject root) throws NicoAPIException{
        if ( root == null ){
            throw new NicoAPIException.ParseException("parse target is null",null);
        }
        try {
            JSONObject meta = root.getJSONObject("meta");
            if ( meta.getInt("status") != 200 ){
                String message = "Unexpected API response status > search ";
                try {
                    String errorCode = meta.getString("errorCode");
                    String errorMessage = meta.getString("errorMessage");
                    message += (errorCode + ":" + errorMessage);
                }catch (JSONException e){}
                throw new NicoAPIException.APIUnexpectedException( message);
            }
            List<VideoInfo> list = new ArrayList<VideoInfo>();
            if ( meta.getInt("totalCount") == 0 ) {
                Log.d("search", "no hit");
                return list;
            }
            JSONArray array = root.getJSONArray("data");
            for ( int i=0 ; i<array.length() ; i++){
                JSONObject item = array.getJSONObject(i);
                list.add( new SearchVideoInfo(item));
            }
            return list;
        }catch(JSONException e){
            throw new NicoAPIException.ParseException(e.getMessage(),root.toString());
        }
    }
}
