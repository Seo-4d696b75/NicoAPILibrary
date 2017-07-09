package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * ニコ動の検索を行う<br>
 *     This class searches videos in Nico.<br><br>
 *
 * {@link NicoClient#getNicoSearch()}で取得したこのインスタンスにメソッドを呼び各種パラメータを設定して、
 * {@link #search()}を呼ぶこので検索します<br>
 *     Get this object from {@link NicoClient#getNicoSearch()},
 *     then set search params by calling appropriate methods,
 *     finally call {@link #search()} and get results.
 *
 * @author Seo-4d696b75
 * @version 0.0 on 2017/01/22.
 */

public class NicoSearch implements Parcelable{

    private String appName;
    private String keyword = "";
    private boolean tagsSearch = false;
    private String sortParam = null;
    private boolean sortDown = true;
    private int resultMax = 50;
    private List<String> filterList;

    private String query = null;

    /* <implementation of parcelable> */

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(appName);
        out.writeString(keyword);
        out.writeString(sortParam);
        out.writeInt(resultMax);
        out.writeBooleanArray(new boolean[]{tagsSearch,sortDown});
        out.writeStringList(filterList);
    }

    public static final Parcelable.Creator<NicoSearch> CREATOR = new Parcelable.Creator<NicoSearch>() {
        public NicoSearch createFromParcel(Parcel in) {
            return new NicoSearch(in);
        }
        public NicoSearch[] newArray(int size) {
            return new NicoSearch[size];
        }
    };

    private NicoSearch(Parcel in) {
        appName = in.readString();
        keyword = in.readString();
        sortParam = in.readString();
        resultMax = in.readInt();
        boolean[] val = new boolean[2];
        in.readBooleanArray(val);
        tagsSearch = val[0];
        sortDown = val[1];
        in.readStringList(filterList);
        res = ResourceStore.getInstance();
        initializeConstants();
    }

    /* </implementation of parcelable> */

    public static final int QUERY_OPERATOR_AND = 0;
    public static final int QUERY_OPERATOR_OR = 1;
    public static final int QUERY_OPERATOR_NOT = 2;
    private Map<Integer,String> queryOperatorMap;

    public static final int SORT_PARAM_VIEW = 10;
    public static final int SORT_PARAM_MY_LIST = 11;
    public static final int SORT_PARAM_COMMENT = 12;
    public static final int SORT_PARAM_DATE = 13;
    public static final int SORT_PARAM_LENGTH = 14;
    private final Map<Integer,Integer> sortParamMap = new HashMap<Integer, Integer>(){
        {
            put(SORT_PARAM_VIEW,R.string.key_search_view_counter);
            put(SORT_PARAM_MY_LIST,R.string.key_search_myList_counter);
            put(SORT_PARAM_COMMENT,R.string.key_search_comment_counter);
            put(SORT_PARAM_DATE,R.string.key_search_date);
            put(SORT_PARAM_LENGTH,R.string.key_search_duration);
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
    private String filterFormat;
    private String filterFrom;
    private String filterTo;
    private String filterExact;
    private SimpleDateFormat dateFormat;
    private ResourceStore res;

    private CookieGroup cookieGroup;

    /**
     * 検索クエリの設定を同時に行うコンストラクタ<br>
     * Constructs an instance, setting search keyword at the same time.
     * @param query can be {@code null}
     * @param appName your application name, cannot be {@code null}
     * @param cookieGroup the login session, may be {@code null} in case of search with no login
     */
    protected NicoSearch (String appName,String query, CookieGroup cookieGroup){
        this.appName = appName;
        this.query = query;
        this.cookieGroup = cookieGroup;
        res = ResourceStore.getInstance();
        filterList = new ArrayList<String>();
        setSortParam(SORT_PARAM_VIEW);
        initializeConstants();
    }

    private void initializeConstants(){
        filterFormat = res.getString(R.string.format_search_range);
        filterFrom = res.getString(R.string.value_search_range_lower);
        filterTo = res.getString(R.string.value_search_range_upper);
        filterExact = res.getString(R.string.value_search_range_exact);
        dateFormat = new SimpleDateFormat(res.getString(R.string.format_search_date), Locale.JAPAN);
        dateFormat.setTimeZone(TimeZone.getTimeZone("Japan"));
        queryOperatorMap = res.searchQueryOperators.getMap();
    }

    /**
     * 検索クエリを全て自前で設定します,{@link SearchVideoInfo 検索ＡＰＩの詳細}<br>
     * Sets all the search keyword following {@link SearchVideoInfo required format}.
     * @param query should not be {@code null}, or throw exception in {@link #search()}
     */
    public synchronized void setQuery (String query){
        this.query = query;
    }

    /**
     * 検索キーワードを設定します。
     * @param keyword the word, should not be {@code null} or empty string
     */
    public synchronized void setKeyword (String keyword){
        if (keyword != null && !keyword.isEmpty()) {
            this.keyword = keyword;
        }
    }
    /**
     * 検索キーワードをAND演算子で追加します<br>
     * Adds search keyword with AND operator.
     * @param keyword should not be {@code null} or empty, or no change is applied
     */
    public synchronized void addKeyword (String keyword){
        addKeyword(keyword, QUERY_OPERATOR_AND);
    }
    /**
     * 検索キーワードを追加します,{@link #QUERY_OPERATOR_AND AND}/{@link #QUERY_OPERATOR_NOT NOT}/{@link #QUERY_OPERATOR_OR OR}の演算子が定数で指定できます<br>
     * Adds search keyword, with operator {@link #QUERY_OPERATOR_AND AND}/{@link #QUERY_OPERATOR_NOT NOT}/{@link #QUERY_OPERATOR_OR OR} in constant.
     * @param keyword should not be {@code null} or empty, or no change is applied
     * @param operatorKey chosen from these constants; {@link #QUERY_OPERATOR_AND AND}/{@link #QUERY_OPERATOR_NOT NOT}/{@link #QUERY_OPERATOR_OR OR}, or no change is applied
     */
    public synchronized void addKeyword (String keyword, int operatorKey) {
        if ( keyword == null || keyword.isEmpty() ){
            return;
        }
        if ( this.keyword.isEmpty()) {
            if (queryOperatorMap.containsKey(operatorKey)) {
                String addition = queryOperatorMap.get(operatorKey) + keyword;
                this.keyword += addition;
            }
        }else{
            this.keyword = keyword;
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
            this.sortParam = res.getString(sortParamMap.get(paramKey));
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
        if ( genre != null && ResourceStore.getInstance().rankingGenreParam.getMap().containsValue(genre) ) {
            setFilter(FILTER_GENRE, genre);
        }
    }
    /**
     * 検索フィルターをジャンル(カテゴリタグ)に関して設定します<br>
     *  Sets search filter about genre, what is called {@link NicoRanking category tag}.
     *  ジャンルは{@link NicoRanking ランキング取得}で定義された定数で指定できます
     * @param key the genre key, defined in {@link NicoRanking }
     */
    public void setFilterGenre (int key){
        Map<Integer,String> genreMap = ResourceStore.getInstance().rankingGenreParam.getMap();
        if ( genreMap.containsKey(key) ){
            setFilter(FILTER_GENRE, genreMap.get(key));
        }
    }
    private synchronized void setFilter (int filterKey, String target){
        String param = "";
        switch ( filterKey ){
            case FILTER_ID:
                param = res.getString(R.string.key_search_videoID);
                break;
            case FILTER_TAGS:
                param = res.getString(R.string.key_search_tags);
                break;
            case FILTER_GENRE:
                param = res.getString(R.string.key_search_genre);
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
                param = res.getString(R.string.key_search_view_counter);
                break;
            case FILTER_MY_LIST:
                param = res.getString(R.string.key_search_myList_counter);
                break;
            case FILTER_COMMENT:
                param = res.getString(R.string.key_search_comment_counter);
                break;
            case FILTER_LENGTH:
                param = res.getString(R.string.key_search_duration);
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
        String param = res.getString(R.string.key_search_date);
        if ( from != null ) {
            String target = dateFormat.format(from);
            String filter = String.format(filterFormat, param, filterFrom, target);
            filterList.add(filter);
        }
        if ( to != null ) {
            String target = dateFormat.format(to);
            String filter = String.format(filterFormat, param, filterTo, target);
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
                dateFrom = dateFormat.parse(from);
            }
            Date dateTo = null;
            if ( to != null ){
                dateTo = dateFormat.parse(to);
            }
            setFilter(dateFrom,dateTo);
        }catch (ParseException e){
            e.printStackTrace();
        }
    }


    /**
     * 検索を実行して結果を取得します<br>
     * Searches videos and gets results.<br>
     * 必ず{@link #setKeyword(String)}/{@link #addKeyword(String)}で検索ワードを設定してから呼んでください。<br>
     * Be sure to call this after setting search keywords in {@link #setKeyword(String)}/{@link #addKeyword(String)}.<br>
     * <strong>ＵＩスレッド禁止</strong>HTTP通信を行うのでバックグランド処理してください。<br>
     * <strong>No UI thread</strong> HTTP communication is done.
     * @return Returns empty List, not {@code null}
     * @throws NicoAPIException if neither of query or keyword is set or fail to parse response
     */
    public SearchVideoGroup search () throws NicoAPIException{
        SearchVideoGroup group;
        synchronized (this) {
            if (appName == null || appName.isEmpty()) {
                throw new NicoAPIException.InvalidParamsException("appName is required in Search");
            }
            if ( this.query == null ) {
                StringBuilder builder = new StringBuilder();
                if (keyword.isEmpty()) {
                    throw new NicoAPIException.InvalidParamsException("no keyword is set > search");
                }
                builder.append(keyword);
                if (tagsSearch) {
                    builder.append(res.getString(R.string.value_search_tagSearch));
                } else {
                    builder.append(res.getString(R.string.value_search_nonTagSearch));
                }
                builder.append(res.getString(R.string.value_search_target_fields));
                for (String filter : filterList) {
                    builder.append(filter);
                }
                builder.append(res.getString(R.string.key_search_sort_order));
                if (sortDown) {
                    builder.append(res.getString(R.string.value_search_sort_order_down));
                } else {
                    builder.append(res.getString(R.string.value_search_sort_order_up));
                }
                builder.append(sortParam);
                builder.append(res.getString(R.string.key_search_result_max));
                builder.append(resultMax);
                builder.append(res.getString(R.string.key_search_appName));
                builder.append(appName);
                this.query = builder.toString();
            }else{
                String key = res.getString(R.string.key_search_appName);
                if ( !query.contains(key) ){
                    query += key;
                    query += appName;
                }
            }

            group = new SearchVideoGroup(this);
        }
        String path = res.getURL(R.string.url_search) + query;
        HttpClient client = res.getHttpClient();
        if ( client.get(path,null) ){
            group.setVideoList(cookieGroup,client.getResponse());
            return group;
        }else{
            throw new NicoAPIException.HttpException(
                    "fail to get search response",
                    NicoAPIException.EXCEPTION_HTTP_SEARCH,
                    client.getStatusCode(), path, "GET"
            );
        }
    }

    /**
     * 検索結果を保持するクラスです　This class keeps the search results.
     */
    public static class SearchVideoGroup {
        private SearchVideoGroup(NicoSearch nicoSearch){
            this.appName = nicoSearch.appName;
            this.query = nicoSearch.query;
            this.keyword = nicoSearch.keyword;
            this.sortParam = nicoSearch.sortParam;
            this.tagSearch = nicoSearch.tagsSearch;
            this.sortDown = nicoSearch.sortDown;
            this.resultMax = nicoSearch.resultMax;
            this.filterList = new ArrayList<String>();
            this.filterList.addAll(nicoSearch.filterList);
        }
        private void setVideoList(CookieGroup cookieGroup,String response) throws NicoAPIException{
            try {
                ResourceStore res = ResourceStore.getInstance();
                JSONObject root = new JSONObject(response);
                JSONObject meta = root.getJSONObject(res.getString(R.string.key_search_result_meta));
                if ( meta.getInt(res.getString(R.string.key_search_result_status)) != Integer.parseInt(res.getString(R.string.value_search_result_status_success)) ){
                    String message = "Unexpected API response status > search ";
                    try {
                        String errorCode = meta.getString(res.getString(R.string.key_search_result_error_code));
                        String errorMessage = meta.getString(res.getString(R.string.key_search_result_error_message));
                        message += (errorCode + ":" + errorMessage);
                    }catch (JSONException e){}
                    throw new NicoAPIException.APIUnexpectedException(
                            message
                    );
                }
                this.id = meta.getString(res.getString(R.string.key_search_result_id));
                this.totalCount = meta.getInt(res.getString(R.string.key_search_result_total_hit));
                this.videoList = SearchVideoInfo.parse(cookieGroup,root);
            }catch (JSONException e){
                throw new NicoAPIException.ParseException(
                        e.getMessage(),response
                );
            }
        }
        private String appName;
        private String query;
        private String keyword;
        private boolean tagSearch;
        private String sortParam;
        private boolean sortDown;
        private int resultMax;
        private List<String> filterList;
        private List<VideoInfo> videoList;
        private String id;
        private int totalCount;
        /**
         * 検索に用いたアプリ名を取得します Gets the application name used to search videos.
         * @return the application name param
         */
        public String getAppName(){return appName;}
        /**
         * 検索に用いた検索クエリを取得します Gets the query used to search videos.
         * @return the query
         */
        public String getQuery(){return query;}
        /**
         * 検索に用いたキーワードを取得します<br> Gets the keywords used in search of videos.
         * @return the keyword, or keywords joined with an operator, such as "AND" and "OR".
         */
        public String getKeyword(){return keyword;}
        /**
         * 検索に用いた検索結果の整列パラメータを取得します Gets the sort param used to search videos.
         */
        public String getSortParam(){return sortParam;}
        /**
         * この検索結果がタグ検索か否かを取得します Gets whether or not the search is tag-search.
         * @return tag-search?
         */
        public boolean isTagSearch(){return tagSearch;}
        /**
         * 検索結果が降順にソートされているか否か取得します
         * Gets whether or not the videos are sorted in descending order.
         * @return in descending order ?
         */
        public boolean isSortDown(){return sortDown;}
        /**
         * 指定した検索結果の最大数を取得します
         * Gets the limit number of results.
         * @return the result number
         */
        public int getResultMax(){return resultMax;}
        /**
         * 指定したフィルター値をまとめて{@code List}で取得します
         * Gets filter values used to search videos.
         * @return filter values in {@code List}, not {@code null}
         */
        public List<String> getFilterList(){
            List<String> list = new ArrayList<String>();
            list.addAll(this.filterList);
            return list;
        }
        /**
         * 検索結果の動画を取得します Gets results videos.<br>
         * 動画が検索結果の順にソートされて格納された{@code List}オブジェクトを返します。
         * このリストオブジェクトに変更を加えても構いません。
         * @return {@code List} object in which videos are sorted as same as in results, not {@code null}
         */
        public List<VideoInfo> getVideos(){
            List<VideoInfo> list = new ArrayList<VideoInfo>();
            list.addAll(this.videoList);
            return list;
        }
        /**
         * 検索ＩＤを取得します Gets searchID.
         * @return the search ID
         */
        public String getSearchId(){return id;}
        /**
         * 検索の合計ヒット数を取得します Gets the total number of hit videos.
         * @return the total hit number
         */
        public int getTotalCount(){return totalCount;}
    }


}
