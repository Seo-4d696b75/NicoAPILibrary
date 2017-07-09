package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * コメントをニコ動に投稿します<br>
 * This posts a comment to Nico.<br>
 *
 * コメントの投稿にはログインが必須です。
 * まず対象の動画とログインした状態の{@link NicoClient}をコンストラクタに渡してインスタンスを得ます。
 * そしたら各種メソッドを呼んでコメントの情報を加えます。設定できる情報は<br>
 * コメント内容・コメントのタイミング(1/100秒単位)・コメント位置・コメント大きさ・コメントの色(プレミア会員限定色非対応)<br>
 * です。他のプレミア会員限定コマンドは対応していません。
 * これらのうちコメント内容と時間を最低限設定してから{@link #post()}を呼ぶと実際に投稿します。
 * また動画の設定によってはコメントの投稿を受け付けない場合があります。<br>
 * Posting a comment to Nico requires login.
 * First, you pass the target video and {@link NicoClient} with login session to this constructor.
 * Then, you can set various information about your comment by calling proper methods.
 * You can set;<br>
 * content of your comment, time (in decimal seconds), position, size, color (except that which is available only for premium membership )<br>
 * Other commands for premium membership only are not supported.
 * You can post your comment by calling {@link #post()}, after setting its content and time at least.
 *
 * @author Seo-4d696b75
 * @version 0.0 on 2017/02/22.
 */

public class NicoCommentPost implements Parcelable{

    private final LoginInfo loginInfo;
    private final String deviceName;
    private final VideoInfo targetVideo;
    private int startTime = -1;
    private String comment = "";
    private boolean isAnonymous = true;
    private String color = CommentInfo.COLOR_WHITE;
    private int position = CommentInfo.POSITION_MIDDLE;
    private float size = CommentInfo.SIZE_MEDIUM;
    private int commentNo = 0;

    private boolean isPost = false;

    private final Object paramSettingLock = new Object();
    private final Object postLock = new Object();

    /**
     * コメント投稿に必要なインスタンスを得ます<br>
     * Gets an instance in order to post a comment.<br>
     * 対象の動画とログインした状態の{@link NicoClient}を渡してください。
     * ログインしていないと例外を投げます。<br>
     * Pass the target video and {@link NicoClient} with login session to this constructor.
     * If not login, an exception is thrown.
     * @param targetVideo the video to which a new comment is posted, cannot be {@code null}
     * @param loginInfo the information of the user, cannot be {@code null}
     * @throws NicoAPIException if any param is {@code null} or not login
     */
    protected NicoCommentPost(VideoInfo targetVideo, LoginInfo loginInfo, String deviceName)throws NicoAPIException{
        if ( targetVideo == null ){
            throw new NicoAPIException.InvalidParamsException(
                    "target video not found > comment",
                    NicoAPIException.EXCEPTION_PARAM_COMMENT_POST_TARGET
            );
        }
        this.targetVideo = targetVideo;
        this.loginInfo = loginInfo;
        this.deviceName = deviceName;
    }

    /* <implementation of parcelable> */

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(loginInfo,flags);
        out.writeString(deviceName);
        out.writeParcelable(targetVideo,flags);
        out.writeInt(startTime);
        out.writeString(comment);
        out.writeString(color);
        out.writeInt(position);
        out.writeFloat(size);
        out.writeInt(commentNo);
        out.writeBooleanArray(new boolean[]{isAnonymous, isPost});
    }

    public static final Parcelable.Creator<NicoCommentPost> CREATOR = new Parcelable.Creator<NicoCommentPost>() {
        public NicoCommentPost createFromParcel(Parcel in) {
            return new NicoCommentPost(in);
        }
        public NicoCommentPost[] newArray(int size) {
            return new NicoCommentPost[size];
        }
    };

    private NicoCommentPost(Parcel in) {
        this.loginInfo = in.readParcelable(LoginInfo.class.getClassLoader());
        this.deviceName = in.readString();
        this.targetVideo = in.readParcelable(VideoInfo.class.getClassLoader());
        this.startTime = in.readInt();
        this.comment = in.readString();
        this.color = in.readString();
        this.position = in.readInt();
        this.size = in.readFloat();
        this.commentNo = in.readInt();
        boolean[] val = new boolean[2];
        in.readBooleanArray(val);
        this.isAnonymous = val[0];
        this.isPost = val[1];
    }

    /* </implementation of parcelable> */

    /**
     * コメントの本文を設定します Sets the text of new comment.<br>
     * コメント本文の内容は必ず設定する必要があります。本文を指定しない、または
     * {@code null}や空文字を渡した状態のままで{@link #post()}を呼ぶと例外を投げますので注意してください。<br>
     * Be careful that, if {@code null} or empty string is passed and then {@link #post()} is called, an exception is thrown.
     * @param comment comment text, should not be {@code null} or empty
     */
    public void setComment(String comment){
        synchronized (paramSettingLock) {
            this.comment = comment;
        }
    }
    /**
     * 現在指定されているコメント本文を取得します  Gets the comment text, which is set now.<br>
     * コメントを未指定の場合は初期値の空文字を返します。
     * @return the comment text, may be {@code null} if {@code null} was passed to {@link #setComment(String)}
     */
    public String getComment (){
        synchronized (paramSettingLock) {
            return comment;
        }
    }

    /**
     * コメントの時間を設定します　Sets comment time.<br>
     * この値は必ず設定する必要があります。
     * コメントが画面に流れ始める時間を1/100秒単位で指定してください。
     * 指定できる範囲は0以上{動画の長さ}未満です。
     * この時間を設定しない、または範囲外を指定して{@link #post()}を呼ぶと例外を投げますので注意してください。<br>
     * The time when the comment begins to flow on screen can be set in decimal seconds.
     * The value must be 0 or more and must be less than the video length.
     * If this value is not set or is set outside the range and then {@link #post()} is called, an exception is thrown.
     * @param startTime comment time, should be 0 or more and less than video length
     */
    public void setTime(int startTime){
        synchronized (paramSettingLock) {
            this.startTime = startTime;
        }
    }
    /**
     * 現在指定されている投稿時間を取得します　Gets the time at which this comment is posted.
     * @return the time in decimal seconds
     */
    public int getTime (){
        synchronized (paramSettingLock) {
            return startTime;
        }
    }

    /**
     * @deprecated if comment is not anonymous, your userID with the comment is visible to anyone.
     * @param isAnonymous
     */
    public void setAnonymous(boolean isAnonymous){
        synchronized (paramSettingLock) {
            this.isAnonymous = isAnonymous;
        }
    }

    /**
     * コメントの色を設定します　Sets color of the comment.<br>
     * コメント色をCOLOR_****定数で指定します。デフォルト値は{@link CommentInfo#COLOR_WHITE}です。
     * 用意された定数以外の無効な値を渡すと変更は無視されます。<br>
     * The color of comment is sets by passing constant COLOR_*****.
     * {@link CommentInfo#COLOR_WHITE} is set as default.
     * If invalid value except for provided ones is passed, setting is canceled.
     * @param color　the constant standing for comment color, chosen from COLOR_****
     */
    public void setColor(String color){
        if ( ResourceStore.getInstance().containColor(color) ){
            synchronized (paramSettingLock) {
                this.color = color;
            }
        }
    }
    /**
     * 現在指定されているコメント色の32bit値を取得します
     * Gets the comment color in 32bit.
     * @return the comment color in αRGB
     */
    public int getColor (){
        synchronized (paramSettingLock) {
            return ResourceStore.getInstance().getColor(this.color);
        }
    }
    /**
     * 現在指定されているコメント色の名称を取得します　
     * Gets name of the comment color.
     * @return name of the color
     */
    public String getColorName(){
        synchronized (paramSettingLock) {
            return color;
        }
    }

    /**
     * コメント色に関して有効な色の名前とその色を表す定数のMapを返します
     * Gets Map of name of valid comment color and its constant.<br>
     * この定数値を{@link #setColor(String)}に渡すことで色を指定できます。
     * またこの値(int)は実際の色をαRGBで表しています。(8bit×4)
     * By passing this constant to {@link #setColor(String)}, comment color can be set.
     * Also this value stands for the actual color in αRGB.
     * @return {@code Map} of color name and its constant
     */
    public Map<String,Integer> getColorMap(){
        return ResourceStore.getInstance().getColorMap();
    }

    /**
     * コメントの表示位置を設定します　Sets displayed-position of the comment.<br>
     * コメント位置をPOSITION_****定数で指定します。デフォルト値は{@link CommentInfo#POSITION_MIDDLE}です。
     * 用意された定数以外の無効な値を渡すと変更は無視されます。<br>
     * The position of comment is sets by passing constant POSITION_*****.
     * {@link CommentInfo#POSITION_MIDDLE} is set as default.
     * If invalid value except for provided ones is passed, setting is canceled.
     * @param position the constant standing for comment position, chosen from POSITION_****
     */
    public void setPosition(int position){
        if ( ResourceStore.getInstance().containPosition(position) ){
            synchronized (paramSettingLock) {
                this.position = position;
            }
        }
    }

    /**
     * コメントの表示サイズを設定します　Sets displayed-size of the comment.<br>
     * コメントサイズをSIZE_****定数で指定します。デフォルト値は{@link CommentInfo#SIZE_MEDIUM}です。
     * 用意された定数以外の無効な値を渡すと変更は無視されます。<br>
     * The size of comment is sets by passing constant SIZE_*****.
     * {@link CommentInfo#SIZE_MEDIUM} is set as default.
     * If invalid value except for provided ones is passed, setting is canceled.
     * @param size the constant standing for comment size, chosen from SIZE_****
     */
    public void setSize(int size){
        if ( ResourceStore.getInstance().containSize(size) ){
            synchronized (paramSettingLock) {
                this.size = size;
            }
        }
    }

    private final int STATUS_SUCCESS = 0;
    private final int STATUS_FAILURE = 1;
    private final int STATUS_INVALID_THREAD = 2;
    private final int STATUS_INVALID_TICKET = 3;
    private final int STATUS_INVALID_POST_KEY = 4;
    private final int STATUS_LOCKED = 5;
    private final int STATUS_READ_ONLY = 6;
    private final int STATUS_TOO_LONG = 8;
    private final Map<Integer,String> statusMap = new HashMap<Integer, String>(){
        {
            put(STATUS_FAILURE,"posting comment is rejected");
            put(STATUS_INVALID_THREAD,"threadID is invalid");
            put(STATUS_INVALID_TICKET,"ticket is invalid");
            put(STATUS_INVALID_POST_KEY,"post key or userID in invalid");
            put(STATUS_LOCKED,"posting comment is blocked");
            put(STATUS_READ_ONLY,"posting comment is not allowed");
            put(STATUS_TOO_LONG,"target comment in too long");
        }
    };

    /**
     * コメントを投稿します Posts new comment.<br>
     * 必ずコメント内容と時間を{@link #setComment(String)},{@link #setTime(int)}で予め設定する必要があります。
     * これら値を設定していない、もしくは無効な値が設定されている状態で呼ばれると例外を投げます。
     * また、これは再利用禁止で投稿に成功したうえで二度以上呼ぶと例外を投げます。<br>
     * The text and time of comment must be set in advance by calling {@link #setComment(String)},{@link #setTime(int)}.
     * If these values are not set or invalid values are set, an exception is thrown.
     * Also you cannot reuse this and, if you call this again after succeeding in posting, an exception is thrown.<br>
     * <strong>ＵＩスレッド禁止</strong>HTTP通信を行うのでバックグランド処理してください。<br>
     *<strong>No UI thread</strong> HTTP communication is done.
     * @throws NicoAPIException if fail to post comment
     */
    public void post() throws NicoAPIException{
        if ( isPost ){
            throw new NicoAPIException.IllegalStateException(
                    "cannot post the comment again",
                    NicoAPIException.EXCEPTION_ILLEGAL_STATE_COMMENT_NON_REUSABLE
            );
        }
        if ( startTime < 0 || startTime >= targetVideo.getLength()*100 ){
            throw new NicoAPIException.InvalidParamsException(
                    "comment time is out of range",
                    NicoAPIException.EXCEPTION_PARAM_COMMENT_POST_TIME
            );
        }
        if ( comment == null || comment.isEmpty() ){
            throw new NicoAPIException.InvalidParamsException(
                    "comment is not set",
                    NicoAPIException.EXCEPTION_PARAM_COMMENT_POST_CONTENT
            );
        }
        ArrayList<String> commandList = new ArrayList<String>();
        ResourceStore res = ResourceStore.getInstance();
        int userID = loginInfo.getUserID();
        String premium = loginInfo.isPremium() ? res.getString(R.string.value_comment_premium) : res.getString(R.string.value_comment_non_premium);
        String comment = "";
        int startTime = 0;
        synchronized (paramSettingLock) {
            comment = this.comment;
            startTime = this.startTime;
            if ( isAnonymous ){
                commandList.add(res.getString(R.string.value_comment_anonymous));
            }
            if ( !color.equals(CommentInfo.COLOR_WHITE) ){
                commandList.add(color);
            }
            if ( position != CommentInfo.POSITION_MIDDLE ){
                commandList.add(res.getPosition(position));
            }
            if ( size != CommentInfo.SIZE_MEDIUM ){
                commandList.add(res.getSize(size));
            }
            commandList.add(String.format(Locale.US,res.getString(R.string.value_comment_device),deviceName));
        }
        synchronized (postLock) {
            try {
                targetVideo.getThreadID();
                targetVideo.getMessageServerURL();
            } catch (NicoAPIException e) {
                targetVideo.getFlv(loginInfo.getCookies());
            }
            CommentInfo.CommentGroup group = targetVideo.getComment(1);
            String command = "";
            if (!commandList.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                builder.append(commandList.get(0));
                for (int i = 1; i < commandList.size(); i++) {
                    builder.append(" ");
                    builder.append(commandList.get(i));
                }
                command = builder.toString();
            }
            HttpClient client = res.getHttpClient();
            String path = String.format(
                    Locale.US,res.getURL(R.string.url_comment_post),
                    group.getLastComment() / 100, group.getThreadID()
            );
            if (client.get(path, loginInfo.getCookies() )) {
                Matcher matcher = res.getPattern(R.string.regex_comment_postKey).matcher(client.getResponse());
                String postKey = matcher.find() ? matcher.group(1) : client.getResponse().split("=")[1];
                String body = String.format(
                        Locale.US, res.getString(R.string.format_comment_post),
                        group.getThreadID(), group.getTicket(), userID, startTime, command, postKey, premium, comment
                );
                path = targetVideo.getMessageServerURL();
                if (client.post(path, body, null )) {
                    matcher = res.getPattern(R.string.regex_comment_post_response).matcher(client.getResponse());
                    if (matcher.find()) {
                        int statusCode = Integer.parseInt(matcher.group(1));
                        commentNo = Integer.parseInt(matcher.group(2));
                        if (statusCode == STATUS_SUCCESS) {
                            isPost = true;
                        } else {
                            throw new NicoAPIException.InvalidParamsException(
                                    statusMap.get(statusCode),
                                    NicoAPIException.EXCEPTION_PARAM_COMMENT_POST
                            );
                        }
                    } else {
                        throw new NicoAPIException.ParseException(
                                "fail to parse post response",client.getResponse(),
                                NicoAPIException.EXCEPTION_PARSE_COMMENT_POST
                        );
                    }
                } else {
                    throw new NicoAPIException.HttpException(
                            "fail to post comment",
                            NicoAPIException.EXCEPTION_HTTP_COMMENT_POST,
                            client.getStatusCode(), path, "GET"
                    );
                }
            } else {
                throw new NicoAPIException.HttpException(
                        "fail to get postKey",
                        NicoAPIException.EXCEPTION_HTTP_COMMENT_POST_KEY,
                        client.getStatusCode(), path, "GET"
                );
            }
        }
    }

    /**
     * 投稿したコメントのIDを取得します　Gets comment ID posted to Nico.<br>
     * コメントの投稿に成功した後でのみ取得できます。
     * 未投稿の状態で呼ぶと例外を投げます。<br>
     * The ID can be gotten only after succeeding in posting comment.
     * If comment is not posted yet, an exception is thrown.
     * @return the comment ID
     * @throws NicoAPIException if comment is not posted yet
     */
    public int getCommentNo () throws NicoAPIException{
        synchronized (postLock) {
            if (isPost) {
                return commentNo;
            } else {
                throw new NicoAPIException.IllegalStateException(
                        "not post a comment yet",
                        NicoAPIException.EXCEPTION_ILLEGAL_STATE_COMMENT_NOT_POST
                );
            }
        }
    }

    /**
     * コメントの投稿先動画を取得します
     * Gets the video to which this comment is posted
     * @return the video, not {@code null}
     */
    public VideoInfo getTargetVideo(){
        return targetVideo;
    }
}
