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
 * ニコ動の検索を行う<br>
 *     this class search videos in Nico.<br><br>
 *
 * {@link NicoClient#getNicoSearch()}で取得したこのインスタンスにメソッドを呼び各種パラメータを設定して、
 * {@link #search()}を呼ぶ。<br>
 *     get this class instance from {@link NicoClient#getNicoSearch()},
 *     then set search params by calling methods,
 *     finally call {@link #search()} and get results.
 *
 * @author Seo-4d696b75
 * @version 0.0 on 2017/01/22.
 */

public class NicoSearch extends HttpResponseGetter {

    private String appName;
    private String searchUrl = "http://api.search.nicovideo.jp/api/v2/snapshot/video/contents/search?q=";
    private String query = null;
    private boolean tagsSearch = false;
    private String sortParam = null;
    private boolean sortDown = true;
    private int resultMax = 50;
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

    private static final int FILTER_ID = 20;
    private static final int FILTER_TAGS = 21;
    private static final int FILTER_GENRE = 22;
    private static final int FILTER_VIEW = 23;
    private static final int FILTER_MY_LIST = 24;
    private static final int FILTER_COMMENT = 25;
    private static final int FILTER_DATE = 26;
    private static final int FILTER_LENGTH = 27;
    private final String filterFormat = "&filters[%s][%S]=%s";
    private final String filterFrom = "gte";
    private final String filterTo = "lte";
    private final String filterExact = "0";

    protected NicoSearch (String appName){
        this(appName,null);
    }

    /**
     * 検索キーワードの設定を同時に行うコンストラクタ<br>
     * Constructs an instance, setting search keyword at the same time.
     * @param query can be {@code null}
     * @param appName your application name, cannot be {@code null}
     */
    protected NicoSearch (String appName,String query){
        this.appName = appName;
        if ( query == null){
            this.query = "";
        }else {
            this.query = query;
        }
        filterList = new ArrayList<String>();
        sortParam = sortParamMap.get(SORT_PARAM_VIEW);
    }

    /**
     * 検索キーワードを全て自前で設定します,{@link SearchVideoInfo 検索ＡＰＩの詳細}<br>
     * Sets all the search keyword following {@link SearchVideoInfo required format}.
     * @param query should not be {@code null}, or throw exception in {@link #search()}
     */
    public synchronized void setQuery (String query){
        this.query = query;
    }
    /**
     * 検索キーワードをAND演算子で追加します<br>
     * Adds search keyword with AND operator.
     * @param query should not be {@code null} or empty, or no change is applied
     */
    public synchronized void addQuery (String query){
        addQuery(query, QUERY_OPERATOR_AND);
    }
    /**
     * 検索キーワードを追加します,{@link #QUERY_OPERATOR_AND AND}/{@link #QUERY_OPERATOR_NOT NOT}/{@link #QUERY_OPERATOR_OR OR}の演算子が定数で指定できます<br>
     * Adds search keyword, with operator {@link #QUERY_OPERATOR_AND AND}/{@link #QUERY_OPERATOR_NOT NOT}/{@link #QUERY_OPERATOR_OR OR} in constant.
     * @param query should not be {@code null} or empty, or no change is applied
     * @param operatorKey chosen from these constants; {@link #QUERY_OPERATOR_AND AND}/{@link #QUERY_OPERATOR_NOT NOT}/{@link #QUERY_OPERATOR_OR OR}, or no change is applied
     */
    public synchronized void addQuery (String query, int operatorKey) {
        if ( query == null || query.isEmpty() ){
            return;
        }
        if ( queryOperatorMap.containsKey(operatorKey) ) {
            String addition = queryOperatorMap.get(operatorKey) + query;
            if ( query.isEmpty()) {
                this.query = addition.substring(1);
            }else{
                this.query += addition;
            }
        }
    }
    /**
     * タグ検索を行うか否か設定します<br>
     *  Sets whether or not search videos with tags.
     * @param tagsSearch search about tags or not
     */
    public synchronized void setTagsSearch (boolean tagsSearch){
        this.tagsSearch = tagsSearch;
    }
    /**
     * 検索結果をソートするパラメータを定数SORT_PARAM_****で指定します<br>
     * Sets sort param by constant; SORT_PARAM_****.<br>
     * デフォルトでは{@link #SORT_PARAM_VIEW 再生数}が選択されます<br>
     * Default value is {@link #SORT_PARAM_VIEW number of view}.
     * @param paramKey chosen from SORT_PARAM_****, or no change is applied
     */
    public synchronized void setSortParam (int paramKey){
        if ( sortParamMap.containsKey(paramKey) ){
            this.sortParam = sortParamMap.get(paramKey);
        }
    }
    /**
     * {@link #setSortParam(int) ソートパラメータ}に関して降順にソートするか否か(つまり昇順)を設定します<br>
     * Sets whether results is sorted in descending order or not.<br>
     * デフォルトは降順です。is {@code true}.
     * @param sortDown descending order or not
     */
    public synchronized void setSortDown (boolean sortDown){
        this.sortDown = sortDown;
    }
    /**
     * 最大検索結果数を設定します、上限は100です<br>
     * Sets max results number, under 100.
     * @param resultMax the max number, must be in range 1-100 or no change is applied
     */
    public synchronized void setResultMax (int resultMax){
        if ( resultMax < 1 || resultMax > 100 ){
            return;
        }
        this.resultMax = resultMax;
    }
    /**
     * 検索フィルターを動画IDに関して設定します<br>
     * Sets search filter about video ID.
     * @param id the target video ID, should not be {@code null}, or no change applied
     */
    public void setFilterID ( String id){
        setFilter(FILTER_ID,id);
    }
    /**
     * 検索フィルターを動画タグに関して設定します<br>
     * Sets search filter about video tag.
     * @param tag the target video tag, should not be {@code null}, or no change applied
     */
    public void setFilterTags ( String tag){
        setFilter(FILTER_TAGS,tag);
    }
    /**
     * 検索フィルターをジャンル(カテゴリタグ)に関して設定します, 参照：{@link NicoRanking 指定可能な値}<br>
     *  Sets search filter about genre, what is called {@link NicoRanking category tag}.
     * @param genre the target genre, should not be {@code null}, or no change applied
     */
    public void setFilterGenre( String genre){
        setFilter(FILTER_GENRE,genre);
    }
    private synchronized void setFilter (int filterKey, String target){
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
                return;
        }
        String filter = String.format(filterFormat,param,filterExact,target);
        filterList.add(filter);
    }

    /**
     * 検索フィルターを動画再生数に関して設定します<br>
     *     Sets search filter about view counter.<br>
     * 最小値と最大値を渡し範囲で指定するか、一方に負数を渡し片側からのみ絞ります。
     *     範囲で指定する場合、空集合だと無視されますので注意してください。<br>
     *     The search filter is set in range by passing min and max, but ignored if empty set.
     *     You can set only one side by passing negative in the other.
     * @param from the min value, ignored if negative
     * @param to the max value, ignored if negative
     */
    public void setFilterView (int from, int to){
        setFilter(FILTER_VIEW,from,to);
    }
    /**
     * 検索フィルターを動画コメント数に関して設定します　
     *     Sets search filter about comment counter.<br>
     * 最小値と最大値を渡し範囲で指定するか、一方に負数を渡し片側からのみ絞ります。
     *     範囲で指定する場合、空集合だと無視されますので注意してください。<br>
     *     The search filter is set in range by passing min and max, but ignored if empty set.
     *     You can set only one side by passing negative in the other.
     * @param from the min value, ignored if negative
     * @param to the max value, ignored if negative
     */
    public void setFilterComment (int from, int to){
        setFilter(FILTER_COMMENT,from,to);
    }
    /**
     * 検索フィルターを動画長さに関して秒単位で設定します　
     *     Sets search filter about length in seconds.<br>
     * 最小値と最大値を渡し範囲で指定するか、一方に負数を渡し片側からのみ絞ります。
     *     範囲で指定する場合、空集合だと無視されますので注意してください。<br>
     *     The search filter is set in range by passing min and max, but ignored if empty set.
     *     You can set only one side by passing negative in the other.
     * @param from the min value, ignored if negative
     * @param to the max value, ignored if negative
     */
    public void setFilterLength (int from, int to ){
        setFilter(FILTER_LENGTH,from,to);
    }
    /**
     * 検索フィルターを動画マイリス数に関して設定します　
     *     Sets search filter about my list counter.<br>
     * 最小値と最大値を渡し範囲で指定するか、一方に負数を渡し片側からのみ絞ります。
     *     範囲で指定する場合、空集合だと無視されますので注意してください。<br>
     *     The search filter is set in range by passing min and max, but ignored if empty set.
     *     You can set only one side by passing negative in the other.
     * @param from the min value, ignored if negative
     * @param to the max value, ignored if negative
     */
    public void setFilterMyList (int from, int to){
        setFilter(FILTER_MY_LIST,from,to);
    }
    private synchronized void setFilter (int filterKey, int from, int to){
        if ( from >= 0 && to >= 0 && to < from ){
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

    /**
     * 検索フィルターを投稿日時に関して設定します<br>
     *     Sets search filter about contribution date.<br>
     * 最小値と最大値を渡し範囲で指定するか、一方に{@code null}を渡し片側からのみ絞ります。
     *     範囲で指定する場合、空集合だと無視されますので注意してください。<br>
     *     The search filter is set in range by passing min and max, but ignored if empty set.
     *     You can set only one side by passing {@code null} in the other.
     * @param from the earliest date, ignored if {@code null}
     * @param to the latest date, ignored if {@code null}
     */
    public void setFilterDate(Date from, Date to){
        setFilter(from,to);
    }
    private synchronized void setFilter (Date from, Date to){
        if ( from == null && to == null ){
            return;
        }
        if ( from != null && to != null && from.getTime() > to.getTime() ){
            return;
        }
        if ( from != null ) {
            String target = dateFormat.format(from);
            String filter = String.format(filterFormat, "startTime", filterFrom, target);
            filterList.add(filter);
        }
        if ( to != null ) {
            String target = dateFormat.format(to);
            String filter = String.format(filterFormat, "startTime", filterTo, target);
            filterList.add(filter);
        }
    }
    /**
     * {@link #setFilterDate(Date, Date)}と同様ですが日時形式に注意してください<br>
     *     Basically same as {@link #setFilterDate(Date, Date)}, but be careful about date format.<br>
     *     Stringで日時を渡す際には{@link #dateFormat 指定の形式}に従ってください。<br>
     *     When you pass in String, argument must follow {@link #dateFormat this format}.
     * @param from the earliest date, ignored if {@code null}
     * @param to the latest date, ignored if {@code null}
     */
    public synchronized void setFilterDate (String from, String to){
        try{
            Date dateFrom = null;
            if ( from != null ){
                dateFormat.parse(from);
            }
            Date dateTo = null;
            if ( to != null ){
                dateFormat.parse(to);
            }
            setFilter(dateFrom,dateTo);
        }catch (ParseException e){
            e.printStackTrace();
            return;
        }
    }
    /**
     * ニコ動検索ＡＰＩでの日時形式です"yyyy-MM-dd'T'HH:mm:ss'+09:00'"<br>
     *  date format in Nico search API; "yyyy-MM-dd'T'HH:mm:ss'+09:00'".<br>
     *  基本的にはISO8601の拡張型に準拠しているが、タイムゾーンはJAPANで固定の模様でかつ、HHとmmの間に半角コロンが挿入されているので注意してください。<br>
     *  based on extended ISO8691, but be careful that time zone seems fixed JAPAN and that ':' is inserted between HH and mm.
     */
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+09:00'"){
        {
            setTimeZone(TimeZone.getTimeZone("Japan"));
        }
    };
    /**
     * 検索を実行して結果を取得します<br>
     * Searches videos and gets results.<br>
     * 必ず{@link #setQuery(String)}/{@link #addQuery(String, int)}で検索ワードを設定してから呼んでください。<br>
     * Be sure to call this after setting search keywords in {@link #setQuery(String)}/{@link #addQuery(String, int)}.
     * @return Returns empty List, not {@code null}
     * @throws NicoAPIException if no query set or fail to parse response
     */
    public List<VideoInfo> search () throws NicoAPIException{
        StringBuilder builder = new StringBuilder();
        synchronized (this) {
            if (appName == null || appName.isEmpty()) {
                throw new NicoAPIException.InvalidParamsException("appName is required in Search");
            }
            if (query.isEmpty()) {
                throw new NicoAPIException.InvalidParamsException("no query is set > search");
            }
            builder.append(searchUrl);
            builder.append(query);
            if (tagsSearch) {
                builder.append("&targets=tagsExact");
            } else {
                builder.append("&targets=title,description,tags");
            }
            builder.append("&fields=contentId,title,description,tags,viewCounter,mylistCounter,commentCounter,startTime,lengthSeconds");
            for (String filter : filterList) {
                builder.append(filter);
            }
            builder.append("&_sort=");
            if (sortDown) {
                builder.append("-");
            } else {
                builder.append("+");
            }
            builder.append(sortParam);
            builder.append("&_limit=");
            builder.append(resultMax);
            builder.append("&_context=");
            builder.append(appName);
        }
        String path = builder.toString();
        if ( tryGet(path) ){
            return SearchVideoInfo.parse(super.response);
        }else{
            throw new NicoAPIException.HttpException(
                    "fail to get search response",
                    NicoAPIException.EXCEPTION_HTTP_SEARCH,
                    super.statusCode, path, "GET"
            );
        }
    }


}
