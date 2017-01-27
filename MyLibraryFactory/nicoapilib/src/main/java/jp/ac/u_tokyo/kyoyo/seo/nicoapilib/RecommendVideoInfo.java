package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Seo on 2017/01/16.
 *
 * this class extending VideoInfoManager provides methods to parse XML
 * from http://flapi.nicovideo.jp/api/getrelation?page={0}&sort={1}&order={2}&video={3}
 *      query params;
 *      {0} : ? any integer
 *      {1} : sort param p:recommend
 *      {2} : order param  d:dow
 *      {3} : video ID (sm*****)
 *
 * reference;
 * how to get recommend : http://d.hatena.ne.jp/picas/20080202/1201955339
 * usage of regex : http://nobuo-create.net/seikihyougen/
 * usage of Pattern and Matcher : http://www.ne.jp/asahi/hishidama/home/tech/java/regexp.html
 *                                  http://www.javadrive.jp/regex/ref/index2.html
 *                                  http://qiita.com/ha_g1/items/d41febac011df4601544
 *                                  http://www.javadrive.jp/regex/option/index4.html
 */

public class RecommendVideoInfo extends VideoInfoManager {

    public RecommendVideoInfo (String item){
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
                        thumbnailUrl = new String[]{value};
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

    public static List<VideoInfo> parse (String xml){
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
