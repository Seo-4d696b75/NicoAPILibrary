package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
 * コメント内容・コメントの(1/100秒単位)・コメント位置・コメント大きさ・コメントの色(プレミア会員限定色非対応)<br>
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

public class NicoCommentPost {

    private final NicoClient client;
    private final VideoInfo targetVideo;
    private int startTime = -1;
    private String comment;
    private boolean isAnonymous = true;
    private int color = COLOR_WHITE;
    private int position = POSITION_MIDDLE;
    private int size = SIZE_MEDIUM;
    private int commentNo;

    private boolean isPost = false;

    private Object paramSettingLock = new Object();
    private Object postLock = new Object();

    /**
     * コメント投稿に必要なインスタンスを得ます<br>
     * Gets an instance in order to post a comment.<br>
     * 対象の動画とログインした状態の{@link NicoClient}を渡してください。
     * ログインしていないと例外を投げます。<br>
     * Pass the target video and {@link NicoClient} with login session to this constructor.
     * If not login, an exception is thrown.
     * @param targetVideo the video to which a new comment is posted, cannot be {@code null}
     * @param client the information of the user, cannot be {@code null}
     * @throws NicoAPIException if any param is {@code null} or not login
     */
    protected NicoCommentPost(VideoInfo targetVideo,NicoClient client)throws NicoAPIException{
        if ( targetVideo == null ){
            throw new NicoAPIException.InvalidParamsException("target video not found > comment");
        }
        if ( client == null ){
            throw new NicoAPIException.InvalidParamsException("login info not found > comment");
        }
        if ( !client.isLogin() ){
            throw new NicoAPIException.NoLoginException("not login > comment",NicoAPIException.EXCEPTION_NOT_LOGIN_COMMENT_POST);
        }
        this.targetVideo = targetVideo;
        this.client = client;
    }

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
    public int getStartTime (){
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

    public static final int COLOR_WHITE     = 0xffffffff;
    public static final int COLOR_RED       = 0xffff0000;
    public static final int COLOR_PINK      = 0xffff8080;
    public static final int COLOR_ORANGE    = 0xffffcc00;
    public static final int COLOR_YELLOW    = 0xffffff00;
    public static final int COLOR_GREEN     = 0xff00ff00;
    public static final int COLOR_CYAN      = 0xff00ffff;
    public static final int COLOR_BLUE      = 0xff0000ff;
    public static final int COLOR_PURPLE    = 0xffc000ff;
    public static final int COLOR_BLACK     = 0xff000000;
    private final Map<Integer,String> colorMap = new LinkedHashMap<Integer, String>(){
        {
            put(COLOR_WHITE,"white");
            put(COLOR_RED,"red");
            put(COLOR_PINK,"pink");
            put(COLOR_ORANGE,"orange");
            put(COLOR_YELLOW,"yellow");
            put(COLOR_GREEN,"green");
            put(COLOR_CYAN,"cyan");
            put(COLOR_BLUE,"blue");
            put(COLOR_PURPLE,"purple");
            put(COLOR_BLACK,"black");
        }
    };

    /**
     * コメントの色を設定します　Sets color of the comment.<br>
     * コメント色をCOLOR_****定数で指定します。デフォルト値は{@link #COLOR_WHITE}です。
     * 用意された定数以外の無効な値を渡すと変更は無視されます。<br>
     * The color of comment is sets by passing constant COLOR_*****.
     * {@link #COLOR_WHITE} is set as default.
     * If invalid value except for provided ones is passed, setting is canceled.
     * @param color　the constant standing for comment color, chosen from COLOR_****
     */
    public void setColor(int color){
        if ( colorMap.containsKey(color) ){
            synchronized (paramSettingLock) {
                this.color = color;
            }
        }
    }
    public int getColor (){
        synchronized (paramSettingLock) {
            return color;
        }
    }
    public String getColorName(){
        synchronized (paramSettingLock) {
            return colorMap.get(color);
        }
    }

    /**
     * コメント色に関して有効な色の名前とその色を表す定数のMapを返します
     * Gets Map of name of valid comment color and its constant.<br>
     * この定数値を{@link #setColor(int)}に渡すことで色を指定できます。
     * またこの値(int)は実際の色をαRGBで表しています。(8bit×4)
     * By passing this constant to {@link #setColor(int)}, comment color can be set.
     * Also this value stands for the actual color in αRGB.
     * @return {@code Map} of color name and its constant
     */
    public Map<String,Integer> getColorMap(){
        Map<String,Integer> map = new LinkedHashMap<String ,Integer>();
        for ( Integer color : colorMap.keySet() ){
            map.put( colorMap.get(color), color);
        }
        return map;
    }

    public static final int POSITION_TOP = 0;
    public static final int POSITION_MIDDLE = 1;
    public static final int POSITION_BOTTOM = 2;
    private final Map<Integer,String> positionMap = new HashMap<Integer, String>(){
        {
            put(POSITION_TOP,"ue");
            put(POSITION_MIDDLE,"naka");
            put(POSITION_BOTTOM,"shita");
        }
    };

    /**
     * コメントの表示位置を設定します　Sets displayed-position of the comment.<br>
     * コメント位置をPOSITION_****定数で指定します。デフォルト値は{@link #POSITION_MIDDLE}です。
     * 用意された定数以外の無効な値を渡すと変更は無視されます。<br>
     * The position of comment is sets by passing constant POSITION_*****.
     * {@link #POSITION_MIDDLE} is set as default.
     * If invalid value except for provided ones is passed, setting is canceled.
     * @param position the constant standing for comment position, chosen from POSITION_****
     */
    public void setPosition(int position){
        if ( positionMap.containsKey(position) ){
            synchronized (paramSettingLock) {
                this.position = position;
            }
        }
    }

    public static final int SIZE_MEDIUM = 800;
    public static final int SIZE_BIG = 801;
    public static final int SIZE_SMALL = 802;
    private final Map<Integer,String> sizeMap = new HashMap<Integer, String>(){
        {
            put(SIZE_BIG,"big");
            put(SIZE_MEDIUM,"medium");
            put(SIZE_SMALL,"small");
        }
    };

    /**
     * コメントの表示サイズを設定します　Sets displayed-size of the comment.<br>
     * コメントサイズをSIZE_****定数で指定します。デフォルト値は{@link #SIZE_MEDIUM}です。
     * 用意された定数以外の無効な値を渡すと変更は無視されます。<br>
     * The size of comment is sets by passing constant SIZE_*****.
     * {@link #SIZE_MEDIUM} is set as default.
     * If invalid value except for provided ones is passed, setting is canceled.
     * @param size the constant standing for comment size, chosen from SIZE_****
     */
    public void setSize(int size){
        if ( sizeMap.containsKey(size) ){
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
     * Also you cannot reuse this and, if you call this again after succeeding in posting, an exception is thrown.
     * @throws NicoAPIException if fail to post comment
     */
    public void post() throws NicoAPIException{
        if ( isPost ){
            throw new NicoAPIException.IllegalStateException("cannot post the comment again",NicoAPIException.EXCEPTION_ILLEGAL_STATE_COMMENT_NON_REUSABLE);
        }
        if ( startTime < 0 || startTime >= targetVideo.getLength()*100 ){
            throw new NicoAPIException.InvalidParamsException("comment time is out of range");
        }
        if ( comment == null || comment.isEmpty() ){
            throw new NicoAPIException.InvalidParamsException("comment is not set");
        }
        int userID = 0;
        int premium = 0;
        String comment = "";
        int startTime = 0;
        ArrayList<String> commandList = new ArrayList<String>();
        synchronized (paramSettingLock) {
            userID = client.getUserID();
            comment = this.comment;
            startTime = this.startTime;
            if ( client.isPremium() ){
                premium = 1;
            }
            if ( isAnonymous ){
                commandList.add("184");
            }
            if ( color != COLOR_WHITE ){
                commandList.add(colorMap.get(color));
            }
            if ( position != POSITION_MIDDLE ){
                commandList.add(positionMap.get(position));
            }
            if ( size != SIZE_MEDIUM ){
                commandList.add(sizeMap.get(size));
            }
            commandList.add("device:"+client.deviceName);
        }
        synchronized (postLock) {
            try {
                targetVideo.getThreadID();
                targetVideo.getMessageServerUrl();
            } catch (NicoAPIException e) {
                targetVideo.getFlv(client.getCookieStore());
            }
            targetVideo.getComment(1);
            CommentInfo.CommentGroup group = targetVideo.commentGroup;
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
            HttpResponseGetter getter = new HttpResponseGetter();
            String path = "http://flapi.nicovideo.jp/api/getpostkey/?version=1&yugi=&device=1&block_no=%d&thread=%s&version_sub=2";
            path = String.format(path, group.lastComment / 100, group.threadID);
            if (getter.tryGet(path, client.getCookieStore())) {
                String postKey = getter.response.split("=")[1];
                String bodyFormat = "<chat thread=\"%d\" ticket=\"%s\" user_id=\"%d\" vpos=\"%d\" mail=\"%s\" postkey=\"%s\" premium=\"%d\">%s</chat>";
                String body = String.format(bodyFormat, group.threadID, group.ticket, userID, startTime, command, postKey, premium, comment);
                path = targetVideo.getMessageServerUrl();
                if (getter.tryPost(path, body)) {
                    Matcher matcher = Pattern.compile("<packet><chat_result thread=\"[0-9]+?\" status=\"([0-9])\" no=\"([0-9]+?)\".*?/></packet>").matcher(getter.response);
                    if (matcher.find()) {
                        int statusCode = Integer.parseInt(matcher.group(1));
                        commentNo = Integer.parseInt(matcher.group(2));
                        if (statusCode == STATUS_SUCCESS) {
                            isPost = true;
                        } else {
                            throw new NicoAPIException.APIUnexpectedException(statusMap.get(statusCode));
                        }
                    } else {
                        throw new NicoAPIException.APIUnexpectedException("response is unexpected");
                    }
                } else {
                    throw new NicoAPIException.APIUnexpectedException("fail to post comment");
                }
            } else {
                throw new NicoAPIException.APIUnexpectedException("fail to get postKey");
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
                throw new NicoAPIException.IllegalStateException("not post a comment yet",NicoAPIException.EXCEPTION_ILLEGAL_STATE_COMMENT_NOT_POST);
            }
        }
    }

    public VideoInfo getTargetVideo(){
        return targetVideo;
    }
}
