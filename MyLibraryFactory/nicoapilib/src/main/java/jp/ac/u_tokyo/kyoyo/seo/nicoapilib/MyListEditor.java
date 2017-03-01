package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Seo-4d696b75 on 2017/02/27.
 */

public class MyListEditor extends HttpResponseGetter {

    protected MyListEditor(LoginInfo info){
        this.info = info;
    }
    private String tokenURL = "http://www.nicovideo.jp/mylist_add/video/";
    protected LoginInfo info;

    protected final String defaultVideoID = "sm9";

    protected String getToken(String anyVideoID) throws NicoAPIException{
        String path = tokenURL + anyVideoID;
        try {
            if ( tryGet(path, info.getCookieStore()) ) {
                Matcher matcher = Pattern.compile("NicoAPI.token = '(.+?)';").matcher(super.response);
                if ( matcher.find() ){
                    return matcher.group(1);
                }else{
                    throw new NicoAPIException.ParseException(
                            "fail to find NicoAPI.token",super.response,
                            NicoAPIException.EXCEPTION_PARSE_MYLIST_TOKEN);
                }
            }else{
                throw new NicoAPIException.HttpException(
                        "fail to get token",
                        NicoAPIException.EXCEPTION_HTTP_MYLIST_TOKEN,
                        super.statusCode, path, "GET");
            }
        }catch (NicoAPIException e){
            throw e;
        }
    }

    protected String getThreadID(VideoInfo video) throws NicoAPIException{
        try {
            video.getThreadID();
        } catch (NicoAPIException e) {
            video.getFlv(info.getCookieStore());
        }
        return String.valueOf(video.getThreadID());
    }

    protected void addVideo (VideoInfo target, String description, String myListID, String rootURL) throws NicoAPIException{
        if ( target == null ){
            throw new NicoAPIException.InvalidParamsException(
                    "no target video > add - myList",
                    NicoAPIException.EXCEPTION_PARAM_MYLIST_TARGET_VIDEO
            );
        }
        if ( description == null ){
            throw new NicoAPIException.InvalidParamsException(
                    "description is null > add - myList",
                    NicoAPIException.EXCEPTION_PARAM_MYLIST_DESCRIPTION
            );
        }
        String threadID = getThreadID(target);
        String path = rootURL + "add";
        Map<String,String> params = new HashMap<String, String>();
        params.put("item_id",threadID);
        params.put("description",description);
        params.put("token",getToken(target.getID()));
        if ( myListID != null ) {
            params.put("gruop_id", myListID);
        }
        if ( tryPost(path,params,info.getCookieStore()) ){
            checkStatusCode(super.response);
        }else{
            throw new NicoAPIException.HttpException(
                    "http failure",
                    NicoAPIException.EXCEPTION_HTTP_MYLIST_ADD,
                    statusCode,path,"POST");
        }
    }

    protected void updateVideo (VideoInfo target, String description, String myListID, String rootURL) throws NicoAPIException{
        if ( target == null ){
            throw new NicoAPIException.InvalidParamsException(
                    "no target video > update - myList",
                    NicoAPIException.EXCEPTION_PARAM_MYLIST_TARGET_VIDEO
            );
        }
        if ( description == null ){
            throw new NicoAPIException.InvalidParamsException(
                    "description is null > update - myList",
                    NicoAPIException.EXCEPTION_PARAM_MYLIST_DESCRIPTION
            );
        }
        String path = rootURL + "update";
        Map<String,String> params = new HashMap<String ,String>();
        params.put("token", getToken(target.getID()));
        params.put("description", description);
        params.put("item_id", getThreadID(target) );
        params.put("item_type", "0");
        if ( myListID != null ){
            params.put("gruop_id", myListID );
        }
        if ( tryPost(path,params,info.getCookieStore()) ){
            checkStatusCode(super.response);
        }else{
            throw new NicoAPIException.HttpException(
                    "http failure",
                    NicoAPIException.EXCEPTION_HTTP_MYLIST_UPDATE,
                    statusCode,path,"POST");
        }
    }

    protected void deleteVideo (MyListVideoInfo[] videoList, String myListID, String rootURL) throws NicoAPIException{
        if ( videoList == null || videoList.length == 0){
            throw new NicoAPIException.InvalidParamsException(
                    "no target video > delete - myList",
                    NicoAPIException.EXCEPTION_PARAM_MYLIST_TARGET_VIDEO
            );
        }
        String path = rootURL + "delete";
        Map<String,String> params = new HashMap<String, String>();
        params.put("token",getToken(videoList[0].getID()));
        if ( myListID != null ) {
            params.put("group_id", myListID);
        }
        for ( int i=0 ; i<videoList.length ; i++){
            params.put(String.format("id_list[0][%d]",i), getThreadID(videoList[i]) );
        }
        if ( tryPost(path,params,info.getCookieStore()) ){
            JSONObject root = checkStatusCode(super.response);
            int delete = getDeleteCount(root);
            if ( delete == videoList.length ){
                return;
            }else{
                throw new NicoAPIException.InvalidParamsException(
                        "fail to delete some of target videos from tempMyList",
                        NicoAPIException.EXCEPTION_PARAM_MYLIST_DELETE);
            }
        }else{
            throw new NicoAPIException.HttpException(
                    "http failure",
                    NicoAPIException.EXCEPTION_HTTP_MYLIST_DELETE,
                    statusCode,path,"POST");
        }

    }

    private int getDeleteCount(JSONObject response) throws NicoAPIException{
        try{
            return response.getInt("delete_count");
        }catch (JSONException e){
            throw new NicoAPIException.ParseException(
                    "fail to parse delete count",response.toString(),
                    NicoAPIException.EXCEPTION_PARSE_MYLIST_DELETE_COUNT);
        }
    }

    protected void moveVideo(MyListVideoInfo[] videoList, String myListID, MyListVideoGroup targetMyList, String rootURL) throws NicoAPIException{
        if ( videoList == null || videoList.length == 0 ){
            throw new NicoAPIException.InvalidParamsException(
                    "no target video > move - myList",
                    NicoAPIException.EXCEPTION_PARAM_MYLIST_TARGET_VIDEO
            );
        }
        if ( targetMyList == null ){
            throw new NicoAPIException.InvalidParamsException(
                    "no target myList > move - myList",
                    NicoAPIException.EXCEPTION_PARAM_MYLIST_TARGET_MYLIST
            );
        }
        String path = rootURL + "move";
        Map<String,String> params = new HashMap<String, String>();
        params.put("token",getToken(videoList[0].getID()));
        params.put("target_group_id",String.valueOf(targetMyList.getMyListID()) );
        if ( myListID != null ) {
            params.put("group_id", myListID);
        }
        for ( int i=0 ; i<videoList.length ; i++){
            params.put(String.format("id_list[0][%d]",i), getThreadID(videoList[i]) );
        }
        if ( tryPost(path,params,info.getCookieStore()) ){
            JSONObject root = checkStatusCode(super.response);
            MoveState state = getMoveState(root);
            if ( state.isSuccess(videoList) ){
                return;
            }else{
                throw new NicoAPIException.InvalidParamsException(
                        "fail to move some of target videos from tempMyList",
                        NicoAPIException.EXCEPTION_PARAM_MYLIST_MOVE);
            }
        }else{
            throw new NicoAPIException.HttpException(
                    "http failure",
                    NicoAPIException.EXCEPTION_HTTP_MYLIST_MOVE,
                    statusCode,path,"POST");
        }
    }

    protected void copyVideo(MyListVideoInfo[] videoList, String myListID, MyListVideoGroup targetMyList, String rootURL) throws NicoAPIException{
        if ( videoList == null || videoList.length == 0 ){
            throw new NicoAPIException.InvalidParamsException(
                    "no target video > copy - myList",
                    NicoAPIException.EXCEPTION_PARAM_MYLIST_TARGET_VIDEO
            );
        }
        if ( targetMyList == null ){
            throw new NicoAPIException.InvalidParamsException(
                    "no target myList > move - myList",
                    NicoAPIException.EXCEPTION_PARAM_MYLIST_TARGET_MYLIST
            );
        }
        String path = rootURL + "copy";
        Map<String,String> params = new HashMap<String, String>();
        params.put("token",getToken(videoList[0].getID()));
        params.put("target_group_id",String.valueOf(targetMyList.getMyListID()) );
        if ( myListID != null ){
            params.put("group_id", myListID);
        }
        for ( int i=0 ; i<videoList.length ; i++){
            params.put(String.format("id_list[0][%d]",i), getThreadID(videoList[i]) );
        }
        if ( tryPost(path,params,info.getCookieStore()) ){
            JSONObject root = checkStatusCode(super.response);
            MoveState state = getMoveState(root);
            if ( state.isSuccess(videoList) ){
                return;
            }else{
                throw new NicoAPIException.InvalidParamsException(
                        "fail to move some of target videos from tempMyList",
                        NicoAPIException.EXCEPTION_PARAM_MYLIST_COPY);
            }
        }else{
            throw new NicoAPIException.HttpException(
                    "http failure",
                    NicoAPIException.EXCEPTION_HTTP_MYLIST_COPY,
                    statusCode,path,"POST");
        }
    }

    private class MoveState{
        protected int[] matches;
        protected int[] duplicates;
        protected int[] targets;
        private MoveState(int[] matches, int[] duplicates, int[] targets){
            this.matches = matches;
            this.duplicates = duplicates;
            this.targets = targets;
        }
        protected boolean isSuccess (MyListVideoInfo[] videoList){
            try {
                for (MyListVideoInfo item : videoList) {
                    int id = item.getThreadID();
                    if ( !isContain(id,targets)){
                        return false;
                    }
                }
                return true;
            }catch (Exception e){
                return false;
            }
        }
        private boolean isContain (int target, int[] array){
            for ( Integer item : array){
                if ( item == target ){
                    return true;
                }
            }
            return false;
        }
    }

    protected MoveState getMoveState (JSONObject root) throws NicoAPIException{
        return new MoveState(
                getItem(root,"matches"),
                getItem(root,"duplicates"),
                getItem(root,"targets")
        );
    }

    private int[] getItem (JSONObject root, String key) throws NicoAPIException{
        try{
            JSONArray array = root.getJSONObject(key).getJSONArray("item");
            int[] detect = new int[array.length()];
            for ( int i=0 ; i<array.length() ; i++){
                JSONObject item = array.getJSONObject(i);
                detect[i] = item.getInt("id");
            }
            return detect;
        }catch (JSONException e){
            throw new NicoAPIException.ParseException(
                    "fail to parse move/copy state > myList",
                    root.toString(),
                    NicoAPIException.EXCEPTION_PARSE_MYLIST_MOVE_STATE
            );
        }
    }

    private final Map<String,String> errorCodeMap = new HashMap<String, String>(){
        {
            put("NOAUTH","editing myList needs login");
            put("NONEXIST","specified video not found");
            put("INVALIDTOKEN","invalid token");
            put("EXIST","specified video already in target myList");
            put("PARAMERROR","invalid param or required param not found");
            put("EXPIRETOKEN","token too old");
        }
    };
    protected JSONObject checkStatusCode(String response) throws NicoAPIException{
        try{
            JSONObject root = new JSONObject(response);
            String status = root.getString("status");
            switch (status) {
                case "ok":
                    return root;
                case "fail":
                    JSONObject error = root.getJSONObject("error");
                    String code = error.getString("code");
                    if ( errorCodeMap.containsKey(code)){
                        String message = errorCodeMap.get(code);
                        switch ( code ){
                            case "NOAUTH":
                                throw new NicoAPIException.NoLoginException(
                                        message,
                                        NicoAPIException.EXCEPTION_NOT_LOGIN_MYLIST_EDIT);
                            case "NONEXIST":
                                throw new NicoAPIException.InvalidParamsException(
                                        message,
                                        NicoAPIException.EXCEPTION_PARAM_MYLIST_EDIT_VIDEO_NOT_FOUND);
                            case "INVALIDTOKEN":
                                throw new NicoAPIException.InvalidParamsException(
                                        message,
                                        NicoAPIException.EXCEPTION_PARAM_MYLIST_EDIT_INVALID_TOKEN);
                            case "EXIST":
                                throw new NicoAPIException.InvalidParamsException(
                                        message,
                                        NicoAPIException.EXCEPTION_PARAM_MYLIST_EDIT_VIDEO_ALREADY_EXIST);
                            case "PARAMERROR":
                                throw new NicoAPIException.InvalidParamsException(
                                        message,
                                        NicoAPIException.EXCEPTION_PARAM_MYLIST_EDIT_PARAM);
                            case "EXPIRETOKEN":
                                throw new NicoAPIException.InvalidParamsException(
                                        message,
                                        NicoAPIException.EXCEPTION_PARAM_MYLIST_EDIT_OLD_TOKEN);
                        }
                    }else{
                        throw new NicoAPIException.APIUnexpectedException(
                                "unexpected error code",
                                NicoAPIException.EXCEPTION_UNEXPECTED_MYLIST_EDIT_ERROR_CODE);
                    }
                default:
                    throw new NicoAPIException.APIUnexpectedException(
                            "unexpected status code",
                            NicoAPIException.EXCEPTION_UNEXPECTED_MYLIST_EDIT_STATUS_CODE);
            }
        }catch (JSONException e){
            throw new NicoAPIException.ParseException(e.getMessage(),response);
        }
    }
}
