package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Seo on 2017/01/16.
 *
 * this class extending VideoInfoManager provides methods to parse JSON
 * from http://www.nicovideo.jp/api/deflist/list
 * note; getting temp my list needs login session stored in cookie
 *
 * reference;
 * how to get temp my list : https://ja.osdn.net/projects/nicolib/wiki/%E3%83%8B%E3%82%B3%E3%83%8B%E3%82%B3%E8%A7%A3%E6%9E%90%E3%83%A1%E3%83%A2
 *
 *
 */

public class TempMyListVideoInfo extends VideoInfoManager {

    public TempMyListVideoInfo( JSONObject item){
        initialize(item);
    }

    private void initialize(JSONObject item){
        try {
            id = item.getString("video_id");
            title = item.getString("title");
            thumbnailUrl = new String[]{item.getString("thumbnail_url")};
            date = convertDate(item.getLong("first_retrieve"));
            viewCounter = item.getInt("view_counter");
            commentCounter = item.getInt("num_res");
            myListCounter = item.getInt("mylist_counter");
            length = item.getInt("length_seconds");
        }catch ( JSONException e){
            e.printStackTrace();
        }
    }

    private String convertDate(long date){
        return dateFormatBase.format(new Date(date*1000));
    }

    public static List<VideoInfo> parse(String response){
        if ( response != null ) {
            try {
                JSONObject root = new JSONObject(response);
                if (!root.getString("status").equals("ok")) {
                    Log.d("tempMyList", "invalid access");
                    return null;
                }
                return parse(root);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static List<VideoInfo> parse(JSONObject root){
        try{
            JSONArray array = root.getJSONArray("mylistitem");
            List<VideoInfo> list = new ArrayList<VideoInfo>();
            for ( int i=0 ; i<array.length() ; i++) {
                JSONObject item = array.getJSONObject(i).getJSONObject("item_data");
                list.add( new TempMyListVideoInfo(item) );
            }
            return list;
        }catch ( JSONException e){
            e.printStackTrace();
        }
        return null;
    }

}
