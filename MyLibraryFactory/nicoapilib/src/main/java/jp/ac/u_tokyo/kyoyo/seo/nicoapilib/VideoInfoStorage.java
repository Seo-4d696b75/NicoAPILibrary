package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.graphics.drawable.Drawable;
import org.apache.http.client.CookieStore;
import java.util.ArrayList;
import java.util.List;

/**
 * 各種APIから取得してきた動画情報を動画単位で管理します<br>
 *     This manages information of each video.<br>
 * ニコ動から取得したすべての動画情報はこのクラスを継承した子クランスのインスタンスで管理されます。
 * 各種フィールドは継承先から設定される構造を想定しています。
 * 取得元のAPI毎に対応した子供クラスが存在し、APIからの情報の処理を定義します。<br>
 * ランキング/マイリスト：{@link RankingVideoInfo}<br>
 * 検索：{@link SearchVideoInfo}<br>
 * とりあえずマイリス：{@link MyListVideoInfo}<br>
 * おすすめ動画：{@link RecommendVideoInfo}<br>
 * ただし、各APIによって取得できるフィールドは異なりますので注意してください。
 * 欠損したフィールド値は{@link VideoInfo#complete()}、
 * {@link VideoInfo#getFlv(CookieStore)}で補うことができます。<br>
 * All the videos from Nico are managed by child classes extending this.
 * All the fields are supposed to be set by these child classes.
 * There are corresponding child class to each API, which defines how to parse response from it.<br>
 * ranking/myList：{@link RankingVideoInfo}<br>
 * search：{@link SearchVideoInfo}<br>
 * temp myList：{@link MyListVideoInfo}<br>
 * recommended video：{@link RecommendVideoInfo}<br>
 * Be careful that some fields may not be initialized depending on each API.
 * You can get these lacking field by calling {@link VideoInfo#complete()}, {@link VideoInfo#getFlv(CookieStore)}
 *
 * @author Seo-4d696b75
 * @version 0.0 on 2016/12/17.
 */


public class VideoInfoStorage {

    /**
     * In usual, not set in any field.<br>
     * Video title and its ID are supposed to be set whenever succeeding in parsing response from Nico.
     * But if not, return this value.
     */
    public final String UNKNOWN = "unknown";

    //in case of ranking
    /**
     * ランキングのジャンルパラメータ（ランキング限定）<br>
     *     genre (category tag) param in ranking (ranking only>.
     */
    protected String genre;
    /**
     * ランキングの種類パラメータ（ランキング限定）<br>
     *     kind param in ranking (ranking only>.
     */
    protected String rankKind;
    /**
     * ランキング期間パラメータ（ランキング限定）<br>
     *     period param in ranking (ranking only>.
     */
    protected String period;
    /**
     * ランキングの発表日時（ランキング限定）<br>
     *     ranking published date (ranking only>.<br>
     *     ライブラリ内の{@link VideoInfo#dateFormatBase 共通形式}に従います<br>
     *     this follows {@link VideoInfo#dateFormatBase common format} in this library.
     */
    protected String pubDate;

    //common
    /**
     * 動画のタイトル<br>
     *     title of video.<br>
     * 取得されたすべての動画でこのフィールド値は保証されます。<br>
     * This value is always set in any video.
     */
    protected String title;
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
    protected String id;
    private boolean official = false;
    /**
     * 動画の投稿日時<br>
     *     contribution date.<br>
     *     ライブラリ内の{@link VideoInfo#dateFormatBase 共通形式}に従います<br>
     *     this follows {@link VideoInfo#dateFormatBase common format} in this library.
     */
    protected String date;
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
    protected List<String> thumbnailUrl;
    /**
     * Androidでの仕様の前提とした動画のサムネイル画像<br>
     * thumbnail image, which is supposed to be used in Android.<br>
     */
    protected Drawable thumbnail;
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
    protected int threadID;
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
    protected Drawable contributorIcon;

    //comment
    protected CommentInfo.CommentGroup commentGroup;

    public static final int GENRE = 0;
    public static final int RANK_KIND = 1;
    public static final int PERIOD = 2;
    public static final int PUB_DATE = 3;
    public static final int TITLE = 4;
    public static final int ID = 5;
    public static final int DATE = 6;
    public static final int DESCRIPTION = 7;
    public static final int THUMBNAIL_URL = 8;
    public static final int LENGTH = 9;
    public static final int VIEW_COUNTER = 10;
    public static final int COMMENT_COUNTER = 11;
    public static final int MY_LIST_COUNTER = 12;
    public static final int TAGS = 13;
    public static final int TAG = 14;
    public static final int THREAD_ID = 15;
    public static final int MESSAGE_SERVER_URL = 16;
    public static final int FLV_URL = 17;
    public static final int CONTRIBUTOR_ID = 18;
    public static final int CONTRIBUTOR_NAME = 19;
    public static final int CONTRIBUTOR_ICON_URL = 20;

    protected VideoInfoStorage(){}

    protected void setID(String id){
        this.id = id;
        if ( id.indexOf("so") >= 0 ){
            official = true;
        }
    }
    public String getDate() throws NicoAPIException{
        return getString(DATE);
    }
    public String getTitle() {
        try {
            return getString(TITLE);
        }catch (NicoAPIException e){
            return UNKNOWN;
        }
    }
    public String getID() {
        try {
            return getString(ID);
        }catch (NicoAPIException e){
            e.printStackTrace();
            return UNKNOWN;
        }
    }
    public boolean isOfficial(){
        return official;
    }
    public String getDescription() throws NicoAPIException{
        return getString(DESCRIPTION);
    }
    public int getThreadID() throws NicoAPIException{
        return getInt(THREAD_ID);
    }
    public String getMessageServerUrl() throws NicoAPIException{
        return getString(MESSAGE_SERVER_URL);
    }
    public String getFlvUrl() throws NicoAPIException{
        return getString(FLV_URL);
    }
    public String getContributorName() throws NicoAPIException{
        return getString(CONTRIBUTOR_NAME);
    }
    public String getContributorIconUrl() throws NicoAPIException{
        return getString(CONTRIBUTOR_ICON_URL);
    }
    public synchronized String getString(int key) throws NicoAPIException{
        String target = null;
        switch ( key ){
            case GENRE:
                target = genre;
                break;
            case RANK_KIND:
                target = rankKind;
                break;
            case PERIOD:
                target = period;
                break;
            case PUB_DATE:
                target = pubDate;
                break;
            case TITLE:
                target = title;
                break;
            case ID:
                target = id;
                break;
            case DATE:
                target = date;
                break;
            case DESCRIPTION:
                target = description;
                break;
            case MESSAGE_SERVER_URL:
                target = messageServerUrl;
                break;
            case FLV_URL:
                target = flvUrl;
                break;
            case CONTRIBUTOR_NAME:
                target = contributorName;
                break;
            case CONTRIBUTOR_ICON_URL:
                target = contributorIconUrl;
                break;
            default:
                throw new NicoAPIException.InvalidParamsException("invalid video filed key : " + key);
        }
        if ( target == null ){
            throw new NicoAPIException.NotInitializedException("requested video field not initialized",key);
        }else{
            return target;
        }
    }
    public int getLength(){
        try {
            return getInt(LENGTH);
        }catch (NicoAPIException e){
            return 0;
        }
    }
    public int getViewCounter() throws NicoAPIException{
        return getInt(VIEW_COUNTER);
    }
    public int getMyListCounter() throws NicoAPIException{
        return getInt(MY_LIST_COUNTER);
    }
    public int getCommentCounter() throws NicoAPIException{
        return getInt(COMMENT_COUNTER);
    }
    public int getContributorID() throws NicoAPIException{
        return getInt(CONTRIBUTOR_ID);
    }
    public synchronized int getInt(int key) throws NicoAPIException{
        int target = -1;
        switch ( key ){
            case LENGTH:
                target = length;
                break;
            case VIEW_COUNTER:
                target = viewCounter;
                break;
            case COMMENT_COUNTER:
                target = commentCounter;
                break;
            case MY_LIST_COUNTER:
                target = myListCounter;
                break;
            case CONTRIBUTOR_ID:
                target = contributorID;
                break;
            case THREAD_ID:
                target = threadID;
                break;
            default:
                throw new NicoAPIException.InvalidParamsException("invalid video filed key : " + key);
        }
        if ( target < 0 ){
            throw new NicoAPIException.NotInitializedException("requested video field not initialized",key);
        }else{
            return  target;
        }
    }

    /**
     * 動画タグを設定します,配列はList<String>に変換されます</String><br>
     *  Sets video tags.
     * @param tags can not be {@code null}
     */
    protected synchronized void setTags(String[] tags){
        if ( this.tags == null ){
            this.tags = new ArrayList<String>();
            for ( String tag : tags){
                this.tags.add(tag);
            }
        }
    }
    /**
     * 動画タグを設定します<br>
     *     Sets video tags.
     * @param tags can not be {@code null}
     */
    protected synchronized void setTags(List<String> tags){
        if ( tags != null ){
            this.tags = tags;
        }
    }
    /**
     * 動画のタグをリストで取得します<br>
     *     Gets video tags in List.<br>
     *     取得元のAPIによっては欠損している場合があります。{@link VideoInfo 詳細はこちら}<br>
     *     This field may be lacking depending on API, {@link VideoInfo details are here}.
     * @return Returns {@code null} if not initialized
     */
    public synchronized List<String> getTagsList() throws NicoAPIException.NotInitializedException{
        if ( tags == null ) {
            throw new NicoAPIException.NotInitializedException("requested video tags not initialized > " + id, TAGS);
        }else{
            return tags;
        }
    }
    /**
     * 動画のタグを配列で取得します<br>
     *     Gets video tags in array.<br>
     *     取得元のAPIによっては欠損している場合があります。{@link VideoInfo 詳細はこちら}<br>
     *     This field may be lacking depending on API, {@link VideoInfo details are here}.
     * @return Returns {@code null} if not initialized
     */
    public synchronized String[] getTags() throws NicoAPIException.NotInitializedException{
        if ( tags == null ){
            throw new NicoAPIException.NotInitializedException("requested video tags not initialized > " + id,TAGS);
        }
        String[] tags = new String[this.tags.size()];
        for ( int i=0 ; i<tags.length ; i++){
            tags[i] = this.tags.get(i);
        }
        return tags;
    }

    /**
     * 動画サムネイル画像のURLを配列で設定します<br>
     *     Sets thumbnail image urls in array.
     * @param thumbnailUrl can not be {@code null}
     */
    protected synchronized void setThumbnailUrl (String[] thumbnailUrl){
        this.thumbnailUrl = new ArrayList<String>();
        for ( String url : thumbnailUrl ){
            this.thumbnailUrl.add(url);
        }
    }
    /**
     * 動画サムネイル画像のURLをリストで設定します<br>
     *     Sets thumbnail image urls in List.
     * @param thumbnailUrl can not be {@code null}
     */
    protected synchronized void setThumbnailUrl (List<String> thumbnailUrl){
        this.thumbnailUrl = thumbnailUrl;
    }
    /**
     * 動画サムネイル画像のURLを追加します<br>
     *     Adds thumbnail image URL.
     * @param url can not be {@code null}
     */
    protected synchronized void setThumbnailUrl(String url){
        if ( thumbnailUrl == null){
            thumbnailUrl = new ArrayList<String>();
        }
        thumbnailUrl.add(url);
    }

    public synchronized String getThumbnailUrl (boolean isHigh) throws NicoAPIException.NotInitializedException{
        if ( thumbnailUrl == null || thumbnailUrl.isEmpty() ){
            throw new NicoAPIException.NotInitializedException("requested video thumbnail URL not initialized > " + id, THUMBNAIL_URL);
        }
        if ( isHigh ){
            return thumbnailUrl.get(thumbnailUrl.size()-1);
        }else{
            return thumbnailUrl.get(0);
        }
    }
    /**
     * 動画のサムネイル画像URLを取得します<br>
     *     Gets URL from which you can get thumbnail image.
     * @return can be {@code null} if not initialized
     */
    public synchronized String getThumbnailUrl () throws NicoAPIException.NotInitializedException{
        return getThumbnailUrl(false);
    }
    public synchronized String[] getThumbnailUrlArray() throws NicoAPIException.NotInitializedException{
        if ( thumbnailUrl == null || thumbnailUrl.isEmpty() ){
            throw new NicoAPIException.NotInitializedException("requested video thumbnail URL not initialized > " + id, THUMBNAIL_URL);
        }
        String[] array = new String[thumbnailUrl.size()];
        for ( int i=0 ; i<array.length ; i++){
            array[i] = thumbnailUrl.get(i);
        }
        return array;
    }

    public float getPoint(){
        return point;
    }

    /**
     * フィールドを保存したまま{@link VideoInfoPackage}クラスのインスタンスに変換します
     * Converts itself into {@link VideoInfoPackage} instance keeping all the fields.
     * @return Returns serializable instance
     */
    public synchronized VideoInfoPackage pack() throws NicoAPIException{
        return new VideoInfoPackage(this);
    }

}

