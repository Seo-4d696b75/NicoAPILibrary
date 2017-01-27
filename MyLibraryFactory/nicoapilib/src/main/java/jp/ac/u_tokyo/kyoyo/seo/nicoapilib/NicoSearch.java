package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Seo-4d696b75 on 2017/01/22.
 */

public class NicoSearch extends HttpResponseGetter {

    private String searchUrl = "http://api.search.nicovideo.jp/api/v2/snapshot/video/contents/search?q=";
    private String query = null;
    private boolean tagsSearch = false;
    private String sortParam = null;
    private boolean sortDown = true;
    private int resultMax = 10;
    private List<String> filterList;

    public static final int QUERY_OPERATOR_AND = 0;
    public static final int QUERY_OPERATOR_OR = 1;
    public static final int QUERY_OPERATOR_NOT = 2;
    private final Map<Integer,String> queryOperatorMap = new HashMap<Integer, String>(){
        {
            put(QUERY_OPERATOR_AND," ");
            put(QUERY_OPERATOR_OR," OR ");
            put(QUERY_OPERATOR_NOT," -");
        }
    };

    public static final int SORT_PARAM_VIEW = 10;
    public static final int SORT_PARAM_MY_LIST = 11;
    public static final int SORT_PARAM_COMMENT = 12;
    public static final int SORT_PARAM_DATE = 13;
    public static final int SORT_PARAM_LENGTH = 14;
    private final Map<Integer,String> sortParamMap = new HashMap<Integer, String>(){
        {
            put(SORT_PARAM_VIEW,"viewCounter");
            put(SORT_PARAM_MY_LIST,"mylistCounter");
            put(SORT_PARAM_COMMENT,"commentCounter");
            put(SORT_PARAM_DATE,"startTime");
            put(SORT_PARAM_LENGTH,"lengthSeconds");
        }
    };

    public static final int FILTER_ID = 20;
    public static final int FILTER_TAGS = 21;
    public static final int FILTER_GENRE = 22;
    public static final int FILTER_VIEW = 23;
    public static final int FILTER_MY_LIST = 24;
    public static final int FILTER_COMMENT = 25;
    public static final int FILTER_DATE = 26;
    public static final int FILTER_LENGTH = 27;
    private final String filterFormat = "&filters[%s][%S]=%s";
    private final String filterFrom = "gte";
    private final String filterTo = "lte";
    private final String filterExact = "0";

    public NicoSearch (){
        this(null);
    }
    public NicoSearch (String query){
        if ( query == null){
            this.query = "";
        }else {
            this.query = query;
        }
        filterList = new ArrayList<String>();
        sortParam = sortParamMap.get(SORT_PARAM_VIEW);
    }

    public void setQuery (String query){
        this.query = query;
    }
    public void addQuery (String query){
        addQuery(query,QUERY_OPERATOR_AND);
    }
    public void addQuery (String query, int operatorKey){
        if ( queryOperatorMap.containsKey(operatorKey) ) {
            String addition = queryOperatorMap.get(operatorKey) + query;
            if ( query.isEmpty()) {
                this.query = addition.substring(1);
            }else{
                this.query += addition;
            }
        }else{
            //TODO exception
        }
    }

    public void setTagsSearch (boolean tagsSearch){
        this.tagsSearch = tagsSearch;
    }

    public void setSortParam (int paramKey){
        if ( sortParamMap.containsKey(paramKey) ){
            this.sortParam = sortParamMap.get(paramKey);
        }else{
            //TODO exception
        }
    }

    public void setSortDown (boolean sortDown){
        this.sortDown = sortDown;
    }

    public void setResultMax (int resultMax){
        if ( resultMax < 1 || resultMax > 100 ){
            //TODO exception
            return;
        }
        this.resultMax = resultMax;
    }

    public void setFilterID ( String id){
        setFilter(FILTER_ID,id);
    }
    public void setFilterTags ( String tag){
        setFilter(FILTER_TAGS,tag);
    }
    public void setFilterGenre( String genre){
        setFilter(FILTER_GENRE,genre);
    }
    public void setFilter (int filterKey, String target){
        String param = "";
        switch ( filterKey ){
            case FILTER_ID:
                param = "contentId";
                break;
            case FILTER_TAGS:
                param = "tags";
                break;
            case FILTER_GENRE:
                param = "categoryTags";
                break;
            case FILTER_DATE:
                return;
            default:
                //TODO exception
                return;
        }
        String filter = String.format(filterFormat,param,filterExact,target);
        filterList.add(filter);
    }

    public void setFilterView (int from, int to){
        setFilter(FILTER_VIEW,from,to);
    }
    public void setFilterComment (int from, int to){
        setFilter(FILTER_COMMENT,from,to);
    }
    public void setFilterLength (int from, int to ){
        setFilter(FILTER_LENGTH,from,to);
    }
    public void setFilterMyList (int from, int to){
        setFilter(FILTER_MY_LIST,from,to);
    }
    public void setFilter (int filterKey, int from, int to){
        if ( from >= 0 && to >= 0 && to < from ){
            //TODO exception
            return;
        }
        String param = "";
        switch ( filterKey ){
            case FILTER_VIEW:
                param = "viewCounter";
                break;
            case FILTER_MY_LIST:
                param = "mylistCounter";
                break;
            case FILTER_COMMENT:
                param = "commentCounter";
                break;
            case FILTER_LENGTH:
                param = "lengthSeconds";
                break;
            default:
                //TODO exception
                return;
        }
        if ( from >= 0 ){
            String target = String.valueOf(from);
            String filter = String.format(filterFormat,param,filterFrom,target);
            filterList.add(filter);
        }
        if ( to >= 0 ){
            String target = String.valueOf(to);
            String filter = String.format(filterFormat,param,filterTo,target);
            filterList.add(filter);
        }
    }

    public void setFilterDate(Date from, Date to){
        setFilter(FILTER_DATE,from,to);
    }
    public void setFilter (int filterKey, Date from, Date to){
        if ( filterKey != FILTER_DATE ){
            //TODO exception
            return;
        }
        if ( from == null && to == null ){
            //TODO exception
            return;
        }
        if ( from != null && to != null && from.getTime() > to.getTime() ){
            //TODO exception
            return;
        }
        String target = dateFormat.format(from);
        String filter = String.format(filterFormat,"startTime",filterFrom,target);
        filterList.add(filter);
        target = dateFormat.format(to);
        filter = String.format(filterFormat,"startTime",filterTo,target);
        filterList.add(filter);
    }

    public void setFilter (int filterKey, String from, String to){
        if ( filterKey != FILTER_DATE ){
            //TODO exception
            return;
        }
        try{
            Date dateFrom = dateFormat.parse(from);
            Date dateTo = dateFormat.parse(to);
            setFilter(filterKey,dateFrom,dateTo);
        }catch (ParseException e){
            //TODO exception
            return;
        }
    }

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+09:00'"){
        {
            setTimeZone(TimeZone.getTimeZone("Japan"));
        }
    };

    public List<VideoInfo> search (){
        if ( query.isEmpty() ){
            //TODO exception
            return null;
        }
        String appName = NicoClient.appName;
        StringBuilder builder = new StringBuilder();
        builder.append(searchUrl);
        builder.append(query);
        if ( tagsSearch ){
            builder.append("&targets=tagsExact");
        }else{
            builder.append("&targets=title,description,tags");
        }
        builder.append("&fields=contentId,title,description,tags,viewCounter,mylistCounter,commentCounter,startTime,lengthSeconds");
        for ( String filter : filterList) {
            builder.append(filter);
        }
        builder.append("&_sort=");
        if ( sortDown ){
            builder.append("-");
        }else{
            builder.append("+");
        }
        builder.append(sortParam);
        builder.append("&_limit=");
        builder.append(resultMax);
        builder.append("&_context=");
        builder.append(appName);
        String path = builder.toString();
        if ( tryGet(path) ){
            return SearchVideoInfo.parse(super.response);
        }
        return null;
    }


}
