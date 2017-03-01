package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import org.json.JSONObject;

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

    private int userID;
    private List<MyListVideoInfo> videoInfoList;

    public int getUserID(){
        return userID;
    }
    public List<MyListVideoInfo> getVideoList(){
        return videoInfoList;
    }

    private String urlRoot = "http://www.nicovideo.jp/api/deflist/";

    public void add(VideoInfo target, String description) throws NicoAPIException{
        addVideo(target,description,null,urlRoot);
    }

    public void update(MyListVideoInfo target, String description) throws NicoAPIException{
        updateVideo(target,description,null,urlRoot);
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
    }

    public void move(MyListVideoInfo[] videoList, MyListVideoGroup target) throws NicoAPIException{
        moveVideo(videoList,null,target,urlRoot);
    }

    public void copy(MyListVideoInfo[] videoList, MyListVideoGroup target) throws NicoAPIException{
        copyVideo(videoList,null,target,urlRoot);
    }

}
