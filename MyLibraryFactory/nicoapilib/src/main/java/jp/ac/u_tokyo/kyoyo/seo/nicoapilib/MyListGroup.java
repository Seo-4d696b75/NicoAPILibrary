package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Seo-4d696b75 on 2017/02/28.
 */

public class MyListGroup extends MyListEditor {

    protected MyListGroup(LoginInfo info) throws NicoAPIException{
        super(info);
        this.info = info;
        this.groupList = loadGroups();
    }

    private String rootURL = "http://www.nicovideo.jp/api/mylistgroup/";
    private LoginInfo info;
    private List<MyListVideoGroup> groupList;

    private boolean isEdit = false;
    private final Object groupGetLock = new Object();

    public List<MyListVideoGroup> getMyListVideoGroup() throws NicoAPIException{
        synchronized ( groupGetLock ) {
            boolean load = false;
            synchronized (this) {
                if (isEdit) {
                    isEdit = false;
                    load = true;
                }
            }
            if (load) {
                //if up-dated videoList is set already in myList, set is in new myList.
                List<MyListVideoGroup> list = loadGroups();
                for ( MyListVideoGroup newGroup : list ){
                    for ( MyListVideoGroup group : groupList ){
                        if ( newGroup.getMyListID() == group.getMyListID() ){
                            synchronized ( group ) {
                                if (!group.isEdit) {
                                    newGroup.videoInfoList = group.videoInfoList;
                                }
                            }
                            break;
                        }
                    }
                }
                this.groupList = list;
            }
            List<MyListVideoGroup> list = new ArrayList<MyListVideoGroup>();
            for (MyListVideoGroup group : groupList) {
                list.add(group);
            }
            return list;
        }
    }


    private List<MyListVideoGroup> loadGroups () throws NicoAPIException{
        String myListGroupUrl = rootURL + "list";
        if ( tryGet(myListGroupUrl,info.getCookieStore()) ){
            JSONObject root = checkStatusCode(super.response);
            try{
                JSONArray group = root.getJSONArray("mylistgroup");
                List<MyListVideoGroup> list = new ArrayList<MyListVideoGroup>();
                for ( int i=0 ; i<group.length() ; i++){
                    JSONObject myListItem = group.getJSONObject(i);
                    list.add( new MyListVideoGroup(info,myListItem));
                }
                return list;
            }catch (JSONException e){
                throw new NicoAPIException.ParseException(
                        e.getMessage(),root.toString(),
                        NicoAPIException.EXCEPTION_PARSE_MYLIST_GROUP_JSON
                );
            }
        }else{
            throw new NicoAPIException.HttpException(
                    "HTTP failure > myList group",
                    NicoAPIException.EXCEPTION_HTTP_MYLIST_GROUP_GET,
                    super.statusCode, myListGroupUrl, "GET"
            );
        }
    }

    public void add (String name, boolean isPublic, int sort, String description) throws NicoAPIException{
        if ( name == null ){
            throw new NicoAPIException.InvalidParamsException(
                    "name is null > add - myList",
                    NicoAPIException.EXCEPTION_PARAM_MYLIST_NAME
            );
        }
        if ( description == null ){
            throw new NicoAPIException.InvalidParamsException(
                    "description is null > add - myList",
                    NicoAPIException.EXCEPTION_PARAM_MYLIST_DESCRIPTION
            );
        }
        if ( sort < 0 || sort > 17 ){
            sort = MYLIST_SORT_REGISTER_LATE;
        }
        String path = rootURL + "add";
        String publicValue = "0";
        if ( isPublic ){
            publicValue = "1";
        }
        Map<String,String> params = new HashMap<String, String>();
        params.put("token", getToken(getVideoID()));
        params.put("name", name);
        params.put("public", publicValue);
        params.put("default_sort", String.valueOf(sort));
        params.put("icon_id","0");
        params.put("description", description);
        if ( tryPost(path,params,info.getCookieStore()) ){
            checkStatusCode(super.response);
            synchronized (this){
                isEdit = true;
            }
        }else{
            throw new NicoAPIException.HttpException(
                    "http failure",
                    NicoAPIException.EXCEPTION_HTTP_MYLIST_ADD,
                    statusCode,path,"POST");
        }
    }

    public void update (MyListVideoGroup group,
                        String name,
                        boolean isPublic,
                        int sort,
                        String description) throws NicoAPIException{
        if ( group == null ){
            throw new NicoAPIException.InvalidParamsException(
                    "no target myList > update - myList",
                    NicoAPIException.EXCEPTION_PARAM_MYLIST_TARGET_MYLIST
            );
        }
        String path = rootURL + "update";
        String publicValue = "0";
        if ( isPublic ){
            publicValue = "1";
        }
        Map<String,String> params = new HashMap<String, String>();
        params.put("group_id",String.valueOf(group.getMyListID()) );
        params.put("token", getToken(getVideoID()));
        if ( name != null ) {
            params.put("name", name);
        }
        params.put("public", publicValue);
        if ( sort >=0 && sort <= 17 ) {
            params.put("default_sort", String.valueOf(sort));
        }
        //params.put("icon_id","0");
        if ( description != null ) {
            params.put("description", description);
        }
        if ( tryPost(path,params,info.getCookieStore()) ){
            checkStatusCode(super.response);
            synchronized (this){
                isEdit = true;
            }
        }else{
            throw new NicoAPIException.HttpException(
                    "http failure",
                    NicoAPIException.EXCEPTION_HTTP_MYLIST_UPDATE,
                    statusCode,path,"POST");
        }
    }

    public void delete (MyListVideoGroup group) throws NicoAPIException{
        if ( group == null ){
            throw new NicoAPIException.InvalidParamsException(
                    "no target myList > delete - myList",
                    NicoAPIException.EXCEPTION_PARAM_MYLIST_TARGET_MYLIST
            );
        }
        String path = rootURL + "delete";
        Map<String,String> params = new HashMap<String, String>();
        params.put("group_id",String.valueOf(group.getMyListID()) );
        params.put("token", getToken(getVideoID()));
        if ( tryPost(path,params,info.getCookieStore()) ){
            checkStatusCode(super.response);
            synchronized (this){
                isEdit = true;
            }
        }else{
            throw new NicoAPIException.HttpException(
                    "http failure",
                    NicoAPIException.EXCEPTION_HTTP_MYLIST_DELETE,
                    statusCode,path,"POST");
        }
    }

    public static final int MYLIST_SORT_REGISTER_EARLY = 0;
    public static final int MYLIST_SORT_REGISTER_LATE = 1;
    public static final int MYLIST_SORT_MEMO_UP = 2;
    public static final int MYLIST_SORT_MEMO_DOWN = 3;
    public static final int MYLIST_SORT_TITLE_UP = 4;
    public static final int MYLIST_SORT_TITLE_DOWN = 5;
    public static final int MYLIST_SORT_CONTRIBUTION_LATE = 6;
    public static final int MYLIST_SORT_CONTRIBUTION_EARLY = 7;
    public static final int MYLIST_SORT_VIEW_DOWN = 8;
    public static final int MYLIST_SORT_VIEW_UP = 9;
    public static final int MYLIST_SORT_COMMENT_NEW = 10;
    public static final int MYLIST_SORT_COMMENT_OLD = 11;
    public static final int MYLIST_SORT_COMMENT_DOWN = 12;
    public static final int MYLIST_SORT_COMMENT_UP = 13;
    public static final int MYLIST_SORT_MYLIST_DOWN = 14;
    public static final int MYLIST_SORT_MYLIST_UP = 15;
    public static final int MYLIST_SORT_LENGTH_DOWN = 16;
    public static final int MYLIST_SORT_LENGTH_UP = 17;

    public void sort (MyListVideoGroup[] groups) throws NicoAPIException{
        if ( groups == null || groups.length == 0 ){
            throw new NicoAPIException.InvalidParamsException(
                    "no target myList > sort - myList",
                    NicoAPIException.EXCEPTION_PARAM_MYLIST_TARGET_MYLIST
            );
        }
        String path = rootURL + "sort";
        Map<String,String> params = new HashMap<String, String>();
        params.put("token", getToken(getVideoID()));
        for ( int i=0 ; i<groups.length ; i++){
            params.put(String.format("group_id_list[%d]",i), String.valueOf( groups[i].getMyListID()) );
        }
        if ( tryPost(path,params,info.getCookieStore()) ){
            checkStatusCode(super.response);
            synchronized (this){
                isEdit = true;
            }
        }else{
            throw new NicoAPIException.HttpException(
                    "http failure",
                    NicoAPIException.EXCEPTION_HTTP_MYLIST_SORT,
                    statusCode,path,"POST");
        }

    }

    private String getVideoID(){
        for ( MyListVideoGroup group : groupList ){
            if ( group.videoInfoList != null && !group.videoInfoList.isEmpty()){
                return group.videoInfoList.get(0).getID();
            }
        }
        try {
            for (MyListVideoGroup group : groupList) {
                group.getVideos();
                if ( !group.videoInfoList.isEmpty() ){
                    return group.videoInfoList.get(0).getID();
                }
            }
        }catch (NicoAPIException e){}
        return super.defaultVideoID;
    }

}
