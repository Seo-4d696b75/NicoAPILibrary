package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;


import android.graphics.Paint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * コメントの取得・管理をする<br>
 * this class keeps information of each comment
 * also provides methods to parse response in Json to this class.<br>
 *
 * reference<br>
 * algorithm of merge sort : 東京大学出版会　情報科学入門 ISBN978-4-13-062452-7<br>
 * how to get comment : https://blog.hayu.io/web/nicovideo-comment-api<br><br>
 *
 *
 * note<br>
 * Json passed to {@link #parse(JSONArray)}  } is supposed to gotten from message server like this;<br>
 * http://msg.nicovideo.jp/10/api.json/thread?version=20090904&thread={0}&res_from=-{1}<br>
 * url of message server can be gotten from getflv API<br>
 * query should be like that (details is known)<br>
 *      params of query;<br>
 *      {0} : thread ID of target video, also can be gotten from getflv API<br>
 *      {1} : max number of comment, must not be over 1000
 *
 * @author Seo-4d696b75
 * @version 0.0 on 2017/01/01.
 */

public class CommentInfo {

    /**
     * コメントが右から流れ始める時間　単位はミリ秒<br>
     * time when the comment begins to flow from one side to the other. unit is milli sec.
     */
    public long start;
    private String[] mail;   //list of comment commands, not mail address
    /**
     * コメントの内容<br>
     *     content of the comment
     */
    public String content;  //comment
    /**
     * コメントの投稿日時<br>
     * date when the comment is contributed
     */
    public String date;
    /**
     * コメントの匿名性<br>
     * whether or not the comment is anonymous<br>
     * yes:0, no:1(default)
     */
    public int anonymity = 1;   //1:normal

    private boolean initialized = false;

    /**
     * 画面上のコメントの座標（ピクセル単位）<br>
     * x,y-coordinate on screen (pix)
     */
    public float x,y;
    /**
     * コメントの画面上での長さ(ピクセル)<br>
     * length on screen (pix)
     */
    public float length = -1f;
    /**
     * コメントの流れる速さ（ピクセル）<br>
     *     speed with which the comment flows (pix)
     */
    public float speed;
    /**
     * コメントの色<br>
     *     color of the comment
     */
    public int color = -1;
    /**
     * 表示行の高さに対するコメントサイズの比率<br>
     *     ratio of comment size to max height, can be between 0.0 to 1.0
     */
    public float size = -1f;
    /**
     * コメントの位置 POSITION定数<br>
     *     position of the comment, can be the following constants
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
    private final static Map<String,Float> sizeMap = new HashMap<String,Float>(){
        {
            put("medium",SIZE_MEDIUM);
            put("small",SIZE_SMALL);
            put("big",SIZE_BIG);
        }
    };

    /**
     * 初期化コンストラクタ <br>
     *     constructor for initialization.<br><br>
     * 適当なJSONを渡してパースする<br>
     * pass Json relevant to each comment, then Json is parsed and fields are initialized
     * @param item 一つのコメントに該当するＪＳＯＮ, Json relevant to each comment
     */
    public CommentInfo (JSONObject item){
        initialize(item);
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
    private void initialize (JSONObject item){
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
            setCommands();
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private String convertDate (long time){
        return VideoInfoManager.dateFormatBase.format(new Date(time * 1000));
    }

    /**
     * 実際にCanvasに描写する際、必要なフィールドを初期化する<br>
     *     initialize fields needed for being shown on Canvas.
     * @param width width of Canvas in pix
     * @param paint cannot be {@code null}
     * @param span  time in which the comment from one side to the other in seconds
     * @param offset offset in the direction of y in pixels
     */
    public void initialize(float width, Paint paint, float span, float offset){
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
     * parse Json form message server and return items sorted along the time series
     * @param root response from message server, cannot be {@code null}
     * @return Returns {@code null} if {@code root} is invalid, see {@link CommentInfo description;"note"}<br>
     *     Returns empty list if response contains no items
     */
    public static List<CommentInfo> parse(JSONArray root){
        List<CommentInfo>commentList = new ArrayList<CommentInfo>();
        try{
            for ( int i=0 ; i<root.length() ; i++){
                JSONObject item = root.getJSONObject(i);
                if ( item.has("chat")) {
                    item = item.getJSONObject("chat");
                    if ( !item.has("vpos") || !item.has("content") ){
                        continue;
                    }
                    commentList.add( new CommentInfo(item));
                }
            }
            return sortComment(commentList);
        } catch( JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @see #parse(JSONArray) works as the same as this method
     * @param res response from message server in String, cannot be {@code null}
     */
    public static List<CommentInfo> parse(String res){
        try{
            JSONArray root = new JSONArray(res);
            return parse(root);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    //sort list of CommentInfo along the time series, using merge sort
    private static List<CommentInfo> sortComment(List<CommentInfo> list){
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
