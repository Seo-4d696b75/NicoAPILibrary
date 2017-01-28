package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.graphics.drawable.Drawable;

import org.apache.http.client.CookieStore;

import java.io.Serializable;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 各種APIから取得してきた動画情報を動画単位で管理します<br>
 *     manage information of each video.<br>
 * ニコ動から取得したすべての動画情報はこのクラスを継承した子クランスのインスタンスで管理されます。
 * 各種フィールドは継承先から設定される構造を想定しています。
 * 取得元のAPI毎に対応した子供クラスが存在し、APIからの情報の処理を定義します。<br>
 * ランキング/マイリスト：{@link RankingVideoInfo}<br>
 * 検索：{@link SearchVideoInfo}<br>
 * とりあえずマイリス：{@link TempMyListVideoInfo}<br>
 * おすすめ動画：{@link RecommendVideoInfo}<br>
 * ただし、各APIによって取得できるフィールドは異なりますので注意してください。
 * 欠損したフィールド値は{@link VideoInfoManager#complete()}、
 * {@link VideoInfoManager#getFlv(CookieStore)}で補うことができます。<br>
 * all the videos from Nico are managed by child classes extending this.
 * all the fields are supposed to be set by these child classes.
 * there are corresponding child class to each API, which defines how to parse response from it.
 * ranking/myList：{@link RankingVideoInfo}<br>
 * search：{@link SearchVideoInfo}<br>
 * temp myList：{@link TempMyListVideoInfo}<br>
 * recommended video：{@link RecommendVideoInfo}<br>
 * be careful that some fields may not be initialized depending on each API.
 * you can get these lacked field by calling {@link VideoInfoManager#complete()}, {@link VideoInfoManager#getFlv(CookieStore)}
 *
 * @author Seo-4d696b75
 * @version 0.0 on 2016/12/17.
 */


public class VideoInfo {

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
     *     ライブラリ内の{@link VideoInfoManager#dateFormatBase 共通形式}に従います<br>
     *     this follows {@link VideoInfoManager#dateFormatBase common format} in this library.
     */
    protected String pubDate;

    //common
    /**
     * 動画のタイトル<br>
     *     title of video
     */
    protected String title;
    /**
     * 動画ID、動画視聴URLの末尾にある"sm******"これです。<br>
     *     video ID, which you can find in the end of watch URL.
     */
    protected String id;
    /**
     * 動画の投稿日時<br>
     *     contribution date.<br>
     *     ライブラリ内の{@link VideoInfoManager#dateFormatBase 共通形式}に従います<br>
     *     this follows {@link VideoInfoManager#dateFormatBase common format} in this library.
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
    protected String threadID;
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
     *     user ID of video contributor.
     */
    protected int contributorID = -1;
    /**
     * 投稿者のユーザ名<br>
     *     name of contributor.
     */
    protected String contributorName;
    /**
     * 投稿者のユーザアイコンのURL,画像はJPG<br>
     *     URL from which you can get user icon image of contributor.
     */
    protected String contributorIconUrl;
    /**
     * Androidでの使用が前提、投稿者のユーザアイコン<br>
     *     user icon image of contributor, supposed to be used in Android.
     */
    protected Drawable contributorIcon;

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

    protected VideoInfo(){}
    public String getString(int key){
        switch ( key ){
            case GENRE:
                return genre;
            case RANK_KIND:
                return rankKind;
            case PERIOD:
                return period;
            case PUB_DATE:
                return pubDate;
            case TITLE:
                return title;
            case ID:
                return id;
            case DATE:
                return date;
            case DESCRIPTION:
                return description;
            case THREAD_ID:
                return threadID;
            case MESSAGE_SERVER_URL:
                return messageServerUrl;
            case FLV_URL:
                return flvUrl;
            case CONTRIBUTOR_NAME:
                return contributorName;
            case CONTRIBUTOR_ICON_URL:
                return contributorIconUrl;
            default:
                return null;
        }
    }
    public int getInt(int key){
        switch ( key ){
            case LENGTH:
                return length;
            case VIEW_COUNTER:
                return viewCounter;
            case COMMENT_COUNTER:
                return commentCounter;
            case MY_LIST_COUNTER:
                return myListCounter;
            case CONTRIBUTOR_ID:
                return contributorID;
            default:
                return 0;
        }
    }

    /**
     * 動画タグを設定します,配列はList<String>に変換されます</String><br>
     *     set video tags.
     * @param tags can not be {@code null}
     */
    protected void setTags(String[] tags){
        if ( this.tags == null ){
            this.tags = new ArrayList<String>();
            for ( String tag : tags){
                this.tags.add(tag);
            }
        }
    }

    /**
     * 動画タグを設定します<br>
     *     set video tags.
     * @param tags can not be {@code null}
     */
    protected void setTags(List<String> tags){
        if ( tags != null ){
            this.tags = tags;
        }
    }

    /**
     * 動画のタグをリストで取得します<br>
     *     get video tags in List.<br>
     *     取得元のAPIによっては欠損している場合があります。{@link VideoInfo 詳細はこちら}<br>
     *     this field may be lacking depending on API, {@link VideoInfo details are here}.
     * @return Returns {@code null} if not initialized
     */
    public List<String> getTagsList(){
        return tags;
    }
    /**
     * 動画のタグを配列で取得します<br>
     *     get video tags in array.<br>
     *     取得元のAPIによっては欠損している場合があります。{@link VideoInfo 詳細はこちら}<br>
     *     this field may be lacking depending on API, {@link VideoInfo details are here}.
     * @return Returns {@code null} if not initialized
     */
    public String[] getTags(){
        String[] tags = new String[this.tags.size()];
        for ( int i=0 ; i<tags.length ; i++){
            tags[i] = this.tags.get(i);
        }
        return tags;
    }

    /**
     * 動画サムネイル画像のURLを配列で設定します<br>
     *     set thumbnail image urls in array.
     * @param thumbnailUrl can not be {@code null}
     */
    protected void setThumbnailUrl (String[] thumbnailUrl){
        this.thumbnailUrl = new ArrayList<String>();
        for ( String url : thumbnailUrl ){
            this.thumbnailUrl.add(url);
        }
    }
    /**
     * 動画サムネイル画像のURLをリストで設定します<br>
     *     set thumbnail image urls in List.
     * @param thumbnailUrl can not be {@code null}
     */
    protected void setThumbnailUrl (List<String> thumbnailUrl){
        this.thumbnailUrl = thumbnailUrl;
    }

    /**
     * 動画サムネイル画像のURLを追加します<br>
     *     add thumbnail image URL.
     * @param url can not be {@code null}
     */
    protected void setThumbnailUrl(String url){
        if ( thumbnailUrl == null){
            thumbnailUrl = new ArrayList<String>();
        }
        thumbnailUrl.add(url);
    }

    public String getThumbnailUrl (boolean isHigh){
        if ( thumbnailUrl == null || thumbnailUrl.isEmpty() ){
            return null;
        }
        if ( isHigh ){
            return thumbnailUrl.get(thumbnailUrl.size()-1);
        }else{
            return thumbnailUrl.get(0);
        }
    }

    /**
     * 動画のサムネイル画像URLを取得します<br>
     *     get URL from which you can get thumbnail image.
     * @return can be {@code null} if not initialized
     */
    public String getThumbnailUrl (){
        return getThumbnailUrl(false);
    }
    public String[] getThumbnailUrlArray() {
        if ( thumbnailUrl == null || thumbnailUrl.isEmpty() ){
            return null;
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
     * convert to {@link VideoInfoPackage} instance keeping all the fields.
     * @return Returns serializable instance
     */
    public VideoInfoPackage pack(){
        return new VideoInfoPackage(this);
    }

    //safe down cast
    /*
    public VideoInfoManager downCast(){
        if (  this instanceof VideoInfo ) {
            VideoInfoManager info = new VideoInfoManager();
            info.setString(VideoInfo.GENRE, genre);
            info.setString(VideoInfo.RANK_KIND, rankKind);
            info.setString(VideoInfo.PERIOD, period);
            info.setString(VideoInfo.PUB_DATE, pubDate);
            info.setString(VideoInfo.TITLE, title);
            info.setString(VideoInfo.ID, id);
            info.setString(VideoInfo.DATE, date);
            info.setString(VideoInfo.DESCRIPTION, description);
            info.setThumbnailUrl(thumbnailUrl);
            info.setInt(VideoInfo.LENGTH, length);
            info.setInt(VideoInfo.VIEW_COUNTER, viewCounter);
            info.setInt(VideoInfo.COMMENT_COUNTER, commentCounter);
            info.setInt(VideoInfo.MY_LIST_COUNTER, myListCounter);
            info.setTags(tags);
            return info;
        }
        return (VideoInfoManager)this;
    }*/

}

