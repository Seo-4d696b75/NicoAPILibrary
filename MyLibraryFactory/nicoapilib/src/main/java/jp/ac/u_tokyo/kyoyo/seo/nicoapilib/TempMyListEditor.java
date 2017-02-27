package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Seo-4d696b75 on 2017/02/27.
 */

public class TempMyListEditor extends NicoListEditor {

    protected TempMyListEditor(String anyVideoID, LoginInfo info){
        super(anyVideoID,info);
    }

    /**
     * @deprecated may fail to get NicoAPI.token
     * @param info
     */
    protected TempMyListEditor(LoginInfo info){
        super(info);
    }

    private String urlRoot = "http://www.nicovideo.jp/api/deflist/";

    public void add(VideoInfo video, String description) throws NicoAPIException{
        String threadID = getThreadID(video);
        String path = urlRoot + "add";
        Map<String,String> params = new HashMap<String, String>();
        params.put("item_id",threadID);
        params.put("description",description);
        params.put("token",getToken());
        if ( tryPost(path,params,info.getCookieStore()) ){
            checkStatusCode(super.response);
        }else{
            throw new NicoAPIException.HttpException(
                    "http failure",
                    NicoAPIException.EXCEPTION_HTTP_TEMP_MYLIST_ADD,
                    statusCode,path,"POST");
        }
    }

    public void update(VideoInfo video, String description) throws NicoAPIException{
        String threadID = getThreadID(video);
        String path = urlRoot + "update";
        Map<String,String> params = new HashMap<String, String>();
        params.put("item_id",threadID);
        params.put("description",description);
        params.put("token",getToken());
        if ( tryPost(path,params,info.getCookieStore()) ){
            checkStatusCode(super.response);
        }else{
            throw new NicoAPIException.HttpException(
                    "http failure",
                    NicoAPIException.EXCEPTION_HTTP_TEMP_MYLIST_UPDATE,
                    statusCode,path,"POST");
        }
    }

    public void delete(VideoInfo video) throws NicoAPIException {
        delete(new VideoInfo[]{video});
    }
    /**
     * @deprecated API response is ambiguous if try to delete plural videos at once.
     * @param videoList
     * @throws NicoAPIException
     */
    public void delete(VideoInfo[] videoList) throws NicoAPIException{
        String path = urlRoot + "delete";
        Map<String,String> params = new HashMap<String, String>();
        params.put("token",getToken());
        for ( int i=0 ; i<videoList.length ; i++){
            params.put(String.format("id_list[0][%d]",i),getThreadID(videoList[i]) );
        }
        if ( tryPost(path,params,info.getCookieStore()) ){
            JSONObject root = checkStatusCode(super.response);
            int delete = getDeleteCount(root);
            if ( delete == videoList.length ){
                return;
            }else{
                throw new NicoAPIException.InvalidParamsException(
                        "fail to delete some of target videos from tempMyList",
                        NicoAPIException.EXCEPTION_PARAM_TEMP_MYLIST_DELETE);
            }
        }else{
            throw new NicoAPIException.HttpException(
                    "http failure",
                    NicoAPIException.EXCEPTION_HTTP_TEMP_MYLIST_DELETE,
                    statusCode,path,"POST");
        }

    }

    public void move(VideoInfo[] videoList, String targetMyListID) throws NicoAPIException{
        String path = urlRoot + "move";
        Map<String,String> params = new HashMap<String, String>();
        params.put("token",getToken());
        params.put("target_group_id",targetMyListID);
        for ( int i=0 ; i<videoList.length ; i++){
            params.put(String.format("id_list[0][%d]",i),getThreadID(videoList[i]) );
        }
        if ( tryPost(path,params,info.getCookieStore()) ){
            JSONObject root = checkStatusCode(super.response);
            int delete = getDeleteCount(root);
            if ( delete == videoList.length ){
                return;
            }else{
                throw new NicoAPIException.InvalidParamsException(
                        "fail to delete some of target videos from tempMyList",
                        NicoAPIException.EXCEPTION_PARAM_TEMP_MYLIST_DELETE);
            }
        }else{
            throw new NicoAPIException.HttpException(
                    "http failure",
                    NicoAPIException.EXCEPTION_HTTP_TEMP_MYLIST_MOVE,
                    statusCode,path,"POST");
        }
    }

}