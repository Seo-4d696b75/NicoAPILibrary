package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import java.io.Serializable;

/**
 * Created by Seo-4d696b75 on 2017/01/26.
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
    protected float point = 0f;

    public VideoInfoPackage (VideoInfo info){
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
