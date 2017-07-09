package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.graphics.Bitmap;
import android.os.Parcelable;

import java.util.Date;
import java.util.List;

import jp.ac.u_tokyo.kyoyo.seo.nicoapilib.CommentInfo.*;

/**
 * このインターフェイスは動画を扱ううえでの基本的な単位となる動画オブジェクトを定義します。
 * @author Seo-4d696b75
 * @version 0.0 on 2017/06/26.
 */

public interface VideoInfo extends Parcelable{

    /**
     * 動画のコメントを取得します　Gets comments of this video.<br>
     * 取得される動画の数や選択は動画の長さに応じてメッセージサーバ側が適当に処理します。<br>
     * <strong>UIスレッド禁止</strong> HTTP通信で取得するのでバックグランドで処理してください。<br>
     * <strong>No UI Thread</strong> This communicates vis HTTP, so has to be called in background.
     * @return  Returns comments, not {@code null}
     * @throws NicoAPIException if fail to get
     */
    CommentGroup getComment() throws NicoAPIException;

    /**
     * 動画のコメントを取得します Gets comments of this video.<br>
     * 引数に渡した数だけのコメントを直近の取得します。
     * <strong>UIスレッド禁止</strong> HTTP通信で取得するのでバックグランドで処理してください。<br>
     * <strong>No UI Thread</strong> This communicates vis HTTP, so has to be called in background.
     * @param max the max number of comments
     * @return Returns comments, not {@code null}
     * @throws NicoAPIException if fail to get
     */
    CommentGroup getComment(int max) throws NicoAPIException;

    /**
     * 動画のコメント数を取得します　Gets the number of comments to this videos.<br>
     * 動画の基本的な統計属性で、この動画に対するコメントの総数を表します。
     * @return Returns the number of comments
     */
    int getCommentCounter();

    /**
     * 動画投稿者のユーザアイコン画像を取得します Gets the image of user icon of the contributor.<br>
     * 一般ユーザが投稿した動画では投稿者のユーザアイコン画像を取得します。<br>
     * <strong>注意</strong>　公式動画の場合はチャンネルアイコン画像を取得します。　
     * <strong>UIスレッド禁止</strong> HTTP通信で取得するのでバックグランドで処理してください。<br>
     * <strong>Attention</strong> In case of official video, the image of channel icon is returned.
     * <strong>No UI Thread</strong> This communicates vis HTTP, so has to be called in background.
     * @return Returns the image from the URL, which can gotten in {@link #getContributorIconURL()}, not {@code null}
     * @throws NicoAPIException if fail to get image
     */
    Bitmap getContributorIcon() throws NicoAPIException;

    /**
     * 動画投稿者のユーザアイコン画像のURLを取得します　Gets URL of the contributor's user icon.<br>
     * 取得元のＡＰＩによってはこの属性が欠損している場合はあります。
     * 欠損した状態で呼ばれると例外を投げるので事前に{@link #complete()}で補完する必要があります。<br>
     * <strong>注意</strong>　公式動画の場合はチャンネルアイコンのURLで代替されます。<br>
     * This value may be lacking in videos gotten from some API,
     * and when called in such case, an exception will be thrown, so
     * this field must be initialized by calling {@link #complete()} in advance.<br>
     * <strong>Attention</strong> This field is substituted with URL of channel icon in case of official video.
     * @return Returns the URL of the contributor's user icon or of channel icon, not {@code null}
     * @throws NicoAPIException if not initialized
     */
    String getContributorIconURL() throws NicoAPIException;

    /**
     * 動画の投稿者のユーザＩＤを取得します　Gets userID of the contributor.<br>
     * 取得元のＡＰＩによってはこの属性が欠損している場合はあります。
     * 欠損した状態で呼ばれると例外を投げるので事前に{@link #complete()}で補完する必要があります。<br>
     * <strong>注意</strong>　公式動画の場合はチャンネルＩＤで代替されます。<br>
     * This value may be lacking in videos gotten from some API,
     * and when called in such case, an exception will be thrown, so
     * this field must be initialized by calling {@link #complete()} in advance.<br>
     * <strong>Attention</strong> This field is substituted with channel ID in case of official video.
     * @return Returns the userID or the channel ID
     * @throws NicoAPIException if not initialized
     */
    int getContributorID() throws NicoAPIException;

    /**
     * 動画の投稿者のユーザ名を取得します　Gets user name of the contributor.<br>
     * 取得元のＡＰＩによってはこの属性が欠損している場合はあります。
     * 欠損した状態で呼ばれると例外を投げるので事前に{@link #complete()}で補完する必要があります。<br>
     * <strong>注意</strong>　公式動画の場合はチャンネル名で代替されます。<br>
     * This value may be lacking in videos gotten from some API,
     * and when called in such case, an exception will be thrown, so
     * this field must be initialized by calling {@link #complete()} in advance.<br>
     * <strong>Attention</strong> This field is substituted with channel name in case of official video.
     * @return Returns the user name or the channel name, not {@code null}
     * @throws NicoAPIException if not initialize
     */
    String getContributorName() throws NicoAPIException;

    /**
     * 動画の投稿日時を取得します　Gets when this video was contributed.
     * @return Returns the date when this video was post, not {@code null}
     */
    Date getDate();

    /**
     * 動画の説明を取得します　Gets the description of this video.<br>
     * 投稿者が投稿時に動画に添える説明文で、動画によっては設定されておらず空文字が返る場合があります。
     * 取得元のＡＰＩによってはこの属性が欠損している場合はあります。
     * 欠損した状態で呼ばれると例外を投げるので事前に{@link #complete()}で補完する必要があります。<br>
     * This description is post with the video together by the contributor.
     * Depending on video, this field may be empty.
     * This value may be lacking in videos gotten from some API,
     * and when called in such case, an exception will be thrown, so
     * this field must be initialized by calling {@link #complete()} in advance.
     * @return Returns the description, not {@code null}
     * @throws NicoAPIException if not initialized
     */
    String getDescription() throws NicoAPIException;

    /**
     * 動画ファイルのURLを取得します Gets URL of this video file.<br>
     * 通常のAPIから取得した動画ではこの値は欠損しています。
     * 欠損した状態で呼ばれると例外を投げるので事前に{@link #getFlv(CookieGroup)}で補完する必要があります。<br>
     * This value will be lacking in videos gotten from usual API,
     * and when called in such case, an exception will be thrown, so
     * this field must be initialized by calling {@link #getFlv(CookieGroup)} in advance.
     * @return Returns the URL, not {@code null}
     * @throws NicoAPIException if not initialized
     */
    String getFlvURL() throws NicoAPIException;

    /**
     * 動画ＩＤを取得します　Gets the videoID.<br>
     * 動画を一意に区別する基本となる動画属性であり、動画視聴URLの末尾にある"sm******"この値です
     * 通常、取得されたすべての動画でこのフィールド値は保証されます。
     * 動画ＩＤには大きく分けて"sm********"と"so*********"の二種類が存在し、
     * 後者は公式動画を表します。公式動画の場合、一部フィールドの内容が通常と異なりますので注意してください。<br>
     * {@link #getContributorName()}　投稿者名→チャンネル名<br>
     * {@link #getContributorID()}　投稿者ID→チャンネルID<br>
     * {@link #getContributorIconURL()}　投稿者アイコンURL→チャンネルアイコンURL<br>
     * {@link #getContributorIcon()}　投稿者アイコン→チャンネルアイコン<br>
     * Each video is identified with this value, which you can find in the end of watch URL.
     * This value is usually set in any video.<br>
     * Video ID is categorized to two types; "sm********" and "so*********".
     * The latter stands for an official video.
     * In case of an official video, some fields have different value than usual;<br>
     * {@link #getContributorName()}　contributor name→channel name<br>
     * {@link #getContributorID} contributor ID→channel ID<br>
     * {@link #getContributorIconURL}　contributor icon URL→channel icon URL<br>
     * {@link #getContributorIcon}　contributor icon→channel icon<br>
     * @return Returns the videoID, not {@code null}
     */
    String getID();

    /**
     * 動画の長さを取得します　Gets the length of this video.<br>
     * 秒単位の値で、通常この値は全ての動画オブジェクトで保証されます。
     * @return Returns the length of this video in seconds
     */
    int getLength();

    /**
     * 動画のメッセージサーバーのURLを取得します　Gets URL of message server.<br>
     * 動画のコメントを取得したり投稿するときにこのサーバと通信します。
     * ただし、通常のAPIから取得した動画ではこの値は欠損しています。
     * 欠損した状態で呼ばれると例外を投げるので事前に{@link #getFlv(CookieGroup)}で補完する必要があります。<br>
     * Comments post to this video can be gotten from the sever specified with that URL.
     * This value will be lacking in videos gotten from usual API,
     * and when called in such case, an exception will be thrown, so
     * this field must be initialized by calling {@link #getFlv(CookieGroup)} in advance.
     * @return Returns the URL of message server, not {@code null}
     * @throws NicoAPIException if not initialized
     */
    String getMessageServerURL() throws NicoAPIException;

    /**
     * 動画のマイリス数を取得します　Gets the number of myList registration.<br>
     * 動画の基本的な統計属性で、この動画がマイリストへ登録された回数を表します。
     * @return Returns the number of myList registration
     */
    int getMyListCounter();

    /**
     * 動画のタグ一覧を取得します Gets a list of tags associated with this video.<br>
     * 多くの取得元ＡＰＩでこの属性が欠損している場合があります。
     * 欠損した状態で呼ばれると例外を投げるので事前に{@link #complete()}で補完する必要があります。<br>
     * This value may be lacking in videos gotten from some API,
     * and when called in such case, an exception will be thrown, so
     * this field must be initialized by calling {@link #complete()} in advance.
     * @return Returns the {@code List} objects of tags, not {@code null}
     * @throws NicoAPIException if not initialized
     */
    List<String> getTags() throws NicoAPIException;

    /**
     * 動画のスレッドID取得します　Gets threadID of this video.<br>
     * スレッドIDは動画を一意に区別する値です。{@link #getID() 動画ID}も同様の性質をもつ値ですが、
     * コメントやマイリス関連の一部ＡＰＩでは動画の指定にこの値が使用されます。
     * 通常のAPIから取得した動画ではこの値は欠損しています。
     * 欠損した状態で呼ばれると例外を投げるので事前に{@link #getFlv(CookieGroup)}で補完する必要があります。<br>
     * This threadID is used to identify each video than others.
     * {@link #getID() VideoID} can also used for the same purpose, but
     * some API, such as APIs around comments and myList, require using this threadID to specify the video.
     * This value will be lacking in videos gotten from usual API,
     * and when called in such case, an exception will be thrown, so
     * this field must be initialized by calling {@link #getFlv(CookieGroup)} in advance.
     * @return Returns the threadID of this video
     * @throws NicoAPIException if not initialized
     */
    int getThreadID() throws NicoAPIException;

    /**
     * 動画サムネル画像を取得します　Gets the thumbnail image of this video.<br>
     * <strong>UIスレッド禁止</strong> HTTP通信で取得するのでバックグランドで処理してください。<br>
     * <strong>No UI Thread</strong> This communicates vis HTTP, so has to be called in background.
     * @return Returns the thumbnail image, not {@code null}
     * @throws NicoAPIException if fail to get the image
     */
    Bitmap getThumbnail() throws NicoAPIException;

    /**
     * 動画サムネイル画像のＵＲＬを取得します　Gets the URL of this video thumbnail.<br>
     * 取得元のＡＰＩによってはこの属性が欠損している場合はあります。
     * 欠損した状態で呼ばれると例外を投げるので事前に{@link #complete()}で補完する必要があります。<br>
     * This value may be lacking in videos gotten from some API,
     * and when called in such case, an exception will be thrown, so
     * this field must be initialized by calling {@link #complete()} in advance.
     * @return Returns the URL of this video thumbnail image, not {@code null}
     * @throws NicoAPIException if not initialized
     */
    String getThumbnailURL() throws NicoAPIException;

    /**
     * 動画のタイトルを取得します Gets the title of this video.<br>
     * 動画の最も基本的な属性であり、通常どのＡＰＩから取得してもこの値は保証されます。
     * ただし、動画を一意に区別するには動画ＩＤもしくはスレッドＩＤで判定するようにしてください。<br>
     * This value is a basic field of this video, and is usually guaranteed to be initialized.
     * But, when the specified video is to be identified, it is recommended to use videoID or threadID, instead of this value.
     * @return Returns the title, not {@code null}
     */
    String getTitle();

    /**
     * 動画の再生回数を取得します　Gets how many times this video is played.<br>
     * 動画の基本的な統計属性で、この動画が再生された総数を表します。
     * @return Returns the times
     */
    int getViewCounter();

    /**
     * 公式動画であるか取得します　Gets whether this video is official.<br>
     * @return Returns {@code true} if this video is official one
     */
    boolean isOfficial();

    String formatViewCounter();
    String formatCommentCounter();
    String formatMyListCounter();


    String formatDate();
    String formatLength();

    /**
     * 欠損した動画のフィールド値を取得します<br>
     * Gets lacking fields of this video.<br>
     * 動画の取得元APIによっては欠損したフィールド値が存在しますので注意してください。
     * このメソッドで取得できるフィールドは以下の通りです<br>
     *     動画タイトル<br>
     *     動画ID<br>
     *     動画説明<br>
     *     動画長さ<br>
     *     動画投稿日時<br>
     *     再生数<br>
     *     コメント数<br>
     *     マイリス数<br>
     *     サムネイル画像のURL<br>
     *     動画タグ<br>
     *     投稿者のユーザIDまたはチャンネルID<br>
     *     投稿者のニックネームまたはチャンネル名<br>
     *     投稿者のユーザアイコンまたはチャンネルアイコン画像のURL<br>
     * <strong>UIスレッド禁止</strong> HTTP通信で取得するのでバックグランドで処理してください。<br>
     * <strong>No UI Thread</strong> This communicates vis HTTP, so has to be called in background.<br>
     * Be careful that there can be lacking fields according to API.
     * By calling this, you can get;<br>
     *     title of video<br>
     *     video ID<br>
     *     video description<br>
     *     length<br>
     *     contributed date<br>
     *     number of view<br>
     *     number of comment<br>
     *     number of myList registered<br>
     *     url of thumbnail<br>
     *     video tag<br>
     *     user ID of contributor or channel ID<br>
     *     user name of contubutor or channel name<br>
     *     url of contributor icon or channel<br>
     * @return Returns {@code true} if succeed
     */
    boolean complete();

    /**
     * スレッドID,メッセージサーバURL,flvURLを取得します<br>
     * Gets threadID, URL of message server and flv URL.<br>
     * 必ずニコ動へのログインが必要で、そのセッション情報を格納したCookieを引数に渡します。
     * 引数のcookieを省略した場合は動画取得時に保持していたセッション情報で代用します。　
     * <strong>注意</strong> 非ログイン状態で取得した動画にはログイン情報が保持されていないため引数を{@code null}で省略した場合は{@code false}を返します。
     * また、公式動画はAPIからのレスポンスが空の場合があり、取得出来ても空文字で初期化される恐れがあります。
     * <strong>UIスレッド禁止</strong> HTTP通信で取得するのでバックグランドで処理してください。<br>
     * Calling this method requires login to Nico.
     * You have to pass that Cookie which stores the login session.
     * In case the cookie is not passed, it is substituted with that which was being held at the time this video was gotten.
     * <strong>Warning </strong> The video which was gotten without login has no login session in its field, so in this case,
     * this method returns {@code false} when called with no cookie param.　
     * Also, in case of official video, it is afraid that some parts of API response may be empty and that some fields may be initialized with empty string.
     * <strong>No UI Thread</strong> This communicates vis HTTP, so has to be called in background.
     * @param cookies the Nico login session, may be {@code null}
     * @return Returns {@code true} if succeed
     */
    boolean getFlv(CookieGroup cookies);

    int TITLE = 4;
    int ID = 5;
    int DATE = 6;
    int DESCRIPTION = 7;
    int THUMBNAIL_URL = 8;
    int LENGTH = 9;
    int VIEW_COUNTER = 10;
    int COMMENT_COUNTER = 11;
    int MY_LIST_COUNTER = 12;
    int TAGS = 13;
    int TAG = 14;
    int THREAD_ID = 15;
    int MESSAGE_SERVER_URL = 16;
    int FLV_URL = 17;
    int CONTRIBUTOR_ID = 18;
    int CONTRIBUTOR_NAME = 19;
    int CONTRIBUTOR_ICON_URL = 20;

    String VIDEO_KEY = "videoInfoObject";
}
