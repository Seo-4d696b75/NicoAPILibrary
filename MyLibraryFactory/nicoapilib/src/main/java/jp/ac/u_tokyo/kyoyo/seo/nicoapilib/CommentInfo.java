package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;


import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * コメントの取得・管理をする<br>
 * This class keeps information of each comment
 * also provides methods to parse response in Json to this class.<br>
 *
 *
 * note<br>
 * JSONArray passed to {@link CommentGroup} is supposed to gotten from message server like this;<br>
 * http://msg.nicovideo.jp/10/api.json/thread?version=20090904&thread={0}&res_from=-{1}<br>
 * query should be like that (details is unknown)<br>
 *      params of query;<br>
 *      {0} : thread ID of target video, also can be gotten from getflv API<br>
 *      {1} : max number of comment, must not be over 1000<br>
 *
 * url of message server can be gotten from getflv API, but when you do HttpGet to this url, response is XML format.
 * By adding ".json" at the end of this url, you can get response in Json.<br>
 *
 * Also you can get the same? response in XML by posting to the message server url;<br>
 * <pre>
 * &lt;packet&gt;
 *      &lt;thread thread="{0}" version="20090904"  /&gt;
 *      &lt;thread_leaves scores="1" thread="{0}"&gt;0-{2}:100,1000&lt;/thread_leaves&gt;
 * &lt;/packet&gt;
 * </pre><br>
 *     {0} : thread ID,  {2} : video length in minutes ( rounded up)<br><br>
 *
 *
 * </pre><br><br>
 * references;<br>
 * how to get comment form Nico : <a href=https://blog.hayu.io/web/nicovideo-comment-api>[NANOWAY]ニコニコ動画のコメント解析</a><br>
 * <a href=http://blog.livedoor.jp/mgpn/archives/51886270.html>[まぢぽん製作所]ニコニコ動画・ニコニコ生放送のコメント取得　備忘録</a><br>
 * NG level of comment in NIco : <a href=http://dic.nicovideo.jp/a/ng%E5%85%B1%E6%9C%89%E6%A9%9F%E8%83%BD>[ニコニコ大百科]NG共有機能</a>
 * details of comment : <a href=http://dic.nicovideo.jp/a/%E3%82%B3%E3%83%A1%E3%83%B3%E3%83%88>[ニコニコ大百科]コメント</a>
 *
 * @author Seo-4d696b75
 * @version 0.0 on 2017/01/01.
 */

public class CommentInfo implements Parcelable{

    private long start;
    private String[] mail;   //list of comment commands, not mail address
    private String content;  //comment
    private Date date;
    private boolean isAnonymous = true;   //1:normal
    private int ngLevel;
    private int commentNo;

    private boolean initialized = false;

    private int color = 0;
    private float size = -1f;
    private int position = -1;

    /**
     * コメントが右から流れ始める時間を取得する
     * Gets time when the comment begins to flow from one side to the other.<br>
     *     単位はミリ秒 measured in milli seconds.
     * @return comment time in milli seconds
     */
    public long getStart(){
        return start;
    }
    /**
     * コメント内容を取得する Gets comment text.
     * @return the comment, not {@code null}
     */
    public String getContent(){
        return content;
    }
    /**
     * コメントの投稿日時を取得する Gets posted-date of the comment.<br>
     * @return the posted-date, not {@code null}
     */
    public Date getDate(){
        return date;
    }
    /**
     * コメントの匿名性を取得する　Gets whether or not the comment is anonymous.
     * @return the value
     */
    public boolean isAnonymous (){
        return isAnonymous;
    }
    /**
     * コメントのＮＧレベルを取得する Gets the NG level.<br>
     * コメントのNGレベルの指標で、この値が大きいほどＮＧされた数が多いコメントであることを意味します。範囲[0-3]
     * 0から順にＮＧ共有レベル強・中・弱・無の閾値に対応します.<br>
     * The larger this value, the more NG this comment receives, in range 0-3.
     * @return the NG level
     */
    public int getNgLevel(){
        return ngLevel;
    }
    /**
     * コメントＩＤを取得する Gets the comment ID.<br>
     * ひとつの動画において各コメントに一意的な値です<br>
     *  Numbering of this comment.
     * @return the comment ID
     */
    public int getCommentNo(){
        return commentNo;
    }
    /**
     * コメントの色を取得する Gets the comment color.<br>
     * 値は実際の色のαRGB値を表します。<br>
     *  This value stands for actual αRGB.
     * @return the comment color in αRGB
     */
    public int getColor(){
        return color;
    }
    /**
     * コメントの位置を取得する　Gets the comment position.
     * @return the constant, one of POSITION_****
     */
    public int getPosition(){
        return position;
    }
    /**
     * コメントの大きさを取得する　Gets the comment size.
     * @return the comment size
     */
    public float getSize(){
        return size;
    }
    
    public static final int POSITION_UP = 0;
    public static final int POSITION_MIDDLE = 1;
    public static final int POSITION_BOTTOM = 2;

    public static final float SIZE_MEDIUM = 0.9f;
    public static final float SIZE_SMALL = 0.8f;
    public static final float SIZE_BIG = 1f;

    public static final String COLOR_WHITE     = "white";
    public static final String COLOR_RED       = "red";
    public static final String COLOR_PINK      = "pink";
    public static final String COLOR_ORANGE    = "orange";
    public static final String COLOR_YELLOW    = "yellow";
    public static final String COLOR_GREEN     = "green";
    public static final String COLOR_CYAN      = "cyan";
    public static final String COLOR_BLUE      = "blue";
    public static final String COLOR_PURPLE    = "purple";
    public static final String COLOR_BLACK     = "black";

    private CommentInfo (JSONObject item) throws NicoAPIException{
        initialize(item);
    }
    private CommentInfo (String xml) throws NicoAPIException{
        initialize(xml);
    }

    //set command of comment from value of "mail"
    private void setCommands(){
        //set default value
        ResourceStore resourceStore = ResourceStore.getInstance();
        color = resourceStore.getColor(COLOR_WHITE);
        position = POSITION_MIDDLE;
        size = SIZE_MEDIUM;
        if ( mail != null ) {
            for (String key : mail) {
                if ( resourceStore.containColor(key)) {
                    color = resourceStore.getColor(key);
                } else if ( resourceStore.containPosition(key)) {
                    position = resourceStore.getPosition(key);
                } else if ( resourceStore.containSize(key)) {
                    size = resourceStore.getSize(key);
                }
            }
        }
    }

    private void setNGLevel(int ng){
        ResourceStore res = ResourceStore.getInstance();
        for (int level = 0; res.containNGLevel(level) ; level++ ){
            if ( ng > res.getNGThreshold(level) ){
                ngLevel = level;
                return;
            }else{
                ngLevel = level + 1;
            }
        }
    }
    

    private void initialize (String xml) throws NicoAPIException{
        Pattern startPattern = ResourceStore.getInstance().getPattern(R.string.regex_comment_pos);
        Pattern anonymityPattern = ResourceStore.getInstance().getPattern(R.string.regex_comment_anonymity);
        Pattern datePattern = ResourceStore.getInstance().getPattern(R.string.regex_comment_date);
        Pattern commandPattern = ResourceStore.getInstance().getPattern(R.string.regex_comment_command);
        Pattern contentPattern = ResourceStore.getInstance().getPattern(R.string.regex_comment_content);
        Pattern scorePattern = ResourceStore.getInstance().getPattern(R.string.regex_comment_score);
        Pattern noPattern = ResourceStore.getInstance().getPattern(R.string.regex_comment_id);
        Matcher startMatcher,dateMatcher,contentMatcher,noMatcher;
        startMatcher = startPattern.matcher(xml);
        dateMatcher = datePattern.matcher(xml);
        contentMatcher = contentPattern.matcher(xml);
        noMatcher = noPattern.matcher(xml);
        if ( startMatcher.find() && dateMatcher.find() && contentMatcher.find() && noMatcher.find() ){
            start = Long.parseLong(startMatcher.group(1)) * 10;
            date = new Date(1000*Long.parseLong(dateMatcher.group(1)));
            content = contentMatcher.group(1);
            commentNo = Integer.parseInt( noMatcher.group(1) );
            Matcher matcher = anonymityPattern.matcher(xml);
            if ( matcher.find() ){
                if ( Integer.parseInt(matcher.group(1)) == 0 ){
                    isAnonymous = false;
                }
            }
            matcher = scorePattern.matcher(xml);
            if (matcher.find()) {
                setNGLevel(Integer.parseInt(matcher.group(1)));
            }else{
                setNGLevel(0);
            }
            matcher = commandPattern.matcher(xml);
            if ( matcher.find() ){
                mail = matcher.group(1).split("\\s");
            }
            setCommands();
        }else{
            throw new NicoAPIException.ParseException(
                    "target attribute not found; 'vpos' 'date' 'content' > comment",
                    xml,NicoAPIException.EXCEPTION_PARSE_COMMENT_XML
            );
        }
    }

    private void initialize (JSONObject item) throws NicoAPIException{
        try {
            //value of "vpos" seems to be time when comment appear,
            // but unit is decimal sec, not milli sec
            ResourceStore res = ResourceStore.getInstance();
            int vpos = item.getInt(res.getString(R.string.key_comment_position));
            start = (long) vpos * 10;
            if (item.has(res.getString(R.string.key_comment_anonymity))) {
                if ( item.getInt(res.getString(R.string.key_comment_anonymity)) == 1 ){
                    isAnonymous = false;
                }
            }
            date = new Date(1000*item.getLong(res.getString(R.string.key_comment_date)));
            if (item.has(res.getString(R.string.key_comment_command))) {
                mail = item.getString(res.getString(R.string.key_comment_command)).split("\\s");
            }
            content = item.getString(res.getString(R.string.key_comment_content));
            commentNo = item.getInt(res.getString(R.string.key_comment_id));
            setCommands();
        }catch (JSONException e){
            throw new NicoAPIException.ParseException(
                    e.getMessage(),item.toString(),
                    NicoAPIException.EXCEPTION_PARSE_COMMENT_JSON
            );
        }
    }


    /*
    /**
     * 実際にCanvasに描写する際、必要なフィールドを初期化する<br>
     * Initialize fields needed for being shown on Canvas.
     * @param width width of Canvas in pix
     * @param paint cannot be {@code null}
     * @param span  time in which the comment flows from one side to the other in seconds
     * @param offset offset in the direction of y in pixels

    public void initialize(float width, Paint paint, float span, float offset){
        if ( paint == null ){
            return;
        }
        if ( ! initialized ){
            length = paint.measureText(content);
            speed = (width + length )/span;
            y = offset;
            switch ( position ){
                case POSITION_BOTTOM:
                case POSITION_UP:
                    x = (width-length)/2f;
                    break;
                case POSITION_MIDDLE:
                    x = width;
                    break;
            }
        }
    }*/

    /*implementation of parcelable*/

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(content);
        out.writeSerializable(date);
        out.writeBooleanArray(new boolean[]{isAnonymous});
        out.writeInt(ngLevel);
        out.writeInt(commentNo);
        out.writeBooleanArray(new boolean[]{initialized});
        out.writeInt(color);
        out.writeFloat(size);
        out.writeInt(position);
    }

    public static final Parcelable.Creator<CommentInfo> CREATOR = new Parcelable.Creator<CommentInfo>() {
        public CommentInfo createFromParcel(Parcel in) {
            return new CommentInfo(in);
        }
        public CommentInfo[] newArray(int size) {
            return new CommentInfo[size];
        }
    };

    private CommentInfo(Parcel in) {
        this.content = in.readString();
        this.date = (Date)in.readSerializable();
        boolean[] val = new boolean[1];
        in.readBooleanArray(val);
        this.isAnonymous = val[0];
        this.ngLevel = in.readInt();
        this.commentNo = in.readInt();
        in.readBooleanArray(val);
        this.initialized = val[0];
        this.color = in.readInt();
        this.size = in.readFloat();
        this.position = in.readInt();
    }

    /**
     * 取得したコメントを保持・管理するクラスです
     */
    public static class CommentGroup implements Parcelable {
        private List<CommentInfo> commentList;
        private int threadID;
        private String ticket;
        private int lastComment;
        private Date date;
        protected CommentGroup (String xml) throws NicoAPIException{
            ResourceStore res = ResourceStore.getInstance();
            Pattern metaPattern = res.getPattern(R.string.regex_comment_group_meta);
            Matcher matcher = metaPattern.matcher(xml);
            if ( matcher.find() ){
                String resultCode = matcher.group(1);
                this.date = new Date(1000*Long.parseLong(matcher.group(3)));
                this.threadID = Integer.parseInt(matcher.group(2));
                this.lastComment = Integer.parseInt( matcher.group(4) );
                this.ticket = matcher.group(5);
                if ( resultCode.equals(res.getString(R.string.value_comment_group_meta_result_success)) ){
                    this.commentList = new ArrayList<CommentInfo>();
                    Pattern itemPattern = res.getPattern(R.string.regex_comment_group_item);
                    matcher = itemPattern.matcher(xml);
                    while (matcher.find()) {
                        CommentInfo info = new CommentInfo(matcher.group());
                        commentList.add(info);
                    }
                    sortComment();
                }else{
                    throw new NicoAPIException.APIUnexpectedException(
                            "Unexpected API response status > comment",
                            NicoAPIException.EXCEPTION_UNEXPECTED_COMMENT_STATUS_CODE
                    );
                }
            }else{
                throw  new NicoAPIException.ParseException(
                        "meta not found in XML > comment",xml,
                        NicoAPIException.EXCEPTION_PARSE_COMMENT_XML_META
                );
            }
        }
        protected CommentGroup (JSONArray root) throws NicoAPIException{
            ResourceStore res = ResourceStore.getInstance();
            try{
                JSONObject meta = root.getJSONObject(0);
                meta = meta.getJSONObject(res.getString(R.string.key_comment_meta));
                int resultCode = meta.getInt(res.getString(R.string.key_comment_status));
                this.threadID = meta.getInt(res.getString(R.string.key_comment_threadID));
                this.date = new Date(1000*meta.getLong(res.getString(R.string.key_comment_date)));
                this.lastComment = meta.getInt(res.getString(R.string.key_comment_last_comment));
                this.ticket = meta.getString(res.getString(R.string.key_comment_ticket));
                if (resultCode == Integer.parseInt(res.getString(R.string.value_comment_group_meta_result_success))) {
                    this.commentList = new ArrayList<CommentInfo>();
                    for (int i = 1; i < root.length(); i++) {
                        JSONObject item = root.getJSONObject(i);
                        if (item.has(res.getString(R.string.key_comment_group_item))) {
                            item = item.getJSONObject(res.getString(R.string.key_comment_group_item));
                            if (!item.has(res.getString(R.string.key_comment_position)) || !item.has(res.getString(R.string.key_comment_content))) {
                                continue;
                            }
                            commentList.add(new CommentInfo(item));
                        }
                    }
                    sortComment();
                } else {
                    throw new NicoAPIException.APIUnexpectedException(
                            "Unexpected API response status > comment",
                            NicoAPIException.EXCEPTION_UNEXPECTED_COMMENT_STATUS_CODE
                    );
                }
            } catch( JSONException e){
                throw new NicoAPIException.ParseException(
                        e.getMessage(),root.toString(),
                        NicoAPIException.EXCEPTION_PARSE_COMMENT_JSON_META
                );
            }
        }

        /**
         * コメントを取得します Gets comments.<br>
         * コメントを格納した{@code List}オブジェクトを変更しても問題ありません。
         * Making change to {@code List} object does not matter.
         * @return {@code List} of comments
         */
        public List<CommentInfo> getComments(){
            List<CommentInfo> list = new ArrayList<CommentInfo>();
            list.addAll(commentList);
            return list;
        }
        /**
         * コメント動画のスレッドＩＤを取得します
         * Gets threadID, from which the comments come.
         * @return the threadID, which stands for the target video
         */
        public int getThreadID(){
            return threadID;
        }
        /**
         * チケット値を取得します Gets ticket value.
         * @return the ticket value, not {@code null}
         */
        public String getTicket(){
            return ticket;
        }
        /**
         * コメント総数を取得します Gets total number of comments.
         * @return the total number
         */
        public int getLastComment(){
            return lastComment;
        }
        /**
         * メッセージサーバとの通信時間を取得する Gets time when this communicates with the message server.
         * @return the communicate-time, not {@code null}
         */
        public Date getDate(){
            return date;
        }

        //sort list of CommentInfo along the time series, using merge sort
        private void sortComment(){
            List<CommentInfo> list = this.commentList;
            if ( list.size() < 2 ){
                return;
            }
            List<List<CommentInfo>> sort = new ArrayList<List<CommentInfo>>();
            for ( int i=0 ; i<list.size() ; i++){
                List<CommentInfo> item = new ArrayList<CommentInfo>();
                item.add(list.get(i));
                sort.add(item);
            }
            int n = sort.size();
            while ( n > 1 ){
                List<List<CommentInfo>> temp = new ArrayList<List<CommentInfo>>();
                for ( int i=0 ; i<n/2 ; i++){
                    temp.add(merge(sort.get(2*i),sort.get(2*i+1)));
                }
                if ( sort.size() == 2*n+1){
                    temp.add(sort.get(2*n));
                }
                sort = temp;
                n = sort.size();
            }
            this.commentList = sort.get(0);
        }

        private List<CommentInfo> merge (List<CommentInfo> a, List<CommentInfo> b){
            List<CommentInfo> list = new ArrayList<CommentInfo>();
            while ( a.size() > 0 && b.size() > 0 ){
                if ( a.get(0).start < b.get(0).start ){
                    list.add(a.get(0));
                    a.remove(0);
                }else{
                    list.add(b.get(0));
                    b.remove(0);
                }
            }
            for ( int i=0 ; i<a.size() ; i++){
                list.add(a.get(i));
            }
            for ( int i=0 ; i<b.size() ; i++){
                list.add(b.get(i));
            }
            return list;
        }

        public int describeContents() {
            return 0;
        }
        public void writeToParcel(Parcel out, int flags) {
            out.writeList(commentList);
            out.writeInt(threadID);
            out.writeString(ticket);
            out.writeInt(lastComment);
            out.writeSerializable(date);
        }
        public static final Parcelable.Creator<CommentGroup> CREATOR = new Parcelable.Creator<CommentGroup>() {
            public CommentGroup createFromParcel(Parcel in) {
                return new CommentGroup(in);
            }
            public CommentGroup[] newArray(int size) {
                return new CommentGroup[size];
            }
        };
        private CommentGroup(Parcel in) {
            this.commentList = new ArrayList<>();
            in.readList(this.commentList,List.class.getClassLoader());
            this.threadID = in.readInt();
            this.ticket = in.readString();
            this.lastComment = in.readInt();
            this.date = (Date)in.readSerializable();
        }
    }



}
