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
 * Created by Seo on 2017/01/15.
 *
 * this class extending VideoInfoManager provides methods to parse ranking and my list video in XML
 * from http://www.nicovideo.jp/ranking/{0}/{1}/{2}?rss=2.0
 *      params;
 *      {0} : rank kind param  (view,mylist,fav)
 *      {1} : target period param  (total,monthly,weekly,daily,hourly)
 *      {2} : target category param     list of valid params : http://dic.nicovideo.jp/a/%E3%82%AB%E3%83%86%E3%82%B4%E3%83%AA%E3%82%BF%E3%82%B0
 *
 * reference;
 * how to get ranking : https://ja.osdn.net/projects/nicolib/wiki/%E3%83%8B%E3%82%B3%E3%83%8B%E3%82%B3%E8%A7%A3%E6%9E%90%E3%83%A1%E3%83%A2
 * usage of regex : http://nobuo-create.net/seikihyougen/
 * usage of Pattern and Matcher : http://www.ne.jp/asahi/hishidama/home/tech/java/regexp.html
 *                                  http://www.javadrive.jp/regex/ref/index2.html
 *                                  http://qiita.com/ha_g1/items/d41febac011df4601544
 *                                  http://www.javadrive.jp/regex/option/index4.html
 */

public class RankingVideoInfo extends VideoInfoManager {

    //pass XML to initialize fields
    //when ranking, also pass ranking params (can be null)

    public RankingVideoInfo(String xml, String genre, String period, String rankKind){
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
                        thumbnailUrl = new String[]{value};
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

    public static List<VideoInfo> parse (String xml,String genre, String period, String rankKind){
        List<VideoInfo> list = new ArrayList<VideoInfo>();
        Matcher matcher = Pattern.compile("<item>.+?</item>",Pattern.DOTALL).matcher(xml);
        while ( matcher.find() ){
            VideoInfo info = new RankingVideoInfo(matcher.group(),genre,period,rankKind);
            list.add(info);
        }
        return list;
    }
}
