package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import org.apache.http.client.CookieStore;

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
     *     {@link RSSVideoInfo ランキング・マイリスト}<br>
     *     {@link RecommendVideoInfo おすすめ動画}<br>
     *     {@link SearchVideoInfo 動画検索}<br>
     *     {@link MyListVideoInfo とりあえずマイリスト}<br>
     * {@link VideoInfo#complete()},{@link VideoInfo#getFlv(CookieStore)}を呼ぶことで
     * 欠損したフィールド値を取得できます。<br>
     * You can get following fields from each API;<br>
     *     {@link RSSVideoInfo ranking and myList}<br>
     *     {@link RecommendVideoInfo reccomend}<br>
     *     {@link SearchVideoInfo search}<br>
     *     {@link MyListVideoInfo temp myList}<br>
     * You can get lacking field by calling
     * {@link VideoInfo#complete()},{@link VideoInfo#getFlv(CookieStore)}
     */
    static class NotInitializedException extends NicoAPIException{
        NotInitializedException (String message, int code){
            super(message,code);
        }
    }

    /**
     * tried to get user icon image, but failed.
     */
    public static final int EXCEPTION_DRAWABLE_USER_ICON = 100;
    /**
     * tried to get thumbnail image of video, but its url is unknown.
     * at {@link VideoInfo#getThumbnail()}
     */
    public static final int EXCEPTION_DRAWABLE_THUMBNAIL_URL = 101;
    /**
     * failed to get thumbnail image of video via HTTP.
     * at {@link VideoInfo#getThumbnail()}
     */
    public static final int EXCEPTION_DRAWABLE_THUMBNAIL = 102;
    /**
     * tried to get contributor icon of video, but its url is unknown.
     * at {@link VideoInfo#getContributorIcon()}
     */
    public static final int EXCEPTION_DRAWABLE_CONTRIBUTOR_ICON_URL = 103;
    /**
     * failed to get contributor icon of video via HTTP.
     * at {@link VideoInfo#getContributorIcon()}
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
     * at {@link VideoInfo#complete()}
     */
    public static final int EXCEPTION_PARSE_GET_THUMBNAIL_INFO_NOT_FOUND = 200;
    /**
     * tried to parse response from getThumbnailInfo API by using java.util.regex.Pattern, but requested pattern not found.
     * at {@link VideoInfo#complete()}
     */
    public static final int EXCEPTION_PARSE_GET_THUMBNAIL_INFO_NO_PATTERN = 201;
    /**
     * failed to parse contributed date into {@link VideoInfo#dateFormatBase common format}, while succeeding in getting its raw value.
     */
    public static final int EXCEPTION_PARSE_GET_THUMBNAIL_INFO_DATE = 202;
    /**
     * parse target about ranking is {@code null}
     */
    public static final int EXCEPTION_PARSE_RANKING_NO_TARGET = 203;
    /**
     * tried to parse response about recommended videos, because the target is {@code null}.
     */
    public static final int EXCEPTION_PARSE_RECOMMEND_NO_TARGET = 204;
    /**
     * tried to parse response about myList or ranking, but target sequence matched with expected format not found.
     */
    public static final int EXCEPTION_PARSE_RANKING_MYLIST_NOT_FOUND = 205;
    /**
     * tried to parse video title of ranking, but its format is unexpected.
     */
    public static final int EXCEPTION_PARSE_RANKING_TITLE = 206;
    /**
     * failed to parse the counting number of views, comments and myLists in case of ranking or myList.
     */
    public static final int EXCEPTION_PARSE_RANKING_MYLIST_COUNTER = 207;
    /**
     * failed to parse video length of ranking or myList because its format is unexpected.
     */
    public static final int EXCEPTION_PARSE_RANKING_MYLIST_LENGTH = 208;
    /**
     * failed to parse video published-date of ranking or myList because its format is unexpected.
     */
    public static final int EXCEPTION_PARSE_RANKING_MYLIST_PUB_DATE = 209;
    /**
     * failed to parse video contributed-date of ranking or myList because its format is unexpected.
     */
    public static final int EXCEPTION_PARSE_RANKING_MYLIST_DATE = 210;
    /**
     * fail to find NicoAPI.token from page HTML.
     */
    public static final int EXCEPTION_PARSE_MYLIST_TOKEN = 211;
    /**
     * failed to parse delete counting
     */
    public static final int EXCEPTION_PARSE_MYLIST_DELETE_COUNT = 212;
    /**
     * fail to parse Json response about myList and tempMyList.
     */
    public static final int EXCEPTION_PARSE_MYLIST_JSON = 213;
    /**
     * fail to parse Json response about myList group.
     */
    public static final int EXCEPTION_PARSE_MYLIST_GROUP_JSON = 214;
    /**
     * fail to parse Json response about myList details
     */
    public static final int EXCEPTION_PARSE_MYLIST_DETAILS_JSON = 215;
    /**
     * fail to parse RSS (in XML) response meta about myList.
     */
    public static final int EXCEPTION_PARSE_MYLIST_RSS = 216;
    /**
     * fail to parse JSON response when try moving or copying myList videos.
     */
    public static final int EXCEPTION_PARSE_MYLIST_MOVE_STATE = 217;
    /**
     * fail to parse response about comment, because essential fields are missing;
     * comment text, posted-date and comment time.
     */
    public static final int EXCEPTION_PARSE_COMMENT_XML = 218;
    /**
     * fail to parse response about comment, because of JsonException or
     * because essential fields are missing;
     * comment text, posted-date and comment time.
     */
    public static final int EXCEPTION_PARSE_COMMENT_JSON = 219;
    /**
     * fail to parse meta of Json response about comment,
     * because expected value not found, or unexpected format.
     */
    public static final int EXCEPTION_PARSE_COMMENT_JSON_META = 220;
    /**
     * fail to parse meta of XML response about comment,
     * because expected value not found, or unexpected format.
     */
    public static final int EXCEPTION_PARSE_COMMENT_XML_META = 221;
    /**
     * fail to parse response of posting comment.
     * The format of the XML response is unexpected.
     */
    public static final int EXCEPTION_PARSE_COMMENT_POST = 222;
    /**
     * fail to parse userName from myPage HTML and from "seiga" API response, either.
     */
    public static final int EXCEPTION_PARSE_LOGIN_USER_NAME = 223;
    /**
     * fail to parse userID from myPage HTML.
     * But uerID also can be gotten from value of cookie; "user_session".
     */
    public static final int EXCEPTION_PARSE_LOGIN_USER_ID = 224;
    /**
     * fail to parse response status about recommended videos.
     */
    public static final int EXCEPTION_PARSE_RECOMMEND_STATUS = 224;
    /**
     * fail to parse response about recommended videos
     * because target fields not found or unexpected format.
     */
    public static final int EXCEPTION_PARSE_RECOMMEND = 225;
    /**
     * fail to find userIconUrl from my page HTML.
     */
    public static final int EXCEPTION_PARSE_LOGIN_USER_ICON = 226;


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
     * tried to parse ranking response without passing required params.
     */
    public static final int EXCEPTION_PARAM_RANKING_NO_PARAM = 303;
    /**
     * tried to parse myList response while passing ranking params.
     */
    public static final int EXCEPTION_PARAM_MYLIST_NOT_RANKING = 304;
    /**
     * target video not found when trying to edit myList.
     */
    public static final int EXCEPTION_PARAM_MYLIST_EDIT_VIDEO_NOT_FOUND = 305;
    /**
     * specified video already exists in target myList.
     */
    public static final int EXCEPTION_PARAM_MYLIST_EDIT_VIDEO_ALREADY_EXIST = 306;
    /**
     * passed NicoAPI.token is invalid.
     */
    public static final int EXCEPTION_PARAM_MYLIST_EDIT_INVALID_TOKEN = 307;
    /**
     * passed NicoAPI.token is expired. New token is required.
     */
    public static final int EXCEPTION_PARAM_MYLIST_EDIT_OLD_TOKEN = 308;
    /**
     * passed param is invalid or required param is missing.
     */
    public static final int EXCEPTION_PARAM_MYLIST_EDIT_PARAM = 309;
    /**
     * fail to delete some of the passed videos because they do not exist in the (temp)MyList.
     * When trying to delete video, you get videos from {@link MyListVideoGroup},{@link TempMyListVideoGroup}
     * and make sure that myList contains the target video.
     */
    public static final int EXCEPTION_PARAM_MYLIST_DELETE = 310;
    /**
     * fail to get myList because not login or the target myList is not public myList of others.
     * Getting public myList does not needs any login, but
     * getting your non-public myList requires login.
     * Not to say, you can not get others' non-public myList in any case.
     */
    public static final int EXCEPTION_PARAM_MYLIST_ID = 311;
    /**
     * fail to move some some of the passed videos because they don't exist in (temp)MyList,
     * or because the same videos already exist in target myList yet.
     */
    public static final int EXCEPTION_PARAM_MYLIST_MOVE = 312;
    /**
     * fail to copy some some of the passed videos because they don't exist in (temp)MyList,
     * or because the same videos already exist in target myList yet.
     */
    public static final int EXCEPTION_PARAM_MYLIST_COPY = 313;
    /**
     * fail to add/delete/update/move/copy target video because {@code null} or empty array is passed.
     */
    public static final int EXCEPTION_PARAM_MYLIST_TARGET_VIDEO = 314;
    /**
     * fail to add/update the video/myList because description param is {@code null}.
     * When want to set no description, empty String has to be passed.
     */
    public static final int EXCEPTION_PARAM_MYLIST_DESCRIPTION = 315;
    /**
     * fail to move/copy the video to target myList because that myList is {@code null}.
     * Be sure to get {@link MyListGroup} and pass some of its {@link MyListVideoGroup}
     */
    public static final int EXCEPTION_PARAM_MYLIST_TARGET_MYLIST = 316;
    /**
     * fail to add/update the myList because name param is {@code null}.
     * When want to set "", empty String has to be passed.
     */
    public static final int EXCEPTION_PARAM_MYLIST_NAME = 317;
    /**
     * fail to get comments because target video is {@code null}.
     */
    public static final int EXCEPTION_PARAM_COMMENT_TARGET = 318;
    /**
     * fail to get {@link NicoCommentPost} instance because target video is {@code null}.
     */
    public static final int EXCEPTION_PARAM_COMMENT_POST_TARGET = 319;
    /**
     * fail to post a comment because posting-time is out of bound.
     */
    public static final int EXCEPTION_PARAM_COMMENT_POST_TIME = 320;
    /**
     * fail to post a comment because comment content is {@code null} or empty String.
     */
    public static final int EXCEPTION_PARAM_COMMENT_POST_CONTENT = 321;
    /**
     * fail to post new comment because of various possible reasons;
     * Passed threadID, userID, post key or ticket is invalid.
     * Posting new comment to the target video is not allowed due to its setting.
     * The comment text is too long.
     */
    public static final int EXCEPTION_PARAM_COMMENT_POST = 322;
    /**
     * fail to login because passed mail address and password are {@code null}, or invalid values.
     */
    public static final int EXCEPTION_PARAM_LOGIN = 323;
    /**
     * fail to get myList object specified by the ID from myListGroup object.
     */
    public static final int EXCEPTION_PARAM_MYLIST_GROUP_ID = 324;
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
     * tried to get myList without login. at {@link NicoClient#getMyList(int)}
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
     * tried to edit myList without login.
     */
    public static final int EXCEPTION_NOT_LOGIN_MYLIST_EDIT = 409;
    /**
     * tried to get userIcon with no login
     */
    public static final int EXCEPTION_NOT_LOGIN_USER_ICON = 410;
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
     * at {@link VideoInfo#complete()}
     */
    public static final int EXCEPTION_UNEXPECTED_GET_THUMBNAIL_INFO_STATUS_CODE = 500;
    /**
     * error code of response from getThumbnailInfo API is unexpected.
     * at {@link VideoInfo#complete()}
     */
    public static final int EXCEPTION_UNEXPECTED_GET_THUMBNAIL_INFO_ERROR_CODE = 501;
    /**
     * status code of response is unexpected when trying to edit MyList.
     */
    public static final int EXCEPTION_UNEXPECTED_MYLIST_EDIT_STATUS_CODE = 502;
    /**
     * error code is unexpected when trying to edit MyList.
     */
    public static final int EXCEPTION_UNEXPECTED_MYLIST_EDIT_ERROR_CODE = 503;
    /**
     * status code is unexpected when trying to parse comment response.
     */
    public static final int EXCEPTION_UNEXPECTED_COMMENT_STATUS_CODE = 504;
    /**
     * status code of response about recommended videos is unexpected.
     */
    public static final int EXCEPTION_UNEXPECTED_RECOMMEND_STATUS_CODE = 505;
    /**
     * the format of ranking RSS in XML is unexpected.
     */
    public static final int EXCEPTION_UNEXPECTED_RANKING = 506;
    /**
     * APIからのレスポンスの各値が想定外の場合に投げます<br>
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
     * at {@link VideoInfo#getComment()}, {@link VideoInfo#getComment(int)}
     */
    public static final int EXCEPTION_ILLEGAL_STATE_COMMENT_NOT_READY = 603;
    /**
     * fail to get userName because userID is still unknown.
     * UserID must be gotten beforehand.
     */
    public static final int EXCEPTION_ILLEGAL_STATE_LOGIN_NAME = 604;
    /**
     * fail to get userID because not access myPage yet.
     * In advance, login and get myPage HTML.
     */
    public static final int EXCEPTION_ILLEGAL_STATE_LOGIN_USER_ID = 605;
    /**
     * fail to get videos in target myList because videos are not initialized yet.
     * In advance, be sure to call {@link MyListVideoGroup#loadVideos()}.
     */
    public static final int EXCEPTION_ILLEGAL_STATE_MYLIST_VIDEO = 606;
    /**
     * cannot to get userIconUrl before access to my page.
     */
    public static final int EXCEPTION_ILLEGAL_STATE_LOGIN_USER_ICON = 607;
    /**
     * try to get {@link jp.ac.u_tokyo.kyoyo.seo.nicoapilib.CommentInfo.CommentGroup} without getting it at {@link VideoInfo#getComment()}
     */
    public static final int EXCEPTION_ILLEGAL_STATE_COMMENT = 608;
    /**
     * tried to get thumbnail image before setting it to video field.
     */
    public static final int EXCEPTION_ILLEGAL_STATE_THUMBNAIL = 609;
    /**
     * tried to get contributor icon before setting it to video field.
     */
    public static final int EXCEPTION_ILLEGAL_STATE_CONTRIBUTOR_ICON = 610;
    /**
     * 想定してない状態を意味します
     */
    static class IllegalStateException extends NicoAPIException{
        IllegalStateException(String message, int code){
            super(message,code);
        }
    }

    /**
     * http failure while trying to add video in (temp)MyList or to add new myList in myList group.
     */
    public static final int EXCEPTION_HTTP_MYLIST_ADD = 700;
    /**
     * http failure while trying to get NicoAPI.token in order to edit myList.
     */
    public static final int EXCEPTION_HTTP_MYLIST_TOKEN = 701;
    /**
     * http failure while trying to update (temp)MyList.
     */
    public static final int EXCEPTION_HTTP_MYLIST_UPDATE = 702;
    /**
     * http failure while trying to delete a video of (temp)MyList.
     */
    public static final int EXCEPTION_HTTP_MYLIST_DELETE = 703;
    /**
     * http failure while trying to move a video from (temp)MyList to another myList.
     */
    public static final int EXCEPTION_HTTP_MYLIST_MOVE = 704;
    /**
     * http failure while trying to get tempMyList.
     */
    public static final int EXCEPTION_HTTP_TEMP_MYLIST_GET = 705;
    /**
     * http failure while trying to get myList group.
     */
    public static final int EXCEPTION_HTTP_MYLIST_GROUP_GET = 706;
    /**
     * http failure while trying to get myList details.
     */
    public static final int EXCEPTION_HTTP_MYLIST_DETAILS_GET = 707;
    /**
     * http failure while trying to get myList videos.
     */
    public static final int EXCEPTION_HTTP_MYLIST_VIDEOS_GET = 708;
    /**
     * http failure while trying to get myList specified with myListID directly.
     */
    public static final int EXCEPTION_HTTP_MYLIST_VIDEOS_GET_DIRECT = 709;
    /**
     * http failure while trying to copy a video from (temp)MyList to another myList.
     */
    public static final int EXCEPTION_HTTP_MYLIST_COPY = 710;
    /**
     * http failure while trying to sort videos in target myList.
     */
    public static final int EXCEPTION_HTTP_MYLIST_SORT = 711;
    /**
     * HTTP failure while getting post key.
     */
    public static final int EXCEPTION_HTTP_COMMENT_POST_KEY = 712;
    /**
     * HTTP failure while posting new comment.
     */
    public static final int EXCEPTION_HTTP_COMMENT_POST = 713;
    /**
     * HTTP failure while getting search response.
     */
    public static final int EXCEPTION_HTTP_SEARCH = 714;
    /**
     * HTTP failure while getting ranking.
     */
    public static final int EXCEPTION_HTTP_RANKING = 715;
    /**
     * HTTP failure while getting comments.
     */
    public static final int EXCEPTION_HTTP_COMMENT = 716;
    /**
     * HTTP通信に失敗
     */
    static class HttpException extends NicoAPIException{
        private int statusCode;
        private String path;
        private String method;
        HttpException(String message, int code, int statusCode, String path, String method){
            super(message,code);
            this.statusCode = statusCode;
            this.path = path;
            this.method = method;
        }
        public int getStatusCode(){return statusCode;}
        public String getPath(){return path;}
        public String getMethod(){return method;}
    }

}
