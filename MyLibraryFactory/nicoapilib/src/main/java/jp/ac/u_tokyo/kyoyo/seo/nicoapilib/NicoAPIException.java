package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import org.apache.http.client.CookieStore;

import java.util.HashMap;
import java.util.Map;


/**
 * この例外クラスはライブラリ内で発生したエラーを区分するものです<br>
 * This class extending Exception helps you recognize what king of error happened.<br><br>
 *
 *
 * @author Seo-4d696b75
 * @version 0.0 on 2017/01/31.
 */
/*
NotInitializedException 対象の動画フィールドが初期化されていない
DrawableFailureException    画像の取得に失敗
ParseException  ＡＰＩからのレスポンスのパースに失敗
NoLoginException    ログインが必要な機能
InvalidParamsException  渡されたパラメータが不正
APIUnexpectedException 予期されないＡＰＩからのレスポンス
*/

public class NicoAPIException extends Exception {

    private int code = 0;
    private NicoAPIException (String message){
        super(message);
    }
    private NicoAPIException (String message, int code ){
        super(message);
        this.code = code;
    }
    public int getCode (){
        return code;
    }

    /**
     * try to get {@link VideoInfo#genre} of video, but this value is not set yet.
     * at {@link VideoInfo#getString(int)}
     */
    public static final int EXCEPTION_NOT_INITIALIZED_GENRE = VideoInfo.GENRE;
    /**
     * try to get {@link VideoInfo#rankKind} of video, but this value is not set yet.
     * at {@link VideoInfo#getString(int)}
     */
    public static final int EXCEPTION_NOT_INITIALIZED_RANK_KIND = VideoInfo.RANK_KIND;
    /**
     * try to get {@link VideoInfo#period} of video, but this value is not set yet.
     * at {@link VideoInfo#getString(int)}
     */
    public static final int EXCEPTION_NOT_INITIALIZED_PERIOD = VideoInfo.PERIOD;
    /**
     * try to get {@link VideoInfo#pubDate} of video, but this value is not set yet.
     * at {@link VideoInfo#getString(int)}
     */
    public static final int EXCEPTION_NOT_INITIALIZED_PUB_DATE = VideoInfo.PUB_DATE;
    /**
     * try to get {@link VideoInfo#date} of video, but this value is not set yet.
     * at {@link VideoInfo#getDate()}, {@link VideoInfo#getString(int)}
     */
    public static final int EXCEPTION_NOT_INITIALIZED_DATE = VideoInfo.DATE;
    /**
     * try to get {@link VideoInfo#description} of video, but this value is not set yet.
     * at {@link VideoInfo#getDescription()}, {@link VideoInfo#getString(int)}
     */
    public static final int EXCEPTION_NOT_INITIALIZED_DESCRIPTION = VideoInfo.DESCRIPTION;
    /**
     * try to get {@link VideoInfo#threadID} of video, but this value is not set yet.
     * at {@link VideoInfo#getThreadID()}, {@link VideoInfo#getString(int)}
     */
    public static final int EXCEPTION_NOT_INITIALIZED_THREAD_ID = VideoInfo.THREAD_ID;
    /**
     * try to get {@link VideoInfo#messageServerUrl} of video, but this value is not set yet.
     * at {@link VideoInfo#getMessageServerUrl()}, {@link VideoInfo#getString(int)}
     */
    public static final int EXCEPTION_NOT_INITIALIZED_MESSAGE_SERVER = VideoInfo.MESSAGE_SERVER_URL;
    /**
     * try to get {@link VideoInfo#flvUrl} of video, but this value is not set yet.
     * at {@link VideoInfo#getFlvUrl()} ()}, {@link VideoInfo#getString(int)}
     */
    public static final int EXCEPTION_NOT_INITIALIZED_FLV_URL = VideoInfo.FLV_URL;
    /**
     * try to get {@link VideoInfo#contributorName} of video, but this value is not set yet.
     * at {@link VideoInfo#getContributorName()}, {@link VideoInfo#getString(int)}
     */
    public static final int EXCEPTION_NOT_INITIALIZED_CONTRIBUTOR_NAME = VideoInfo.CONTRIBUTOR_NAME;
    /**
     * try to get {@link VideoInfo#contributorIconUrl} of video, but this value is not set yet.
     * at {@link VideoInfo#getContributorIconUrl()} ()}, {@link VideoInfo#getString(int)}
     */
    public static final int EXCEPTION_NOT_INITIALIZED_CONTRIBUTOR_ICON_URL = VideoInfo.CONTRIBUTOR_ICON_URL;
    /**
     * try to get {@link VideoInfo#length} of video, but this value is not set yet.
     * at {@link VideoInfo#getLength()} ()}, {@link VideoInfo#getInt(int)}
     */
    public static final int EXCEPTION_NOT_INITIALIZED_LENGTH = VideoInfo.LENGTH;
    /**
     * try to get {@link VideoInfo#viewCounter} of video, but this value is not set yet.
     * at {@link VideoInfo#getViewCounter()} ()}, {@link VideoInfo#getInt(int)}
     */
    public static final int EXCEPTION_NOT_INITIALIZED_VIEW_COUNTER = VideoInfo.VIEW_COUNTER;
    /**
     * try to get {@link VideoInfo#commentCounter} of video, but this value is not set yet.
     * at {@link VideoInfo#getCommentCounter()} ()}, {@link VideoInfo#getInt(int)}
     */
    public static final int EXCEPTION_NOT_INITIALIZED_COMMENT_COUNTER = VideoInfo.COMMENT_COUNTER;
    /**
     * try to get {@link VideoInfo#myListCounter} of video, but this value is not set yet.
     * at {@link VideoInfo#getMyListCounter()} ()}, {@link VideoInfo#getInt(int)}
     */
    public static final int EXCEPTION_NOT_INITIALIZED_MY_LIST_COUNTER = VideoInfo.MY_LIST_COUNTER;
    /**
     * try to get {@link VideoInfo#contributorID} of video, but this value is not set yet.
     * at {@link VideoInfo#getContributorID()} ()}, {@link VideoInfo#getInt(int)}
     */
    public static final int EXCEPTION_NOT_INITIALIZED_CONTRIBUTOR_ID = VideoInfo.CONTRIBUTOR_ID;
    /**
     * 対象の動画フィールドが初期化されていない場合に投げられます<br>
     * Thrown if target video field is not initialized.<br><br>
     *
     * 各ＡＰＩから取得できるフィールドは以下から参照してください。<br>
     *     {@link RankingVideoInfo ランキング・マイリスト}<br>
     *     {@link RecommendVideoInfo おすすめ動画}<br>
     *     {@link SearchVideoInfo 動画検索}<br>
     *     {@link TempMyListVideoInfo とりあえずマイリスト}<br>
     * {@link VideoInfoManager#complete()},{@link VideoInfoManager#getFlv(CookieStore)}を呼ぶことで
     * 欠損したフィールド値を取得できます。<br>
     * You can get following fields from each API;<br>
     *     {@link RankingVideoInfo ranking and myList}<br>
     *     {@link RecommendVideoInfo reccomend}<br>
     *     {@link SearchVideoInfo search}<br>
     *     {@link TempMyListVideoInfo temp myList}<br>
     * You can get lacking field by calling
     * {@link VideoInfoManager#complete()},{@link VideoInfoManager#getFlv(CookieStore)}
     */
    static class NotInitializedException extends NicoAPIException{
        NotInitializedException (String message, int code){
            super(message,code);
        }
    }

    /**
     * tried to get user icon image, but failed.
     * at {@link NicoLogin#getUserIcon()} probably via {@link NicoClient#getUserIcon()}
     */
    public static final int EXCEPTION_DRAWABLE_USER_ICON = 100;
    /**
     * tried to get thumbnail image of video, but its url is unknown.
     * at {@link VideoInfoManager#getThumbnail()}
     */
    public static final int EXCEPTION_DRAWABLE_THUMBNAIL_URL = 101;
    /**
     * failed to get thumbnail image of video via HTTP.
     * at {@link VideoInfoManager#getThumbnail()}
     */
    public static final int EXCEPTION_DRAWABLE_THUMBNAIL = 102;
    /**
     * tried to get contributor icon of video, but its url is unknown.
     * at {@link VideoInfoManager#getContributorIcon()}
     */
    public static final int EXCEPTION_DRAWABLE_CONTRIBUTOR_ICON_URL = 103;
    /**
     * failed to get contributor icon of video via HTTP.
     * at {@link VideoInfoManager#getContributorIcon()}
     */
    public static final int EXCEPTION_DRAWABLE_CONTRIBUTOR_ICON = 104;
    /**
     * 画像の取得に失敗すると投げられます<br>
     * Thrown if fail to get image.
     */
    static class DrawableFailureException extends NicoAPIException{
        DrawableFailureException (String message,int code){
            super(message,code);
        }
    }

    /**
     * tried to parse response from getThumbnailInfo API, but target sequence matched with expected format not found.
     * at {@link VideoInfoManager#complete()}
     */
    public static final int EXCEPTION_PARSE_GET_THUMBNAIL_INFO_NOT_FOUND = 200;
    /**
     * tried to parse response from getThumbnailInfo API by using java.util.regex.Pattern, but requested pattern not found.
     * at {@link VideoInfoManager#complete()}
     */
    public static final int EXCEPTION_PARSE_GET_THUMBNAIL_INFO_NO_PATTERN = 201;
    /**
     * failed to parse contributed date into {@link VideoInfoManager#dateFormatBase common format}, while succeeding in getting its raw value.
     */
    public static final int EXCEPTION_PARSE_GET_THUMBNAIL_INFO_DATE = 202;
    /**
     * APIからのレスポンスのパースに失敗すると投げられます<br>
     * Thrown if fail to parse API response.<br><br>
     *
     * APIのレスポンスが想定されたフォーマットと異なることを意味します。
     * 通常は投げられません。APIの仕様変更が考えられます。<br>
     * This means that API response does not follow expected format.
     * This exception is not usually thrown. Change in API may be to blame.
     */
    static class ParseException extends NicoAPIException{
        private String target;
        ParseException(String message,String target){
            super(message);
            this.target = target;
        }
        ParseException(String message,String target,int code){
            super(message,code);
            this.target = target;
        }
        public String getTarget(){return target;}
    }

    /**
     * tried to get information of the video at getThumbnailInfo API, but such video is not found.
     */
    public static final int EXCEPTION_PARAM_GET_THUMBNAIL_INFO_NOT_FOUND = 300;
    /**
     * tried to get information of the video at getThumbnailInfo API, but requested video has been deleted.
     */
    public static final int EXCEPTION_PARAM_GET_THUMBNAIL_INFO_DELETED = 301;
    /**
     * tried to get information of the video at getThumbnailInfo API, but requested video is available only for specific communities.
     */
    public static final int EXCEPTION_PARAM_GET_THUMBNAIL_INFO_COMMUNITY = 302;
    /**
     * 不正な引数を渡すと投げられます<br>
     * Thrown if invalid argument is passed.
     */
    static class InvalidParamsException extends NicoAPIException{
        InvalidParamsException(String message){
            super(message);
        }
        InvalidParamsException(String message,int code){
            super(message,code);
        }
    }

    /**
     * tried to get userName without login. at {@link LoginInfo#getUserName()}
     */
    public static final int EXCEPTION_NOT_LOGIN_USER_NAME = 400;
    /**
     * tried to get userID without login. at {@link LoginInfo#getUserID()}
     */
    public static final int EXCEPTION_NOT_LOGIN_USER_ID = 401;
    /**
     * tried to get {@link LoginInfo#isPremium} without login. at {@link LoginInfo#isPremium()}
     */
    public static final int EXCEPTION_NOT_LOGIN_USER_PREMIUM = 402;
    /**
     * tried to get cookie storing login session without login. at {@link LoginInfo#getCookieStore()}
     */
    public static final int EXCEPTION_NOT_LOGIN_COOKIE = 403;
    /**
     * tried to get myListGroup without login. at {@link NicoClient#getMyListGroup()}
     */
    public static final int EXCEPTION_NOT_LOGIN_MYLIST_GROUP = 404;
    /**
     * tried to get myList without login. at {@link NicoClient#getMyList(String)}
     */
    public static final int EXCEPTION_NOT_LOGIN_MYLIST = 405;
    /**
     * tried to get tempMyList without login. at {@link NicoClient#getTempMyList()}
     */
    public static final int EXCEPTION_NOT_LOGIN_TEMP_MYLIST = 406;
    /**
     * tried to get comment without login. at {@link NicoClient#getComment(VideoInfo)}, {@link NicoClient#getComment(VideoInfo, int)}
     */
    public static final int EXCEPTION_NOT_LOGIN_COMMENT = 407;
    /**
     * tried to post comment without login. at {@link NicoClient#getNicoCommentPost(VideoInfo)}
     */
    public static final int EXCEPTION_NOT_LOGIN_COMMENT_POST = 408;
    /**
     * ログインしていない状態でログイン必須の機能を使おうとすると投げます<br>
     * Thrown if try to use login-required method without login.
     */
    static class NoLoginException extends NicoAPIException{
        NoLoginException(String message, int code){
            super(message,code);
        }
    }

    /**
     * status code of response from getThumbnailInfo API is unexpected; not "ok" or "fail".
     * at {@link VideoInfoManager#complete()}
     */
    public static final int EXCEPTION_UNEXPECTED_GET_THUMBNAIL_INFO_STATUS_CODE = 500;
    /**
     * error code of response from getThumbnailInfo API is unexpected.
     * at {@link VideoInfoManager#complete()}
     */
    public static final int EXCEPTION_UNEXPECTED_GET_THUMBNAIL_INFO_ERROR_CODE = 501;
    /**
     * APIからのレスポンスの状態が想定外の場合に投げます<br>
     * Thrown if status of API response is not expected.<br>
     * 各種不正なパラメータやユーザアカウントの設定、APIのアクセス制限などが考えられます。<br>
     * Various things may be a cause; invalid params, setting of user account in Nico, access limit ro API....
     */
    static class APIUnexpectedException extends NicoAPIException{
        APIUnexpectedException(String message){
            super(message);
        }
        APIUnexpectedException(String message, int code){
            super(message,code);
        }
    }

    /**
     * tried to call {@link NicoRanking#get()} again after having called it.
     */
    public static final int EXCEPTION_ILLEGAL_STATE_RANKING_NON_REUSABLE = 600;
    /**
     * tried to call {@link NicoCommentPost#post()} again after having called it.
     */
    public static final int EXCEPTION_ILLEGAL_STATE_COMMENT_NON_REUSABLE = 601;
    /**
     * tried to get your comment ID before posting it. at {@link NicoCommentPost#getCommentNo()}
     */
    public static final int EXCEPTION_ILLEGAL_STATE_COMMENT_NOT_POST = 602;
    /**
     * tried to get comment while {@link VideoInfo#messageServerUrl} and {@link VideoInfo#threadID} are unknown.
     * at {@link VideoInfoManager#getComment()}, {@link VideoInfoManager#getComment(int)}
     */
    public static final int EXCEPTION_ILLEGAL_STATE_COMMENT_NOT_READY = 603;
    /**
     * 想定してない状態を意味します
     */
    static class IllegalStateException extends NicoAPIException{
        IllegalStateException(String message, int code){
            super(message,code);
        }
    }

}
