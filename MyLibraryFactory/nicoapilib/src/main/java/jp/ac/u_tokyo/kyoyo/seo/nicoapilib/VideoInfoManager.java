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
 *
 * {@link VideoInfo}に関する追加メソッドを提供します<br>
 * This class extending {@link VideoInfo} provides additional utilities.<br><br>
 *
 * In {@link #complete()}, getThumbnail API is used (you need no login).
 * This method parses the response <br>
 * from http://ext.nicovideo.jp/api/getthumbinfo/{videoID}.<br><br>
 *
 * respected response format;<br>
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 *     &lt;nicovideo_thumb_response status="ok"&gt;
 *         &lt;thumb&gt;
 *             &lt;video_id&gt;sm9&lt;/video_id&gt;
 *             &lt;title&gt;新・豪血寺一族 -煩悩解放 - レッツゴー！陰陽師&lt;/title&gt;
 *             &lt;description&gt;レッツゴー！陰陽師（フルコーラスバージョン）&lt;/description&gt;
 *             &lt;thumbnail_url&gt;http://tn-skr2.smilevideo.jp/smile?i=9&lt;/thumbnail_url&gt;
 *             &lt;first_retrieve&gt;2007-03-06T00:33:00+09:00&lt;/first_retrieve&gt;
 *             &lt;length&gt;5:19&lt;/length&gt;
 *             &lt;movie_type&gt;flv&lt;/movie_type&gt;
 *             &lt;size_high&gt;21138631&lt;/size_high&gt;
 *             &lt;size_low&gt;17436492&lt;/size_low&gt;
 *             &lt;view_counter&gt;16171959&lt;/view_counter&gt;
 *             &lt;comment_num&gt;4452339&lt;/comment_num&gt;
 *             &lt;mylist_counter&gt;166984&lt;/mylist_counter&gt;
 *             &lt;last_res_body&gt;...........&lt;/last_res_body&gt;
 *             &lt;watch_url&gt;http://www.nicovideo.jp/watch/sm9&lt;/watch_url&gt;
 *             &lt;thumb_type&gt;video&lt;/thumb_type&gt;
 *             &lt;embeddable&gt;1&lt;/embeddable&gt;
 *             &lt;no_live_play&gt;0&lt;/no_live_play&gt;
 *             &lt;tags domain="jp"&gt;
 *                 &lt;tag lock="1"&gt;陰陽師&lt;/tag&gt;
 *                 &lt;tag lock="1"&gt;レッツゴー！陰陽師&lt;/tag&gt;
 *                 &lt;tag lock="1"&gt;公式&lt;/tag&gt;
 *                 &lt;tag lock="1"&gt;音楽&lt;/tag&gt;
 *                 &lt;tag lock="1"&gt;ゲーム&lt;/tag&gt;
 *                 &lt;tag&gt;空耳&lt;/tag&gt;
 *                 &lt;tag&gt;sm9&lt;/tag&gt;
 *                 &lt;tag&gt;ニコニコ忘年会&lt;/tag&gt;
 *                 &lt;tag&gt;全ての元凶&lt;/tag&gt;
 *             &lt;/tags&gt;
 *             &lt;user_id&gt;4&lt;/user_id&gt;
 *             &lt;user_nickname&gt;運営長の中の人&lt;/user_nickname&gt;
 *             &lt;user_icon_url&gt;https://secure-dcdn.cdn.nimg.jp/nicoaccount/usericon/s/0/4.jpg?1271141672&lt;/user_icon_url&gt;
 *         &lt;/thumb&gt;
 *     &lt;/nicovideo_thumb_response&gt;
 * </pre><br><br>
 *
 * In {@link #getFlv(CookieStore)}, getFlv API is used (you need login).
 * This method parses the response <br>
 * from http://flapi.nicovideo.jp/api/getflv/{videoID}.<br><br>
 *
 * respected response format;<br>
 * thread_id=1173206704
 * &l=111
 * &url=http://smile-pcm31.nicovideo.jp/smile?v=8702.9279
 * &link=http://www.smilevideo.jp/view/8702/573999
 * &ms=http://msg.nicovideo.jp/7/api/
 * &user_id={userIDofContributor}
 * &is_premium=1
 * &nickname={userNameOfContributor}
 * &time=1282199207
 * &done=true
 * &feedrev=b852b
 * &ng_up=............&.....&....&.....
 * &hms=hiroba07.nicovideo.jp
 * &hmsp=2529
 * &hmst=1000000125
 * &hmstk=1282199267.MpPPtW9DzfDKV3sgSiFOIaEjp6o
 * &rpu={
 *  "count":928779,
 *  "users":[
 *      "userName1",
 *      "userName2",
 *      ...... ,
 *      ],
 *  "extra":0
 * }
 * <pre>
 * </pre><br><br>
 *
 * reference;<br>
 * format of ISO8601 : <a href=https://ja.wikipedia.org/wiki/ISO_8601>[Wikipedia]ISO 8601</a><br>
 *                      <a href=http://d.hatena.ne.jp/drambuie/20110219/p1>[Hatena::Diary]JavaでのISO 8601形式の日時の処理</a><br>
 * usage of Date.class: <a href=http://www.javaroad.jp/java_date1.htm>[Javaの道]Dateクラス</a><br>
 * usage of SimpleDateFormat.class : <a href=http://www.javaroad.jp/java_date3.htm>[Javaの道]SimpleDateFormatクラス</a><br>
 *                                  <a href=http://java-reference.sakuraweb.com/java_date_format.html>[Javaちょこっとリファレンス]日付をフォーマットする</a><br>
 *                                  <a href=http://d.hatena.ne.jp/rudi/20101201/1291214680>[Hatena::Diary]SimpleDateFormatでEEEやMMMがparseできない</a><br>
 * usage of NumberFormat.class : <a href=http://java-reference.sakuraweb.com/java_number_format.html>[Javaちょこっとリファレンス]数値をフォーマットする</a><br>
 * algorithm of alignment :  東京大学出版会　情報科学入門 ISBN978-4-13-062452-7<br>
 * how to use getFlv API : <a href=http://d.hatena.ne.jp/MineAP/20100819/1282201560>[MineAPの（開発）日記]getflvの戻り値についてまとめ(2010年8月版)</a><br>
 *
 * @author Seo-4d696b75
 * @version 0.0  on 2017/01/15.
 */

public class VideoInfoManager extends VideoInfo {

    protected VideoInfoManager(){}
    protected VideoInfoManager(VideoInfoPackage info){

    }

    private synchronized void unPack(VideoInfoPackage info){
        genre = info.genre;
        rankKind = info.rankKind;
        period = info.period;
        pubDate = info.pubDate;
        title = info.title;
        id = info.id;
        date = info.date;
        description = info.description;
        setThumbnailUrl(info.thumbnailUrl);
        length = info.length;
        viewCounter = info.viewCounter;
        commentCounter = info.commentCounter;
        myListCounter = info.myListCounter;
        setTags(tags);
        point = info.point;
    }

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
    /**
     * このライブラリでの日時表記の共通形式です。<br>
     *     This is date format common in the library.<br>
     *     ISO 8601標準形"yyyyMMdd'T'HHmmssZ"に従います。
     */
    public static final SimpleDateFormat dateFormatBase = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ");

    //some fields of Nico video may be empty, so this complete them
    /**
     * 欠損した動画のフィールド値を取得します<br>
     * Gets lacking fields of video.<br>
     * {@link VideoInfo 動画の取得元API}によっては欠損したフィールド値が存在しますので注意してください。
     * このメソッドで取得できるフィールドは以下の通りです<br>
     *     {@link VideoInfo#title 動画タイトル}<br>
     *     {@link VideoInfo#id 動画ID}<br>
     *     {@link VideoInfo#description 動画説明}<br>
     *     {@link VideoInfo#length 動画長さ}<br>
     *     {@link VideoInfo#date 動画投稿日時}<br>
     *     {@link VideoInfo#viewCounter 再生数}<br>
     *     {@link VideoInfo#commentCounter コメント数}<br>
     *     {@link VideoInfo#myListCounter マイリス数}<br>
     *     {@link VideoInfo#thumbnailUrl サムネイル画像のURL}<br>
     *     {@link VideoInfo#tags 動画タグ}<br>
     *     {@link VideoInfo#contributorID 投稿者のユーザID}<br>
     *     {@link VideoInfo#contributorName 投稿者のニックネーム}<br>
     *     {@link VideoInfo#contributorIconUrl 投稿者のユーザアイコン画像のURL}<br>
     * Be careful that there can be lacking fields {@link VideoInfo according to API}.
     * By calling this, you can get;<br>
     *     {@link VideoInfo#title title of video}<br>
     *     {@link VideoInfo#id video ID}<br>
     *     {@link VideoInfo#description video description}<br>
     *     {@link VideoInfo#length length}<br>
     *     {@link VideoInfo#date contributed date}<br>
     *     {@link VideoInfo#viewCounter number of view}<br>
     *     {@link VideoInfo#commentCounter number of comment}<br>
     *     {@link VideoInfo#myListCounter number of myList registered}<br>
     *     {@link VideoInfo#thumbnailUrl url of thumbnail}<br>
     *     {@link VideoInfo#tags video tag}<br>
     *     {@link VideoInfo#contributorID user ID of contributor}<br>
     *     {@link VideoInfo#contributorName user name of contubutor}<br>
     *     {@link VideoInfo#contributorIconUrl url of user icon of contributor}<br>
     * @return Returns {@code true} if succeed
     */
    public boolean complete(){
        String path = thumbUrl + id;
        HttpResponseGetter getter= new HttpResponseGetter();
        if ( !getter.tryGet(path) ){
            return false;
        }
        String res = getter.response;
        try {
            check(res);
            String target;
            int num;
            if ( thumbnailUrl == null){
                target = extract(res,VideoInfo.THUMBNAIL_URL);
                setThumbnailUrl(target);
            }
            if ( date == null){
                target = extract(res,VideoInfo.DATE);
                target = convertDate(target);
                date = target;
            }
            if ( contributorName == null){
                target = extract(res,VideoInfo.CONTRIBUTOR_NAME);
                contributorName = target;
            }
            if ( contributorIconUrl == null){
                target = extract(res,VideoInfo.CONTRIBUTOR_ICON_URL);
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
                parseTags(target);
            }
            return true;
        } catch (NicoAPIException e){
            e.printStackTrace();
            return false;
        }
    }
    private void check (String res) throws NicoAPIException{
        if ( !extract(res,STATUS).equals("ok") ){
            throw new NicoAPIException.APIUnexpectedException("Unexpected API response status > complete");
        }
    }
    private String extract (String res, int key) throws NicoAPIException{
        if ( patternMap.containsKey(key) ) {
            Matcher matcher = patternMap.get(key).matcher(res);
            if ( matcher.find() ){
                return matcher.group(1);
            }
            throw new NicoAPIException.ParseException("target sequence matched with " + matcher.pattern().pattern() + " not found > complete",res);
        }
        throw new NicoAPIException.ParseException("no such key",String.valueOf(key));
    }

    private void parseTags(String res) throws NicoAPIException{
        Matcher matcher = patternMap.get(TAG).matcher(res);
        List<String> list = new ArrayList<String>();
        while ( matcher.find() ){
            list.add(matcher.group(1));
        }
        if ( tags.size() == 0 ){
            throw new NicoAPIException.ParseException("target sequence matched with " + matcher.pattern().pattern() + " not found > complete",res);
        }
        tags = list;
    }

    private String convertDate (String date) throws NicoAPIException{
        date = date.substring(0,date.length()-3) + "00";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        try {
            return dateFormatBase.format(dateFormat.parse(date));
        }catch (ParseException e){
            throw new NicoAPIException.ParseException(e.getMessage(),date);
        }
    }

    /**
     * スレッドID,メッセージサーバURL,flvURLを取得します<br>
     * Gets threadID, URL of message server and flv URL.
     * @param cookieStore the Nico login session
     * @return Returns {@code true} if succeed
     */
    protected boolean getFlv (CookieStore cookieStore){
        String path = flvUrl + id;
        HttpResponseGetter getter = new HttpResponseGetter();
        if ( !getter.tryGet(path,cookieStore)){
            return false;
        }
        String res = getter.response;
        String target;
        try {
            if (threadID == null) {
                target = extract(res, VideoInfo.THREAD_ID);
                threadID = URLDecoder.decode(target);
            }
            if (messageServerUrl == null) {
                target = extract(res, VideoInfo.MESSAGE_SERVER_URL);
                messageServerUrl = URLDecoder.decode(target);
            }
            if (flvUrl == null) {
                target = extract(res, VideoInfo.FLV_URL);
                flvUrl = URLDecoder.decode(target);
            }
        }catch(NicoAPIException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Androidでの使用を前提にサムネイル画像を取得します<br>
     * Gets thumbnail image, supposed to be used in Android.<br>
     * 一度取得した画像は動画フィールドに保存されます。
     * @return Returns thumbnail image, not {@code null}
     * @throws NicoAPIException if fail to get
     */
    public synchronized Drawable getThumbnail () throws NicoAPIException{
        return getThumbnail(false);
    }
    public synchronized Drawable getThumbnail (boolean isHigh) throws NicoAPIException{
        if ( thumbnail == null ){
            String path = "";
            try {
                path = getThumbnailUrl(isHigh);
            }catch (NicoAPIException.NotInitializedException e){
                complete();
                try{
                    path = getThumbnailUrl(isHigh);
                }catch (NicoAPIException.NotInitializedException ee){
                    throw new NicoAPIException.DrawableFailureException("fail to get thumbnail URL > " + id);
                }
            }
            thumbnail = getDrawable(path);
        }
        if ( thumbnail == null ){
            throw new NicoAPIException.DrawableFailureException("fail to get thumbnail > " + id);
        }
        return thumbnail;
    }

    /**
     * Androidでの使用を前提に投稿者のユーザアイコン画像を取得します<br>
     * Gets user icon image of contributor, supposed to be used in Android.<br>
     * 一度取得した画像は動画フィールドに保存されます。
     * @return Returns contributor icon, not {@code null}
     * @throws NicoAPIException if fail to get
     */
    public synchronized Drawable getContributorIcon() throws NicoAPIException{
        if ( contributorIcon == null ) {
            if (contributorIconUrl == null) {
                if (!complete()) {
                    throw new NicoAPIException.DrawableFailureException("fail to get contributor icon URL > " + id);
                }
            }
            contributorIcon = getDrawable(contributorIconUrl);
        }
        if ( contributorIcon == null ){
            throw new NicoAPIException.DrawableFailureException("fail to get contributor icon > " + id);
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

    /*

     memo;
     2016/12/28
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

    /**
     * 表示のため動画長さをフォーマットします<br>
     * Formats video length in order to be shown.<br>
     * @return Returns in format "mm:ss"
     */
    public synchronized String formatLength(){
        return String.format("%01d:%02d",length/60,length%60);
    }

    public synchronized String formatCounter(int key){
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

    /**
     * 動画投稿日をDateクラスに変換して返します<br>
     *　Converts contributed date into Date class.
     * @return Returns, not {@code null}
     * @throws NicoAPIException if date is null
     */
    public synchronized Date parseDate() throws NicoAPIException{
        if ( date == null ){
            throw new NicoAPIException.NotInitializedException("value of date is not initialized > " + id);
        }
        try{
            return dateFormatBase.parse(date);
        } catch (ParseException e) {
            throw new NicoAPIException.ParseException( e.getMessage() + " > " + id,date);
        }
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
    public synchronized String[] splitTitle(){
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
    public synchronized void analyze(VideoInfoManager target,String[] keyWords) throws NicoAPIException {
        try {
            target.getTags();
        } catch (NicoAPIException.NotInitializedException e){
            if ( !complete() ){
                throw e;
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
