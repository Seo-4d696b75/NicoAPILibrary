package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Seo on 2017/01/16.
 *
 * this class extending VideoInfoManager provides methods to parse JSON
 * from http://api.search.nicovideo.jp/api/v2/snapshot/video/contents/search?q={your query}
 *      details of this API : http://site.nicovideo.jp/search-api-docs/snapshot.html
 *
 */

public class SearchVideoInfo extends VideoInfoManager {

    public  SearchVideoInfo (JSONObject item){
        initialize(item);
    }

    private void initialize(JSONObject item){
        try{
            title = item.getString("title");
            id = item.getString("contentId");
            date = convertDate(item.getString("startTime"));
            description = item.getString("description");
            length = item.getInt("lengthSeconds");
            viewCounter = item.getInt("viewCounter");
            commentCounter = item.getInt("commentCounter");
            myListCounter = item.getInt("mylistCounter");
            setTags( item.getString("tags").split("\\s") );
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    private String convertDate (String date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+09:00'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Japan"));
        try {
            return dateFormatBase.format(dateFormat.parse(date));
        }catch (ParseException e){
            e.printStackTrace();
        }
        return null;
    }

    public static List<VideoInfo> parse (String response){
        try {
            JSONObject root = new JSONObject(response);
            return parse(root);
        }catch ( JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    public static List<VideoInfo> parse (JSONObject root){
        try {
            JSONObject meta = root.getJSONObject("meta");
            if ( meta.getInt("status") != 200 ){
                Log.d("search","fail to get");
                return null;
            }
            List<VideoInfo> list = new ArrayList<VideoInfo>();
            if ( meta.getInt("totalCount") == 0 ) {
                Log.d("search", "no hit");
                return list;
            }
            JSONArray array = root.getJSONArray("data");
            for ( int i=0 ; i<array.length() ; i++){
                JSONObject item = array.getJSONObject(i);
                list.add( new SearchVideoInfo(item));
            }
            return list;
        }catch(JSONException e){
            Log.d("search","fail to parse Json");
            return null;
        }
    }
}
