package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;


import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ニコ動ＡＰＩからのマイリスト情報をパースします<br>
 * This parses myList and tempMyList response from Nico.<br><br>
 *
 * This class extending VideoInfo provides methods to parse JSON<br>
 * from <br>
 * tempMyList : http://www.nicovideo.jp/api/deflist/list<br>
 * myList : http://www.nicovideo.jp/api/mylist/list?group_id={myListID}<br>
 * note; getting myList needs login session stored in cookie<br><br>
 *
 * expected response format;<br>
 * <pre>
 * {
 *  "mylistitem":[
 *      {
 *          "item_type":"0",
 *          "item_id":"1464652997",
 *          "description":"",
 *          "item_data":{
 *              "video_id":"sm....",
 *              "title":"\u3010\u521d\u97f3\u30df\u30af\u3011Pathos \u3010\u30aa\u30ea\u30b8\u30ca\u30eb\u66f2MV\u3011",
 *              "thumbnail_url":"http:\/\/tn-skr2.smilevideo.jp\/smile?i=28955541",
 *              "first_retrieve":1464688800,
 *              "update_time":1481814468,
 *              "view_counter":"70903",
 *              "mylist_counter":"2941",
 *              "num_res":"681",
 *              "group_type":"default",
 *              "length_seconds":"290",
 *              "deleted":"0",
 *              "last_res_body":"\u3093\u3042\u301c^  (中略) 8888888888... ",
 *              "watch_id":"sm28955541"
 *              },
 *          "watch":0,
 *          "create_time":1465732817,
 *          "update_time":1465732817
 *      },
 *      ......
 *  ],
 *  "status":"ok"
 * }
 * </pre><br><br>
 *
 * reference;<br>
 * how to get temp my list : <a href=https://ja.osdn.net/projects/nicolib/wiki/%E3%83%8B%E3%82%B3%E3%83%8B%E3%82%B3%E8%A7%A3%E6%9E%90%E3%83%A1%E3%83%A2>[ニコ★リブ]ニコニコ解析メモ</a><br>
 *
 * @author Seo-4d696b75
 * @version 0.0  on 2017/01/16.
 */

public class MyListVideoInfo extends VideoInfo implements Parcelable{

    private MyListVideoInfo( JSONObject item)  throws NicoAPIException.ParseException {
        initialize(item);
    }
    protected MyListVideoInfo (String xml) throws NicoAPIException{
        RSSVideoInfo.initialize(this,xml,false);
    }

    protected String myListItemDescription;
    protected String addDate,updateDate;
    public synchronized String getMyListItemDescription(){
        return myListItemDescription;
    }
    public synchronized String getAddDate(){
        return addDate;
    }
    public synchronized String getUpdateDate(){
        return updateDate;
    }
    protected synchronized void setMyListItemDescription(String description){
        this.myListItemDescription = description;
    }
    protected synchronized void setUpdateDate (String updateDate){
        this.updateDate = updateDate;
    }

    private synchronized void initialize(JSONObject item)  throws NicoAPIException.ParseException {
        try {
            threadID = item.getInt("item_id");
            myListItemDescription = item.getString("description");
            addDate = convertDate(item.getLong("create_time"));
            updateDate = convertDate(item.getLong("update_time"));
            JSONObject data = item.getJSONObject("item_data");
            setID( data.getString("video_id") );
            title = data.getString("title");
            setThumbnailUrl( data.getString("thumbnail_url") );
            date = convertDate(data.getLong("first_retrieve"));
            viewCounter = data.getInt("view_counter");
            commentCounter = data.getInt("num_res");
            myListCounter = data.getInt("mylist_counter");
            length = data.getInt("length_seconds");
        }catch ( JSONException e){
            throw new NicoAPIException.ParseException(
                    e.getMessage(),
                    item.toString(),
                    NicoAPIException.EXCEPTION_PARSE_MYLIST_JSON);
        }
    }

    protected static String convertDate(long date){
        return dateFormatBase.format(new Date(date*1000));
    }

    /**
     * ニコ動APIからのマイリスト情報をパースします<br>
     * Parses temp myList response from Nico.<br>
     * 基本的には{@link #parse(JSONObject)}と同じですが、JSONに対応した内容のStringを渡します。<br>
     * Basically, this is as same as {@link #parse(JSONObject)}, but String is passed.
     * @param response the string corresponding to root of result, cannot be {@code null}
     * @return Returns empty List if no hit, not {@code null}
     * @throws NicoAPIException if f invalid argument not following required format
     */
    protected static List<MyListVideoInfo> parse(String response)  throws NicoAPIException {
        if ( response != null ) {
            try {
                JSONObject root = new JSONObject(response);
                return parse(root);
            } catch (JSONException e) {
                throw new NicoAPIException.ParseException(e.getMessage(),response);
            }
        }
        throw new NicoAPIException.ParseException("parse target is null",null);
    }

    /**
     * ニコ動APIからのマイリスト情報をパースします<br>
     * Parses temp myList response from Nico.<br>
     * APIやレスポンスの形式など詳細は{@link MyListVideoInfo ここから参照}してください。<br>
     * 取得できるフィールドは以下の通りです<br>
     *     {@link VideoInfo#title 動画タイトル}<br>
     *     {@link VideoInfo#id 動画ID}<br>
     *     {@link VideoInfo#length 動画長さ}<br>
     *     {@link VideoInfo#date 動画投稿日時}<br>
     *     {@link VideoInfo#viewCounter 再生数}<br>
     *     {@link VideoInfo#commentCounter コメント数}<br>
     *     {@link VideoInfo#myListCounter マイリス数}<br>
     *     {@link VideoInfo#thumbnailUrl 動画サムネイル画像のURL}<br>
     * More details about API and response format are {@link MyListVideoInfo available here}.<br>
     * You can get these fields of video;<br>
     *     {@link VideoInfo#title title of video}<br>
     *     {@link VideoInfo#id video ID}<br>
     *     {@link VideoInfo#length length}<br>
     *     {@link VideoInfo#date contributed date}<br>
     *     {@link VideoInfo#viewCounter number of view}<br>
     *     {@link VideoInfo#commentCounter number of comment}<br>
     *     {@link VideoInfo#myListCounter number of myList registered}<br>
     *     {@link VideoInfo#thumbnailUrl URL of video thumbnail}<br>
     * @param root the JSONObject corresponding to root of result, cannot be {@code null}
     * @return Returns empty List if no hit, not {@code null}
     * @throws NicoAPIException if f invalid argument not following required format
     */
    protected static List<MyListVideoInfo> parse(JSONObject root)  throws NicoAPIException {
        try{
            JSONArray array = root.getJSONArray("mylistitem");
            List<MyListVideoInfo> list = new ArrayList<MyListVideoInfo>();
            for ( int i=0 ; i<array.length() ; i++) {
                JSONObject item = array.getJSONObject(i);
                list.add( new MyListVideoInfo(item) );
            }
            return list;
        }catch ( JSONException e){
            throw new NicoAPIException.ParseException(e.getMessage(),root.toString());
        }
    }

    /*implementation of parcelable*/
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out,flags);
        out.writeString(myListItemDescription);
        out.writeString(addDate);
        out.writeString(updateDate);
    }

    public static final Parcelable.Creator<MyListVideoInfo> CREATOR = new Parcelable.Creator<MyListVideoInfo>() {
        public MyListVideoInfo createFromParcel(Parcel in) {
            return new MyListVideoInfo(in);
        }
        public MyListVideoInfo[] newArray(int size) {
            return new MyListVideoInfo[size];
        }
    };

    private MyListVideoInfo(Parcel in) {
        title = in.readString();
        id = in.readString();
        date = in.readString();
        description = in.readString();
        thumbnailUrl = in.readArrayList(ArrayList.class.getClassLoader());
        thumbnail = in.readParcelable(Bitmap.class.getClassLoader());
        length = in.readInt();
        viewCounter = in.readInt();
        commentCounter = in.readInt();
        myListCounter = in.readInt();
        tags = in.readArrayList(ArrayList.class.getClassLoader());
        threadID = in.readInt();
        messageServerUrl = in.readString();
        flvUrl = in.readString();
        contributorID = in.readInt();
        contributorName = in.readString();
        contributorIconUrl = in.readString();
        contributorIcon = in.readParcelable(Bitmap.class.getClassLoader());
        point = in.readFloat();
        commentGroup = in.readParcelable(CommentInfo.CommentGroup.class.getClassLoader());
        myListItemDescription = in.readString();
        addDate = in.readString();
        updateDate = in.readString();
    }
}
