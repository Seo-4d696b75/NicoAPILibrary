package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.http.client.CookieStore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * このクラスはマイリストを保持・管理します
 * This class contains and manages one myList.<br>
 * このオブジェクトが一つのマイリストに対応し,
 * マイリスが所属する{@link MyListGroup グループ}から{@link MyListGroup#getMyListVideoGroup()}で取得できます。
 * {@link NicoClient#getMyList(int)}でマイリスＩＤから直接取得も可能です。
 * マイリスに登録された動画の取得やマイリスを編集するには、このオブジェクトを操作します。<br>
 * This object corresponds to one myList.
 * By calling {@link MyListGroup#getMyListVideoGroup()} in {@link MyListGroup myListGroup},
 * you can get this object.
 * When videos belonging to this myList are needed or you make any change to this myList,
 * you call appropriate methods in this object.
 *
 * @author Seo-4d696b75
 * @version 0.0 on 2017/02/28.
 */

public class MyListVideoGroup extends MyListEditor implements Parcelable{

    //from myList group
    protected MyListVideoGroup (LoginInfo info, JSONObject item) throws JSONException {
        setDetails(item);
        super.info = info;
    }
    //from myListID directly
    private MyListVideoGroup (LoginInfo info, String xml, String name, String description) throws NicoAPIException{
        super.info = info;
        this.info = info;
        this.videoInfoList = new ArrayList<MyListVideoInfo>();
        Matcher matcher = Pattern.compile("<item>.+?</item>",Pattern.DOTALL).matcher(xml);
        while (matcher.find() ){
            videoInfoList.add(new MyListVideoInfo( matcher.group()));
        }
        this.name = name;
        this.description = description;
        getDetails();
    }

    private synchronized void setDetails(JSONObject item) throws JSONException{
        myListID = item.getInt("id");
        userID = item.getInt("user_id");
        name = item.getString("name");
        description = item.getString("description");
        if (item.getInt("public") == 1) {
            isPublic = true;
        } else {
            isPublic = false;
        }
        defaultSort = item.getInt("default_sort");
        createDate = MyListVideoInfo.convertDate(item.getLong("create_time"));
        updateDate = MyListVideoInfo.convertDate(item.getLong("update_time"));
        iconID = item.getInt("icon_id");
    }

    /**
     * 各マイリストに固有の値です　ID of each myList.
     */
    protected int myListID;
    /**
     * このマイリスの制作者のユーザＩＤです
     * ID of the user who made this myList.
     * 他ユーザの公開マイリスを取得した場合、自身のユーザＩＤと異なります。
     */
    protected int userID;
    /**
     * このマイリスの名前　name of this myList.
     * 同一ユーザのマイリスグループ内であっても、名前が一意である保証はないので注意してください。
     */
    protected String name;
    /**
     * このマイリスの説明・コメント　comment of this myList.
     */
    protected String description;
    /**
     * マイリスの公開設定 whether or not this myList is public.
     */
    protected boolean isPublic;
    /**
     * マイリス整列時に用いるソート方法のパラメータです
     * parameter defining how to sort videos in this myList.<br>
     * {@link MyListGroup マイリスグループ内で}用意されている以下の定数値をとり得ます; MYLIST_SORT_****
     */
    protected int defaultSort;
    /**
     * マイリスを制作した日時です
     * date when this myList is created.
     * 表示形式はライブラリ内の{@link VideoInfo#dateFormatBase 共通形式}に従います。
     */
    protected String createDate;
    /**
     * マイリスを最後に編集した日時です
     * date when this myList was edited at last time.
     * 動画をマイリスに追加したり削除しても変化しません。
     * 初期値はマイリス制作日時と同一で、
     * {@link MyListGroup#update(MyListVideoGroup, String, boolean, int, String) マイリスグループからマイリス設定を編集}
     * するとその日時で上書きされます。
     * 表示形式はライブラリ内の{@link VideoInfo#dateFormatBase 共通形式}に従います。
     */
    protected String updateDate;
    protected int iconID;
    protected List<MyListVideoInfo> videoInfoList;

    /* <implementation of parcelable> */

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(super.info, flags);
        out.writeInt(myListID);
        out.writeInt(userID);
        out.writeString(name);
        out.writeString(description);
        out.writeBooleanArray(new boolean[]{isPublic});
        out.writeInt(defaultSort);
        out.writeString(createDate);
        out.writeString(updateDate);
        out.writeInt(iconID);
        out.writeList(videoInfoList);
    }

    public static final Parcelable.Creator<MyListVideoGroup> CREATOR = new Parcelable.Creator<MyListVideoGroup>() {
        public MyListVideoGroup createFromParcel(Parcel in) {
            return new MyListVideoGroup(in);
        }
        public MyListVideoGroup[] newArray(int size) {
            return new MyListVideoGroup[size];
        }
    };

    private MyListVideoGroup(Parcel in) {
        super.info = in.readParcelable(LoginInfo.class.getClassLoader());
        this.myListID = in.readInt();
        this.userID = in.readInt();
        this.name = in.readString();
        this.description = in.readString();
        boolean[] val = new boolean[1];
        in.readBooleanArray(val);
        this.isPublic = val[0];
        this.defaultSort = in.readInt();
        this.createDate = in.readString();
        this.updateDate = in.readString();
        this.iconID = in.readInt();
        this.videoInfoList = in.readArrayList(ArrayList.class.getClassLoader());
    }

    /* </implementation of parcelable> */

    /**
     * マイリスＩＤを取得します　Gets myListID.
     * @return the myListID
     * @see #myListID
     */
    public synchronized int getMyListID(){
        return myListID;
    }
    /**
     * マイリス製作者のユーザＩＤを取得します　Gets userID who made this myList.
     * @return the userID
     * @see #userID
     */
    public synchronized int getUserID(){
        return userID;
    }
    /**
     * マイリスの名前を取得します　Gets myList name.
     * @return the name of this myList
     * @see #name
     */
    public synchronized String getName(){
        return name;
    }
    /**
     * マイリスの説明を取得します　Gets description of this myList.
     * @return the myList description
     */
    public synchronized String getDescription(){
        return description;
    }
    /**
     * マイリスの公開設定を取得します　Gets whether or not this myList is public.
     * @return whether this myList is public
     */
    public synchronized boolean isPublic(){
        return isPublic;
    }
    /**
     * ソートパラメータを取得します　Gets sort parameter.
     * @return the parameter, one of MYLIST_SORT_*****
     * @see #defaultSort
     */
    public synchronized int getDefaultSort(){
        return defaultSort;
    }
    public synchronized int getIconID(){
        return iconID;
    }
    /**
     * マイリス制作日時を取得します　Gets date when this myList was created.
     * @return the date
     * @see #createDate
     */
    public synchronized String getCreateDate(){
        return createDate;
    }
    /**
     * マイリスの最終編集日時を取得します　Gets date when this myLis was edited at last time.
     * @return the date
     * @see #updateDate
     */
    public synchronized String getUpdateDate(){
        return updateDate;
    }

    private String detailsGetURL = "http://www.nicovideo.jp/api/mylistgroup/get";
    private String sortURL = "http://www.nicovideo.jp/api/mylistgroup/sort";
    private String rootURL = "http://www.nicovideo.jp/api/mylist/";

    /**
     * このマイリスの設定値を取得してセットします
     * Gets setting values of this myList and set them.<br>
     * {@link MyListGroup#update(MyListVideoGroup, String, boolean, int, String)}
     * でマイリスの設定を変更した後に呼ばれます。<br>
     * <strong>ＵＩスレッド禁止</strong> HTTP通信を行うのでバックグランドで処理して下さい。<br>
     * <strong>No UI thread</strong>: HTTP communication is done
     * @throws NicoAPIException　if fail
     */
    protected void getDetails() throws NicoAPIException{
        Map<String,String> param = new HashMap<String ,String>();
        param.put("group_id", String.valueOf(myListID));
        if ( tryPost( detailsGetURL,param,info.getCookieStore() ) ){
            JSONObject root = checkStatusCode(super.response);
            try{
                setDetails( root.getJSONObject("mylistgroup"));
            }catch (JSONException e){
                throw new NicoAPIException.ParseException(
                        e.getMessage(),root.toString(),
                        NicoAPIException.EXCEPTION_PARSE_MYLIST_DETAILS_JSON
                );
            }
        }else{
            throw new NicoAPIException.HttpException(
                    "HTTP failure > myList details",
                    NicoAPIException.EXCEPTION_HTTP_MYLIST_DETAILS_GET,
                    super.statusCode, detailsGetURL,"POST"
            );
        }
    }

    /**
     * このマイリスに登録されている動画を取得します　Gets videos belonging to this myList.<br>
     * {@link MyListGroup#getMyListVideoGroup() マイリスグループから取得した}段階では、
     * まだ登録動画は未取得なので{@link #loadVideos()}で予め取得しておく必要があります。
     * この登録動画のフィールドが初期化されていない状態で呼ばれると例外を投げます。
     * また、動画を格納する返り値の{@code List}オブジェクトに変更を加えても問題はありません。
     * ただし、マイリスの登録動画を編集してもその{@code List}オブジェクトには反映されないので再取得してください。<br>
     * When this myList is gotten from {@link MyListGroup#getMyListVideoGroup()},
     * the registered videos are not set yet. Be sure to call {@link #loadVideos()} and set videos in advance.
     * Making any change to returned {@code List} object does not matter.
     * When you make any change to this myList, no change is applied to the {@code List} object.
     * @return {@code List} of videos belonging to this myList
     * @throws NicoAPIException if videos are not set yet
     */
    public synchronized List<MyListVideoInfo> getVideos() throws NicoAPIException{
        if ( videoInfoList == null ){
            throw new NicoAPIException.IllegalStateException(
                    "videos not initialized > myList",
                    NicoAPIException.EXCEPTION_ILLEGAL_STATE_MYLIST_VIDEO
            );
        }
        List<MyListVideoInfo> list = new ArrayList<MyListVideoInfo>();
        for (MyListVideoInfo info : videoInfoList) {
            list.add(info);
        }
        return list;
    }

    /**
     * マイリスに登録されている動画を取得してフィールドに保持します
     * Gets videos belonging to this myList and set them.<br>
     * <strong>ＵＩスレッド禁止</strong> HTTP通信を行うのでバックグランドで処理して下さい。<br>
     * <strong>No UI thread</strong>: HTTP communication is done
     * @throws NicoAPIException if fail to get videos
     */
    public void loadVideos() throws NicoAPIException{
        String path = rootURL + "list";
        Map<String, String> param = new HashMap<String, String>();
        param.put("group_id", String.valueOf(myListID));
        if (tryPost(path, param, info.getCookieStore())) {
            JSONObject root = checkStatusCode(super.response);
            List<MyListVideoInfo> list  = MyListVideoInfo.parse(root);
            synchronized (this) {
                this.videoInfoList = replaceList(this.videoInfoList,list);
            }
        } else {
            throw new NicoAPIException.HttpException(
                    "HTTP failure > myList details",
                    NicoAPIException.EXCEPTION_HTTP_MYLIST_VIDEOS_GET,
                    super.statusCode, path, "POST"
            );
        }
    }

    protected static List<MyListVideoGroup> parse (JSONObject root, LoginInfo info)throws NicoAPIException{
        List<MyListVideoGroup> list = new ArrayList<MyListVideoGroup>();
        try {
            JSONArray group = root.getJSONArray("mylistgroup");
            for (int i = 0; i < group.length(); i++) {
                JSONObject myListItem = group.getJSONObject(i);
                list.add(new MyListVideoGroup(info, myListItem));
            }
        } catch (JSONException e) {
            throw new NicoAPIException.ParseException(
                    e.getMessage(), root.toString(),
                    NicoAPIException.EXCEPTION_PARSE_MYLIST_GROUP_JSON
            );
        }
        return list;
    }

    /**
     * マイリスＩＤから直接マイリスを取得します
     * Gets myList specified by myListID directly.<br><br>
     * <strong>ＵＩスレッド禁止</strong> HTTP通信を行うのでバックグランドで処理して下さい。<br>
     * <strong>No UI thread</strong>: HTTP communication is done<br>
     * 公開マイリスの場合はログインしていなくても他ユーザのマイリスでも取得できます。
     * ただし、他ユーザのマイリスを編集するのは不可能ですので注意してください。
     * このメソッドから得た{@link MyListVideoGroup}オブジェクトには登録動画がすでに取得されています。
     * {@link #loadVideos()}を呼ばなくても{@link #getVideos()}で動画を取得できます。<br>
     *  When the target myList is public, it is possible to get the myList even though not login or it is other user's.
     *  But be careful that making any change to other user's myList is impossible.
     *  {@link MyListVideoGroup} object gotten from this already contains videos registered in the myList.
     *  Getting these videos in {@link #getVideos()} does not requires calling {@link #loadVideos()} beforehand.
     * @param myListID the ID
     * @param info the login status
     * @return the target myList
     * @throws NicoAPIException if fail to get the target because of public-setting or other
     */
    protected static MyListVideoGroup getMyListGroup(int myListID, LoginInfo info) throws NicoAPIException{
        CookieStore loginCookie = null;
        try{
            loginCookie = info.getCookieStore();
        }catch (NicoAPIException.NoLoginException e){}
        String myListUrl = String.format("http://www.nicovideo.jp/mylist/%d?rss=2.0",myListID);
        HttpResponseGetter getter = new HttpResponseGetter();
        if ( getter.tryGet(myListUrl,loginCookie) ){
            Matcher matcher = Pattern.compile(
                    "<channel>.+<title>マイリスト\\s?(\\S*?)‐ニコニコ動画</title>.+<description>(.*?)</description>.+?<lastBuildDate>",
                    Pattern.DOTALL).matcher(getter.response);
            if ( matcher.find() ){
                String name = matcher.group(1);
                String description = matcher.group(2);
                if ( name.isEmpty() && description.equals("このマイリストは非公開に設定されています。")){
                    throw new NicoAPIException.InvalidParamsException(
                            "fail to get myList from its ID",
                            NicoAPIException.EXCEPTION_PARAM_MYLIST_ID
                    );
                }else{
                    return new MyListVideoGroup(info,getter.response, name, description);
                }
            }else{
                throw new NicoAPIException.ParseException(
                        "fail to parse RSS meta > myList",
                        getter.response,
                        NicoAPIException.EXCEPTION_PARSE_MYLIST_RSS
                );
            }
        }else{
            throw new NicoAPIException.HttpException(
                    "HTTP failure > myList RSS",
                    NicoAPIException.EXCEPTION_HTTP_MYLIST_VIDEOS_GET_DIRECT,
                    getter.statusCode,myListUrl,"GET"
            );
        }
    }

    /**
     * マイリストに動画を追加登録します　Adds new video to this myList.<br>
     * 対象の動画と登録時に添えるコメント・説明を渡します。
     * 説明は空文字でも可能ですが、{@code null}は渡せません。
     * すでに登録済みの動画を追加することはできず、例外を投げます。<br>
     * You pass target video and comment/description about it.
     * The description can be empty {@code String}, but cannot be {@code null}.
     * That video which already exists in this myList cannot be added, or an exception is thrown.<br>
     * <strong>ＵＩスレッド禁止</strong> HTTP通信を行うのでバックグランドで処理して下さい。<br>
     * <strong>No UI thread</strong>: HTTP communication is done<br>
     * @param target the target video
     * @param description the comment with the video
     * @throws NicoAPIException if fail to add the video
     */
    public void add (VideoInfo target, String description) throws NicoAPIException{
        addVideo(target,description,String.valueOf(getMyListID()),rootURL);
        loadVideos();
    }

    /**
     * 登録動画の説明文を編集します Edits the description of registered video.<br>
     * 説明は空文字でも可能ですが、{@code null}は渡せません。
     * マイリスに存在しない動画を渡すと例外を投げます。<br>
     * The description can be empty {@code String}, but cannot be {@code null}.
     * If video which does not exist in this myList is passed, an exception is thrown.<br>
     * <strong>ＵＩスレッド禁止</strong> HTTP通信を行うのでバックグランドで処理して下さい。<br>
     * <strong>No UI thread</strong>: HTTP communication is done<br>
     * @param target the target video
     * @param description the new description
     * @throws NicoAPIException if fail to edit
     */
    public void update (MyListVideoInfo target, String description) throws NicoAPIException{
        updateVideo(target,description,String.valueOf(getMyListID()),rootURL);
        loadVideos();
    }

    /**
     * 動画をマイリスから削除します　Deletes video of this myList.<br>
     * 対象の動画を渡します。マイリスに存在しない動画渡せず、例外を投げます。<br>
     * Pass the target video. That video which does not exist in this myList cannot be passed,
     * or an exception is thrown.<br>
     * <strong>ＵＩスレッド禁止</strong> HTTP通信を行うのでバックグランドで処理して下さい。<br>
     * <strong>No UI thread</strong>: HTTP communication is done<br>
     * @param video the target video
     * @throws NicoAPIException if fail to delete the target
     */
    public void delete(MyListVideoInfo video) throws NicoAPIException {
        delete(new MyListVideoInfo[]{video});
    }
    /**
     * @deprecated API response is ambiguous if try to delete plural videos at once.
     * @param videoList
     * @throws NicoAPIException
     */
    public void delete(MyListVideoInfo[] videoList) throws NicoAPIException{
        deleteVideo(videoList,String.valueOf(getMyListID()),rootURL);
        loadVideos();
    }

    /**
     * 動画をマイリスから別のマイリスに移動します
     * Moves videos to another myList.<br>
     * 対象動画を配列で渡し、一度に複数の動画をまとめて移動できます。
     * ただし、マイリスに存在しない動画は移動させられず、例外を投げます。
     * また、他ユーザのマイリスへ移動させることもできません。<br>
     * Pass target videos in array and plural videos can be moved at once.
     * However those videos which do not exist in this myList cannot be moved, or an exception is thrown.
     * Also moving any video to other user's myList is not possible.<br>
     * <strong>ＵＩスレッド禁止</strong> HTTP通信を行うのでバックグランドで処理して下さい。<br>
     * <strong>No UI thread</strong>: HTTP communication is done<br>
     * @param videoList the target video
     * @param target the target myList
     * @throws NicoAPIException if fail to move the targets
     */
    public void move(MyListVideoInfo[] videoList, MyListVideoGroup target) throws NicoAPIException{
        moveVideo(videoList,String.valueOf(getMyListID()),target,rootURL);
        loadVideos();
        target.loadVideos();
    }

    /**
     * 動画をマイリスから別のマイリスに複製します
     * Copies videos to another myList.<br>
     * 対象動画を配列で渡し、一度に複数の動画をまとめて複製できます。
     * ただし、マイリスに存在しない動画は複製できず、例外を投げます。
     * また、他ユーザのマイリスへ複製することもできません。<br>
     * Pass target videos in array and plural videos can be copied at once.
     * However those videos which do not exist in this myList cannot be copied, or an exception is thrown.
     * Also copying any video to other user's myList is not possible.<br>
     * <strong>ＵＩスレッド禁止</strong> HTTP通信を行うのでバックグランドで処理して下さい。<br>
     * <strong>No UI thread</strong>: HTTP communication is done<br>
     * @param videoList the target video
     * @param target the target myList
     * @throws NicoAPIException if fail to copy the targets
     */
    public void copy(MyListVideoInfo[] videoList, MyListVideoGroup target) throws NicoAPIException{
        copyVideo(videoList,String.valueOf(getMyListID()),target,rootURL);
        target.loadVideos();
    }

    /**
     * マイリス内の動画をソートします
     * Sorts videos in this myList.<br>
     *  整列の方法はマイリス設定の{@link #defaultSort ソートパラメータ}で規定されます。<br><br>
     * <strong>ＵＩスレッド禁止</strong> HTTP通信を行うのでバックグランドで処理して下さい。<br>
     * <strong>No UI thread</strong>: HTTP communication is done<br>
     *  {@link #defaultSort The sort param} defines how to sort them.
     * @throws NicoAPIException if fail to sort videos
     */
    public void sort () throws NicoAPIException{
        if ( videoInfoList == null ){
            getVideos();
        }
        if ( videoInfoList.size() < 2 ){
            return;
        }
        Map<String,String> params = new HashMap<String, String>();
        params.put("token", getToken(videoInfoList.get(0).getID()) );
        params.put("group_id_list[0]", String.valueOf(getMyListID()) );
        if ( tryPost(sortURL,params,info.getCookieStore()) ){
            checkStatusCode(super.response);
            loadVideos();
        }else{
            throw new NicoAPIException.HttpException(
                    "http failure",
                    NicoAPIException.EXCEPTION_HTTP_MYLIST_SORT,
                    statusCode,sortURL,"POST");
        }
    }
}
