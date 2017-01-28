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
 * parse recommend response form Nico.    <br><br>
 *
 * this class extending VideoInfoManager provides methods to parse XML<br>
 * from http://flapi.nicovideo.jp/api/getrelation?page={0}&sort={1}&order={2}&video={3}<br>
 *      query params;<br>
 *      {0} : ? any integer<br>
 *      {1} : sort param p:recommend<br>
 *      {2} : order param  d:dow<br>
 *      {3} : video ID (sm*****)<br><br>
 *
 * reference;<br>
 * how to get recommend : http://d.hatena.ne.jp/picas/20080202/1201955339<br>
 * usage of regex : http://nobuo-create.net/seikihyougen/<br>
 * usage of Pattern and Matcher : http://www.ne.jp/asahi/hishidama/home/tech/java/regexp.html<br>
 *                                  http://www.javadrive.jp/regex/ref/index2.html<br>
 *                                  http://qiita.com/ha_g1/items/d41febac011df4601544<br>
 *                                  http://www.javadrive.jp/regex/option/index4.html<br>
 * @version  0.0  on 2017/01/16.
 * @author Seo-4d696b75
 */

public class RecommendVideoInfo extends VideoInfoManager {

    protected RecommendVideoInfo (String item){
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
    private void initialize(String item){
        Matcher valueMatcher;
        for ( Integer key : patternMap.keySet() ){
            valueMatcher = patternMap.get(key).matcher(item);
            if ( valueMatcher.find() ){
                String value = valueMatcher.group(1);
                switch ( key ){
                    case VideoInfo.ID:
                        id = value;
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
            }
        }
    }

    private String convertDate (long time){
        return dateFormatBase.format(new Date(time*1000));
    }

    /**
     * ニコ動APIから取得したxml形式のレスポンスをパースします<br>
     * parse recommend response in XML.<br>
     * APIの詳細は{@link RecommendVideoInfo ここから参照}できます。<br>
     * 取得できる動画のフィールドは以下の通りです。<br>
     *     {@link VideoInfo#title 動画タイトル}<br>
     *     {@link VideoInfo#id 動画ID}<br>
     *     {@link VideoInfo#thumbnail 動画サムネイル画像のURL}<br>
     *     {@link VideoInfo#date 投稿日時}<br>
     *     {@link VideoInfo#length 動画長さ}<br>
     *     {@link VideoInfo#viewCounter 再生数}<br>
     *     {@link VideoInfo#commentCounter コメント数}<br>
     *     {@link VideoInfo#myListCounter マイリス数}<br>
     * more details is {@link RankingVideoInfo available here}.<br>
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
     * @return
     */
    protected static List<VideoInfo> parse (String xml){
        Matcher matcher = Pattern.compile("<related_video status=\"(.+?)\">").matcher(xml);
        if ( !matcher.find() || !matcher.group(1).equals("ok")  ){
            return null;
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
