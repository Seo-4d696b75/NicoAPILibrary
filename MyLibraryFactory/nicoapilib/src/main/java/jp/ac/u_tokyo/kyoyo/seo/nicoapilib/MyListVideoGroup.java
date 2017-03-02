package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import org.apache.http.client.CookieStore;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Seo-4d696b75 on 2017/02/28.
 */

public class MyListVideoGroup extends MyListEditor {

    //from myList group
    protected MyListVideoGroup (LoginInfo info, JSONObject item) throws JSONException {
        super(info);
        setDetails(item);
        this.info = info;
    }
    //from myListID directly
    private MyListVideoGroup (LoginInfo info, String xml, String name, String description) throws NicoAPIException{
        super(info);
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

    protected int myListID;
    protected int userID;
    protected String name;
    protected String description;
    protected boolean isPublic;
    private int defaultSort;
    private String createDate;
    private String updateDate;
    private int iconID;
    protected List<MyListVideoInfo> videoInfoList;
    private LoginInfo info;

    protected boolean isEdit = false;

    public synchronized int getMyListID(){
        return myListID;
    }
    public synchronized int getUserID(){
        return userID;
    }
    public synchronized String getName(){
        return name;
    }
    public synchronized String getDescription(){
        return description;
    }
    public synchronized boolean isPublic(){
        return isPublic;
    }
    public synchronized int getDefaultSort(){
        return defaultSort;
    }
    public synchronized int getIconID(){
        return iconID;
    }
    public synchronized String getCreateDate(){
        return createDate;
    }
    public synchronized String getUpdateDate(){
        return updateDate;
    }

    private String detailsGetURL = "http://www.nicovideo.jp/api/mylistgroup/get";
    private String sortURL = "http://www.nicovideo.jp/api/mylistgroup/sort";
    private String rootURL = "http://www.nicovideo.jp/api/mylist/";

    private void getDetails() throws NicoAPIException{
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

    private final Object listGetLock = new Object();
    public List<MyListVideoInfo> getVideos() throws NicoAPIException{
        synchronized (listGetLock) {
            boolean load = false;
            synchronized ( this ) {
                if (videoInfoList == null || isEdit) {
                    load = true;
                    isEdit = false;
                }
            }
            if ( load ){
                loadVideos();;
            }
            List<MyListVideoInfo> list = new ArrayList<MyListVideoInfo>();
            for ( MyListVideoInfo info : videoInfoList){
                list.add(info);
            }
            return list;
        }
    }
    private void loadVideos() throws NicoAPIException{
        String path = rootURL + "list";
        Map<String, String> param = new HashMap<String, String>();
        param.put("group_id", String.valueOf(myListID));
        if (tryPost(path, param, info.getCookieStore())) {
            JSONObject root = checkStatusCode(super.response);
            synchronized (this) {
                this.videoInfoList = MyListVideoInfo.parse(root);
            }
        } else {
            throw new NicoAPIException.HttpException(
                    "HTTP failure > myList details",
                    NicoAPIException.EXCEPTION_HTTP_MYLIST_VIDEOS_GET,
                    super.statusCode, path, "POST"
            );
        }
    }

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

    public void add (VideoInfo target, String description) throws NicoAPIException{
        addVideo(target,description,String.valueOf(getMyListID()),rootURL);
        synchronized (this){
            isEdit = true;
        }
    }

    public void update (MyListVideoInfo target, String description) throws NicoAPIException{
        updateVideo(target,description,String.valueOf(getMyListID()),rootURL);
        synchronized (this){
            isEdit = true;
        }
    }

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
        synchronized (this){
            isEdit = true;
        }
    }

    public void move(MyListVideoInfo[] videoList, MyListVideoGroup target) throws NicoAPIException{
        moveVideo(videoList,String.valueOf(getMyListID()),target,rootURL);
        synchronized (this){
            isEdit = true;
        }
        synchronized (target){
            target.isEdit = true;
        }
    }

    public void copy(MyListVideoInfo[] videoList, MyListVideoGroup target) throws NicoAPIException{
        copyVideo(videoList,String.valueOf(getMyListID()),target,rootURL);
        synchronized (target){
            target.isEdit = true;
        }
    }

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
            synchronized (this){
                isEdit = true;
            }
        }else{
            throw new NicoAPIException.HttpException(
                    "http failure",
                    NicoAPIException.EXCEPTION_HTTP_MYLIST_SORT,
                    statusCode,sortURL,"POST");
        }
    }
}
