package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ニコ動のランキングを取得する<br>
 * This gets ranking search in Nico.<br><br>
 *
 * ランキング検索にはカテゴリ、期間、種類の3パラメータが必要です。
 * これらパラメータを各種適当なメソッドを呼び設定してからランキングを取得します。
 * 各パラメータに対応した定数が用意されているので、これを指定することができます。
 * また、各パラメータの名前と値のMapを取得してその値で指定することも可能です。
 * 例）ジャンルパラメータ ("カテゴリ合算","all")... 　#この"all"またはこれに対応した定数GENRE_ALLを#setGenre(String/int)に渡す。
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

public class NicoRanking extends HttpResponseGetter {

    private String rankingUrl = "http://www.nicovideo.jp/ranking/%s/%s/%s?rss=2.0";

    private String genre;
    private String kind;
    private String period;

    private boolean isDone;

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

    private Map<Integer,String> genreNameMap = new LinkedHashMap<Integer, String>(){
        {
            put(GENRE_ALL, "カテゴリ合算");
            put(GENRE_VOCALOID, "VOCALOID");
            put(GENRE_MUSIC, "音楽");
            put(GENRE_ANIME, "アニメ");
            put(GENRE_GAME, "ゲーム");
            put(GENRE_DANCE, "踊ってみた");
            put(GENRE_SING, "歌ってみた");
            put(GENRE_PLAY, "演奏してみた");
            put(GENRE_TOHO, "東方");
            put(GENRE_IMAS, "アイドルマスター");
            put(GENRE_ENT, "エンターテイメント");
            put(GENRE_ANIMAL, "動物");
            put(GENRE_RADIO, "ラジオ");
            put(GENRE_SPORT, "スポーツ");
            put(GENRE_POLITICS, "政治");
            put(GENRE_SCIENCE, "科学");
            put(GENRE_HISTORY, "歴史");
            put(GENRE_COOKING, "料理");
            put(GENRE_NATURE, "自然");
            put(GENRE_DIARY, "日記");
            put(GENRE_LECTURE, "ニコニコ動画講座");
            put(GENRE_NICOINDIES, "ニコニコインディーズ");
            put(GENRE_DRIVE, "車載動画");
            put(GENRE_TECH, "ニコニコ技術部");
            put(GENRE_HANDCRAFT, "ニコニコ手芸部");
            put(GENRE_MAKE, "作ってみた");
            put(GENRE_DRAW, "描いてみた");
            put(GENRE_TRAVEL, "旅行");
            put(GENRE_ARE, "例のアレ");
            put(GENRE_OTHER, "その他");
        }
    };

    private Map<Integer,String> genreValueMap = new LinkedHashMap<Integer, String>(){
        {
            put(GENRE_ALL, "all");
            put(GENRE_VOCALOID, "vocaloid");
            put(GENRE_MUSIC, "music");
            put(GENRE_ANIME, "anime");
            put(GENRE_GAME, "game");
            put(GENRE_DANCE, "dance");
            put(GENRE_SING, "sing");
            put(GENRE_PLAY, "play");
            put(GENRE_TOHO, "toho");
            put(GENRE_IMAS, "imas");
            put(GENRE_ENT, "ent");
            put(GENRE_ANIMAL, "animal");
            put(GENRE_RADIO, "radio");
            put(GENRE_SPORT, "sport");
            put(GENRE_POLITICS, "politics");
            put(GENRE_SCIENCE, "science");
            put(GENRE_HISTORY, "history");
            put(GENRE_COOKING, "cooking");
            put(GENRE_NATURE, "nature");
            put(GENRE_DIARY, "diary");
            put(GENRE_LECTURE, "lecture");
            put(GENRE_NICOINDIES, "nicoindies");
            put(GENRE_DRIVE, "drive");
            put(GENRE_TECH, "tech");
            put(GENRE_HANDCRAFT, "handcraft");
            put(GENRE_MAKE, "make");
            put(GENRE_DRAW, "draw");
            put(GENRE_TRAVEL, "travel");
            put(GENRE_ARE, "are");
            put(GENRE_OTHER, "other");
        }
    };

    private Map<Integer,String> periodNameMap = new LinkedHashMap<Integer, String>(){
        {
            put(PERIOD_TOTAL,"合計");
            put(PERIOD_MONTHLY,"月間");
            put(PERIOD_WEEKLY,"週間");
            put(PERIOD_DAILY,"24時間");
            put(PERIOD_HOURLY,"毎時");
        }
    };

    private Map<Integer,String> periodValueMap = new LinkedHashMap<Integer, String>(){
        {
            put(PERIOD_TOTAL,"total");
            put(PERIOD_MONTHLY,"monthly");
            put(PERIOD_WEEKLY,"weekly");
            put(PERIOD_DAILY,"daily");
            put(PERIOD_HOURLY,"hourly");
        }
    };

    private Map<Integer,String> kindNameMap = new LinkedHashMap<Integer, String>(){
        {
            put(KIND_FAV,"総合");
            put(KIND_VIEW,"再生");
            put(KIND_RES,"コメント");
            put(KIND_MYLIST,"マイリスト");
        }
    };

    private Map<Integer,String> kindValueMap = new LinkedHashMap<Integer, String>(){
        {
            put(KIND_FAV,"fav");
            put(KIND_VIEW,"view");
            put(KIND_RES,"res");
            put(KIND_MYLIST,"mylist");
        }
    };

    protected NicoRanking (){
        genre = "";
        period = "";
        kind = "";
        isDone = false;
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
     * @return Returns empty List if no hit, not {@code null}
     * @throws NicoAPIException if fail to get or call this twice again
     */
    public synchronized List<VideoInfo> get () throws NicoAPIException {
        if ( isDone ){
            throw new NicoAPIException.IllegalStateException("NicoRanking is not reusable > ranking", NicoAPIException.EXCEPTION_ILLEGAL_STATE_RANKING_NON_REUSABLE);
        }else{
            isDone = true;
        }
        if (  kind.isEmpty() ){
            kind = kindValueMap.get(KIND_FAV);
        }
        String path = String.format(rankingUrl, kind, period, genre);
        tryGet(path);
        return RankingVideoInfo.parse(super.response, genre, period, kind);
    }

    public synchronized String getGenre(){
        return genre;
    }
    public synchronized String getKind(){
        return kind;
    }
    public synchronized String getPeriod(){
        return period;
    }
}
