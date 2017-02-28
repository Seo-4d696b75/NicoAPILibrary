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
 * expected response format;<br>
 * <pre>
 * &lt;related_video status="ok"&gt;
 * &lt;total_count&gt;16&lt;/total_count&gt;
 * &lt;page_count&gt;2&lt;/page_count&gt;
 * &lt;data_count&gt;16&lt;/data_count&gt;
 * &lt;type&gt;recommend&lt;/type&gt;
 * &lt;video&gt;
 *     &lt;url&gt;http://www.nicovideo.jp/watch/sm29775929&lt;/url&gt;
 *     &lt;thumbnail&gt;http://tn-skr2.smilevideo.jp/smile?i=29775929&lt;/thumbnail&gt;
 *     &lt;title&gt;僕の歌をこの世で一番最後に聴いてくれる人は誰ですか&lt;/title&gt;
 *     &lt;view&gt;31625&lt;/view&gt;
 *     &lt;comment&gt;697&lt;/comment&gt;
 *     &lt;mylist&gt;1166&lt;/mylist&gt;
 *     &lt;length&gt;197&lt;/length&gt;
 *     &lt;time&gt;1475661900&lt;/time&gt;
 * &lt;/video&gt;
 * ....................
 * &lt;/related_video&gt;
 * </pre><br><br>
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

public class RecommendVideoInfo extends VideoInfo {

    private RecommendVideoInfo (String item) throws NicoAPIException.ParseException {
        initialize(item);
    }

    private final Map<Integer,Pattern> patternMap = new HashMap<Integer, Pattern>(){
        {
            put(VideoInfo.ID, Pattern.compile("<url>.+/(.+?)</url>"));
            put(VideoInfo.THUMBNAIL_URL,Pattern.compile("<thumbnail>(.+?)</thumbnail>"));
            put(VideoInfo.TITLE,Pattern.compile("<title>(.+?)</title>"));
            put(VideoInfo.VIEW_COUNTER,Pattern.compile("<view>([0-9]+?)</view>"));
            put(VideoInfo.COMMENT_COUNTER,Pattern.compile("<comment>([0-9]+?)</comment>"));
            put(VideoInfo.MY_LIST_COUNTER,Pattern.compile("<mylist>([0-9]+?)</mylist>"));
            put(VideoInfo.LENGTH,Pattern.compile("<length>([0-9]+?)</length>"));
            put(VideoInfo.DATE,Pattern.compile("<time>([0-9]+?)</time>"));
        }
    };
    private synchronized void initialize(String item) throws NicoAPIException.ParseException {
        Matcher valueMatcher;
        for ( Integer key : patternMap.keySet() ){
            valueMatcher = patternMap.get(key).matcher(item);
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
                        setThumbnailUrl(value);
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
                        date = convertDate(time);
                        break;
                    default:
                }
            }else{
                throw new NicoAPIException.ParseException(
                        "target partial sequence matched with \"" + valueMatcher.pattern().pattern() + "\" not found",
                        item
                );
            }
        }
    }

    private String convertDate (long time){
        return dateFormatBase.format(new Date(time*1000));
    }

    /**
     * ニコ動APIから取得したxml形式のレスポンスをパースします<br>
     * Parses recommend response in XML.<br>
     * APIの詳細やレスポンスの形式は{@link RecommendVideoInfo ここから参照}できます。<br>
     * 取得できる動画のフィールドは以下の通りです。<br>
     *     {@link VideoInfo#title 動画タイトル}<br>
     *     {@link VideoInfo#id 動画ID}<br>
     *     {@link VideoInfo#thumbnail 動画サムネイル画像のURL}<br>
     *     {@link VideoInfo#date 投稿日時}<br>
     *     {@link VideoInfo#length 動画長さ}<br>
     *     {@link VideoInfo#viewCounter 再生数}<br>
     *     {@link VideoInfo#commentCounter コメント数}<br>
     *     {@link VideoInfo#myListCounter マイリス数}<br>
     * More details about API and response format is {@link RankingVideoInfo available here}.<br>
     *     you can get following fields of videos;<br>
     *     {@link VideoInfo#title title of video}<br>
     *     {@link VideoInfo#id video ID}<br>
     *     {@link VideoInfo#thumbnail URL of thumbnail}<br>
     *     {@link VideoInfo#length length}<br>
     *     {@link VideoInfo#date contributed date}<br>
     *     {@link VideoInfo#viewCounter number of view}<br>
     *     {@link VideoInfo#commentCounter number of comment}<br>
     *     {@link VideoInfo#myListCounter number of myList registered}<br>
     * @param xml recommend response, can not be {@code null}
     * @return Returns empty List if no hit, not {@code null}
     * @throws NicoAPIException if invalid response not following required format
     */
    protected static List<VideoInfo> parse (String xml) throws NicoAPIException{
        if ( xml == null ){
            throw new NicoAPIException.ParseException("parse target is null",null);
        }
        Matcher matcher = Pattern.compile("<related_video status=\"(.+?)\">").matcher(xml);
        if ( !matcher.find() || !matcher.group(1).equals("ok")  ){
            matcher = Pattern.compile("<error>.+<code>(.+?)</code>.+<description>(.+?)</description>.+</error>",Pattern.DOTALL).matcher(xml);
            String message = "unexpected API response status > recommend ";
            if ( matcher.find() ){
                message += matcher.group(1);
                message += ":";
                message += matcher.group(2);
            }
            throw new NicoAPIException.APIUnexpectedException(message);
        }
        Pattern patternItem = Pattern.compile("<video>.+?</video>",Pattern.DOTALL);
        List<VideoInfo> list = new ArrayList<VideoInfo>();
        matcher = patternItem.matcher(xml);
        while( matcher.find() ){
            String item = matcher.group();
            list.add( new RecommendVideoInfo(item) );
        }
        return list;
    }
}
