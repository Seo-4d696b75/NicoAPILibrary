package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;


/**
 * ニコ動のランキングを取得する<br>
 * This gets ranking search in Nico.<br><br>
 *
 * ランキング検索にはカテゴリ、期間、種類の3パラメータが必要です。
 * これらパラメータを各種適当なメソッドを呼び設定してからランキングを取得します。
 * 各パラメータに対応した定数が用意されているので、これを指定することができます。
 * また、各パラメータの名前と値のMapを取得してその値で指定することも可能です。
 * 例）ジャンルパラメータ ("カテゴリ合算":all)... 　##この"all"またはこれに対応した定数GENRE_ALLを#setGenre(String/int)に渡す。
 * 独自に設定をしないとデフォルトでカテゴリ合算、期間合計、総合ランキングとして扱います。
 * パラメータの詳細は<a href=http://dic.nicovideo.jp/a/%E3%82%AB%E3%83%86%E3%82%B4%E3%83%AA%E3%82%BF%E3%82%B0>ここから参照してください。</a><br>
 * Getting ranking needs 3 params; category, period and ranking kind.
 * This class gets it from Nico after you set these params by calling relevant methods.
 * There are constants provided corresponding to each param, so you can specify param with these constants.
 * Also you can get Map of each param name and its value, so param can be specified with its value directly.
 * ex) genre param ("カテゴリ合算","all")...  # you pass "all" or constant GENRE_ALL to #setGenre(String/int)
 * If you not set them, this interprets as all category, entire period, overall ranking.
 *
 * @author Seo-4d696b75
 * @version  0.0 on 2017/02/03.
 */

public class NicoRanking implements Parcelable {

    private String rankingUrl;

    private String genre;
    private String kind;
    private String period;

    private CookieGroup cookieGroup;

    private boolean isDone;

    /* <implementation of parcelable> */

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(genre);
        out.writeString(kind);
        out.writeString(period);
        out.writeBooleanArray(new boolean[]{isDone});
    }

    public static final Parcelable.Creator<NicoRanking> CREATOR = new Parcelable.Creator<NicoRanking>() {
        public NicoRanking createFromParcel(Parcel in) {
            return new NicoRanking(in);
        }
        public NicoRanking[] newArray(int size) {
            return new NicoRanking[size];
        }
    };

    private NicoRanking(Parcel in) {
        genre = in.readString();
        kind = in.readString();
        period = in.readString();
        boolean[] val = new boolean[1];
        in.readBooleanArray(val);
        this.isDone = val[0];
        setMap();
    }

    /* </implementation of parcelable> */

    public static final int GENRE_ALL = 400;
    public static final int GENRE_VOCALOID = 401;
    public static final int GENRE_MUSIC = 402;
    public static final int GENRE_ANIME = 403;
    public static final int GENRE_GAME = 404;
    public static final int GENRE_DANCE = 405;
    public static final int GENRE_SING = 406;
    public static final int GENRE_PLAY = 407;
    public static final int GENRE_TOHO = 408;
    public static final int GENRE_IMAS = 409;
    public static final int GENRE_ENT = 410;
    public static final int GENRE_ANIMAL = 411;
    public static final int GENRE_RADIO = 412;
    public static final int GENRE_SPORT = 413;
    public static final int GENRE_POLITICS = 414;
    public static final int GENRE_SCIENCE = 415;
    public static final int GENRE_HISTORY = 416;
    public static final int GENRE_COOKING = 417;
    public static final int GENRE_NATURE = 418;
    public static final int GENRE_DIARY = 419;
    public static final int GENRE_LECTURE = 420;
    public static final int GENRE_NICOINDIES = 421;
    public static final int GENRE_DRIVE = 422;
    public static final int GENRE_TECH = 423;
    public static final int GENRE_HANDCRAFT = 424;
    public static final int GENRE_MAKE = 425;
    public static final int GENRE_DRAW = 426;
    public static final int GENRE_TRAVEL = 427;
    public static final int GENRE_ARE = 428;
    public static final int GENRE_OTHER = 429;

    public static final int KIND_FAV = 500;
    public static final int KIND_VIEW = 501;
    public static final int KIND_MYLIST = 502;
    public static final int KIND_RES = 503;

    public static final int PERIOD_TOTAL = 600;
    public static final int PERIOD_MONTHLY = 601;
    public static final int PERIOD_WEEKLY = 602;
    public static final int PERIOD_DAILY = 603;
    public static final int PERIOD_HOURLY = 604;

    private Map<Integer,String> genreNameMap;
    private Map<Integer,String> genreValueMap;
    private Map<Integer,String> periodNameMap;
    private Map<Integer,String> periodValueMap;
    private Map<Integer,String> kindNameMap;
    private Map<Integer,String> kindValueMap;

    private void setMap(){
        ResourceStore res = ResourceStore.getInstance();
        rankingUrl = res.getURL(R.string.url_ranking);
        genreNameMap = res.rankingGenreName.getMap();
        genreValueMap = res.rankingGenreParam.getMap();
        periodNameMap = res.rankingPeriodName.getMap();
        periodValueMap = res.rankingPeriodParam.getMap();
        kindNameMap = res.rankingKindName.getMap();
        kindValueMap = res.rankingKindParam.getMap();
    }

    protected NicoRanking (CookieGroup cookieGroup){
        genre = "";
        period = "";
        kind = "";
        isDone = false;
        setMap();
        this.cookieGroup = cookieGroup;
    }

    /**
     * ジャンル(カテゴリ)の名前と値のMapを取得する<br>
     * Gets the Map of genre name and its value.
     * @return Map of genreName and genreValue
     */
    public Map<String,String> getGenreMap(){
        Map<String,String> genreMap = new LinkedHashMap<String, String>();
        for ( Integer key : genreValueMap.keySet() ){
            genreMap.put(genreNameMap.get(key),genreValueMap.get(key));
        }
        return genreMap;
    }

    /**
     * ランキング種類パラメータの名前と値のMapを取得する<br>
     * Gets the Map of kind param name nad its value.
     * @return Map of kind param name and its value
     */
    public Map<String,String> getKindMap(){
        Map<String,String> kindMap = new LinkedHashMap<String, String>();
        for ( Integer key : kindValueMap.keySet() ){
            kindMap.put(kindNameMap.get(key),kindValueMap.get(key));
        }
        return kindMap;
    }

    /**
     * 期間パラメータの名前と値のMapを取得する<br>
     * Gets the Map of period param name and its value.
     * @return Map of period param name and its value
     */
    public Map<String,String> getPeriodMap(){
        Map<String,String> periodMap = new LinkedHashMap<String, String>();
        for ( Integer key : periodValueMap.keySet() ){
            periodMap.put(periodNameMap.get(key),periodValueMap.get(key));
        }
        return periodMap;
    }

    /**
     * ジャンルを対応する定数で設定します<br>
     * Sets genre param with constant.<br>
     * 定数以外の無効な値を渡すと変更は反映されません。<br>
     * If invalid value except the constant is passed, no change is applied.
     * @param key chosen from GENRE_*****, other value is ignored
     */
    public synchronized void setGenre(int key){
        if ( genreValueMap.containsKey(key) ){
            genre = genreValueMap.get(key);
        }
    }
    /**
     * ジャンルを設定します<br>
     * Sets genre
     * 有効な値は{@link #getGenreMap()}で名称と共に取得できます。<br>
     * You can get valid params with their names in {@link #getGenreMap()}.
     * @param genre invalid value is ignored
     */
    public synchronized void setGenre(String genre){
        if ( genre != null && genreValueMap.containsValue(genre) ){
            this.genre = genre;
        }
    }

    /**
     * ランキング種類パラメータを対応する定数で設定します<br>
     * Sets kind param with constant.<br>
     * 定数以外の無効な値を渡すと変更は反映されません。<br>
     * If invalid value except the constant is passed, no change is applied.
     * @param key chosen from KIND_*****, other value is ignored
     */
    public synchronized void setKind(int key){
        if ( kindValueMap.containsKey(key) ){
            kind = kindValueMap.get(key);
        }
    }

    /**
     * ランキング種類パラメータを設定します<br>
     * Sets kind param
     * 有効な値は{@link #getKindMap()}}で名称と共に取得できます。<br>
     * You can get valid params with their names in {@link #getKindMap()}.
     * @param kind invalid value is ignored
     */
    public synchronized void setKind(String kind){
        if ( kind != null && kindValueMap.containsValue(kind) ){
            this.kind = kind;
        }
    }

    /**
     * ランキング期間パラメータを対応する定数で設定します<br>
     * Sets period param with constant.<br>
     * 定数以外の無効な値を渡すと変更は反映されません。<br>
     * If invalid value except the constant is passed, no change is applied.
     * @param key chosen from PERIOD_*****, other value is ignored
     */
    public synchronized void setPeriod(int key){
        if ( periodValueMap.containsKey(key) ){
            period = periodValueMap.get(key);
        }
    }

    /**
     * ランキング期間パラメータを設定します<br>
     * Sets period param
     * 有効な値は{@link #getPeriodMap()}}で名称と共に取得できます。<br>
     * You can get valid params with their names in {@link #getPeriodMap()}.
     * @param period invalid value is ignored
     */
    public synchronized void setPeriod(String period){
        if ( period != null && periodValueMap.containsValue(period) ){
            this.period = period;
        }
    }

    /**
     * ランキングを取得します<br>
     * Gets ranking from Nico.<br>
     * カテゴリ、期間、種類の3パラメータを設定してから取得できます。
     * 設定をしないとデフォルトでカテゴリ合算、期間合計、総合ランキングとして扱います。
     * このインスタンスは再利用はできません、二度以上呼ぶと例外を投げます。<br>
     * This gets ranking from Nico after you set params; genre, kind and period.
     * If you not set them, this interprets as all category, entire period, overall ranking.
     * You cannot reuse this instance. If called twice again, an exception is thrown.
     * @return Returns {@link RankingVideoGroup} instance with empty List if no hit, not {@code null}
     * @throws NicoAPIException if fail to get or call this twice again
     */
    public RankingVideoGroup getRanking () throws NicoAPIException {
        String kind,period,genre;
        synchronized (this) {
            if (isDone) {
                throw new NicoAPIException.IllegalStateException(
                        "NicoRanking is not reusable > ranking",
                        NicoAPIException.EXCEPTION_ILLEGAL_STATE_RANKING_NON_REUSABLE
                );
            } else {
                isDone = true;
            }
            kind = this.kind;
            period = this.period;
            genre = this.genre;
            if (kind.isEmpty()) {
                kind = kindValueMap.get(KIND_FAV);
            }
        }
        HttpClient client = ResourceStore.getInstance().getHttpClient();
        String path = String.format(rankingUrl, kind, period, genre);
        if ( client.get(path,null) ) {
            return new RankingVideoGroup(cookieGroup,client.getResponse(),genre,period,kind);
        }else{
            throw new NicoAPIException.HttpException(
                    "fail to get ranking",
                    NicoAPIException.EXCEPTION_HTTP_RANKING,
                    client.getStatusCode(), path, "GET"
            );
        }
    }

    /**
     * ランキング検索結果を保持するクラスです<br>
     * This class object contains the result of ranking search.<br><br>
     * ランキング検索に用いたパラメータと取得した動画のリストを保持します。<br>
     * This keeps the three parameters used to get ranking and result-videos in List.
     */
    public static class RankingVideoGroup {

        private String genre;
        private String period;
        private String kind;
        private Date pubDate;
        private List<VideoInfo> videoList;

        private RankingVideoGroup (CookieGroup cookieGroup, String xml, String genre, String period, String kind) throws NicoAPIException{
            this.genre = genre;
            this.kind = kind;
            this.period = period;
            Matcher matcher = ResourceStore.getInstance().getPattern(R.string.regex_rss_ranking_meta).matcher(xml);
            if ( matcher.find() ) {
                this.pubDate = convertPubDate(matcher.group(1));
                this.videoList = RSSVideoInfo.parse(cookieGroup,xml);
            }else{
                throw new NicoAPIException.APIUnexpectedException(
                        "not ranking RSS > ranking",
                        NicoAPIException.EXCEPTION_UNEXPECTED_RANKING
                );
            }
        }
        private Date convertPubDate (String date) throws NicoAPIException.ParseException{
            try{
                SimpleDateFormat dateFormat = new SimpleDateFormat(ResourceStore.getInstance().getString(R.string.format_rss_publish_date), Locale.US);
                return dateFormat.parse(date);
            }catch (ParseException e){
                throw new NicoAPIException.ParseException(e.getMessage(), date,NicoAPIException.EXCEPTION_PARSE_RANKING_MYLIST_PUB_DATE);
            }
        }
        /**
         * 検索に使用したカテゴリ（ジャンル）パラメータの値を取得します<br>
         * Gets category/genre parameter used to get ranking results.
         * @return the category parameter
         */
        public String getGenre(){
            return genre;
        }
        /**
         * 検索に使用した対象期間パラメータの値を取得します<br>
         * Gets target period parameter used to get ranking results.
         * @return the period parameter
         */
        public String getPeriod(){
            return period;
        }
        /**
         * 検索に使用したランキング種類パラメータの値を取得します<br>
         * Gets ranking kind parameter used to get ranking results.
         * @return the kind parameter
         */
        public String getKind(){
            return kind;
        }
        /**
         * ランキングの発表日時を取得します<br>
         *     Gets ranking published date.<br>
         * @return the publication date
         */
        public Date getPubDate(){
            return pubDate;
        }
        /**
         * ランキング検索で得た動画を取得します<br>
         * Gets the videos gotten from ranking search.<br>
         * 返り値である動画を格納する{@code List}オブジェクトに変更を加えても問題はありません。
         * Making changes to {@code List} object returned does not matter.
         * @return the videos in {@code List}
         */
        public List<VideoInfo> getVideoList(){
            List<VideoInfo> list = new ArrayList<VideoInfo>();
            for ( VideoInfo info : videoList ){
                list.add(info);
            }
            return list;
        }
    }
}
