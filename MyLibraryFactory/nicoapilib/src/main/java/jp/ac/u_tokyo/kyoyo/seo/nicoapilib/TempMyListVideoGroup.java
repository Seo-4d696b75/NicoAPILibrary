package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Seo-4d696b75 on 2017/02/28.
 */

public class TempMyListVideoGroup extends MyListEditor {

    protected TempMyListVideoGroup(LoginInfo info) throws NicoAPIException{
        super(info);
        userID = info.getUserID();
        this.info = info;
        loadVideos();
    }

    private int userID;
    private List<MyListVideoInfo> videoInfoList;
    private LoginInfo info;

    public int getUserID(){
        return userID;
    }

    private final Object listGetLock = new Object();
    private boolean isEdit = false;

    public List<MyListVideoInfo> getVideoList() throws NicoAPIException{
        synchronized ( listGetLock ) {
            boolean load = false;
            synchronized ( this ){
                if ( isEdit ){
                    isEdit = false;
                    load = true;
                }
            }
            if ( load ){
                loadVideos();
            }
            List<MyListVideoInfo> list = new ArrayList<MyListVideoInfo>();
            for (MyListVideoInfo info : videoInfoList) {
                list.add(info);
            }
            return list;
        }
    }

    private void loadVideos() throws NicoAPIException{
        String tempMyListUrl = "http://www.nicovideo.jp/api/deflist/list";
        if ( tryGet(tempMyListUrl, info.getCookieStore()) ){
            JSONObject root = checkStatusCode(super.response);
            videoInfoList = MyListVideoInfo.parse(root);
        }else{
            throw new NicoAPIException.HttpException(
                    "HTTP failure > tempMyList",
                    NicoAPIException.EXCEPTION_HTTP_TEMP_MYLIST_GET,
                    super.statusCode, tempMyListUrl, "GET"
            );
        }
    }

    private String urlRoot = "http://www.nicovideo.jp/api/deflist/";

    public void add(VideoInfo target, String description) throws NicoAPIException{
        addVideo(target,description,null,urlRoot);
        synchronized ( this ){
            isEdit = true;
        }
    }

    public void update(MyListVideoInfo target, String description) throws NicoAPIException{
        updateVideo(target,description,null,urlRoot);
        synchronized ( this ){
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
        deleteVideo(videoList,null,urlRoot);
        synchronized ( this ){
            isEdit = true;
        }
    }

    public void move(MyListVideoInfo[] videoList, MyListVideoGroup target) throws NicoAPIException{
        moveVideo(videoList,null,target,urlRoot);
        synchronized ( this ){
            isEdit = true;
        }
        synchronized ( target ){
            target.isEdit = true;
        }
    }

    public void copy(MyListVideoInfo[] videoList, MyListVideoGroup target) throws NicoAPIException{
        copyVideo(videoList,null,target,urlRoot);
        synchronized ( target ){
            target.isEdit = true;
        }
    }

}
