package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import java.io.Serializable;

/**
 * 動画情報をIntentで渡すためのクラスです<br>
 * this class can be passed with Intent.<br>
 *  Activity間をIntentで{@link VideoInfo}の子クラスは渡せません。
 * {@link VideoInfo#pack()}で取得したこのクラスを代わりに渡してください。
 * 渡した先で{@link #unpack()}を呼ぶことで{@link VideoInfoManager 子クラス}のメソッドが使えます。<br>
 * child classes of {@link VideoInfo} can not be passed between Activities with Intent.
 * substitute with this class instance gotten from {@link VideoInfo#pack()}.
 * @author Seo-4d696b75
 * @version on 2017/01/26.
 */

public class VideoInfoPackage implements Serializable {

    private String genre;
    private String rankKind;
    private String period;
    private String pubDate;
    private String title;
    private String id;
    private String date;
    private String description;
    private String[] thumbnailUrl;
    private int length = -1;
    private int viewCounter = -1;
    private int commentCounter = -1;
    private int myListCounter = -1;
    private String[] tags;
    private float point = 0f;

    /**
     * Intentで渡せるようにSerializableなインスタンスを返します<br>
     * convert into serializable instance so that it can be pass with Intent.
     * @param info Returns serializable
     */
    protected VideoInfoPackage (VideoInfo info){
        if ( info == null ){
            //TODO exception
        }else{
            genre = info.genre;
            rankKind = info.rankKind;
            period = info.period;
            pubDate = info.pubDate;
            title = info.title;
            id = info.id;
            date = info.date;
            description = info.description;
            thumbnailUrl = info.getThumbnailUrlArray();
            length = info.length;
            viewCounter = info.viewCounter;
            commentCounter = info.commentCounter;
            myListCounter = info.myListCounter;
            tags = info.getTags();
            point = info.point;
        }
    }

    /**
     * {@link VideoInfoManager 子クラス}のメソッドが使えるように変換します<br>
     * convert this class so that methods of {@link VideoInfoManager child class} can be used.
     * @return Returns instance keeping all the fields
     */
    public VideoInfoManager unpack(){
        VideoInfoManager info = new VideoInfoManager();
        info.genre = this.genre;
        info.rankKind = this.rankKind;
        info.period = this.period;
        info.pubDate = this.pubDate;
        info.title = this.title;
        info.id = this.id;
        info.date = this.date;
        info.description = description;
        info.setThumbnailUrl(this.thumbnailUrl);
        info.length = this.length;
        info.viewCounter = this.viewCounter;
        info.commentCounter = this.commentCounter;
        info.myListCounter = this.myListCounter;
        info.setTags(tags);
        return info;
    }
}
