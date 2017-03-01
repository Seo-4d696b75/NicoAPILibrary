package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

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
 * Created by Seo-4d696b75 on 2017/02/28.
 */

public class MyListGroup extends MyListEditor {

    protected MyListGroup(LoginInfo info) throws NicoAPIException{
        super(info);
        this.info = info;
        String myListGroupUrl = rootURL + "list";
        if ( tryGet(myListGroupUrl,info.getCookieStore()) ){
            JSONObject root = checkStatusCode(super.response);
            this.groupList = parse(root);
        }else{
            throw new NicoAPIException.HttpException(
                    "HTTP failure > myList group",
                    NicoAPIException.EXCEPTION_HTTP_MYLIST_GROUP_GET,
                    super.statusCode, myListGroupUrl, "GET"
            );
        }
    }

    private String rootURL = "http://www.nicovideo.jp/api/mylistgroup/";
    private LoginInfo info;
    private List<MyListVideoGroup> groupList;

    public List<MyListVideoGroup> getMyListVideoGroup(){
        return groupList;
    }

    private List<MyListVideoGroup> parse(JSONObject root)throws NicoAPIException{
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
    }

    public void add (String name, boolean isPublic, int sort, String description) throws NicoAPIException{
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
            //TODO
        }else{
            //TODO
        }
    }

    public void update (MyListVideoGroup group,
                        String name,
                        boolean isPublic,
                        int sort,
                        String description) throws NicoAPIException{
        String path = rootURL + "update";
        String publicValue = "0";
        if ( isPublic ){
            publicValue = "1";
        }
        Map<String,String> params = new HashMap<String, String>();
        params.put("group_id",String.valueOf(group.getMyListID()) );
        params.put("token", getToken(getVideoID()));
        params.put("name", name);
        params.put("public", publicValue);
        params.put("default_sort", String.valueOf(sort));
        params.put("icon_id","0");
        params.put("description", description);
        if ( tryPost(path,params,info.getCookieStore()) ){
            checkStatusCode(super.response);
            //TODO
        }else{
            //TODO
        }
    }

    public void delete (MyListVideoGroup group) throws NicoAPIException{
        String path = rootURL + "delete";
        Map<String,String> params = new HashMap<String, String>();
        params.put("group_id",String.valueOf(group.getMyListID()) );
        params.put("token", getToken(getVideoID()));
        if ( tryPost(path,params,info.getCookieStore()) ){
            checkStatusCode(super.response);
            //TODO
        }else{
            //TODO
        }
    }

    public void sort (MyListVideoGroup[] groups) throws NicoAPIException{
        String path = rootURL + "sort";
        Map<String,String> params = new HashMap<String, String>();
        params.put("token", getToken(getVideoID()));
        for ( int i=0 ; i<groups.length ; i++){
            params.put(String.format("group_id_list[%d]",i), String.valueOf( groups[i].getMyListID()) );
        }
        if ( tryPost(path,params,info.getCookieStore()) ){
            checkStatusCode(super.response);
            //TODO
        }else{
            //TODO
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
