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
        String myListGroupUrl = "http://www.nicovideo.jp/api/mylistgroup/list";
        if ( tryGet(myListGroupUrl,info.getCookieStore()) ){
            JSONObject root = checkStatusCode(super.response);
            this.myListInfoList = parse(root);
        }else{
            throw new NicoAPIException.HttpException(
                    "HTTP failure > myList group",
                    NicoAPIException.EXCEPTION_HTTP_MYLIST_GROUP_GET,
                    super.statusCode, myListGroupUrl, "GET"
            );
        }
    }

    private LoginInfo info;
    private List<MyListVideoGroup> myListInfoList;
    public List<MyListVideoGroup> getMyListVideoGroup(){
        return myListInfoList;
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



}
