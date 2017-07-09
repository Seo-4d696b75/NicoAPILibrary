package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import org.apache.http.client.CookieStore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;

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

abstract class MyListEditor  {

    protected MyListEditor(CookieGroup cookieGroup){
        this.defaultVideoID = ResourceStore.getInstance().getString(R.string.value_myList_token_default_videoID);
        this.cookieGroup = cookieGroup;
    }

    protected final CookieGroup cookieGroup;

    /**
     * トークンを得るための動画ＩＤ初期値 Default videoID to get Nico.token<br>
     * マイリスに変更を加えるのに必須なトークンを得るには何らかの有効な動画ＩＤが必要です。
     * この値が将来的に有効である保証はないので他の値で代用するよう推奨されます。
     */
    protected final String defaultVideoID;

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
        ResourceStore res = ResourceStore.getInstance();
        HttpClient client = res.getHttpClient();
        String path = res.getURL(R.string.url_nico_api_token) + anyVideoID;
        if ( client.get(path, cookieGroup)) {
            Matcher matcher = ResourceStore.getInstance().getPattern(R.string.regex_myList_token).matcher(client.getResponse());
            if (matcher.find()) {
                return matcher.group(1);
            } else {
                throw new NicoAPIException.ParseException(
                        "fail to find NicoAPI.token", client.getResponse(),
                        NicoAPIException.EXCEPTION_PARSE_MYLIST_TOKEN);
            }
        } else {
            throw new NicoAPIException.HttpException(
                    "fail to get token",
                    NicoAPIException.EXCEPTION_HTTP_MYLIST_TOKEN,
                    client.getStatusCode(), path, "GET");
        }
    }

    /**
     * マイリス編集に必要な対象動画のスレッドＩＤを取得します
     * Gets target Video threadID.<br>
     * 動画のスレッドＩＤが欠損している場合も{@link VideoInfo#getFlv(CookieGroup)}で補って返します。
     * If the target threadID is not set yet, call {@link VideoInfo#getFlv(CookieGroup)} and get that value.<br>
     * <strong>ＵＩスレッド禁止</strong> HTTP通信を行うのでバックグランドで処理して下さい。<br>
     * <strong>No UI thread</strong>: HTTP communication is done<br>
     * @param video the target video
     * @return threadID in String
     * @throws NicoAPIException if fail to get threadID in {@link VideoInfo#getFlv(CookieGroup)}
     */
    protected String getThreadID(VideoInfo video) throws NicoAPIException{
        try {
            video.getThreadID();
        } catch (NicoAPIException e) {
            video.getFlv(null);
        }
        return String.valueOf(video.getThreadID());
    }

    protected void addVideo (VideoInfo target, String description, String myListID, String path) throws NicoAPIException{
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
        ResourceStore res = ResourceStore.getInstance();
        HttpClient client = res.getHttpClient();
        String threadID = getThreadID(target);
        Map<String,String> params = new HashMap<String, String>();
        params.put(res.getString(R.string.key_myList_video_threadID),threadID);
        params.put(res.getString(R.string.key_myList_description),description);
        params.put(res.getString(R.string.key_myList_token),getToken(target.getID()));
        if ( myListID != null ) {
            params.put(res.getString(R.string.key_myList_param_myListID), myListID);
        }
        if ( client.post(path,params,cookieGroup) ){
            checkStatusCode(client.getResponse());
        }else{
            throw new NicoAPIException.HttpException(
                    "http failure",
                    NicoAPIException.EXCEPTION_HTTP_MYLIST_ADD,
                    client.getStatusCode(),path,"POST");
        }
    }

    protected void updateVideo (VideoInfo target, String description, String myListID, String path) throws NicoAPIException{
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
        ResourceStore res = ResourceStore.getInstance();
        HttpClient client = res.getHttpClient();
        Map<String,String> params = new HashMap<String ,String>();
        params.put(res.getString(R.string.key_myList_token), getToken(target.getID()));
        params.put(res.getString(R.string.key_myList_description), description);
        params.put(res.getString(R.string.key_myList_video_threadID), getThreadID(target) );
        params.put(res.getString(R.string.key_myList_item_type), res.getString(R.string.value_myList_item_type_video));
        if ( myListID != null ){
            params.put(res.getString(R.string.key_myList_param_myListID), myListID );
        }
        if ( client.post(path,params,cookieGroup) ){
            checkStatusCode(client.getResponse());
        }else{
            throw new NicoAPIException.HttpException(
                    "http failure",
                    NicoAPIException.EXCEPTION_HTTP_MYLIST_UPDATE,
                    client.getStatusCode(),path,"POST");
        }
    }

    protected void deleteVideo (MyListVideoInfo[] videoList, String myListID, String path) throws NicoAPIException{
        if ( videoList == null || videoList.length == 0){
            throw new NicoAPIException.InvalidParamsException(
                    "no target video > delete - myList",
                    NicoAPIException.EXCEPTION_PARAM_MYLIST_TARGET_VIDEO
            );
        }
        ResourceStore res = ResourceStore.getInstance();
        HttpClient client = res.getHttpClient();
        Map<String,String> params = new HashMap<String, String>();
        params.put(res.getString(R.string.key_myList_token),getToken(videoList[0].getID()));
        if ( myListID != null ) {
            params.put(res.getString(R.string.key_myList_param_myListID), myListID);
        }
        for ( int i=0  ; i<videoList.length ; i++){
            if ( videoList[i] == null ){
                throw new NicoAPIException.InvalidParamsException(
                        "no target video > delete - myList",
                        NicoAPIException.EXCEPTION_PARAM_MYLIST_TARGET_VIDEO
                );
            }else {
                params.put(String.format(
                        Locale.US,
                        res.getString(R.string.key_myList_video_threadID_list),
                        res.getString(R.string.value_myList_item_type_video),
                        i
                        ), getThreadID(videoList[i])
                );
            }
        }
        if ( client.post(path,params,cookieGroup) ){
            JSONObject root = checkStatusCode(client.getResponse());
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
                    client.getStatusCode(),path,"POST");
        }

    }

    private int getDeleteCount(JSONObject response) throws NicoAPIException{
        try{
            String key = ResourceStore.getInstance().getString(R.string.key_myList_delete_count);
            return response.getInt(key);
        }catch (JSONException e){
            throw new NicoAPIException.ParseException(
                    "fail to parse delete count",response.toString(),
                    NicoAPIException.EXCEPTION_PARSE_MYLIST_DELETE_COUNT);
        }
    }

    protected void moveVideo(MyListVideoInfo[] videoList, String myListID, MyListVideoGroup targetMyList, String path) throws NicoAPIException{
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
        ResourceStore res = ResourceStore.getInstance();
        HttpClient client = res.getHttpClient();
        Map<String,String> params = new HashMap<String, String>();
        params.put(res.getString(R.string.key_myList_token),getToken(videoList[0].getID()));
        params.put(res.getString(R.string.key_myList_target_myListID),String.valueOf(targetMyList.getMyListID()) );
        if ( myListID != null ) {
            params.put(res.getString(R.string.key_myList_param_myListID), myListID);
        }
        for ( int i=0 ; i<videoList.length ; i++){
            if ( videoList[i] == null ){
                throw new NicoAPIException.InvalidParamsException(
                        "no target video > move - myList",
                        NicoAPIException.EXCEPTION_PARAM_MYLIST_TARGET_VIDEO
                );
            }else {
                params.put(String.format(
                        Locale.US,
                        res.getString(R.string.key_myList_video_threadID_list),
                        res.getString(R.string.value_myList_item_type_video),i
                    ), getThreadID(videoList[i])
                );
            }
        }
        if ( client.post(path,params,cookieGroup) ){
            JSONObject root = checkStatusCode(client.getResponse());
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
                    client.getStatusCode(),path,"POST");
        }
    }

    protected void copyVideo(MyListVideoInfo[] videoList, String myListID, MyListVideoGroup targetMyList, String path) throws NicoAPIException{
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
        ResourceStore res = ResourceStore.getInstance();
        HttpClient client = res.getHttpClient();
        Map<String,String> params = new HashMap<String, String>();
        params.put(res.getString(R.string.key_myList_token),getToken(videoList[0].getID()));
        params.put(res.getString(R.string.key_myList_target_myListID),String.valueOf(targetMyList.getMyListID()) );
        if ( myListID != null ){
            params.put(res.getString(R.string.key_myList_param_myListID), myListID);
        }
        for ( int i=0 ; i<videoList.length ; i++){
            if ( videoList[i] == null ){
                throw new NicoAPIException.InvalidParamsException(
                        "no target video > copy - myList",
                        NicoAPIException.EXCEPTION_PARAM_MYLIST_TARGET_VIDEO
                );
            }else {
                params.put(String.format(
                        Locale.US,
                        res.getString(R.string.key_myList_video_threadID_list),
                        res.getString(R.string.value_myList_item_type_video),i
                        ), getThreadID(videoList[i])
                );
            }
        }
        if ( client.post(path,params,cookieGroup) ){
            JSONObject root = checkStatusCode(client.getResponse());
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
                    client.getStatusCode(),path,"POST");
        }
    }

    private class MoveState{
        private int[] matches;
        private int[] duplicates;
        private int[] targets;
        private MoveState(int[] matches, int[] duplicates, int[] targets){
            this.matches = matches;
            this.duplicates = duplicates;
            this.targets = targets;
        }
        private boolean isSuccess (MyListVideoInfo[] videoList){
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
        ResourceStore res = ResourceStore.getInstance();
        String detectedKey = res.getString(R.string.key_myList_move_response_detected);
        String duplicateKey = res.getString(R.string.key_myList_move_response_duplicate);
        String executedKey = res.getString(R.string.key_myList_move_response_executed);
        return new MoveState(
                getItem(root,detectedKey),
                getItem(root,duplicateKey),
                getItem(root,executedKey)
        );
    }

    private int[] getItem (JSONObject root, String key) throws NicoAPIException{
        ResourceStore res = ResourceStore.getInstance();
        String itemKey = res.getString(R.string.key_myList_move_response_item);
        String threadIDKey = res.getString(R.string.key_myList_move_response_threadID);
        try{
            JSONArray array = root.getJSONObject(key).getJSONArray(itemKey);
            int[] detect = new int[array.length()];
            for ( int i=0 ; i<array.length() ; i++){
                JSONObject item = array.getJSONObject(i);
                detect[i] = item.getInt(threadIDKey);
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

    /**
     * マイリス編集のレスポンスのステータスを確認します
     * Checks the response status of editing myList.<br>
     * マイリス編集のパラメータをPOSTしたJSON形式のレスポンスのステータスは共通形式なのでまとめて確認します。
     * @param response the response of posting
     * @return JSONObject which the response is converted into
     * @throws NicoAPIException if format or error code is unexpected
     */
    protected JSONObject checkStatusCode(String response) throws NicoAPIException{
        ResourceStore res = ResourceStore.getInstance();
        String statusKey = res.getString(R.string.key_myList_status);
        String successValue = res.getString(R.string.value_myList_status_success);
        String failValue = res.getString(R.string.value_myList_status_fail);
        try{
            JSONObject root = new JSONObject(response);
            String status = root.getString(statusKey);
            if ( status.equals(successValue) ) {
                return root;
            }else if ( status.equals(failValue) ) {
                JSONObject error = root.getJSONObject("error");
                String code = error.getString("code");
                switch (code) {
                    case "NOAUTH":
                        throw new NicoAPIException.NoLoginException(
                                "editing myList needs login",
                                NicoAPIException.EXCEPTION_NOT_LOGIN_MYLIST_EDIT);
                    case "NONEXIST":
                        throw new NicoAPIException.InvalidParamsException(
                                "specified video not found",
                                NicoAPIException.EXCEPTION_PARAM_MYLIST_EDIT_VIDEO_NOT_FOUND);
                    case "INVALIDTOKEN":
                        throw new NicoAPIException.InvalidParamsException(
                                "invalid token",
                                NicoAPIException.EXCEPTION_PARAM_MYLIST_EDIT_INVALID_TOKEN);
                    case "EXIST":
                        throw new NicoAPIException.InvalidParamsException(
                                "specified video already in target myList",
                                NicoAPIException.EXCEPTION_PARAM_MYLIST_EDIT_VIDEO_ALREADY_EXIST);
                    case "PARAMERROR":
                        throw new NicoAPIException.InvalidParamsException(
                                "invalid param or required param not found",
                                NicoAPIException.EXCEPTION_PARAM_MYLIST_EDIT_PARAM);
                    case "EXPIRETOKEN":
                        throw new NicoAPIException.InvalidParamsException(
                                "token too old",
                                NicoAPIException.EXCEPTION_PARAM_MYLIST_EDIT_OLD_TOKEN);
                    default:
                        throw new NicoAPIException.APIUnexpectedException(
                                "unexpected error code",
                                NicoAPIException.EXCEPTION_UNEXPECTED_MYLIST_EDIT_ERROR_CODE);
                }
            }else{
                throw new NicoAPIException.APIUnexpectedException(
                        "unexpected status code",
                        NicoAPIException.EXCEPTION_UNEXPECTED_MYLIST_EDIT_STATUS_CODE);
            }
        }catch (JSONException e){
            throw new NicoAPIException.ParseException(e.getMessage(),response);
        }
    }

    /*
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
                        before.setMyListItemDetails(
                                after.getMyListItemDescription(),
                                after.getAddDate(),
                                after.getUpdateDate()
                        );
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
    }*/
}
