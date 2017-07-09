package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.CommentInfo.*;

/**
 * 各種APIから取得してきた動画情報を動画単位で管理します<br>
 *     This manages information of each video.<br><br>
 * このパッケージで動画を扱うとき基本となる動画オブジェクトを定義した{@link VideoInfo}インターフェイスの実現クラスです。
 * ニコ動から取得したすべての動画情報はこのクラスを継承したサブクラスのインスタンスでパースされ、
 * 各種フィールドが初期化されます。
 * 取得元のAPI毎に対応した子供クラスが存在し、APIからの情報の処理を定義します。<br>
 * ランキング/マイリスト：{@link RSSVideoInfo}<br>
 * 検索：{@link SearchVideoInfo}<br>
 * (とりあえず)マイリス：{@link NicoMyListVideoInfo}<br>
 * おすすめ動画：{@link RecommendVideoInfo}<br>
 * ただし、各APIによって取得できるフィールドは異なりますので注意してください。
 * 欠損したフィールド値は{@link NicoVideoInfo#complete()}、
 * {@link VideoInfo#getFlv(CookieGroup)}で補うことができます。<br>
 * This class implements {@link VideoInfo} interface defining the basic video object in this package.
 * All the videos from Nico are managed by subclasses extending this.
 * All the fields are supposed to be set by these child classes.
 * There are corresponding child class to each API, which defines how to parse response from it.<br>
 * ranking/myList：{@link RSSVideoInfo}<br>
 * search：{@link SearchVideoInfo}<br>
 * (temp) myList：{@link MyListVideoInfo}<br>
 * recommended video：{@link RecommendVideoInfo}<br>
 * Be careful that some fields may not be initialized depending on each API.
 * You can get these lacking field by calling {@link VideoInfo#complete()}, {@link VideoInfo#getFlv(CookieGroup)}
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
 * @version 0.0 on 2016/12/17.
 */

public class NicoVideoInfo implements VideoInfo {

    /**
     * In usual, this value is not set in any field.<br>
     * Video title and its ID are supposed to be set whenever succeeding in parsing response from Nico.
     * But if not, return this value.
     */
    public static final String UNKNOWN = "unknown";

    //common
    /**
     * 動画のタイトル<br>
     *     title of video.<br>
     * 取得されたすべての動画でこのフィールド値は保証されます。<br>
     * This value is always set in any video.
     */
    protected String title = UNKNOWN;
    /**
     * 動画ID、動画視聴URLの末尾にある"sm******"これです。<br>
     *     video ID, which you can find in the end of watch URL.<br>
     * 取得されたすべての動画でこのフィールド値は保証されます。<br>
     * 動画ＩＤには大きく分けて"sm********"と"so*********"の二種類が存在し、
     * 後者は公式動画を表します。公式動画の場合、一部フィールドの内容が通常と異なります。<br>
     * {@link #contributorName}　投稿者名→チャンネル名<br>
     * {@link #contributorID}　投稿者ID→チャンネルID<br>
     * {@link #contributorIconUrl}　投稿者アイコンURL→チャンネルアイコンURL<br>
     * {@link #contributorIcon}　投稿者アイコン→チャンネルアイコン<br>
     * This value is always set in any video.<br>
     * Video ID is categorized to two types; "sm********" and "so*********".
     * The latter stands for an official video.
     * In case of an official video, some fields have different value than usual;<br>
     * {@link #contributorName}　contributor name→channel name<br>
     * {@link #contributorID} contributor ID→channel ID<br>
     * {@link #contributorIconUrl}　contributor icon URL→channel icon URL<br>
     * {@link #contributorIcon}　contributor icon→channel icon<br>
     */
    protected String id = UNKNOWN;
    private boolean official = false;
    /**
     * 動画の投稿日時<br>
     *     contribution date.<br>
     */
    protected Date date = new Date();
    /**
     * 動画の説明<br>
     *     description of video.
     */
    protected String description;
    /**
     * 動画サムネイル画像のURL<br>
     *     url of thumbnail image.<br>
     * 画像は.jpgです。thumbnail id JPG image.
     */
    protected String thumbnailUrl;
    /**
     * Androidでの仕様の前提とした動画のサムネイル画像<br>
     * thumbnail image, which is supposed to be used in Android.<br>
     */
    protected Bitmap thumbnail;
    /**
     * 動画の長さ、秒単位<br>
     *     video length in seconds.<br>
     *     取得に失敗すると負数が代入されます。<br>
     *     this is negative if fail to get.
     */
    protected int length = -1;

    //statistics of the video
    /**
     * 動画の総再生数<br>
     *     the number of view.<br>
     *     取得に失敗すると負数が代入されます。<br>
     *     this is negative if fail to get.
     */
    protected int viewCounter = -1;
    /**
     * 動画の総コメント数<br>
     *     comment number.<br>
     *     取得に失敗すると負数が代入されます。<br>
     *     this is negative if fail to get.
     */
    protected int commentCounter = -1;
    /**
     * 動画のマイリス数<br>
     *     the number of myList register.<br>
     *     取得に失敗すると負数が代入されます。<br>
     *     this is negative if fail to get.
     */
    protected int myListCounter = -1;

    //tags attributed to the video
    /**
     * 動画のタグ<br>
     *     tags of video.
     */
    protected List<String> tags;
    /**
     * 動画のスレッドID。<br>
     *     メッセージサーバとの通信やflvURLの取得に使用します。
     */
    protected int threadID = -1;
    /**
     * 動画のコメントの取得先<br>
     *     you can get comments from this.
     */
    protected String messageServerUrl;
    /**
     * 動画の所在。
     */
    protected String flvUrl;

    protected float point = 0f;

    //contributor
    /**
     * 投稿者のユーザID<br>
     * user ID of video contributor.<br>
     * 公式動画の場合はチャンネルIDで代替されます。<br>
     * In case of an official video, this is substituted with channel ID.
     */
    protected int contributorID = -1;
    /**
     * 投稿者のユーザ名<br>
     * name of contributor.<br>
     * 公式動画の場合はチャンネル名で代替されます。<br>
     * In case of an official video, this is substituted with channel name.
     */
    protected String contributorName;
    /**
     * 投稿者のユーザアイコンのURL,画像はJPG<br>
     * URL from which you can get user icon image of contributor.<br>
     * 公式動画の場合はチャンネルアイコンで代替されます。<br>
     * In case of an official video, this is substituted with channel icon.
     */
    protected String contributorIconUrl;
    /**
     * Androidでの使用が前提、投稿者のユーザアイコン<br>
     *     user icon image of contributor, supposed to be used in Android.<br>
     * 公式動画の場合はチャンネルアイコンで代替されます。<br>
     * In case of an official video, this is substituted with channel icon.
     */
    protected Bitmap contributorIcon;

    //comment
    protected CommentGroup commentGroup;

    protected NicoVideoInfo(CookieGroup cookieGroup){
        resource = ResourceStore.getInstance();
        this.cookieGroup = cookieGroup;
    }

    protected void setID(String id){
        this.id = id;
        if ( id.contains("so") ){
            official = true;
        }
    }
    public Date getDate(){
        return date;
    }
    public String getTitle() {
        return title;
    }
    public String getID() {
        return id;
    }
    public boolean isOfficial(){
        return official;
    }
    public synchronized String getDescription() throws NicoAPIException{
        if ( description == null ) {
            throw new NicoAPIException.NotInitializedException("requested video field not initialized > description", VideoInfo.DESCRIPTION);
        }else{
            return description;
        }
    }
    public synchronized int getThreadID() throws NicoAPIException{
        if ( threadID < 0 ){
            throw new NicoAPIException.NotInitializedException("requested video field not initialized > threadID",VideoInfo.THREAD_ID);
        }else{
            return threadID;
        }
    }
    public synchronized String getMessageServerURL() throws NicoAPIException{
        if ( messageServerUrl == null ){
            throw new NicoAPIException.NotInitializedException("requested video field not initialized > MessageSeverURL",VideoInfo.MESSAGE_SERVER_URL);
        }else{
            return messageServerUrl;
        }
    }
    public synchronized String getFlvURL() throws NicoAPIException{
        if ( flvUrl == null ){
            throw new NicoAPIException.NotInitializedException("requested video field not initialized > flvURL",VideoInfo.FLV_URL);
        }else{
            return flvUrl;
        }
    }
    public synchronized String getContributorName() throws NicoAPIException{
        if ( contributorName == null ){
            throw new NicoAPIException.NotInitializedException("requested video field not initialized > contributorName",VideoInfo.CONTRIBUTOR_NAME);
        }else{
            return contributorName;
        }
    }
    public synchronized String getContributorIconURL() throws NicoAPIException{
        if ( contributorIconUrl == null ){
            throw new NicoAPIException.NotInitializedException("requested video field not initialized > contributorIconURL",VideoInfo.CONTRIBUTOR_ICON_URL);
        }else{
            return contributorIconUrl;
        }
    }
    public int getLength(){
        return length;
    }
    public int getViewCounter(){
        return viewCounter;
    }
    public int getMyListCounter(){
        return myListCounter;
    }
    public int getCommentCounter(){
        return commentCounter;
    }
    public synchronized int getContributorID() throws NicoAPIException{
        if ( contributorID < 0 ){
            throw new NicoAPIException.NotInitializedException("requested video field not initialized > contributorID",VideoInfo.CONTRIBUTOR_ID);
        }else{
            return contributorID;
        }
    }

    /**
     * コメントを取得します<br>
     * Gets comments.<br>
     * 【ＵＩスレッド禁止】HTTP通信を行うのでバックグランド処理してください。
     * 取得するコメント数は動画長さに応じて適当に設定されます。
     * すでに取得済みの場合はフィールドに保持されているコメントをそのまま返します。
     * メッセージサーバーのURLとスレッドＩＤが必要ですが、未取得の場合は内部的に取得を試みます。
     * 事前に{@link #getFlv(CookieGroup)}を適切に呼んでおくように推奨されます。<br>
     *【No UI thread】HTTP communication is done.<br>
     * The number of comments is set corresponding to video length.
     * Once comments are gotten, this returns these comments.
     * This requires message server Url and Thread ID, and if these values are not gotten,
     * this internally tries to get them.
     * It is recommended to call {@link #getFlv(CookieGroup)} properly in advance.
     * @return Returns List of {@link CommentInfo} sorted along time series, not {@code null}
     * @throws NicoAPIException if fail to get comment
     */
    public synchronized CommentGroup getComment () throws NicoAPIException{
        if ( commentGroup == null ) {
            /*throw new NicoAPIException.IllegalStateException(
                    "commentGroup not ready",
                    NicoAPIException.EXCEPTION_ILLEGAL_STATE_COMMENT
            );*/
            loadComment();
        }
        return commentGroup;
    }

    /**
     * コメントを取得します<br>
     * Gets comments.<br>
     * 【ＵＩスレッド禁止】HTTP通信を行うのでバックグランド処理してください。
     * 一度取得すれば{@link VideoInfo#getComment()}からいつでも取得できます。
     * メッセージサーバーのURLとスレッドＩＤが必要ですが、未取得の場合は内部的に取得を試みます。
     * 事前に{@link #getFlv(CookieGroup)}を適切に呼んでおくように推奨されます。<br>
     * 【No UI thread】HTTP communication is done.
     * Once the comment is gotten, it can be accessed from {@link VideoInfo#getComment()}.
     * This requires message server Url and Thread ID, and if these values are not gotten,
     * this internally tries to get them.
     * It is recommended to call {@link #getFlv(CookieGroup)} properly in advance.
     * @param max the limit number of comment response from 0 to 1000, if over 1000, fixed to 1000
     * @return Returns List of {@link CommentInfo} sorted along time series, not {@code null}
     * @throws NicoAPIException if fail to get comment
     */
    public synchronized CommentGroup getComment(int max) throws NicoAPIException{
        loadComment(max);
        return commentGroup;
    }

    /**
     * この動画のサムネイル画像を取得します【ＵＩスレッド禁止】<br>
     * Gets the thumbnail image of this video.<br>
     * HTTP通信を行うためバックグランド処理してください。ただし、すでに取得済みならフィールドに保持されたサムネルを返します<br>
     * <string>No UI Thread</string>This communicates by HTTP, so has to be called in background.
     * @return Returns thumbnail image, not {@code null}
     * @throws NicoAPIException if fail to get
     */
    public synchronized Bitmap getThumbnail()throws NicoAPIException{
        if ( thumbnail == null ){
            /*throw new NicoAPIException.IllegalStateException(
                    "thumbnail not ready",
                    NicoAPIException.EXCEPTION_ILLEGAL_STATE_THUMBNAIL
            );*/
            this.thumbnail = loadThumbnail();
        }
        return thumbnail;
    }

    /**
     * 投稿ユーザのアイコン画像を取得します【UIスレッド禁止】<br>
     * Gets the contributor's icon image.<br>
     * HTTP通信で取得するためバックグランド処理してください。ただし、すでに取得済みならフィールドに保持された画像を返します<br>
     *  一般ユーザが投稿した動画では投稿者のユーザアイコン画像を取得します。<br>
     *  <strong>注意</strong>　公式動画の場合はチャンネルアイコン画像を取得します。<br>
     *  <strong>No UI Thread</strong>This communicates vis HTTP, so has to be called in background.
     *  <strong>Attention</strong> In case of official video, the image of channel icon is returned.
     * @return Returns contributor icon, not {@code null}
     * @throws NicoAPIException if fail to get image
     */
    public synchronized Bitmap getContributorIcon()throws NicoAPIException{
        if ( contributorIcon == null ){
            /*throw new NicoAPIException.IllegalStateException(
                    "contributorIcon not ready",
                    NicoAPIException.EXCEPTION_ILLEGAL_STATE_CONTRIBUTOR_ICON
            );*/
            this.contributorIcon = loadContributorIcon();
        }
        return contributorIcon;
    }

    /**
     * 動画のタグをリストで取得します<br>
     *     Gets video tags in List.<br>
     *     取得元のAPIによっては欠損している場合があります。{@link VideoInfo 詳細はこちら}<br>
     *     This field may be lacking depending on API, {@link VideoInfo details are here}.
     * @return Returns {@code null} if not initialized
     */
    public synchronized List<String> getTags() throws NicoAPIException.NotInitializedException{
        if ( tags == null ){
            throw new NicoAPIException.NotInitializedException("requested video tags not initialized > " + id,TAGS);
        }
        return new ArrayList<String>(this.tags);
    }

    /**
     * 動画のサムネイル画像URLを取得します<br>
     *     Gets URL from which you can get thumbnail image.
     * @return can be {@code null} if not initialized
     */
    public synchronized String getThumbnailURL () throws NicoAPIException.NotInitializedException{
        if ( thumbnailUrl == null || thumbnailUrl.isEmpty() ){
            throw new NicoAPIException.NotInitializedException("requested video thumbnail URL not initialized > " + id, THUMBNAIL_URL);
        }
        return thumbnailUrl;
    }


    private final ResourceStore resource;
    private final CookieGroup cookieGroup;

    /* <implementation of parcelable> */

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(title);
        out.writeString(id);
        out.writeSerializable(date);
        out.writeString(description);
        out.writeString(thumbnailUrl);
        out.writeParcelable(thumbnail,flags);
        out.writeInt(length);
        out.writeInt(viewCounter);
        out.writeInt(commentCounter);
        out.writeInt(myListCounter);
        out.writeStringList(tags);
        out.writeInt(threadID);
        out.writeString(messageServerUrl);
        out.writeString(flvUrl);
        out.writeInt(contributorID);
        out.writeString(contributorName);
        out.writeString(contributorIconUrl);
        out.writeParcelable(contributorIcon,flags);
        out.writeFloat(point);
        out.writeParcelable(commentGroup,flags);
        out.writeParcelable(cookieGroup,flags);
    }

    public static final Parcelable.Creator<NicoVideoInfo> CREATOR = new Parcelable.Creator<NicoVideoInfo>() {
        public NicoVideoInfo createFromParcel(Parcel in) {
            return new NicoVideoInfo(in);
        }
        public NicoVideoInfo[] newArray(int size) {
            return new NicoVideoInfo[size];
        }
    };

    protected NicoVideoInfo(Parcel in) {
        title = in.readString();
        id = in.readString();
        date = (Date)in.readSerializable();
        description = in.readString();
        thumbnailUrl = in.readString();
        thumbnail = in.readParcelable(Bitmap.class.getClassLoader());
        length = in.readInt();
        viewCounter = in.readInt();
        commentCounter = in.readInt();
        myListCounter = in.readInt();
        tags = new ArrayList<>();
        in.readStringList(tags);
        threadID = in.readInt();
        messageServerUrl = in.readString();
        flvUrl = in.readString();
        contributorID = in.readInt();
        contributorName = in.readString();
        contributorIconUrl = in.readString();
        contributorIcon = in.readParcelable(Bitmap.class.getClassLoader());
        point = in.readFloat();
        commentGroup = in.readParcelable(CommentInfo.CommentGroup.class.getClassLoader());
        cookieGroup = in.readParcelable(CookieGroup.class.getClassLoader());
        resource = ResourceStore.getInstance();
    }

    /* </implementation of parcelable> */


    //some fields of Nico video may be empty, so this complete them

    public boolean complete(){
        String path = resource.getURL(R.string.url_getThumbnailAPI) + id;
        HttpClient client = resource.getHttpClient();
        if ( !client.get(path,null) ){
            return false;
        }
        String res = client.getResponse();
        try {
            check(res);
            synchronized (this) {
                description = extract(res, R.string.regex_thumb_description);
                thumbnailUrl = extract(res, R.string.regex_thumb_thumbnail);
                date = convertDate(extract(res, R.string.regex_thumb_date));
                contributorName = extract(res, R.string.regex_thumb_userName, 2);
                contributorIconUrl = extract(res, R.string.regex_thumb_userIconUrl, 2);
                viewCounter = Integer.parseInt(extract(res, R.string.regex_thumb_view_counter));
                int comment = Integer.parseInt(extract(res, R.string.regex_thumb_comment_counter));
                if ( commentCounter < 0 || comment > 0 ){
                    //公式動画の場合は0が返る場合があり
                    commentCounter = comment;
                }
                myListCounter = Integer.parseInt(extract(res, R.string.regex_thumb_myList_counter));
                contributorID = Integer.parseInt(extract(res, R.string.regex_thumb_userID, 2));
                parseTags(extract(res, R.string.regex_thumb_tags));
            }
            return true;
        } catch (NicoAPIException e){
            e.printStackTrace();
            return false;
        }
    }

    private void check (String res) throws NicoAPIException{
        String statusCode = extract(res,R.string.regex_thumb_result_status);
        if ( !statusCode.equals(resource.getString(R.string.value_getThumbnailAPI_status_success)) ){
            if ( statusCode.equals(resource.getString(R.string.value_getThumbnailAPI_status_failure))) {
                Matcher matcher = resource.getPattern(R.string.regex_thumb_error).matcher(res);
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
        Pattern pattern = resource.getPattern(key);
        if ( pattern != null ) {
            Matcher matcher = pattern.matcher(res);
            if ( matcher.find() ){
                return matcher.group(group);
            }
            throw new NicoAPIException.ParseException("target sequence matched with " + matcher.pattern().pattern() + " not found > complete",res,NicoAPIException.EXCEPTION_PARSE_GET_THUMBNAIL_INFO_NOT_FOUND);
        }
        throw new NicoAPIException.ParseException("no such pattern",String.valueOf(key),NicoAPIException.EXCEPTION_PARSE_GET_THUMBNAIL_INFO_NO_PATTERN);
    }

    private void parseTags(String res) throws NicoAPIException{
        Matcher matcher = resource.getPattern(R.string.regex_thumb_tag).matcher(res);
        List<String> list = new ArrayList<String>();
        while ( matcher.find() ){
            list.add(matcher.group(1));
        }
        if ( list.size() == 0 ){
            throw new NicoAPIException.ParseException("target sequence matched with " + matcher.pattern().pattern() + " not found > complete",res,NicoAPIException.EXCEPTION_PARSE_GET_THUMBNAIL_INFO_NOT_FOUND);
        }
        tags = list;
    }

    private Date convertDate (String date) {
        try {
            return new SimpleDateFormat(resource.getString(R.string.format_thumbnail_date),Locale.US).parse(date);
        }catch (ParseException e){
            return new Date();
        }
    }



    public boolean getFlv (CookieGroup cookies){
        if (cookies == null && this.cookieGroup == null ){
            return false;
        }
        if ( isOfficial() ){
            String path = String.format(resource.getURL(R.string.url_watch),getID());
            HttpClient client = resource.getHttpClient();
            if (!client.get(path, cookies == null ? this.cookieGroup : cookies)) {
                return false;
            }
            Matcher matcher =  resource.getPattern(R.string.regex_flv_watch_html_encode).matcher(client.getResponse());
            if ( !matcher.find() ){
                return false;
            }
            String flvInfo = matcher.group(1);
            try {
                while (flvInfo.contains("%")) {
                    flvInfo = URLDecoder.decode(flvInfo, "UTF-8");
                }
                synchronized ( this ){
                    threadID = Integer.parseInt(extract(flvInfo, R.string.regex_flv_threadID));
                    flvUrl = extract(flvInfo, R.string.regex_flv_video);
                    messageServerUrl = extract(flvInfo, R.string.regex_flv_message_server);
                }
            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
                return false;
            } catch (NicoAPIException e) {
                e.printStackTrace();
                return false;
            }
        }else {
            String path = resource.getURL(R.string.url_getFlvAPI) + id;
            HttpClient client = resource.getHttpClient();
            if (!client.get(path, cookies == null ? this.cookieGroup : cookies)) {
                return false;
            }
            String res = client.getResponse();
            try {
                synchronized (this) {
                    threadID = Integer.parseInt(URLDecoder.decode(extract(res, R.string.regex_flv_threadID), "UTF-8"));
                    messageServerUrl = URLDecoder.decode(extract(res, R.string.regex_flv_message_server), "UTF-8");
                    flvUrl = URLDecoder.decode(extract(res, R.string.regex_flv_video), "UTF-8");
                }
            } catch (NicoAPIException e) {
                e.printStackTrace();
                return false;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }


    private Bitmap loadThumbnail () throws NicoAPIException{
        String path;
        try {
            path = getThumbnailURL();
        } catch (NicoAPIException.NotInitializedException e) {
            complete();
            try {
                path = getThumbnailURL();
            } catch (NicoAPIException.NotInitializedException ee) {
                throw new NicoAPIException.DrawableFailureException(
                        "fail to get thumbnail URL > " + id,
                        NicoAPIException.EXCEPTION_DRAWABLE_THUMBNAIL_URL
                );
            }
        }
        Bitmap thumbnail = resource.getHttpClient().getBitmap(path,null);
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


    private Bitmap loadContributorIcon() throws NicoAPIException{
        String path;
        try {
            path = getContributorIconURL();
        } catch (NicoAPIException.NotInitializedException e) {
            complete();
            try {
                path = getContributorIconURL();
            } catch (NicoAPIException.NotInitializedException ee) {
                throw new NicoAPIException.DrawableFailureException(
                        "fail to get contributor icon URL > " + id,
                        NicoAPIException.EXCEPTION_DRAWABLE_CONTRIBUTOR_ICON_URL
                );
            }
        }
        Bitmap icon = resource.getHttpClient().getBitmap(path,null);
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



    /**
     * 表示のため動画長さをフォーマットします<br>
     * Formats video length in order to be shown.<br>
     * @return Returns in format "mm:ss"
     */
    public String formatLength(){
        int length = getLength();
        return String.format(Locale.US,"%d:%02d",length/60,length%60);
    }

    private String formatCounter(int key){
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
        }catch (NicoAPIException e){}
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        return numberFormat.format(counter);
    }

    public String formatViewCounter() {
        return formatCounter(VIEW_COUNTER);
    }

    public String formatMyListCounter() {
        return formatCounter(MY_LIST_COUNTER);
    }

    public String formatCommentCounter() {
        return formatCounter(COMMENT_COUNTER);
    }

    public String formatDate(){
        return new SimpleDateFormat(resource.getString(R.string.format_date),Locale.US).format(date);
    }


    protected CommentGroup loadComment () throws NicoAPIException{
        try{
            getThreadID();
            getMessageServerURL();
        }catch ( NicoAPIException.NotInitializedException e){
            getFlv(null);
        }
        try {
            int threadID = getThreadID();
            String messageServerUrl = getMessageServerURL();
            HttpClient client = resource.getHttpClient();
            String postEntity = null;
            if ( isOfficial() ) {
                getThreadKey(threadID);
                postEntity = String.format(
                        Locale.US,
                        resource.getString(R.string.format_comment_get_official),
                        threadID,
                        getLength() / 60 + 1,
                        threadKey,
                        forceValue,
                        resource.getString(R.string.value_comment_version)
                );
            }else{
                postEntity = String.format(
                        Locale.US,
                        resource.getString(R.string.format_comment_get),
                        threadID,
                        getLength() / 60 + 1,
                        resource.getString(R.string.value_comment_version)
                );
            }
            if (client.post(messageServerUrl, postEntity,null)) {
                CommentInfo.CommentGroup commentGroup = new CommentInfo.CommentGroup(client.getResponse());
                synchronized (this) {
                    this.commentGroup = commentGroup;
                    return commentGroup;
                }
            } else {
                throw new NicoAPIException.HttpException(
                        "HTTP failure > comment",
                        NicoAPIException.EXCEPTION_HTTP_COMMENT,
                        client.getStatusCode(), messageServerUrl, "POST"
                );
            }
        } catch (NicoAPIException.NotInitializedException e) {
            throw new NicoAPIException.IllegalStateException("ThreadID and MessageServerUrl are unknown", NicoAPIException.EXCEPTION_ILLEGAL_STATE_COMMENT_NOT_READY);
        }
    }

    protected CommentGroup loadComment (int max) throws NicoAPIException{
        if (max <= 0) {
            max = 100;
        }
        if (max > 1000) {
            max = 1000;
        }
        try{
            getThreadID();
            getMessageServerURL();
        }catch ( NicoAPIException.NotInitializedException e){
            getFlv(null);
        }
        try {
            int threadID = getThreadID();
            String messageServerUrl = getMessageServerURL();
            HttpClient client = resource.getHttpClient();
            String postEntity = null;
            if ( isOfficial() ){
                getThreadKey(threadID);
                postEntity = String.format(
                        Locale.US,
                        resource.getString(R.string.format_comment_get_latest_official),
                        max,
                        resource.getString(R.string.value_comment_version),
                        threadID,
                        threadKey,
                        forceValue
                );
            }else {
                postEntity = String.format(
                        Locale.US,
                        resource.getString(R.string.format_comment_get_latest),
                        max,
                        resource.getString(R.string.value_comment_version),
                        threadID
                );
            }
            if (client.post(messageServerUrl, postEntity, null)) {
                CommentInfo.CommentGroup commentGroup = new CommentInfo.CommentGroup(client.getResponse());
                synchronized (this) {
                    this.commentGroup = commentGroup;
                    return commentGroup;
                }
            } else {
                throw new NicoAPIException.HttpException(
                        "HTTP failure > comment",
                        NicoAPIException.EXCEPTION_HTTP_COMMENT,
                        client.getStatusCode(), messageServerUrl, "POST"
                );
            }
        } catch (NicoAPIException.NotInitializedException e) {
            throw new NicoAPIException.IllegalStateException("ThreadID and MessageServerUrl are unknown", NicoAPIException.EXCEPTION_ILLEGAL_STATE_COMMENT_NOT_READY);
        }
    }

    private void getThreadKey(int threadID) throws NicoAPIException{
        String path = String.format(
                Locale.US,
                resource.getURL(R.string.url_getThreadKeyAPI),
                threadID
        );
        HttpClient client = resource.getHttpClient();
        if ( client.get(path,null) ){
            Matcher matcher = resource.getPattern(R.string.regex_threadKey).matcher(client.getResponse());
            if ( matcher.find() ){
                threadKey = matcher.group(1);
                forceValue = Integer.parseInt(matcher.group(2));
            }else{
                throw new NicoAPIException.ParseException(
                        "fail to parse response from getThreadKeyAPI",
                        client.getResponse(),
                        NicoAPIException.EXCEPTION_PARSE_THREADKEY
                );
            }
        }else{
            throw new NicoAPIException.HttpException(
                    "HTTP failure while getting threadKey",
                    NicoAPIException.EXCEPTION_HTTP_THREADKEY,
                    client.getStatusCode(),path,"GET"
            );
        }
    }

    private int forceValue;
    private String threadKey;

    //Jsonでも取得できる
    public CommentGroup loadCommentInJson (int max) throws NicoAPIException{
        try{
            getThreadID();
            getMessageServerURL();
        }catch ( NicoAPIException.NotInitializedException e){
            getFlv(null);
        }
        HttpClient client = resource.getHttpClient();
        try {
            int threadID = getThreadID();
            String messageServerUrl = getMessageServerURL();
            String paramFormat = resource.getString(R.string.format_comment_get_json);
            String param = String.format(Locale.US,paramFormat, threadID, max);
            String path = messageServerUrl;
            Matcher matcher = Pattern.compile("(.+/api)/?").matcher(path);
            if (matcher.find()) {
                path = matcher.group(1) + param;
                client.get(path,null);
                CommentInfo.CommentGroup commentGroup = new CommentInfo.CommentGroup(new JSONArray(client.getResponse()));
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
                    e.getMessage(), client.getResponse(),
                    NicoAPIException.EXCEPTION_PARSE_COMMENT_JSON_META
            );
        }
    }


}
