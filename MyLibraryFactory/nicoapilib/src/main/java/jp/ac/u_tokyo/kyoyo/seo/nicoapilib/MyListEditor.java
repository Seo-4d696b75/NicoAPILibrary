package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import org.apache.http.client.CookieStore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * このクラスはマイリストの編集機能を提供します<br>
 * This provides utilities to make changes to myLists.<br>
 * このクラスを継承した各子クラスで各メソッドは使用されます。<br>
 *  {@link MyListGroup マイリスグループを管理するクラス}<br>
 *  {@link MyListVideoGroup マイリスを管理するクラス}<br>
 *  {@link TempMyListVideoGroup とりあえずマイリスを管理するクラス}<br>
 * Each methods are called by these classes extending this;<br>
 *  {@link MyListGroup the class managing MyListGroup}<br>
 *  {@link MyListVideoGroup the class managing MyList}<br>
 *  {@link TempMyListVideoGroup the class managing TempMyList}<br>
 *
 * @author Seo-4d696b75
 * @version 0.0 on 2017/02/27.
 */

public abstract class MyListEditor extends HttpResponseGetter {

    protected MyListEditor(){}
    private final String tokenURL = "http://www.nicovideo.jp/mylist_add/video/";
    protected LoginInfo info;
    /**
     * トークンを得るための動画ＩＤ初期値 Default videoID to get Nico.token<br>
     * マイリスに変更を加えるのに必須なトークンを得るには何らかの有効な動画ＩＤが必要です。
     * この値が将来的に有効である保証はないので他の値で代用するよう推奨されます。
     */
    protected final String defaultVideoID = "sm9";
    /**
     * マイリス変更に必要なトークンを取得する Gets Nico.token to edit myList.<br>
     * トークンを得るには何らかの有効な動画ＩＤが必要です。
     * 一つでも動画を取得済みならその有効な動画ＩＤを用いますが、見つからない場合は初期値で代用します。
     * Getting the token requires any valid videoID.<br>
     * <strong>ＵＩスレッド禁止</strong> HTTP通信を行うのでバックグランドで処理して下さい。<br>
     * <strong>No UI thread</strong>: HTTP communication is done<br>
     * @param anyVideoID the videoID
     * @return Nico.token
     * @throws NicoAPIException if not login or passed videoID is invalid
     */
    protected String getToken(String anyVideoID) throws NicoAPIException{
        String path = tokenURL + anyVideoID;
        if (tryGet(path, info.getCookieStore())) {
            Matcher matcher = Pattern.compile("NicoAPI.token = '(.+?)';").matcher(super.response);
            if (matcher.find()) {
                return matcher.group(1);
            } else {
                throw new NicoAPIException.ParseException(
                        "fail to find NicoAPI.token", super.response,
                        NicoAPIException.EXCEPTION_PARSE_MYLIST_TOKEN);
            }
        } else {
            throw new NicoAPIException.HttpException(
                    "fail to get token",
                    NicoAPIException.EXCEPTION_HTTP_MYLIST_TOKEN,
                    super.statusCode, path, "GET");
        }
    }
    /**
     * マイリス編集に必要な対象動画のスレッドＩＤを取得します
     * Gets target Video threadID.<br>
     * 動画のスレッドＩＤが欠損している場合も{@link VideoInfo#getFlv(CookieStore)}で補って返します。
     * If the target threadID is not set yet, call {@link VideoInfo#getFlv(CookieStore)} and get that value.<br>
     * <strong>ＵＩスレッド禁止</strong> HTTP通信を行うのでバックグランドで処理して下さい。<br>
     * <strong>No UI thread</strong>: HTTP communication is done<br>
     * @param video the target video
     * @return threadID in String
     * @throws NicoAPIException if fail to get threadID in {@link VideoInfo#getFlv(CookieStore)}
     */
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
            if ( videoList[i] == null ){
                throw new NicoAPIException.InvalidParamsException(
                        "no target video > delete - myList",
                        NicoAPIException.EXCEPTION_PARAM_MYLIST_TARGET_VIDEO
                );
            }else {
                params.put(String.format("id_list[0][%d]", i), getThreadID(videoList[i]));
            }
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
            if ( videoList[i] == null ){
                throw new NicoAPIException.InvalidParamsException(
                        "no target video > move - myList",
                        NicoAPIException.EXCEPTION_PARAM_MYLIST_TARGET_VIDEO
                );
            }else {
                params.put(String.format("id_list[0][%d]", i), getThreadID(videoList[i]));
            }
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
            if ( videoList[i] == null ){
                throw new NicoAPIException.InvalidParamsException(
                        "no target video > copy - myList",
                        NicoAPIException.EXCEPTION_PARAM_MYLIST_TARGET_VIDEO
                );
            }else {
                params.put(String.format("id_list[0][%d]", i), getThreadID(videoList[i]));
            }
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

    private MoveState getMoveState (JSONObject root) throws NicoAPIException{
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

    /**
     * マイリス編集のレスポンスのステータスを確認します
     * Checks the response status of editing myList.<br>
     * マイリス編集のパラメータをPOSTしたJSON形式のレスポンスのステータスは共通形式なのでまとめて確認します。
     * @param response the response of posting
     * @return JSONObject which the response is converted into
     * @throws NicoAPIException if format or error code is unexpected
     */
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

    protected List<MyListVideoInfo> replaceList (List<MyListVideoInfo> beforeList, List<MyListVideoInfo> afterList){
        List<MyListVideoInfo> list = new ArrayList<MyListVideoInfo>();
        if ( beforeList != null ){
            //元のリストがセット済みならなるべく元のオブジェクトを維持して差分のみ変更する
            for (Iterator<MyListVideoInfo> iteratorBefore = beforeList.iterator(); iteratorBefore.hasNext() ; ){
                MyListVideoInfo before = iteratorBefore.next();
                boolean isFind = false;
                for ( Iterator<MyListVideoInfo> iteratorAfter = afterList.iterator() ; iteratorAfter.hasNext() ;  ){
                    MyListVideoInfo after = iteratorAfter.next();
                    if ( after.getID().equals( before.getID() ) ){
                        isFind = true;
                        synchronized ( before ) {
                            before.myListItemDescription = after.myListItemDescription;
                            before.addDate = after.addDate;
                            before.updateDate = after.updateDate;
                        }
                        iteratorAfter.remove();
                        break;
                    }
                }
                if ( !isFind ){
                    iteratorBefore.remove();
                }
            }
            if ( !beforeList.isEmpty() ){
                list.addAll(beforeList);
            }
            if ( !afterList.isEmpty() ){
                list.addAll(afterList);
            }
        }else {
            list.addAll(afterList);
        }
        return list;
    }
}
