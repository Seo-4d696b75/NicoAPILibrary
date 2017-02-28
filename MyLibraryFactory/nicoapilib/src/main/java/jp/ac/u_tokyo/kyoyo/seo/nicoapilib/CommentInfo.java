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

    /**
     * コメントが右から流れ始める時間　単位はミリ秒<br>
     * time when the comment begins to flow from one side to the other, measured in milli seconds.
     */
    public long start;
    private String[] mail;   //list of comment commands, not mail address
    /**
     * コメントの内容<br>
     * content of the comment
     */
    public String content;  //comment
    /**
     * コメントの投稿日時、{@link VideoInfo#dateFormatBase 共通形式}に従う<br>
     * date when the comment is contributed, based on {@link VideoInfo#dateFormatBase common format}
     */
    public String date;
    /**
     * コメントの匿名性<br>
     * whether or not the comment is anonymous<br>
     * yes:0, no:1(default)
     */
    public int anonymity = 1;   //1:normal
    /**
     * コメントのNGレベルの指標です<br>
     * NG level of this comment.<br>
     * この値が大きいほどＮＧされた数が多いコメントであることを意味します。範囲[0-3]
     * 0から順にＮＧ共有レベル強・中・弱・無の閾値に対応します.<br>
     * The larger this value, the more NG this comment receives, in range 0-3.
     */
    public int ngLevel;
    /**
     * ひとつの動画において各コメントに一意的な値です<br>
     *  Numbering of this comment.
     */
    public int commentNo;

    private boolean initialized = false;

    /**
     * 画面上のコメントの座標（ピクセル単位）<br>
     * x,y-coordinate on screen, measured in pixels
     */
    public float x,y;
    /**
     * コメントの画面上での長さ(ピクセル単位)<br>
     * length on screen, measured in pixels
     */
    public float length = -1f;
    /**
     * コメントの流れる速さ（ピクセル/s）<br>
     *     speed with which the comment flows, measured in pixels per second
     */
    public float speed;
    /**
     * コメントの色<br>
     * color of the comment
     */
    public int color = 0;
    /**
     * 表示行の高さに対するコメントサイズの比率<br>
     *    ratio of comment size to max height, can be between 0.0 to 1.0
     */
    public float size = -1f;
    /**
     * コメントの位置 POSITION定数<br>
     *     position of the comment, can be the following constants; POSITION_****
     */
    public int position = -1;

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
                anonymity = Integer.parseInt(matcher.group(1));
            }else{
                anonymity = 1;
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
            throw new NicoAPIException.ParseException("target attribute not found; 'vpos' 'date' 'content' > comment",xml);
        }
    }
    private void initialize (JSONObject item) throws NicoAPIException{
        try {
            //value of "vpos" seems to be time when comment appear,
            // but unit is decimal sec, not milli sec
            int vpos = item.getInt("vpos");
            start = (long) vpos * 10;
            if (item.has("anonymity")) {
                anonymity = item.getInt("anonymity");
            }
            date = convertDate(item.getLong("date"));
            if (item.has("mail")) {
                mail = item.getString("mail").split("\\s");
            }
            content = item.getString("content");
            commentNo = item.getInt("no");
            setCommands();
        }catch (JSONException e){
            throw new NicoAPIException.ParseException(e.getMessage(),item.toString());
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
            if ( meta.has("thread") ) {
                meta = meta.getJSONObject("thread");
                if ( meta.has("resultcode") && meta.has("thread") && meta.has("last_res") && meta.has("ticket") ) {
                    int resultCode = meta.getInt("resultcode");
                    String threadID = meta.getString("thread");
                    int lastComment = meta.getInt("last_res");
                    String ticket = meta.getString("ticket");
                    if ( resultCode == 0 ) {
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
                        return new CommentGroup(sortComment(commentList),threadID,lastComment,ticket);
                    }else{
                        throw  new NicoAPIException.APIUnexpectedException("Unexpected API response status > comment");
                    }
                }else{
                    throw  new NicoAPIException.APIUnexpectedException("Unexpected API response meta > comment");
                }
            }else{
                throw  new NicoAPIException.APIUnexpectedException("Unexpected API response, meta not found > comment");
            }
        } catch( JSONException e){
            throw new NicoAPIException.ParseException(e.getMessage(),root.toString());
        }
    }

    protected static CommentGroup parse(String xml) throws NicoAPIException{
        List<CommentInfo>commentList = new ArrayList<CommentInfo>();
        Pattern metaPattern = Pattern.compile("<thread resultcode=\"([0-9]+?)\" thread=\"([0-9]+?)\".+? last_res=\"([0-9]+?)\" ticket=\"(.+?)\".+?/>");
        Matcher matcher = metaPattern.matcher(xml);
        if ( matcher.find() ){
            String resultCode = matcher.group(1);
            String threadID = matcher.group(2);
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
                throw  new NicoAPIException.APIUnexpectedException("Unexpected API response status > comment");
            }
        }else{
            throw  new NicoAPIException.APIUnexpectedException("Unexpected API response meta > comment");
        }
    }

    protected static class CommentGroup {
        protected List<CommentInfo> commentList;
        protected String threadID;
        protected String ticket;
        protected int lastComment;
        private CommentGroup (List<CommentInfo> list, String threadID, int lastComment, String ticket){
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
