package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;


import android.graphics.Paint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
 * JSONArray passed to {@link #parse(JSONArray)} is supposed to gotten from message server like this;<br>
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
 * expected response format;<br>
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;packet&gt;
 *     &lt;thread
 *          resultcode="0"
 *          thread="1173108780"
 *          server_time="1487684957"
 *          last_res="4464366"
 *          ticket="0x087dabd6"
 *          revision="12"
 *          click_revision="34166"/&gt;
 *     &lt;leaf
 *          thread="1173108780"
 *          count="1520974"/&gt;
 *     ...............
 *     &lt;chat
 *          thread="1173108780"
 *          no="4118020"
 *          vpos="36114"
 *          leaf="6"
 *          date="1361103390"
 *          score="-2200"
 *          premium="1"
 *          anonymity="1"
 *          user_id="Yg_6QWiiS_T2mq3WJuzUcp41MvQ"
 *          mail="184"&gt;
 *              混ぜるな危険ｗｗｗ
 *          &lt;/chat&gt;
 *  .......
 * &lt;/packet&gt;
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

public class CommentInfo {

    private long start;
    private String[] mail;   //list of comment commands, not mail address
    private String content;  //comment
    private String date;
    private boolean isAnonymous = true;   //1:normal
    private int ngLevel;
    private int commentNo;

    private boolean initialized = false;

    public float x,y;
    public float length = -1f;
    public float speed;
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
     * 日時の形式は{@link VideoInfo#dateFormatBase}に従います。<br>
     * The Date format follows {@link VideoInfo#dateFormatBase}.
     * @return
     */
    public String getDate(){
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
     * @return
     */
    public float getSize(){
        return size;
    }

    public static final int POSITION_UP = 0;
    public static final int POSITION_MIDDLE = 1;
    public static final int POSITION_BOTTOM = 2;

    private static final float SIZE_MEDIUM = 0.9f;
    private static final float SIZE_SMALL = 0.8f;
    private static final float SIZE_BIG = 1f;

    private final static Map<String,Integer> colorMap = new HashMap<String,Integer>(){
        {
            put("white", 0xffffffff);
            put("red",0xffff0000);
            put("pink",0xffff8080);
            put("orange",0xffffcc00);
            put("yellow",0xffffff00);
            put("green",0xff00ff00);
            put("cyan",0xff00ffff);
            put("blue",0xff0000ff);
            put("purple",0xffc000ff);
            put("black",0xff000000);
        }
    };
    private final static Map<String,Integer> positionMap= new HashMap<String,Integer>(){
        {
            put("naka",POSITION_MIDDLE);
            put("ue",POSITION_UP);
            put("shita",POSITION_BOTTOM);
        }
    };
    private final static Map<String,Float> sizeMap = new HashMap<String,Float>() {
        {
            put("medium", SIZE_MEDIUM);
            put("small", SIZE_SMALL);
            put("big", SIZE_BIG);
        }
    };

    private CommentInfo (JSONObject item) throws NicoAPIException{
        initialize(item);
    }
    private CommentInfo (String xml) throws NicoAPIException{
        initialize(xml);
    }

    //set command of comment from value of "mail"
    private void setCommands(){
        //set default value
        color = colorMap.get("white");
        position = POSITION_MIDDLE;
        size = SIZE_MEDIUM;
        if ( mail != null ) {
            for (String key : mail) {
                if ( colorMap.containsKey(key)) {
                    color = colorMap.get(key);
                } else if ( positionMap.containsKey(key)) {
                    position = positionMap.get(key);
                } else if ( sizeMap.containsKey(key)) {
                    size = sizeMap.get(key);
                }
            }
        }
    }

    private final Map<Integer,Integer> ngThresholdMap = new LinkedHashMap<Integer, Integer>(){
        {
            put(0,-1000);
            put(1,-4800);
            put(2,-10000);
        }
    };
    private void setNGLevel(int ng){
        for (int level : ngThresholdMap.keySet() ){
            if ( ng > ngThresholdMap.get(level) ){
                ngLevel = level;
                return;
            }else{
                ngLevel = level + 1;
            }
        }
    }

    private Pattern startPattern = Pattern.compile("vpos=\"([0-9]+?)\"");
    private Pattern anonymityPattern = Pattern.compile("anonymity=\"([0-9])\"");
    private Pattern datePattern = Pattern.compile("date=\"([0-9]+?)\"");
    private Pattern commandPattern = Pattern.compile("mail=\"(.+?)\"");
    private Pattern contentPattern = Pattern.compile(">(.+?)</chat>");
    private Pattern scorePattern = Pattern.compile("score=\"(-[0-9]+?)\"");
    private Pattern noPattern = Pattern.compile("no=\"([0-9]+?)\"");

    private void initialize (String xml) throws NicoAPIException{
        Matcher startMatcher,dateMatcher,contentMatcher,noMatcher;
        startMatcher = startPattern.matcher(xml);
        dateMatcher = datePattern.matcher(xml);
        contentMatcher = contentPattern.matcher(xml);
        noMatcher = noPattern.matcher(xml);
        if ( startMatcher.find() && dateMatcher.find() && contentMatcher.find() && noMatcher.find() ){
            start = Long.parseLong(startMatcher.group(1)) * 10;
            date = convertDate(Long.parseLong(dateMatcher.group(1)));
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
            int vpos = item.getInt("vpos");
            start = (long) vpos * 10;
            if (item.has("anonymity")) {
                if ( item.getInt("anonymity") == 1 ){
                    isAnonymous = false;
                }
            }
            date = convertDate(item.getLong("date"));
            if (item.has("mail")) {
                mail = item.getString("mail").split("\\s");
            }
            content = item.getString("content");
            commentNo = item.getInt("no");
            setCommands();
        }catch (JSONException e){
            throw new NicoAPIException.ParseException(
                    e.getMessage(),item.toString(),
                    NicoAPIException.EXCEPTION_PARSE_COMMENT_JSON
            );
        }
    }

    private String convertDate (long time){
        return VideoInfo.dateFormatBase.format(new Date(time * 1000));
    }

    /**
     * 実際にCanvasに描写する際、必要なフィールドを初期化する<br>
     * Initialize fields needed for being shown on Canvas.
     * @param width width of Canvas in pix
     * @param paint cannot be {@code null}
     * @param span  time in which the comment flows from one side to the other in seconds
     * @param offset offset in the direction of y in pixels
     */
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
    }

    /**
     * APIからのレスポンスをパースして、時系列に整列したリストを返す<br>
     * Parses JSONObject form message server and return items sorted along the time series
     * @param root response from message server, cannot be {@code null}
     * @return Returns empty list if response contains no items
     * @throws NicoAPIException if API response does not follow expected format or argument is {@code null}
     * @see CommentInfo expected response format
     *
     */
    protected static CommentGroup parse(JSONArray root) throws NicoAPIException{
        List<CommentInfo>commentList = new ArrayList<CommentInfo>();
        try{
            JSONObject meta = root.getJSONObject(0);
            meta = meta.getJSONObject("thread");
            int resultCode = meta.getInt("resultcode");
            int threadID = meta.getInt("thread");
            int lastComment = meta.getInt("last_res");
            String ticket = meta.getString("ticket");
            if (resultCode == 0) {
                for (int i = 1; i < root.length(); i++) {
                    JSONObject item = root.getJSONObject(i);
                    if (item.has("chat")) {
                        item = item.getJSONObject("chat");
                        if (!item.has("vpos") || !item.has("content")) {
                            continue;
                        }
                        commentList.add(new CommentInfo(item));
                    }
                }
                return new CommentGroup(sortComment(commentList), threadID, lastComment, ticket);
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

    protected static CommentGroup parse(String xml) throws NicoAPIException{
        List<CommentInfo>commentList = new ArrayList<CommentInfo>();
        Pattern metaPattern = Pattern.compile("<thread resultcode=\"([0-9]+?)\" thread=\"([0-9]+?)\".+? last_res=\"([0-9]+?)\" ticket=\"(.+?)\".+?/>");
        Matcher matcher = metaPattern.matcher(xml);
        if ( matcher.find() ){
            String resultCode = matcher.group(1);
            int threadID = Integer.parseInt(matcher.group(2));
            int lastComment = Integer.parseInt( matcher.group(3) );
            String ticket = matcher.group(4);
            if ( resultCode.equals("0") ){
                Pattern itemPattern = Pattern.compile("<chat ((?!deleted).)+?>.+?</chat>");
                matcher = itemPattern.matcher(xml);
                while( matcher.find() ){
                    CommentInfo info = new CommentInfo(matcher.group());
                    commentList.add( info );
                }
                return new CommentGroup(sortComment(commentList),threadID,lastComment,ticket);
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

    protected static class CommentGroup {
        protected List<CommentInfo> commentList;
        protected int threadID;
        protected String ticket;
        protected int lastComment;
        private CommentGroup (List<CommentInfo> list, int threadID, int lastComment, String ticket){
            this.commentList = list;
            this.threadID = threadID;
            this.ticket = ticket;
            this.lastComment = lastComment;
        }
    }

    //sort list of CommentInfo along the time series, using merge sort
    private static List<CommentInfo> sortComment(List<CommentInfo> list){
        if ( list.size() < 2 ){
            return list;
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
        return sort.get(0);
    }

    private static List<CommentInfo> merge (List<CommentInfo> a, List<CommentInfo> b){
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

}
