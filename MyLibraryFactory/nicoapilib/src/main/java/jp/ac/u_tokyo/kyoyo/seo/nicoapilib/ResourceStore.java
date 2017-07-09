package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Debug;
import android.util.Log;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by Seo-4d696b75 on 2017/05/04.
 */

public class ResourceStore extends Application{

    @Override
    public void onCreate(){
        super.onCreate();
        patternMap = new HashMap<Integer,Pattern>();
        colorMap = new LinkedHashMap<String, Integer>();
        positionMap = new HashMap<>();
        sizeMap = new HashMap<>();
        ngThresholdMap = new HashMap<>();
        initialize();
        store = this;
    }

    private void readPattern(int srcID){
        patternMap.put(srcID,Pattern.compile(getString(srcID),Pattern.DOTALL));
    }

    private void initialize(){
        Resources resources = getResources();
        //edit myList
        readPattern( R.string.regex_myList_token);
        //rss
        readPattern(R.string.regex_rss_item);
        readPattern(R.string.regex_rss_meta);
        //comment
        readPattern( R.string.regex_comment_pos);
        readPattern( R.string.regex_comment_anonymity);
        readPattern( R.string.regex_comment_content);
        readPattern( R.string.regex_comment_date);
        readPattern( R.string.regex_comment_command);
        readPattern( R.string.regex_comment_score);
        readPattern( R.string.regex_comment_id);
        readPattern( R.string.regex_comment_group_meta);
        readPattern( R.string.regex_comment_group_item);
        readPattern( R.string.regex_comment_postKey);
        readPattern( R.string.regex_comment_post_response);
        colorMap.put(CommentInfo.COLOR_WHITE,resources.getInteger(R.integer.white));
        colorMap.put(CommentInfo.COLOR_RED,resources.getInteger(R.integer.red));
        colorMap.put(CommentInfo.COLOR_PINK,resources.getInteger(R.integer.pink));
        colorMap.put(CommentInfo.COLOR_ORANGE,resources.getInteger(R.integer.orange));
        colorMap.put(CommentInfo.COLOR_YELLOW,resources.getInteger(R.integer.yellow));
        colorMap.put(CommentInfo.COLOR_GREEN,resources.getInteger(R.integer.green));
        colorMap.put(CommentInfo.COLOR_CYAN,resources.getInteger(R.integer.cyan));
        colorMap.put(CommentInfo.COLOR_BLUE,resources.getInteger(R.integer.blue));
        colorMap.put(CommentInfo.COLOR_PURPLE,resources.getInteger(R.integer.purple));
        colorMap.put(CommentInfo.COLOR_BLACK,resources.getInteger(R.integer.black));
        positionMap.put(getString(R.string.value_comment_position_top),CommentInfo.POSITION_UP);
        positionMap.put(getString(R.string.value_comment_position_middle),CommentInfo.POSITION_MIDDLE);
        positionMap.put(getString(R.string.value_comment_position_bottom),CommentInfo.POSITION_BOTTOM);
        sizeMap.put(getString(R.string.value_comment_size_small),CommentInfo.SIZE_SMALL);
        sizeMap.put(getString(R.string.value_comment_size_medium),CommentInfo.SIZE_MEDIUM);
        sizeMap.put(getString(R.string.value_comment_size_large),CommentInfo.SIZE_BIG);
        ngThresholdMap.put(0,resources.getInteger(R.integer.value_comment_NG_threshold_0));
        ngThresholdMap.put(1,resources.getInteger(R.integer.value_comment_NG_threshold_1));
        ngThresholdMap.put(2,resources.getInteger(R.integer.value_comment_NG_threshold_2));
        //login
        readPattern(R.string.regex_myPage_userName);
        readPattern(R.string.regex_seiga_userName);
        readPattern(R.string.regex_myPage_user_icon);
        readPattern(R.string.regex_myPage_user_profile);
        //recommend rss
        readPattern(R.string.regex_recommend_videoID);
        readPattern(R.string.regex_recommend_thumbnail);
        readPattern(R.string.regex_recommend_title);
        readPattern(R.string.regex_recommend_view_counter);
        readPattern(R.string.regex_recommend_comment_counter);
        readPattern(R.string.regex_recommend_myList_counter);
        readPattern(R.string.regex_recommend_duration);
        readPattern(R.string.regex_recommend_date);
        readPattern(R.string.regex_recommend_status);
        readPattern(R.string.regex_recommend_error);
        readPattern(R.string.regex_recommend_body);
        //rss
        readPattern(R.string.regex_rss_title);
        readPattern(R.string.regex_rss_id);
        readPattern(R.string.regex_rss_thumbnail);
        readPattern(R.string.regex_rss_description);
        readPattern(R.string.regex_rss_duration);
        readPattern(R.string.regex_rss_date);
        readPattern(R.string.regex_rss_view_counter);
        readPattern(R.string.regex_rss_myList_counter);
        readPattern(R.string.regex_rss_comment_counter);
        readPattern(R.string.regex_rss_title_ranking_order);
        readPattern(R.string.regex_rss_duration_format);

        try {
            this.httpClientClass = Class.forName(getString(R.string.reflection_http_client));
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    private Class httpClientClass;

    private Map<Integer,Pattern> patternMap;
    private Map<String,Integer> colorMap;
    private Map<String,Integer> positionMap;
    private Map<String,Float> sizeMap;
    private Map<Integer,Integer> ngThresholdMap;

    private static ResourceStore store;

    protected static ResourceStore getInstance(){
        return store;
    }

    protected Pattern getPattern(int key){
        if ( !patternMap.containsKey(key) ){
            patternMap.put(key,Pattern.compile(getString(key),Pattern.DOTALL));
        }
        return patternMap.get(key);
    }

    int getColor(String key){
        return colorMap.get(key);
    }
    boolean containColor(String key){
        return colorMap.containsKey(key);
    }
    Map<String,Integer> getColorMap(){
        Map<String,Integer> map = new LinkedHashMap<String ,Integer>();
        for ( String color : colorMap.keySet() ){
            map.put( color, colorMap.get(color));
        }
        return map;
    }

    int getPosition(String key){
        return positionMap.get(key);
    }
    String getPosition(int value){
        for ( String key : positionMap.keySet() ){
            if ( positionMap.get(key) == value ){
                return key;
            }
        }
        return CommentInfo.COLOR_BLACK;
    }
    boolean containPosition(String key){
        return positionMap.containsKey(key);
    }
    boolean containPosition(int value){
        for ( String key : positionMap.keySet() ){
            if ( positionMap.get(key) == value ){
                return true;
            }
        }
        return false;
    }

    float getSize(String key){
        return sizeMap.get(key);
    }
    String getSize(float value){
        for ( String key : sizeMap.keySet() ){
            if ( sizeMap.get(key) == value ){
                return key;
            }
        }
        return getString(R.string.value_comment_position_middle);
    }
    boolean containSize(String key){
        return sizeMap.containsKey(key);
    }
    boolean containSize(float value){
        for ( String key : sizeMap.keySet() ){
            if ( sizeMap.get(key) == value ){
                return true;
            }
        }
        return false;
    }

    int getNGThreshold(int key){
        return ngThresholdMap.get(key);
    }
    boolean containNGLevel(int key){
        return ngThresholdMap.containsKey(key);
    }

    String getURL(int key){
        return super.getString(key);
        //return urlMap.containsKey(key) ? urlMap.get(key) : "";
    }

    protected final StringMapLoader rankingGenreName = new StringMapLoader(NicoRanking.GENRE_ALL,R.array.value_ranking_genre_name);
    protected final StringMapLoader rankingGenreParam = new StringMapLoader(NicoRanking.GENRE_ALL,R.array.value_ranking_genre_param);
    protected final StringMapLoader rankingPeriodName = new StringMapLoader(NicoRanking.PERIOD_TOTAL,R.array.value_ranking_period_name);
    protected final StringMapLoader rankingPeriodParam = new StringMapLoader(NicoRanking.PERIOD_TOTAL,R.array.value_ranking_period_param);
    protected final StringMapLoader rankingKindName = new StringMapLoader(NicoRanking.KIND_FAV,R.array.value_ranking_kind_name);
    protected final StringMapLoader rankingKindParam = new StringMapLoader(NicoRanking.KIND_FAV,R.array.value_ranking_kind_param);
    protected final StringMapLoader searchQueryOperators = new StringMapLoader(NicoSearch.QUERY_OPERATOR_AND,R.array.value_search_query_operator);

    protected class StringMapLoader {
        private StringMapLoader(int initialKey, int stringArrayID ){
            this.initialKey = initialKey;
            this.stringArrayID = stringArrayID;
        }
        private int initialKey,stringArrayID;
        private Map<Integer,String> map;
        protected synchronized Map<Integer,String> getMap(){
            if ( map == null ){
                map = new LinkedHashMap<Integer, String>();
                String[] array = getResources().getStringArray(stringArrayID);
                for ( int i=0 ; i<array.length ; i++){
                    map.put(i+initialKey,array[i]);
                }
            }
            return map;
        }
    }
    protected class IntegerMapLoader {
        private IntegerMapLoader(int initialKey, int arrayID ){
            this.initialKey = initialKey;
            this.arrayID = arrayID;
        }
        private int initialKey,arrayID;
        private Map<Integer,Integer> map;
        protected synchronized Map<Integer,Integer> getMap(){
            if ( map == null ){
                map = new LinkedHashMap<>();
                int[] array = getResources().getIntArray(arrayID);
                for ( int i=0 ; i<array.length ; i++){
                    map.put(i+initialKey,array[i]);
                }
            }
            return map;
        }
    }

    protected HttpClient getHttpClient(){
        try {
            return (HttpClient) httpClientClass.newInstance();
        }catch (Exception e){
            e.printStackTrace();
            Log.d("errer",e.getMessage());
            return null;
        }
    }

}
