package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import java.io.Serializable;

/**
 * 動画情報をIntentで渡すためのクラスです<br>
 * This class can be passed with Intent.<br>
 *  Activity間をIntentで{@link VideoInfo}の子クラスは渡せません。
 * {@link VideoInfo#pack()}で取得したこのクラスを代わりに渡してください。
 * 渡した先で{@link #unpack()}を呼ぶことで{@link VideoInfo 子クラス}のメソッドが使えます。<br>
 * Child classes of {@link VideoInfo} can not be passed between Activities with Intent.
 * You substitute with this class instance gotten from {@link VideoInfo#pack()}.
 * @author Seo-4d696b75
 * @version on 2017/01/26.
 */

public class VideoInfoPackage implements Serializable {

    protected String genre;
    protected String rankKind;
    protected String period;
    protected String pubDate;
    protected String title;
    protected String id;
    protected String date;
    protected String description;
    protected String[] thumbnailUrl;
    protected int length = -1;
    protected int viewCounter = -1;
    protected int commentCounter = -1;
    protected int myListCounter = -1;
    protected String[] tags;
    protected int threadID;
    protected String messageServerURL;
    protected String flvURL;
    protected float point = 0f;

    public static final String INTENT_KEY = "videoPackage";

    /**
     * Intentで渡せるようにSerializableなインスタンスを返します<br>
     * Converts into serializable instance so that it can be pass with Intent.
     * @param info the target video, cannot be {@code null}
     * @throws NicoAPIException if argument is {@code null}
     */
    protected VideoInfoPackage (VideoInfoStorage info) throws NicoAPIException{
        if ( info == null ){
            throw new NicoAPIException.InvalidParamsException("no target video > pack");
        }else{
            genre = info.genre;
            rankKind = info.rankKind;
            period = info.period;
            pubDate = info.pubDate;
            title = info.title;
            id = info.id;
            date = info.date;
            description = info.description;
            try {
                thumbnailUrl = info.getThumbnailUrlArray();
            }catch(NicoAPIException.NotInitializedException e){
                thumbnailUrl = null;
            }
            length = info.length;
            viewCounter = info.viewCounter;
            commentCounter = info.commentCounter;
            myListCounter = info.myListCounter;
            try {
                tags = info.getTags();
            }catch ( NicoAPIException.NotInitializedException e){
                tags = null;
            }
            point = info.point;
            threadID = info.threadID;
            messageServerURL = info.messageServerUrl;
            flvURL = info.flvUrl;
        }
    }

    /**
     * {@link VideoInfo 子クラス}のメソッドが使えるように変換します<br>
     * Converts itself so that methods of {@link VideoInfo child class} can be used.
     * @return Returns instance keeping all the fields
     */
    public VideoInfo unpack(){
        VideoInfo info = new VideoInfo(this);
        return info;
    }
}
