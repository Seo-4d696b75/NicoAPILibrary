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
 * RSSで取得したニコ動のランキング情報またはマイリス情報をパースします<br>
 * This class parses ranking or myList response from Nico.<br><br>
 *
 * ニコ動ランキングAPIから取得できるフィールドは{@link #parse(String)}  こちらを参照}<br>
 * You can get {@link #parse(String)}  these fields} from ranking and myList API.<br><br>
 *
 * This class extending VideoInfo provides methods to parse ranking response in XML<br>
 * from http://www.nicovideo.jp/ranking/{0}/{1}/{2}?rss=2.0<br>
 *      params;<br>
 *      {0} : rank kind param  (view,mylist,fav,res)<br>
 *      {1} : target period param  (total,monthly,weekly,daily,hourly)<br>
 *      {2} : target category param     <br>
 *      list of valid params is <a href=http://dic.nicovideo.jp/a/%E3%82%AB%E3%83%86%E3%82%B4%E3%83%AA%E3%82%BF%E3%82%B0>available here; "ニコニコ大百科".</a><br>
 *          {1} and {2} can be empty or invalid value, then interpreted as default value; "daily" and "all" respectively.<br>
 * expected response format;<br>
 * <pre>
 * &lt;?xml version="1.0" encoding="utf-8"?&gt;
 *     &lt;rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom"&gt;
 *         &lt;channel&gt;
 *             &lt;title&gt;カテゴリ合算の再生ランキング(24時間)‐ニコニコ動画&lt;/title&gt;
 *             &lt;link&gt;http://www.nicovideo.jp/ranking/view/daily/all&lt;/link&gt;
 *             &lt;description&gt;毎時更新&lt;/description&gt;
 *             &lt;pubDate&gt;Thu, 02 Feb 2017 20:00:00 +0900&lt;/pubDate&gt;
 *             &lt;lastBuildDate&gt;Thu, 02 Feb 2017 20:00:00 +0900&lt;/lastBuildDate&gt;
 *             &lt;generator&gt;ニコニコ動画&lt;/generator&gt;
 *             &lt;language&gt;ja-jp&lt;/language&gt;
 *             &lt;copyright&gt;(c) DWANGO Co., Ltd.&lt;/copyright&gt;
 *             &lt;docs&gt;http://blogs.law.harvard.edu/tech/rss&lt;/docs&gt;
 *             &lt;atom:link rel="self" type="application/rss+xml" href="http://www.nicovideo.jp/ranking/view//?rss=2.0"/&gt;
 *             &lt;item&gt;
 *                 &lt;title&gt;第1位：この素晴らしい世界に祝福を！２　第3話「この迷宮の主に安らぎを！」&lt;/title&gt;
 *                 &lt;link&gt;http://www.nicovideo.jp/watch/so30539458&lt;/link&gt;
 *                 &lt;guid isPermaLink="false"&gt;tag:nicovideo.jp,2017-02-02:/watch/so30539458&lt;/guid&gt;
 *                 &lt;pubDate&gt;Thu, 02 Feb 2017 20:00:00 +0900&lt;/pubDate&gt;
 *                 &lt;description&gt;&lt;![CDATA[
 *                 &lt;p class="nico-thumbnail"&gt;&lt;img alt="この素晴らしい世界に祝福を！２　第3話「この迷宮の主に安らぎを！」" src="http://tn-skr3.smilevideo.jp/smile?i=30539458" width="94" height="70" border="0"/&gt;&lt;/p&gt;
 *                 &lt;p class="nico-description"&gt;「ふふん。この私が誰だか忘れてない？」アクアの浪費に賠償金……。&lt;/p&gt;
 *                 &lt;p class="nico-info"&gt;
 *                     &lt;small&gt;
 *                         &lt;strong class="nico-info-number"&gt;137,886&lt;/strong&gt;pts.｜
 *                         &lt;strong class="nico-info-length"&gt;23:40&lt;/strong&gt;｜
 *                         &lt;strong class="nico-info-date"&gt;2017年02月02日 01：00：00&lt;/strong&gt; 投稿&lt;br/&gt;&lt;strong&gt;合計&lt;/strong&gt;&nbsp;&#x20;再生：
 *                         &lt;strong class="nico-info-total-view"&gt;137,886&lt;/strong&gt;&nbsp;&#x20;コメント：
 *                         &lt;strong class="nico-info-total-res"&gt;8,807&lt;/strong&gt;&nbsp;&#x20;マイリスト：
 *                         &lt;strong class="nico-info-total-mylist"&gt;1,594&lt;/strong&gt;&lt;br/&gt;&lt;strong&gt;日間&lt;/strong&gt;&nbsp;&#x20;再生：
 *                         &lt;strong class="nico-info-daily-view"&gt;137,886&lt;/strong&gt;&nbsp;&#x20;コメント：
 *                         &lt;strong class="nico-info-daily-res"&gt;8,761&lt;/strong&gt;&nbsp;&#x20;マイリスト：
 *                         &lt;strong class="nico-info-daily-mylist"&gt;1,594&lt;/strong&gt;&lt;br/&gt;
 *                     &lt;/small&gt;&lt;/p&gt;
 *                 ]]&gt;&lt;/description&gt;
 *             &lt;/item&gt;
 *             ...............
 *         &lt;/channel&gt;
 *     &lt;/rss&gt;
 * </pre><br><br>
 *
 *
 * also parse myList response in XML<br>
 *     from http://www.nicovideo.jp/mylist/{3}?rss=2.0<br>
 *     {3} : target myList ID<br><br>
 *
 * expected response format;<br>
 * <pre>
 * &lt;?xml version="1.0" encoding="utf-8"?&gt;
 * &lt;rss version="2.0"
 *  xmlns:dc="http://purl.org/dc/elements/1.1/"
 *  xmlns:atom="http://www.w3.org/2005/Atom"&gt;
 *  &lt;channel&gt;
 *      &lt;title&gt;マイリスト {nameOfYourMyList}‐ニコニコ動画&lt;/title&gt;
 *      &lt;link&gt;http://www.nicovideo.jp/mylist/{myListID}&lt;/link&gt;
 *      &lt;atom:link rel="self" type="application/rss+xml" href="http://www.nicovideo.jp/mylist/{myListID}?rss=2.0"/&gt;
 *      &lt;description&gt;&lt;/description&gt;
 *      &lt;pubDate&gt;Sat, 01 Aug 2015 19:54:33 +0900&lt;/pubDate&gt;
 *      &lt;lastBuildDate&gt;Sat, 01 Aug 2015 19:54:33 +0900&lt;/lastBuildDate&gt;
 *      &lt;generator&gt;ニコニコ動画&lt;/generator&gt;
 *      &lt;dc:creator&gt;{userName}&lt;/dc:creator&gt;
 *      &lt;language&gt;ja-jp&lt;/language&gt;
 *      &lt;copyright&gt;(c) DWANGO Co., Ltd.&lt;/copyright&gt;
 *      &lt;docs&gt;http://blogs.law.harvard.edu/tech/rss&lt;/docs&gt;
 *      &lt;item&gt;
 *          &lt;title&gt;【初音ミク】ヒビカセ【オリジナル】&lt;/title&gt;
 *          &lt;link&gt;http://www.nicovideo.jp/watch/sm24536934&lt;/link&gt;
 *          &lt;guid isPermaLink="false"&gt;tag:nicovideo.jp,2014-09-23:/watch/1411482212&lt;/guid&gt;
 *          &lt;pubDate&gt;Sat, 01 Aug 2015 19:55:06 +0900&lt;/pubDate&gt;
 *          &lt;description&gt;
 *              &lt;![CDATA[&lt;p class="nico-thumbnail"&gt;&lt;img alt="【初音ミク】ヒビカセ【オリジナル】" src="http://tn-skr3.smilevideo.jp/smile?i=24536934" width="94" height="70" border="0"/&gt;&lt;/p&gt;
 *              &lt;p class="nico-description"&gt;ノ(-.-)?　　　れをるVer sm24536932　　　Music/Vocal Edit :ギガ.......&lt;/p&gt;
 *              &lt;p class="nico-info"&gt;
 *                  &lt;small&gt;
 *                      &lt;strong class="nico-info-length"&gt;4:15&lt;/strong&gt;｜
 *                      &lt;strong class="nico-info-date"&gt;2014年09月24日 00：00：00&lt;/strong&gt; 投稿
 *                  &lt;/small&gt;
 *              &lt;/p&gt;]]&gt;
 *          &lt;/description&gt;
 *      &lt;/item&gt;
 *      .........
 *  &lt;/channel&gt;
 * &lt;/rss&gt;
 * </pre>
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

public class RSSVideoInfo extends VideoInfo {

    private RSSVideoInfo(String xml, boolean isRanking) throws NicoAPIException.ParseException{
        initialize(this, xml, isRanking);
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

    protected static void initialize(VideoInfo info, String xml, boolean ranking) throws NicoAPIException.ParseException{
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
                            }else{
                                throw new NicoAPIException.ParseException("title format is not expected > ranking"
                                        ,value,NicoAPIException.EXCEPTION_PARSE_RANKING_TITLE);
                            }
                        }
                        info.title = value;
                        break;
                    case VideoInfo.ID:
                        info.setID(value);
                        break;
                    case VideoInfo.DESCRIPTION:
                        info.description = value;
                        break;
                    case VideoInfo.PUB_DATE:
                        info.pubDate = convertPubDate(value);
                        break;
                    case VideoInfo.THUMBNAIL_URL:
                        info.setThumbnailUrl(value);
                        break;
                    case VideoInfo.LENGTH:
                        info.length = parseLength(value);
                        break;
                    case VideoInfo.DATE:
                        info.date = convertDate(value);
                        break;
                    case VideoInfo.VIEW_COUNTER:
                        info.viewCounter = parseCounter(value);
                        break;
                    case VideoInfo.COMMENT_COUNTER:
                        info.commentCounter = parseCounter(value);
                        break;
                    case VideoInfo.MY_LIST_COUNTER:
                        info.myListCounter = parseCounter(value);
                        break;
                    default:
                }
            }else{
                if ( !ranking ){
                    switch ( key ){
                        case VideoInfo.COMMENT_COUNTER:
                        case VideoInfo.MY_LIST_COUNTER:
                        case VideoInfo.VIEW_COUNTER:
                            continue;
                        default:
                    }
                }
                throw new NicoAPIException.ParseException(
                        "target partial sequence matched with \"" + matcher.pattern().pattern() + "\" not found",
                        xml,NicoAPIException.EXCEPTION_PARSE_RANKING_MYLIST_NOT_FOUND
                );
            }
        }
        if ( !ranking ){
            info.complete();
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

    private static int parseLength (String value) throws NicoAPIException.ParseException{
        Matcher matcher = Pattern.compile("([0-9]+):([0-9]+)").matcher(value);
        if ( matcher.find() ) {
            int min = Integer.parseInt(matcher.group(1));
            int sec = Integer.parseInt(matcher.group(2));
            return min * 60 + sec;
        }else{
            throw new NicoAPIException.ParseException("video length not following required format",value,NicoAPIException.EXCEPTION_PARSE_RANKING_MYLIST_LENGTH);
        }
    }

    private static String convertPubDate (String date) throws NicoAPIException.ParseException{
        try{
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
            return dateFormatBase.format(dateFormat.parse(date));
        }catch (ParseException e){
            throw new NicoAPIException.ParseException(e.getMessage(), date,NicoAPIException.EXCEPTION_PARSE_RANKING_MYLIST_PUB_DATE);
        }
    }

    private static String convertDate (String date) throws NicoAPIException.ParseException{
        try{
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy'年'MM'月'dd'日' HH：mm：ss");
            return dateFormatBase.format(dateFormat.parse(date));
        }catch (ParseException e){
            throw new NicoAPIException.ParseException(e.getMessage(),date,NicoAPIException.EXCEPTION_PARSE_RANKING_MYLIST_DATE);
        }
    }

    /**
     * ニコ動APIから取得したXML形式のランキングおよびマイリス情報をパースします<br>
     * Parses xml text from Nico API about ranking or myList.<br>
     * APIの詳細や有効なパラメータ、レスポンスの形式は{@link RSSVideoInfo ここから参照}できます。<br>
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
     *     More details about valid params and response format are {@link RSSVideoInfo available here}.<br>
     *     You can get following fields of videos;<br>
     *     {@link VideoInfo#title title of video}<br>
     *     {@link VideoInfo#id video ID}<br>
     *     {@link VideoInfo#pubDate ranking published date}<br>
     *     {@link VideoInfo#description video description}<br>
     *     {@link VideoInfo#length length}<br>
     *     {@link VideoInfo#date contributed date}<br>
     *     {@link VideoInfo#viewCounter number of view}<br>
     *     {@link VideoInfo#commentCounter number of comment}<br>
     *     {@link VideoInfo#myListCounter number of myList registered}<br>
     * @param xml the response from Nico in XML, cannot be {@code null}
     * @return Returns empty list if no hit, not {@code null}
     * @throws NicoAPIException if invalid response not following required format
     */
    protected static List<VideoInfo> parse (String xml) throws NicoAPIException{
        if ( xml == null ){
            throw new NicoAPIException.ParseException(
                    "parse target is null",null,
                    NicoAPIException.EXCEPTION_PARSE_RANKING_NO_TARGET);
        }
        checkResponse(xml);
        List<VideoInfo> list = new ArrayList<VideoInfo>();
        Matcher matcher = Pattern.compile("<item>.+?</item>",Pattern.DOTALL).matcher(xml);
        while ( matcher.find() ){
            VideoInfo info = new RSSVideoInfo(matcher.group(), true);
            list.add(info);
        }
        return list;
    }

    private static void checkResponse(String xml) throws NicoAPIException{
        Matcher matcher = Pattern.compile("<channel>.+<title>.+?ランキング.+?‐ニコニコ動画</title>.+</channel>",Pattern.DOTALL).matcher(xml);
        if ( matcher.find() ){
            return;
        }else{
            throw new NicoAPIException.APIUnexpectedException(
                    "not ranking RSS > ranking",
                    NicoAPIException.EXCEPTION_UNEXPECTED_RANKING
            );
        }
    }
}
