package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import org.apache.http.client.CookieStore;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
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

public class VideoInfo extends VideoInfoStorage implements Parcelable{

    protected VideoInfo(){}

    public static final String VIDEO_KEY = "videoInfoObject";

    /* <implementation of parcelable> */

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(title);
        out.writeString(id);
        out.writeString(date);
        out.writeString(description);
        out.writeList(thumbnailUrl);
        out.writeParcelable(thumbnail,flags);
        out.writeInt(length);
        out.writeInt(viewCounter);
        out.writeInt(commentCounter);
        out.writeInt(myListCounter);
        out.writeList(tags);
        out.writeInt(threadID);
        out.writeString(messageServerUrl);
        out.writeString(flvUrl);
        out.writeInt(contributorID);
        out.writeString(contributorName);
        out.writeString(contributorIconUrl);
        out.writeParcelable(contributorIcon,flags);
        out.writeFloat(point);
        out.writeParcelable(commentGroup,flags);
    }

    public static final Parcelable.Creator<VideoInfo> CREATOR = new Parcelable.Creator<VideoInfo>() {
        public VideoInfo createFromParcel(Parcel in) {
            return new VideoInfo(in);
        }
        public VideoInfo[] newArray(int size) {
            return new VideoInfo[size];
        }
    };

    private VideoInfo(Parcel in) {
        title = in.readString();
        id = in.readString();
        date = in.readString();
        description = in.readString();
        thumbnailUrl = in.readArrayList(ArrayList.class.getClassLoader());
        thumbnail = in.readParcelable(Bitmap.class.getClassLoader());
        length = in.readInt();
        viewCounter = in.readInt();
        commentCounter = in.readInt();
        myListCounter = in.readInt();
        tags = in.readArrayList(ArrayList.class.getClassLoader());
        threadID = in.readInt();
        messageServerUrl = in.readString();
        flvUrl = in.readString();
        contributorID = in.readInt();
        contributorName = in.readString();
        contributorIconUrl = in.readString();
        contributorIcon = in.readParcelable(Bitmap.class.getClassLoader());
        point = in.readFloat();
        commentGroup = in.readParcelable(CommentInfo.CommentGroup.class.getClassLoader());
    }

    /* </implementation of parcelable> */

    //url from which details of video you can get
    private final String thumbUrl = "http://ext.nicovideo.jp/api/getthumbinfo/";
    private final String getFlvUrl = "http://flapi.nicovideo.jp/api/getflv/";
    //Pattern to extract value from plain text
    private final int STATUS = 100;
    private final Map<Integer,Pattern> patternMap = new HashMap<Integer,Pattern>(){
        {
            put(STATUS,Pattern.compile("<nicovideo_thumb_response status=\"(.+?)\">"));
            put(VideoInfo.DESCRIPTION,Pattern.compile("<description>(.+?)</description>",Pattern.DOTALL));
            put(VideoInfo.THUMBNAIL_URL,Pattern.compile("<thumbnail_url>(.+?)</thumbnail_url>"));
            put(VideoInfo.DATE,Pattern.compile("<first_retrieve>(.+?)</first_retrieve>"));
            put(VideoInfo.VIEW_COUNTER,Pattern.compile("<view_counter>(.+?)</view_counter>"));
            put(VideoInfo.COMMENT_COUNTER,Pattern.compile("<comment_num>(.+?)</comment_num>"));
            put(VideoInfo.MY_LIST_COUNTER,Pattern.compile("<mylist_counter>(.+?)</mylist_counter>"));
            put(VideoInfo.TAGS,Pattern.compile("<tags domain=\"jp\">(.+?)</tags>",Pattern.DOTALL));
            put(VideoInfo.TAG,Pattern.compile("<tag.*>(.+?)</tag>"));
            put(VideoInfo.CONTRIBUTOR_ID,Pattern.compile("<(user|ch)_id>([0-9]+?)</(user|ch)_id>"));
            put(VideoInfo.CONTRIBUTOR_NAME,Pattern.compile("<(user_nickname|ch_name)>(.+?)</(user_nickname|ch_name)>"));
            put(VideoInfo.CONTRIBUTOR_ICON_URL,Pattern.compile("<(user|ch)_icon_url>(.+?)</(user|ch)_icon_url>"));
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
     *     {@link VideoInfo#contributorID 投稿者のユーザIDまたはチャンネルID}<br>
     *     {@link VideoInfo#contributorName 投稿者のニックネームまたはチャンネル名}<br>
     *     {@link VideoInfo#contributorIconUrl 投稿者のユーザアイコンまたはチャンネルアイコン画像のURL}<br>
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
     *     {@link VideoInfo#contributorID user ID of contributor or channel ID}<br>
     *     {@link VideoInfo#contributorName user name of contubutor or channel name}<br>
     *     {@link VideoInfo#contributorIconUrl url of contributor icon or channel}<br>
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
            synchronized (this) {
                target = extract(res, VideoInfo.DESCRIPTION);
                description = target;
                target = extract(res, VideoInfo.THUMBNAIL_URL);
                setThumbnailUrl(target);
                target = extract(res, VideoInfo.DATE);
                target = convertDate(target);
                date = target;
                target = extract(res, VideoInfo.CONTRIBUTOR_NAME, 2);
                contributorName = target;
                target = extract(res, VideoInfo.CONTRIBUTOR_ICON_URL, 2);
                contributorIconUrl = target;
                target = extract(res, VideoInfo.VIEW_COUNTER);
                num = Integer.parseInt(target);
                viewCounter = num;
                target = extract(res, VideoInfo.COMMENT_COUNTER);
                num = Integer.parseInt(target);
                commentCounter = num;
                target = extract(res, VideoInfo.MY_LIST_COUNTER);
                num = Integer.parseInt(target);
                myListCounter = num;
                target = extract(res, VideoInfo.CONTRIBUTOR_ID, 2);
                num = Integer.parseInt(target);
                contributorID = num;
                target = extract(res, VideoInfo.TAGS);
                parseTags(target);
            }
            return true;
        } catch (NicoAPIException e){
            e.printStackTrace();
            return false;
        }
    }
    private void check (String res) throws NicoAPIException{
        String statusCode = extract(res,STATUS);
        if ( !statusCode.equals("ok") ){
            if ( statusCode.equals("fail")) {
                Matcher matcher = Pattern.compile("<error>.*<code>(.+?)</code>.*</error>",Pattern.DOTALL).matcher(res);
                if ( matcher.find() ){
                    String code = matcher.group(1);
                    switch (code){
                        case "NOT_FOUND":
                            throw new NicoAPIException.InvalidParamsException("no such video found > complete",NicoAPIException.EXCEPTION_PARAM_GET_THUMBNAIL_INFO_NOT_FOUND);
                        case "DELETED":
                            throw new NicoAPIException.InvalidParamsException("requested video deleted > complete",NicoAPIException.EXCEPTION_PARAM_GET_THUMBNAIL_INFO_DELETED);
                        case "COMMUNITY":
                            throw new NicoAPIException.InvalidParamsException("requested video in community only > complete",NicoAPIException.EXCEPTION_PARAM_GET_THUMBNAIL_INFO_COMMUNITY);
                        default:
                            throw new NicoAPIException.APIUnexpectedException("Unexpected errorCode > complete",NicoAPIException.EXCEPTION_UNEXPECTED_GET_THUMBNAIL_INFO_ERROR_CODE);
                    }
                }
            }else {
                throw new NicoAPIException.APIUnexpectedException("Unexpected statusCode > complete",NicoAPIException.EXCEPTION_UNEXPECTED_GET_THUMBNAIL_INFO_STATUS_CODE);
            }
        }
    }
    private String extract (String res, int key) throws NicoAPIException{
        return extract(res,key,1);
    }
    private String extract (String res, int key, int group) throws NicoAPIException{
        if ( patternMap.containsKey(key) ) {
            Matcher matcher = patternMap.get(key).matcher(res);
            if ( matcher.find() ){
                return matcher.group(group);
            }
            throw new NicoAPIException.ParseException("target sequence matched with " + matcher.pattern().pattern() + " not found > complete",res,NicoAPIException.EXCEPTION_PARSE_GET_THUMBNAIL_INFO_NOT_FOUND);
        }
        throw new NicoAPIException.ParseException("no such pattern",String.valueOf(key),NicoAPIException.EXCEPTION_PARSE_GET_THUMBNAIL_INFO_NO_PATTERN);
    }

    private void parseTags(String res) throws NicoAPIException{
        Matcher matcher = patternMap.get(TAG).matcher(res);
        List<String> list = new ArrayList<String>();
        while ( matcher.find() ){
            list.add(matcher.group(1));
        }
        if ( list.size() == 0 ){
            throw new NicoAPIException.ParseException("target sequence matched with " + matcher.pattern().pattern() + " not found > complete",res,NicoAPIException.EXCEPTION_PARSE_GET_THUMBNAIL_INFO_NOT_FOUND);
        }
        tags = list;
    }

    private String convertDate (String date) throws NicoAPIException{
        date = date.substring(0,date.length()-3) + "00";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        try {
            return dateFormatBase.format(dateFormat.parse(date));
        }catch (ParseException e){
            throw new NicoAPIException.ParseException(e.getMessage(),date,NicoAPIException.EXCEPTION_PARSE_GET_THUMBNAIL_INFO_DATE);
        }
    }

    /**
     * スレッドID,メッセージサーバURL,flvURLを取得します<br>
     * Gets threadID, URL of message server and flv URL.<br>
     * 必ずニコ動へのログインが必要で、そのセッション情報を格納したCookieを引数に渡します。
     * 公式動画の場合は取得できず、常に{@code false}を返します。<br>
     * Calling this method requires login to Nico.
     * You have to pass that Cookie which stores the login session.
     * In case of official video, you cannot get those values and {@code false} is always returned.
     * @param cookieStore the Nico login session
     * @return Returns {@code true} if succeed
     */
    public boolean getFlv (CookieStore cookieStore){
        if ( isOfficial()){
            return false;
        }
        String path = getFlvUrl + id;
        HttpResponseGetter getter = new HttpResponseGetter();
        if ( !getter.tryGet(path,cookieStore)){
            return false;
        }
        String res = getter.response;
        String target;
        try {
            synchronized (this) {
                target = extract(res, VideoInfo.THREAD_ID);
                threadID = Integer.parseInt(URLDecoder.decode(target));
                target = extract(res, VideoInfo.MESSAGE_SERVER_URL);
                messageServerUrl = URLDecoder.decode(target);
                target = extract(res, VideoInfo.FLV_URL);
                super.flvUrl = URLDecoder.decode(target);
            }
        }catch(NicoAPIException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * サムネイル画像を取得しフィールドに保存します【ＵＩスレッド禁止】<br>
     * Gets thumbnail image, supposed to be used in Android.<br>
     * 一度取得した画像は動画フィールドに保存され、{@link VideoInfo#getThumbnail()}から取得できます。
     * @return Returns thumbnail image, not {@code null}
     * @throws NicoAPIException if fail to get
     */
    public Bitmap loadThumbnail () throws NicoAPIException{
        return loadThumbnail(false);
    }
    public Bitmap loadThumbnail (boolean isHigh) throws NicoAPIException{
        String path;
        try {
            path = getThumbnailUrl(isHigh);
        } catch (NicoAPIException.NotInitializedException e) {
            complete();
            try {
                path = getThumbnailUrl(isHigh);
            } catch (NicoAPIException.NotInitializedException ee) {
                throw new NicoAPIException.DrawableFailureException(
                        "fail to get thumbnail URL > " + id,
                        NicoAPIException.EXCEPTION_DRAWABLE_THUMBNAIL_URL
                );
            }
        }
        Bitmap thumbnail = new HttpResponseGetter().getBitmap(path);
        if (thumbnail == null) {
            throw new NicoAPIException.DrawableFailureException(
                    "fail to get thumbnail > " + id,
                    NicoAPIException.EXCEPTION_DRAWABLE_THUMBNAIL
            );
        } else {
            synchronized (this){
                this.thumbnail = thumbnail;
                return thumbnail;
            }
        }
    }

    /**
     * 投稿者のユーザアイコン画像を取得し保存します【ＵＩスレッド禁止】<br>
     * Gets user icon image of contributor, supposed to be used in Android.<br>
     * 一度取得した画像は動画フィールドに保存され、{@link VideoInfo#getContributorIcon()}からいつでも取得できます。
     * HTTP通信を行うためバックグランド処理してください。<br>
     * This communicates by HTTP, so has to be called in background.
     * @return Returns contributor icon, not {@code null}
     * @throws NicoAPIException if fail to get
     */
    public Bitmap loadContributorIcon() throws NicoAPIException{
        String path;
        try {
            path = getContributorIconUrl();
        } catch (NicoAPIException.NotInitializedException e) {
            complete();
            try {
                path = getContributorIconUrl();
            } catch (NicoAPIException.NotInitializedException ee) {
                throw new NicoAPIException.DrawableFailureException(
                        "fail to get contributor icon URL > " + id,
                        NicoAPIException.EXCEPTION_DRAWABLE_CONTRIBUTOR_ICON_URL
                );
            }
        }
        Bitmap icon = new HttpResponseGetter().getBitmap(path);
        if (icon == null) {
            throw new NicoAPIException.DrawableFailureException(
                    "fail to get contributor icon > " + id,
                    NicoAPIException.EXCEPTION_DRAWABLE_CONTRIBUTOR_ICON
            );
        } else {
            synchronized (this){
                this.contributorIcon = icon;
                return icon;
            }
        }
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
     * 表示のため動画長さをフォーマットします<br>
     * Formats video length in order to be shown.<br>
     * @return Returns in format "mm:ss"
     */
    public String formatLength(){
        int length = getLength();
        return String.format("%01d:%02d",length/60,length%60);
    }

    public String formatCounter(int key) throws NicoAPIException{
        int counter = 0;
        try {
            switch ( key ){
                case VideoInfo.VIEW_COUNTER:
                    counter = getViewCounter();
                    break;
                case VideoInfo.COMMENT_COUNTER:
                    counter = getCommentCounter();
                    break;
                case VideoInfo.MY_LIST_COUNTER:
                    counter = getMyListCounter();
                    break;
                default:
                    throw new NicoAPIException.InvalidParamsException("invalid key at format > " + key);
            }
        }catch (NicoAPIException e){
            throw e;
        }
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        return numberFormat.format(counter);
    }
    public String formatViewCounter() throws NicoAPIException {
        return formatCounter(VIEW_COUNTER);
    }
    public String formatMyListCounter() throws NicoAPIException {
        return formatCounter(MY_LIST_COUNTER);
    }
    public String formatCommentCounter() throws NicoAPIException {
        return formatCounter(COMMENT_COUNTER);
    }

    /**
     * 動画投稿日をDateクラスに変換して返します<br>
     *　Converts contributed date into Date class.
     * @return Returns, not {@code null}
     * @throws NicoAPIException if date is null
     */
    public Date parseDate() throws NicoAPIException{
        String date = getDate();
        try{
            return dateFormatBase.parse(date);
        } catch (ParseException e) {
            throw new NicoAPIException.ParseException( e.getMessage() + " > " + id,date);
        }
    }

    /**
     * コメントを取得します<br>
     * Gets comments.<br>
     * 【ＵＩスレッド禁止】HTTP通信を行うのでバックグランド処理してください。
     * メッセージサーバーのURLとスレッドＩＤが必要で、未取得の場合は例外を投げます。
     * 必ず事前に{@link #getFlv(CookieStore)}を呼んでください。
     * 取得するコメント数は動画長さに応じて適当に設定されます。
     * 一度取得すれば{@link VideoInfo#getComment()}からいつでも取得できます。<br>
     *【No UI thread】HTTP communication is done.
     * This requires message server Url and Thread ID, and throws an exception if these values are not gotten.
     * Be sure to call {@link #getFlv(CookieStore)} in advance.
     * The number of comments is set corresponding to video length.
     * Once the comment is gotten, it can be accessed from {@link VideoInfo#getComment()}
     * @return Returns List of {@link CommentInfo} sorted along time series, not {@code null}
     * @throws NicoAPIException if fail to get comment
     */
    public CommentInfo.CommentGroup loadComment () throws NicoAPIException{
        try {
            int threadID = getThreadID();
            String messageServerUrl = getMessageServerUrl();
            HttpResponseGetter getter = new HttpResponseGetter();
            String postEntityFormat = "<packet><thread thread=\"%1$d\" version=\"20090904\"  /><thread_leaves scores=\"1\" thread=\"%1$d\">0-%2$d:100,1000</thread_leaves></packet>";
            String postEntity = String.format(postEntityFormat, threadID, getLength() / 60 + 1);
            if (getter.tryPost(messageServerUrl, postEntity)) {
                CommentInfo.CommentGroup commentGroup = new CommentInfo.CommentGroup(getter.response);
                synchronized (this) {
                    this.commentGroup = commentGroup;
                    return commentGroup;
                }
            } else {
                throw new NicoAPIException.HttpException(
                        "HTTP failure > comment",
                        NicoAPIException.EXCEPTION_HTTP_COMMENT,
                        getter.statusCode, messageServerUrl, "POST"
                );
            }
        } catch (NicoAPIException.NotInitializedException e) {
            throw new NicoAPIException.IllegalStateException("ThreadID and MessageServerUrl are unknown", NicoAPIException.EXCEPTION_ILLEGAL_STATE_COMMENT_NOT_READY);
        }
    }
    /**
     * コメントを取得します<br>
     * Gets comments.<br>
     * 【ＵＩスレッド禁止】HTTP通信を行うのでバックグランド処理してください。
     * メッセージサーバーのURLとスレッドＩＤが必要で、未取得の場合は例外を投げます。
     * 必ず事前に{@link #getFlv(CookieStore)}を呼んでください。
     * 一度取得すれば{@link VideoInfo#getComment()}からいつでも取得できます。<br>
     * 【No UI thread】HTTP communication is done.
     * This requires message server Url and Thread ID, and throws an exception if these values are not gotten.
     * Be sure to call {@link #getFlv(CookieStore)} in advance.
     * Once the comment is gotten, it can be accessed from {@link VideoInfo#getComment()}
     * @param max the limit number of comment response from 0 to 1000, if over 1000, fixed to 1000
     * @return Returns List of {@link CommentInfo} sorted along time series, not {@code null}
     * @throws NicoAPIException if fail to get comment
     */
    public CommentInfo.CommentGroup loadComment (int max) throws NicoAPIException{
        if (max <= 0) {
            max = 100;
        }
        if (max > 1000) {
            max = 1000;
        }
        try {
            int threadID = getThreadID();
            String messageServerUrl = getMessageServerUrl();
            HttpResponseGetter getter = new HttpResponseGetter();
            String postEntityFormat = "<thread res_from=\"-%d\" version=\"20061206\" scores=\"1\" thread=\"%d\" />";
            String postEntity = String.format(postEntityFormat, max, threadID);
            if (getter.tryPost(messageServerUrl, postEntity)) {
                CommentInfo.CommentGroup commentGroup = new CommentInfo.CommentGroup(getter.response);
                synchronized (this) {
                    this.commentGroup = commentGroup;
                    return commentGroup;
                }
            } else {
                throw new NicoAPIException.HttpException(
                        "HTTP failure > comment",
                        NicoAPIException.EXCEPTION_HTTP_COMMENT,
                        getter.statusCode, messageServerUrl, "POST"
                );
            }
        } catch (NicoAPIException.NotInitializedException e) {
            throw new NicoAPIException.IllegalStateException("ThreadID and MessageServerUrl are unknown", NicoAPIException.EXCEPTION_ILLEGAL_STATE_COMMENT_NOT_READY);
        }
    }
    //Jsonでも取得できる
    public CommentInfo.CommentGroup loadCommentByJson (int max) throws NicoAPIException{
        HttpResponseGetter getter = new HttpResponseGetter();
        try {
            int threadID = getThreadID();
            String messageServerUrl = getMessageServerUrl();
            String paramFormat = ".json/thread?version=20090904&thread=%d&res_from=-%d";
            String param = String.format(paramFormat, threadID, max);
            String path = messageServerUrl;
            Matcher matcher = Pattern.compile("(.+/api)/?").matcher(path);
            if (matcher.find()) {
                path = matcher.group(1) + param;
                getter.tryGet(path);
                CommentInfo.CommentGroup commentGroup = new CommentInfo.CommentGroup(new JSONArray(getter.response));
                synchronized (this) {
                    this.commentGroup = commentGroup;
                    return commentGroup;
                }
            } else {
                throw new NicoAPIException.APIUnexpectedException("format of message server URL is unexpected > " + path);
            }
        } catch (NicoAPIException.NotInitializedException e) {
            throw new NicoAPIException.IllegalStateException(
                    "ThreadID and MessageServerUrl are unknown",
                    NicoAPIException.EXCEPTION_ILLEGAL_STATE_COMMENT_NOT_READY
            );
        } catch (JSONException e) {
            throw new NicoAPIException.ParseException(
                    e.getMessage(), getter.response,
                    NicoAPIException.EXCEPTION_PARSE_COMMENT_JSON_META
            );
        }
    }

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
    public synchronized void analyze(VideoInfo  target,String[] keyWords) throws NicoAPIException {
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