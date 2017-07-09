package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RSSで取得したニコ動のランキング情報またはマイリス情報をパースします<br>
 * This class parses ranking or myList response from Nico.<br><br>
 *
 * ニコ動ランキングAPIから取得できるフィールドは{@link #parse(CookieGroup, String)}  こちらを参照}<br>
 * You can get {@link #parse(CookieGroup, String)}  these fields} from ranking and myList API.<br><br>
 *
 * This class extending VideoInfo provides methods to parse ranking response in XML<br>
 * from http://www.nicovideo.jp/ranking/{0}/{1}/{2}?rss=2.0<br>
 *      params;<br>
 *      {0} : rank kind param  (view,mylist,fav,res)<br>
 *      {1} : target period param  (total,monthly,weekly,daily,hourly)<br>
 *      {2} : target category param     <br>
 *      list of valid params is <a href=http://dic.nicovideo.jp/a/%E3%82%AB%E3%83%86%E3%82%B4%E3%83%AA%E3%82%BF%E3%82%B0>available here; "ニコニコ大百科".</a><br>
 *          {1} and {2} can be empty or invalid value, then interpreted as default value; "daily" and "all" respectively.<br>
 *
 *
 *
 * also parse myList response in XML<br>
 *     from http://www.nicovideo.jp/mylist/{3}?rss=2.0<br>
 *     {3} : target myList ID<br><br>
 *
 * reference;<br>
 * how to get ranking : <a href=https://ja.osdn.net/projects/nicolib/wiki/%E3%83%8B%E3%82%B3%E3%83%8B%E3%82%B3%E8%A7%A3%E6%9E%90%E3%83%A1%E3%83%A2>[ニコ★リブ]ニコニコ解析メモ</a><br>
 * usage of regex : <a href=http://nobuo-create.net/seikihyougen>[一番かんたんなJava入門]【Java】正規表現って何？</a><br>
 * usage of Pattern and Matcher : <a href=http://www.ne.jp/asahi/hishidama/home/tech/java/regexp.html>Javaの正規表現</a><br>
 * <a href=http://www.javadrive.jp/regex/ref/index2.html>[JavaDrive]パターン内の括弧毎にマッチした部分文字列を取得</a><br>
 * <a href=http://qiita.com/ha_g1/items/d41febac011df4601544>[Qiita]正規表現の最短マッチ</a><br>
 * <a href=http://www.javadrive.jp/regex/option/index4.html>[JavaDrive]DOTALLモード</a><br>
 * @version 0.0 2017/01/15.
 * @author Seo-4d696b75
 */

class RSSVideoInfo extends NicoVideoInfo {

    private RSSVideoInfo(CookieGroup cookies, String xml, boolean isRanking) throws NicoAPIException.ParseException{
        super(cookies);
        initialize(xml);
    }

    private void initialize(String xml) throws NicoAPIException.ParseException{
        Map<Integer, Integer> map = new HashMap<Integer, Integer>() {
            {
                put(VideoInfo.TITLE, R.string.regex_rss_title);
                put(VideoInfo.ID, R.string.regex_rss_id);
                put(VideoInfo.DESCRIPTION, R.string.regex_rss_description);
                put(VideoInfo.THUMBNAIL_URL, R.string.regex_rss_thumbnail);
                put(VideoInfo.LENGTH, R.string.regex_rss_duration);
                put(VideoInfo.DATE, R.string.regex_rss_date);
                put(VideoInfo.VIEW_COUNTER, R.string.regex_rss_view_counter);
                put(VideoInfo.MY_LIST_COUNTER, R.string.regex_rss_myList_counter);
                put(VideoInfo.COMMENT_COUNTER, R.string.regex_rss_comment_counter);
            }
        };
        Matcher matcher,rankingMatcher;
        ResourceStore res = ResourceStore.getInstance();
        for ( int key : map.keySet() ){
            matcher = res.getPattern(map.get(key)).matcher(xml);
            if ( matcher.find() ){
                String value = matcher.group(1);
                switch ( key ){
                    case VideoInfo.TITLE:
                        //the number of ranking has to be taken away
                        rankingMatcher = res.getPattern(R.string.regex_rss_title_ranking_order).matcher(value);
                        if (rankingMatcher.find()) {
                            value = rankingMatcher.group(1);
                        } else {
                            throw new NicoAPIException.ParseException("title format is not expected > ranking"
                                    , value, NicoAPIException.EXCEPTION_PARSE_RANKING_TITLE);
                        }

                        title = value;
                        break;
                    case VideoInfo.ID:
                        setID(value);
                        break;
                    case VideoInfo.DESCRIPTION:
                        description = value;
                        break;
                    case VideoInfo.THUMBNAIL_URL:
                        thumbnailUrl = value;
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
            }else{
                throw new NicoAPIException.ParseException(
                        "target partial sequence matched with \"" + matcher.pattern().pattern() + "\" not found",
                        xml,NicoAPIException.EXCEPTION_PARSE_RANKING_MYLIST_NOT_FOUND
                );
            }
        }
    }

    private static int parseCounter(String value) throws NicoAPIException.ParseException{
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        try{
            return numberFormat.parse(value).intValue();
        }catch (ParseException e){
            throw new NicoAPIException.ParseException(e.getMessage(),value,NicoAPIException.EXCEPTION_PARSE_RANKING_MYLIST_COUNTER);
        }
    }

    protected static int parseLength (String value) throws NicoAPIException.ParseException{
        Matcher matcher = ResourceStore.getInstance().getPattern(R.string.regex_rss_duration_format).matcher(value);
        if ( matcher.find() ) {
            int min = Integer.parseInt(matcher.group(1));
            int sec = Integer.parseInt(matcher.group(2));
            return min * 60 + sec;
        }else{
            throw new NicoAPIException.ParseException("video length not following required format",value,NicoAPIException.EXCEPTION_PARSE_RANKING_MYLIST_LENGTH);
        }
    }

    protected static Date convertDate (String date) throws NicoAPIException.ParseException{
        try{
            String format = ResourceStore.getInstance().getString(R.string.format_rss_date);
            return new SimpleDateFormat(format,Locale.JAPAN).parse(date);
        }catch (ParseException e){
            throw new NicoAPIException.ParseException(e.getMessage(),date,NicoAPIException.EXCEPTION_PARSE_RANKING_MYLIST_DATE);
        }
    }

    /**
     * ニコ動APIから取得したXML形式のランキング情報をパースします<br>
     * Parses xml text from Nico API about ranking.<br>
     * APIの詳細や有効なパラメータ、レスポンスの形式は{@link RSSVideoInfo ここから参照}できます。<br>
     * 取得できる動画のフィールドは以下の通りです。<br>
     *     動画タイトル<br>
     *     動画ID<br>
     *     動画説明<br>
     *     動画長さ<br>
     *     動画投稿日時<br>
     *     再生数<br>
     *     コメント数<br>
     *     マイリス数<br>
     *     More details about valid params and response format are {@link RSSVideoInfo available here}.<br>
     *     You can get following fields of videos;<br>
     *     title of video<br>
     *     video ID<br>
     *     video description<br>
     *     length<br>
     *     contributed date<br>
     *     number of view<br>
     *     number of comment<br>
     *     number of myList registered<br>
     * @param xml the response from Nico in XML, cannot be {@code null}
     * @return Returns empty list if no hit, not {@code null}
     * @throws NicoAPIException if invalid response not following required format
     */
    protected static List<VideoInfo> parse (CookieGroup cookies, String xml) throws NicoAPIException{
        if ( xml == null ){
            throw new NicoAPIException.ParseException(
                    "parse target is null",null,
                    NicoAPIException.EXCEPTION_PARSE_RANKING_NO_TARGET);
        }
        List<VideoInfo> list = new ArrayList<VideoInfo>();
        Matcher matcher = ResourceStore.getInstance().getPattern(R.string.regex_rss_item).matcher(xml);
        while ( matcher.find() ){
            VideoInfo info = new RSSVideoInfo(cookies, matcher.group(), true);
            list.add(info);
        }
        return list;
    }

}
