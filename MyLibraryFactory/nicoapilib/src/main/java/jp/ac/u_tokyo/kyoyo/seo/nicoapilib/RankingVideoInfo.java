package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 取得したニコ動のランキング情報またはマイリス情報をパースします<br>
 * 　this class parses ranking or myList response from Nico.<br><br>
 *
 * ニコ動ランキングAPIから取得できるフィールドは{@link #parse(String, String, String, String) こちらを参照}<br>
 *   you can get {@link #parse(String, String, String, String) these fields} from ranking API.<br><br>
 *
 * this class extending VideoInfoManager provides methods to parse ranking response in XML<br>
 * from http://www.nicovideo.jp/ranking/{0}/{1}/{2}?rss=2.0<br>
 *      params;<br>
 *      {0} : rank kind param  (view,mylist,fav)<br>
 *      {1} : target period param  (total,monthly,weekly,daily,hourly)<br>
 *      {2} : target category param     list of valid params : http://dic.nicovideo.jp/a/%E3%82%AB%E3%83%86%E3%82%B4%E3%83%AA%E3%82%BF%E3%82%B0<br><br>
 *
 * also parse myList response in XML<br>
 *     from http://www.nicovideo.jp/mylist/{3}?rss=2.0<br>
 *     {3} : target myList ID<br><br>
 *
 * reference;<br>
 * how to get ranking : https://ja.osdn.net/projects/nicolib/wiki/%E3%83%8B%E3%82%B3%E3%83%8B%E3%82%B3%E8%A7%A3%E6%9E%90%E3%83%A1%E3%83%A2<br>
 * usage of regex : http://nobuo-create.net/seikihyougen/<br>
 * usage of Pattern and Matcher : http://www.ne.jp/asahi/hishidama/home/tech/java/regexp.html<br>
 *                                  http://www.javadrive.jp/regex/ref/index2.html<br>
 *                                  http://qiita.com/ha_g1/items/d41febac011df4601544<br>
 *                                  http://www.javadrive.jp/regex/option/index4.html<br>
 * @version 0.0 2017/01/15.
 * @author Seo-4d696b75
 */

public class RankingVideoInfo extends VideoInfoManager {

    //pass XML to initialize fields
    //when ranking, also pass ranking params (can be null)

    private RankingVideoInfo(String xml, String genre, String period, String rankKind){
        this.genre = genre;
        this.period = period;
        this.rankKind = rankKind;
        initialize(xml);
    }

    //map of pattern to extract value from text
    private static final Map<Integer,Pattern> patternMap = new HashMap<Integer,Pattern>(){
        {
            put(VideoInfo.TITLE,Pattern.compile("<title>(.+?)</title>",Pattern.DOTALL));
            put(VideoInfo.ID,Pattern.compile("<link>.+/(.+?)</link>",Pattern.DOTALL));
            put(VideoInfo.PUB_DATE,Pattern.compile("<pubDate>(.+?)</pubDate>",Pattern.DOTALL));
            put(VideoInfo.THUMBNAIL_URL,Pattern.compile("src=\"(.+?)\"",Pattern.DOTALL));
            put(VideoInfo.DESCRIPTION,Pattern.compile("<p class=\"nico-description\">(.+?)</p>",Pattern.DOTALL));
            put(VideoInfo.LENGTH,Pattern.compile("<strong class=\"nico-info-length\">(.+?)</strong>",Pattern.DOTALL));
            put(VideoInfo.DATE,Pattern.compile("<strong class=\"nico-info-date\">(.+?)</strong>",Pattern.DOTALL));
            put(VideoInfo.VIEW_COUNTER,Pattern.compile("<strong class=\"nico-info-total-view\">(.+?)</strong>",Pattern.DOTALL));
            put(VideoInfo.COMMENT_COUNTER,Pattern.compile("<strong class=\"nico-info-total-res\">(.+?)</strong>",Pattern.DOTALL));
            put(VideoInfo.MY_LIST_COUNTER,Pattern.compile("<strong class=\"nico-info-total-mylist\">(.+?)</strong>",Pattern.DOTALL));

        }
    };

    private void initialize(String xml){
        boolean ranking = true;
        if ( genre == null && rankKind == null && period == null ){
            ranking = false;
        }
        Matcher matcher,rankingMatcher;
        for ( Integer key : patternMap.keySet()){
            matcher = patternMap.get(key).matcher(xml);
            if ( matcher.find() ){
                String value = matcher.group(1);
                switch ( key ){
                    case VideoInfo.TITLE:
                        if ( ranking ){
                            //the number of ranking has to be taken away
                            rankingMatcher = Pattern.compile(".+：(.+)").matcher(value);
                            if ( rankingMatcher.find() ){
                                value = rankingMatcher.group(1);
                            }
                        }
                        title = value;
                        break;
                    case VideoInfo.ID:
                        id = value;
                        break;
                    case VideoInfo.DESCRIPTION:
                        description = value;
                        break;
                    case VideoInfo.PUB_DATE:
                        pubDate = convertPubDate(value);
                        break;
                    case VideoInfo.THUMBNAIL_URL:
                        setThumbnailUrl(value);
                        break;
                    case VideoInfo.LENGTH:
                        length = parseLength(value);
                        break;
                    case VideoInfo.DATE:
                        date = convertDate(value);
                        break;
                    case VideoInfo.VIEW_COUNTER:
                        viewCounter = parseCounter(value);
                        break;
                    case VideoInfo.COMMENT_COUNTER:
                        commentCounter = parseCounter(value);
                        break;
                    case VideoInfo.MY_LIST_COUNTER:
                        myListCounter = parseCounter(value);
                        break;
                    default:
                }
            }
        }
        if ( !ranking ){
            complete();
        }
    }

    private int parseCounter(String value){
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        try{
            return numberFormat.parse(value).intValue();
        }catch (ParseException e){
            e.printStackTrace();
        }
        return 0;
    }

    private int parseLength (String value){
        int m = value.indexOf(":");
        int min = Integer.parseInt(value.substring(0,m));
        int sec = Integer.parseInt(value.substring(m+1));
        return min*60+sec;
    }

    private String convertPubDate (String date){
        try{
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
            return dateFormatBase.format(dateFormat.parse(date));
        }catch (ParseException e){
            e.printStackTrace();
        }
        return null;
    }

    private String convertDate (String date){
        try{
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy'年'MM'月'dd'日' HH：mm：ss");
            return dateFormatBase.format(dateFormat.parse(date));
        }catch (ParseException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ニコ動APIから取得したXML形式のランキングおよびマイリス情報をパースします<br>
     *     parse xml text from Nico API about ranking or myList.<br>
     * ランキングの場合は検索に用いたジャンル、期間、種類パラメータを渡してください。
     * {@code null}を渡した場合はマイリスと判断されます。
     * APIの詳細や有効なパラメータは{@link RankingVideoInfo ここから参照}できます。<br>
     * 取得できる動画のフィールドは以下の通りです。<br>
     *     {@link VideoInfo#title 動画タイトル}<br>
     *     {@link VideoInfo#id 動画ID}<br>
     *     {@link VideoInfo#pubDate ランキング発表日時}<br>
     *     {@link VideoInfo#description 動画説明}<br>
     *     {@link VideoInfo#length 動画長さ}<br>
     *     {@link VideoInfo#date 動画投稿日時}<br>
     *     {@link VideoInfo#viewCounter 再生数}<br>
     *     {@link VideoInfo#commentCounter コメント数}<br>
     *     {@link VideoInfo#myListCounter マイリス数}<br>
     *     in case of ranking, pass genre period kind params used in search.
     *     if pass {@code null}, response is interpreted to be myList.
     *     more details and valid params are {@link RankingVideoInfo available here}.<br>
     *     you can get following fields of videos;<br>
     *     {@link VideoInfo#title title of video}<br>
     *     {@link VideoInfo#id video ID}<br>
     *     {@link VideoInfo#pubDate ranking published date}<br>
     *     {@link VideoInfo#description video description}<br>
     *     {@link VideoInfo#length length}<br>
     *     {@link VideoInfo#date contributed date}<br>
     *     {@link VideoInfo#viewCounter number of view}<br>
     *     {@link VideoInfo#commentCounter number of comment}<br>
     *     {@link VideoInfo#myListCounter number of myList registered}<br>
     * @param xml response from Nico in XML, can
     * @param genre genre param, can be {@code null} in case of myList
     * @param period period param, can be {@code null} in case of myList
     * @param rankKind ranking kind param, can be {@code null} in case of myList
     * @return Returns {@code null} if no response or invalid response, and returns empty list if no hit
     */
    protected static List<VideoInfo> parse (String xml,String genre, String period, String rankKind){
        if ( xml == null ){
            //TODO
            return null;
        }
        List<VideoInfo> list = new ArrayList<VideoInfo>();
        Matcher matcher = Pattern.compile("<item>.+?</item>",Pattern.DOTALL).matcher(xml);
        while ( matcher.find() ){
            VideoInfo info = new RankingVideoInfo(matcher.group(),genre,period,rankKind);
            list.add(info);
        }
        return list;
    }
}
