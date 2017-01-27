package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.graphics.drawable.Drawable;
import android.util.Log;

import org.apache.http.client.CookieStore;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Seo on 2017/01/15.
 *
 * this class extending VideoInfo provides additional utilities
 *
 * reference;
 * format of ISO8601 : https://ja.wikipedia.org/wiki/ISO_8601
 *                      http://d.hatena.ne.jp/drambuie/20110219/p1
 * usage of Date.class: http://www.javaroad.jp/java_date1.htm
 * usage of SimpleDateFormat.class : http://www.javaroad.jp/java_date3.htm,
 *                                  http://java-reference.sakuraweb.com/java_date_format.html
 *                                  http://d.hatena.ne.jp/rudi/20101201/1291214680
 * usage of NumberFormat.class : http://java-reference.sakuraweb.com/java_number_format.html
 * algorithm of alignment :  東京大学出版会　情報科学入門 ISBN978-4-13-062452-7
 *
 */

public class VideoInfoManager extends VideoInfo {

    public VideoInfoManager(){}

    //url from which details of video you can get
    private String thumbUrl = "http://ext.nicovideo.jp/api/getthumbinfo/";
    private String flvUrl = "http://flapi.nicovideo.jp/api/getflv/";
    //Pattern to extract value from plain text
    private final int STATUS = 100;
    private final Map<Integer,Pattern> patternMap = new HashMap<Integer,Pattern>(){
        {
            put(STATUS,Pattern.compile("<nicovideo_thumb_response status=\"(.+?)\">"));
            put(VideoInfo.THUMBNAIL_URL,Pattern.compile("<thumbnail_url>(.+?)</thumbnail_url>"));
            put(VideoInfo.DATE,Pattern.compile("<first_retrieve>(.+?)</first_retrieve>"));
            put(VideoInfo.VIEW_COUNTER,Pattern.compile("<view_counter>(.+?)</view_counter>"));
            put(VideoInfo.COMMENT_COUNTER,Pattern.compile("<comment_num>(.+?)</comment_num>"));
            put(VideoInfo.MY_LIST_COUNTER,Pattern.compile("<mylist_counter>(.+?)</mylist_counter>"));
            put(VideoInfo.TAGS,Pattern.compile("<tags domain=\"jp\">(.+?)</tags>",Pattern.DOTALL));
            put(VideoInfo.TAG,Pattern.compile("<tag.*>(.+?)</tag>"));
            put(VideoInfo.CONTRIBUTOR_ID,Pattern.compile("<user_id>([0-9]+?)</user_id>"));
            put(VideoInfo.CONTRIBUTOR_NAME,Pattern.compile("<user_nickname>(.+?)</user_nickname>"));
            put(VideoInfo.CONTRIBUTOR_ICON_URL,Pattern.compile("<user_icon_url>(.+?)</user_icon_url>"));
            put(VideoInfo.THREAD_ID,Pattern.compile("thread_id=([0-9]+?)&"));
            put(VideoInfo.FLV_URL,Pattern.compile("url=(.+?)&"));
            put(VideoInfo.MESSAGE_SERVER_URL,Pattern.compile("ms=(.+?)&"));
        }
    };
     //basic date format in this app, based on ISO 8601
    public static final SimpleDateFormat dateFormatBase = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ");

    //some fields of Nico video may be empty, so this complete them
    public boolean complete(){
        String path = thumbUrl + id;
        HttpResponseGetter getter= new HttpResponseGetter();
        if ( !getter.tryGet(path) ){
            return false;
        }
        String res = getter.response;
        try {
            if ( !extract(res,STATUS).equals("ok") ){
                return false;
            }
            String target;
            int num;
            if ( thumbnailUrl == null){
                target = extract(res,VideoInfo.THUMBNAIL_URL);
                if ( target == null ){
                    return false;
                }
                setThumbnailUrl(target);
            }
            if ( date == null){
                target = extract(res,VideoInfo.DATE);
                if ( target == null ){
                    return false;
                }
                target = convertDate(target);
                date = target;
            }
            if ( contributorName == null){
                target = extract(res,VideoInfo.CONTRIBUTOR_NAME);
                if ( target == null ){
                    return false;
                }
                contributorName = target;
            }
            if ( contributorIconUrl == null){
                target = extract(res,VideoInfo.CONTRIBUTOR_ICON_URL);
                if ( target == null ){
                    return false;
                }
                contributorIconUrl = target;
            }
            if ( viewCounter < 0 ){
                target = extract(res,VideoInfo.VIEW_COUNTER);
                num = Integer.parseInt(target);
                if ( num < 0 ){
                    return false;
                }
                viewCounter = num;
            }
            if ( commentCounter < 0 ){
                target = extract(res,VideoInfo.COMMENT_COUNTER);
                num = Integer.parseInt(target);
                if ( num < 0 ){
                    return false;
                }
                commentCounter = num;
            }
            if ( myListCounter < 0){
                target = extract(res,VideoInfo.MY_LIST_COUNTER);
                num = Integer.parseInt(target);
                if ( num < 0 ){
                    return false;
                }
                myListCounter = num;
            }
            if ( contributorID <= 0 ){
                target = extract(res,VideoInfo.CONTRIBUTOR_ID);
                num = Integer.parseInt(target);
                if ( num < 0 ){
                    return false;
                }
                contributorID = num;
            }
            if ( tags == null ){
                target = extract(res,VideoInfo.TAGS);
                String[] tags = parseTags(target,VideoInfo.TAG);
                if ( tags == null || tags.length == 0 ){
                    return false;
                }
                setTags(tags);
            }
            return true;
        } catch (NullPointerException e){
            e.printStackTrace();
            return false;
        }
    }

    private String extract (String res, int key){
        if ( patternMap.containsKey(key) ) {
            Matcher matcher = patternMap.get(key).matcher(res);
            if ( matcher.find() ){
                return matcher.group(1);
            }
        }
        return null;
    }

    private String[] parseTags(String res, int key){
        if ( patternMap.containsKey(key) ){
            Matcher matcher = patternMap.get(key).matcher(res);
            List<String> list = new ArrayList<String>();
            while ( matcher.find() ){
                list.add(matcher.group(1));
            }
            String[] tags = new String[list.size()];
            for ( int i=0 ; i<tags.length ; i++){
                tags[i] = list.get(i);
            }
            return tags;
        }
        return null;
    }

    private String convertDate (String date){
        date = date.substring(0,date.length()-3) + "00";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        try {
            return dateFormatBase.format(dateFormat.parse(date));
        }catch (ParseException e){
            e.printStackTrace();
        }
        return null;
    }

    public boolean getFlv (CookieStore cookieStore){
        String path = flvUrl + id;
        HttpResponseGetter getter = new HttpResponseGetter();
        if ( !getter.tryGet(path,cookieStore)){
            return false;
        }
        String res = getter.response;
        String target;
        if ( threadID == null ){
            target = extract(res,VideoInfo.THREAD_ID);
            if ( target == null ){
                return false;
            }
            threadID = URLDecoder.decode(target);
        }
        if ( messageServerUrl == null ){
            target = extract(res,VideoInfo.MESSAGE_SERVER_URL);
            if ( target == null ){
                return false;
            }
            messageServerUrl = URLDecoder.decode(target);
        }
        if ( flvUrl == null ){
            target = extract(res,VideoInfo.FLV_URL);
            if ( target == null ){
                return false;
            }
            flvUrl = URLDecoder.decode(target);
        }
        return true;
    }

    public Drawable getThumbnail (){
        return getThumbnail(false);
    }
    public Drawable getThumbnail (boolean isHigh){
        if ( thumbnail == null ){
            String path = getThumbnailUrl(isHigh);
            if ( path == null ){
                if ( complete() ){
                    path = getThumbnailUrl(isHigh);
                }else{
                    return null;
                }
            }
            thumbnail = getDrawable(path);
        }
        return thumbnail;
    }

    public Drawable getContributorIcon(){
        if ( contributorIcon == null ) {
            if (contributorIconUrl == null) {
                if (!complete()) {
                    return null;
                }
            }
            contributorIcon = getDrawable(contributorIconUrl);
        }
        return contributorIcon;
    }

    private Drawable getDrawable (String path){
        if ( path != null ){
            try {
                URL url = new URL(path);
                InputStream input = (InputStream) url.getContent();
                Drawable image = Drawable.createFromStream(input, "thumbnail");
                input.close();
                return image;
            }catch (MalformedURLException e){
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     *
     * memo;
     * 2016/12/28
     動画長さ
     nico ranking/myList(xml):	String.format("%d:%02d",min,sec)
     nico search(Json):	sec
     nico tempMyList(Json):	"sec" //String型
     youtube:	String.format("PT%dM%dS",min,sec)

     カウント数
     nico myList/ranking(xml):	NumberFormat.getNumberInstance().format(counter)  //三桁区切りに半角コンマ
     nico search (Json):	counter
     nico tempMyList(Json):	"counter"//String
     youtube:	"counter"

     日時
     nico ranking/myList pubDate(xml):	SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z",Locale.ENGLISH)//月と曜日は英略表記,間に半角スペース

     nico ranking/myLilst upDate(xml):	SimpleDateFormat("yyyy'年'MM'月'dd'日' HH：mm：ss")//間に半角スペース,全角コンマあり
     nico tempMyList(Json):	UnixTime(sec)
     nico search(Json):	SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")//タイムゾーンはHH:mm、間の半角コロンあり
     youtube:	 ISO 8601 SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")//常にUTC
     //parse時はsetTimeZone(TimeZone.getTimeZone("UTC"))でタイムゾーンを明示的に指定
     */


    public String formatLength(){
        return String.format("%01d:%02d",length/60,length%60);
    }

    public String formatCounter(int key){
        int counter = 0;
        switch ( key ){
            case VideoInfo.VIEW_COUNTER:
                counter = viewCounter;
                break;
            case VideoInfo.COMMENT_COUNTER:
                counter = commentCounter;
                break;
            case VideoInfo.MY_LIST_COUNTER:
                counter = myListCounter;
                break;
            default:
                Log.d("VideoManager","formatCounter : invalid key");
        }
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        return numberFormat.format(counter);
    }

    public Date parseDate(){
        try{
            return dateFormatBase.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    //convert to instance of rapent class
    /*
    public VideoInfo upCast(){
        VideoInfo info = new VideoInfo();
        info.setString(VideoInfo.GENRE,genre);
        info.setString(VideoInfo.RANK_KIND,rankKind);
        info.setString(VideoInfo.PERIOD,period);
        info.setString(VideoInfo.PUB_DATE,pubDate);
        info.setString(VideoInfo.TITLE,title);
        info.setString(VideoInfo.ID,id);
        info.setString(VideoInfo.DATE,date);
        info.setString(VideoInfo.DESCRIPTION,description);
        info.setThumbnailUrl(thumbnailUrl);
        info.setInt(VideoInfo.LENGTH,length);
        info.setInt(VideoInfo.VIEW_COUNTER,viewCounter);
        info.setInt(VideoInfo.COMMENT_COUNTER,commentCounter);
        info.setInt(VideoInfo.MY_LIST_COUNTER,myListCounter);
        info.setTags(tags);
        return info;
    }*/

    //list of em punctuation
    private String emPunct = "・：；´｀¨＾￣＿〆〇―‐／＼～∥｜…‥‘’“”（）〔〕［］｛｝〈〉《》「」『』【】＜＞≦≧＃＆＠§☆★○●◎◇◆□■△▲▽▼※〒→←↑↓〓∈∋⊆⊇⊂⊃∪∩∧∨￢⇒⇔∀∃∠⊥≪≫♯";

    //remove punctuation and split title
    public String[] splitTitle(){
        Pattern delimiter = Pattern.compile(String.format("\\s*[\\p{Punct}%s]+\\s*",emPunct),0);
        Pattern space = Pattern.compile("[\\s]+");
        Pattern alphabets = Pattern.compile("[A-z,.;:!?']+");
        String[] tokens =  delimiter.split(title);
        List<String> list = new ArrayList<String>();
        for ( String token : tokens ){
            String[] parts = space.split(token);
            String target = "";
            for ( String part : parts ){
                if ( alphabets.matcher(part).matches() ){
                    target += part;
                }else{
                    if ( !target.isEmpty() ){
                        list.add(target);
                        target = "";
                    }else{
                        list.add(part);
                    }
                }
            }
            if ( !target.isEmpty() ) {
                list.add(target);
            }
        }
        tokens = new String[list.size()];
        for ( int i=0 ; i<tokens.length ; i++){
            tokens[i] = list.get(i);
        }
        return tokens;
    }

    //how much this video is close to target video?
    public void analyze(VideoInfoManager target,String[] keyWords){
        if ( target.getTags() == null ){
            if ( !target.complete() ){
                return;
            }
        }
        float titleP = compareTags(keyWords,splitTitle());
        float lengthP = compareLength(target.getInt(VideoInfo.LENGTH),length);
        float dateP = compareDate(target.parseDate(), parseDate());
        float tagsP = compareTags(target.getTags(),getTags());
        point = 0.3f*titleP + 0.3f*lengthP + 0.1f*dateP + 0.3f*tagsP;
    }

    private int match = 2;
    private int misMatch = -1;
    private int gap = -2;
    private double base = 0.3;
    private double base1 = 5.0;
    private double base2 = -0.003;
    private float compareString(String a, String b){
        int allMatch = match * Math.max(a.length(), b.length());
        int m = a.length();
        int n = b.length();
        int[][] array = new int[m+1][n+1];
        array[0][0] = 0;
        for ( int i=1 ; i<m+1 ; i++){
            array[i][0] = array[i-1][0] + gap;
        }
        for ( int i=1 ; i<n+1 ; i++){
            array[0][i] = array[0][i-1] + gap;
        }
        for ( int i=1 ; i<m+1 ; i++){
            for ( int k=1 ; k<n+1 ; k++){
                array[i][k] = Math.max(
                        array[i-1][k-1]+compareCharacter(a.charAt(i-1),b.charAt(k-1)),
                        Math.max(array[i-1][k]+gap,array[i][k-1]+gap)
                );
            }
        }
        return ((float)array[m][n]/allMatch+1.0f)/2.0f;
    }
    private float compareTags(String[] a, String[] b){
        if ( a == null || b == null ){
            return 0f;
        }
        int m = Math.max(a.length, b.length);
        int p = 0;
        for ( String aa : a ){
            for ( String bb : b){
                if ( compareString(aa,bb) > 0.8f ){
                    p++;
                }
            }
        }
        return (float)p/m;
    }
    private int compareCharacter(Character a, Character b){
        if ( a.equals(b)){
            return match;
        }
        return misMatch;
    }
    private float compareLength(int a, int b){
        return (float)Math.pow(base,(double)Math.abs(a-b)/a);
    }

    private float compareDate(Date a, Date b){
        double dig = (double)(b.getTime()-a.getTime())/(1000*60*60*24);
        float p;
        if ( dig < 0.0 ){
            p = (float)Math.pow(base1, dig);
        }else{
            p =  (float)(1.0 + base2*dig);
            if ( p < 0.0f ){
                p = 0.0f;
            }
        }
        return p;
    }

}
