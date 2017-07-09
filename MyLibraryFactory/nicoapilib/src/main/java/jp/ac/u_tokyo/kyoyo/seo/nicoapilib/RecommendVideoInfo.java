package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 取得したニコ動のおすすめ動画情報をパースします<br>
 * This parses recommend response form Nico.    <br><br>
 *
 * This class extending VideoInfo provides methods to parse XML<br>
 * from http://flapi.nicovideo.jp/api/getrelation?page={0}&sort={1}&order={2}&video={3}<br>
 *      query params;<br>
 *      {0} : ? any integer<br>
 *      {1} : sort param p:recommend<br>
 *      {2} : order param  d:down<br>
 *      {3} : video ID (sm*****)<br><br>
 *
 * reference;<br>
 * how to get recommend : <a href=http://d.hatena.ne.jp/picas/20080202/1201955339>[Hatena::Diary]ニコニコ動画のAPIのメモ</a><br>
 * usage of regex : <a href=http://nobuo-create.net/seikihyougen>[一番かんたんなJava入門]【Java】正規表現って何？</a><br>
 * usage of Pattern and Matcher : <a href=http://www.ne.jp/asahi/hishidama/home/tech/java/regexp.html>Javaの正規表現</a><br>
 * <a href=http://www.javadrive.jp/regex/ref/index2.html>[JavaDrive]パターン内の括弧毎にマッチした部分文字列を取得</a><br>
 * <a href=http://qiita.com/ha_g1/items/d41febac011df4601544>[Qiita]正規表現の最短マッチ</a><br>
 * <a href=http://www.javadrive.jp/regex/option/index4.html>[JavaDrive]DOTALLモード</a><br>
 * @version  0.0  on 2017/01/16.
 * @author Seo-4d696b75
 */

class RecommendVideoInfo extends NicoVideoInfo {

    /**
     *
     * @param cookies may be {@code null} in case of not login
     * @param item
     * @throws NicoAPIException.ParseException
     */
    private RecommendVideoInfo (CookieGroup cookies, String item) throws NicoAPIException.ParseException {
        super(cookies);
        initialize(item);
    }

    private void initialize(String item) throws NicoAPIException.ParseException {
        Map<Integer,Integer> map = new HashMap<Integer, Integer>(){
            {
                put(VideoInfo.TITLE, R.string.regex_recommend_title);
                put(VideoInfo.ID, R.string.regex_recommend_videoID);
                put(VideoInfo.THUMBNAIL_URL, R.string.regex_recommend_thumbnail);
                put(VideoInfo.LENGTH, R.string.regex_recommend_duration);
                put(VideoInfo.DATE, R.string.regex_recommend_date);
                put(VideoInfo.VIEW_COUNTER, R.string.regex_recommend_view_counter);
                put(VideoInfo.MY_LIST_COUNTER, R.string.regex_recommend_myList_counter);
                put(VideoInfo.COMMENT_COUNTER, R.string.regex_recommend_comment_counter);
            }
        };
        ResourceStore res = ResourceStore.getInstance();
        for ( int key : map.keySet() ){
            Matcher valueMatcher = res.getPattern(map.get(key)).matcher(item);
            if ( valueMatcher.find() ){
                String value = valueMatcher.group(1);
                switch ( key ){
                    case VideoInfo.ID:
                        setID(value);
                        break;
                    case VideoInfo.TITLE:
                        title = value;
                        break;
                    case VideoInfo.THUMBNAIL_URL:
                        thumbnailUrl = value;
                        break;
                    case VideoInfo.VIEW_COUNTER:
                        viewCounter = Integer.parseInt(value);
                        break;
                    case VideoInfo.COMMENT_COUNTER:
                        commentCounter = Integer.parseInt(value);
                        break;
                    case VideoInfo.MY_LIST_COUNTER:
                        myListCounter = Integer.parseInt(value);
                        break;
                    case VideoInfo.LENGTH:
                        length = Integer.parseInt(value);
                        break;
                    case VideoInfo.DATE:
                        long time = Long.parseLong(value);
                        date = new Date(1000*time);
                        break;
                    default:
                }
            }else{
                throw new NicoAPIException.ParseException(
                        "target partial sequence matched with \"" + valueMatcher.pattern().pattern() + "\" not found",
                        item,
                        NicoAPIException.EXCEPTION_PARSE_RECOMMEND
                );
            }
        }
    }

    /**
     * ニコ動APIから取得したxml形式のレスポンスをパースします<br>
     * Parses recommend response in XML.<br>
     * 取得できる動画のフィールドは以下の通りです。<br>
     *     動画タイトル<br>
     *     動画ID<br>
     *     動画サムネイル画像のURL<br>
     *     投稿日時<br>
     *     動画長さ<br>
     *     再生数<br>
     *     コメント数<br>
     *     マイリス数<br>
     *     you can get following fields of videos;<br>
     *     title of video<br>
     *     video ID<br>
     *     URL of thumbnail<br>
     *     length<br>
     *     contributed date<br>
     *     number of view<br>
     *     number of comment<br>
     *     number of myList registered<br>
     * @param xml recommend response, can not be {@code null}
     * @return Returns empty List if no hit, not {@code null}
     * @throws NicoAPIException if invalid response not following required format
     */
    protected static List<VideoInfo> parse (CookieGroup cookies, String xml) throws NicoAPIException{
        if ( xml == null ){
            throw new NicoAPIException.ParseException(
                    "parse target is null > recommend",null,
                    NicoAPIException.EXCEPTION_PARSE_RECOMMEND_NO_TARGET
            );
        }
        ResourceStore res = ResourceStore.getInstance();
        Matcher matcher = res.getPattern(R.string.regex_recommend_status).matcher(xml);
        if ( matcher.find() ) {
            if ( matcher.group(1).equals(res.getString(R.string.value_recommend_status_success))) {
                Pattern patternItem = res.getPattern(R.string.regex_recommend_body);
                List<VideoInfo> list = new ArrayList<VideoInfo>();
                matcher = patternItem.matcher(xml);
                while( matcher.find() ){
                    String item = matcher.group();
                    list.add( new RecommendVideoInfo(cookies,item) );
                }
                return list;
            }else{
                matcher = res.getPattern(R.string.regex_recommend_error).matcher(xml);
                String message = "unexpected API response status > recommend ";
                if (matcher.find()) {
                    message += matcher.group(1);
                    message += ":";
                    message += matcher.group(2);
                }
                throw new NicoAPIException.APIUnexpectedException(
                        message,
                        NicoAPIException.EXCEPTION_UNEXPECTED_RECOMMEND_STATUS_CODE
                );
            }
        }else{
            throw new NicoAPIException.APIUnexpectedException(
                    "fail to parse response meta > recommend",
                    NicoAPIException.EXCEPTION_PARSE_RECOMMEND_STATUS
            );
        }
    }
}
