package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

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
 *
 * reference;<br>
 * how to get temp my list : <a href=https://ja.osdn.net/projects/nicolib/wiki/%E3%83%8B%E3%82%B3%E3%83%8B%E3%82%B3%E8%A7%A3%E6%9E%90%E3%83%A1%E3%83%A2>[ニコ★リブ]ニコニコ解析メモ</a><br>
 *
 * @author Seo-4d696b75
 * @version 0.0  on 2017/01/16.
 */

class NicoMyListVideoInfo extends NicoVideoInfo implements MyListVideoInfo{

    private NicoMyListVideoInfo( CookieGroup cookies, JSONObject item)  throws NicoAPIException.ParseException {
        super(cookies);
        initialize(item);
    }

    /**
     * RSSで取得したマイリス動画情報をパースします
     * Parses the myList response from RSS.<br>
     * {@link MyListVideoGroup#getMyListGroup(CookieGroup, int)}でマイリスＩＤから直接取得する際に使用します
     * @param xml the response
     * @throws NicoAPIException if fail to parse
     */
    protected NicoMyListVideoInfo ( CookieGroup cookies, String xml) throws NicoAPIException{
        super(cookies);
        initialize(xml);
    }

    protected String myListItemDescription;
    protected Date addDate,updateDate;

    /**
     * 動画のマイリス登録時に添えた説明を取得します
     * Gets the description added when the video is registered.
     * @return the description
     */
    @Override
    public synchronized String getMyListItemDescription(){
        return myListItemDescription;
    }
    /**
     * この動画のマイリスへの登録日時を取得します
     * Gets the date when this video was registered to the myList.<br>
     * @return the registered-date
     */
    @Override
    public synchronized Date getAddDate(){
        return addDate;
    }
    /**
     * この動画のマイリスへの登録説明を変更した最終日時を取得します　　
     * Gets the last date when the description was updated.<br>
     * @return the update date
     */
    @Override
    public synchronized Date getUpdateDate(){
        return updateDate;
    }

    protected synchronized void setMyListItemDetails(String description, Date addDate, Date updateDate){
        this.myListItemDescription = description;
        this.addDate = addDate;
        this.updateDate = updateDate;
    }

    private void initialize(JSONObject item)  throws NicoAPIException.ParseException {
        ResourceStore res = ResourceStore.getInstance();
        try {
            threadID = item.getInt(res.getString(R.string.key_myList_video_threadID));
            myListItemDescription = item.getString(res.getString(R.string.key_myList_video_description));
            addDate = new Date(1000*item.getLong(res.getString(R.string.key_myList_video_registration_time)));
            updateDate = new Date(1000*item.getLong(res.getString(R.string.key_myList_video_update_time)));
            JSONObject data = item.getJSONObject(res.getString(R.string.key_myList_video_data));
            setID( data.getString(res.getString(R.string.key_myList_video_id)) );
            title = data.getString(res.getString(R.string.key_myList_video_title));
            thumbnailUrl = data.getString(res.getString(R.string.key_myList_video_thumbnail));
            date = new Date(1000*data.getLong(res.getString(R.string.key_myList_video_contribution)));
            viewCounter = data.getInt(res.getString(R.string.key_myList_video_view));
            commentCounter = data.getInt(res.getString(R.string.key_myList_video_comment));
            myListCounter = data.getInt(res.getString(R.string.key_myList_video_myList));
            length = data.getInt(res.getString(R.string.key_myList_video_duration));
        }catch ( JSONException e){
            throw new NicoAPIException.ParseException(
                    e.getMessage(),
                    item.toString(),
                    NicoAPIException.EXCEPTION_PARSE_MYLIST_JSON);
        }
    }

    private void initialize(String xml) throws NicoAPIException.ParseException {
        Map<Integer, Integer> map = new HashMap<Integer, Integer>() {
            {
                put(VideoInfo.TITLE, R.string.regex_rss_title);
                put(VideoInfo.ID, R.string.regex_rss_id);
                put(VideoInfo.DESCRIPTION, R.string.regex_rss_description);
                put(VideoInfo.THUMBNAIL_URL, R.string.regex_rss_thumbnail);
                put(VideoInfo.LENGTH, R.string.regex_rss_duration);
                put(VideoInfo.DATE, R.string.regex_rss_date);
            }
        };
        Matcher matcher, rankingMatcher;
        ResourceStore res = ResourceStore.getInstance();
        for (int key : map.keySet()) {
            matcher = res.getPattern(map.get(key)).matcher(xml);
            if (matcher.find()) {
                String value = matcher.group(1);
                switch (key) {
                    case VideoInfo.TITLE:
                        //the number of ranking has to be taken away
                        rankingMatcher = res.getPattern(R.string.regex_rss_title_ranking_order).matcher(value);
                        if (rankingMatcher.find()) {
                            value = rankingMatcher.group(1);
                        } else {
                            throw new NicoAPIException.ParseException("title format is not expected > ranking"
                                    , value, NicoAPIException.EXCEPTION_PARSE_RANKING_TITLE);
                        }
                        title = value;
                        break;
                    case VideoInfo.ID:
                        setID(value);
                        break;
                    case VideoInfo.DESCRIPTION:
                        description = value;
                        break;
                    case VideoInfo.THUMBNAIL_URL:
                        thumbnailUrl = value;
                        break;
                    case VideoInfo.LENGTH:
                        length = RSSVideoInfo.parseLength(value);
                        break;
                    case VideoInfo.DATE:
                        date = new Date(1000*Integer.parseInt(value));
                        break;
                    default:
                }
            } else {
                throw new NicoAPIException.ParseException(
                        "target partial sequence matched with \"" + matcher.pattern().pattern() + "\" not found",
                        xml, NicoAPIException.EXCEPTION_PARSE_RANKING_MYLIST_NOT_FOUND
                );
            }
        }
        complete();
    }


    /**
     * ニコ動APIからのマイリスト情報をパースします<br>
     * Parses temp myList response from Nico.<br>
     * @param response the string corresponding to root of result, cannot be {@code null}
     * @return Returns empty List if no hit, not {@code null}
     * @throws NicoAPIException if f invalid argument not following required format
     */
    protected static List<MyListVideoInfo> parse(CookieGroup cookies, String response)  throws NicoAPIException {
        if ( response != null ) {
            try {
                JSONObject root = new JSONObject(response);
                return parse(cookies,root);
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
     *     動画タイトル<br>
     *     動画ID<br>
     *     動画長さ<br>
     *     動画投稿日時<br>
     *     再生数<br>
     *     コメント数<br>
     *     マイリス数<br>
     *     動画サムネイル画像のURL<br>
     * More details about API and response format are {@link MyListVideoInfo available here}.<br>
     * You can get these fields of video;<br>
     *     title of video<br>
     *     video ID<br>
     *     length<br>
     *     contributed date<br>
     *     number of view<br>
     *     number of comment<br>
     *     number of myList registered<br>
     *     URL of video thumbnail<br>
     * @param root the JSONObject corresponding to root of result, cannot be {@code null}
     * @return Returns empty List if no hit, not {@code null}
     * @throws NicoAPIException if f invalid argument not following required format
     */
    protected static List<MyListVideoInfo> parse(CookieGroup cookies, JSONObject root)  throws NicoAPIException {
        try{
            JSONArray array = root.getJSONArray(ResourceStore.getInstance().getString(R.string.key_myList_group));
            List<MyListVideoInfo> list = new ArrayList<MyListVideoInfo>();
            for ( int i=0 ; i<array.length() ; i++) {
                JSONObject item = array.getJSONObject(i);
                list.add( new NicoMyListVideoInfo(cookies,item) );
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
        out.writeSerializable(addDate);
        out.writeSerializable(updateDate);
    }

    public static final Parcelable.Creator<NicoMyListVideoInfo> CREATOR = new Parcelable.Creator<NicoMyListVideoInfo>() {
        public NicoMyListVideoInfo createFromParcel(Parcel in) {
            return new NicoMyListVideoInfo(in);
        }
        public NicoMyListVideoInfo[] newArray(int size) {
            return new NicoMyListVideoInfo[size];
        }
    };

    private NicoMyListVideoInfo(Parcel in) {
        super(in);
        myListItemDescription = in.readString();
        addDate = (Date)in.readSerializable();
        updateDate = (Date)in.readSerializable();
    }

}
