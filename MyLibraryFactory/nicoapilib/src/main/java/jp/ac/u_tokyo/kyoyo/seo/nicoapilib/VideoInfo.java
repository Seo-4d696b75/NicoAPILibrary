package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.graphics.drawable.Drawable;

import java.io.Serializable;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Seo on 2016/12/17.
 *
 * this class stores information of each video
 *
 * note;
 * this class is serializable, but child classes extending this may be not.
 * when passed with Intent, it is safe to convert them to the instance of this class
 */


public class VideoInfo {

    //in case of ranking
    protected String genre;
    protected String rankKind;
    protected String period;
    protected String pubDate;

    //common
    protected String title;
    protected String id;
    protected String date;
    protected String description;
    protected List<String> thumbnailUrl;
    protected Drawable thumbnail;
    protected int length = -1;

    //statistics of the video
    protected int viewCounter = -1;
    protected int commentCounter = -1;
    protected int myListCounter = -1;

    //tags attributed to the video
    protected List<String> tags;

    protected String threadID;
    protected String messageServerUrl;
    protected String flvUrl;

    protected float point = 0f;

    //contributor
    protected int contributorID = -1;
    protected String contributorName;
    protected String contributorIconUrl;
    protected Drawable contributorIcon;

    public static final int GENRE = 0;
    public static final int RANK_KIND = 1;
    public static final int PERIOD = 2;
    public static final int PUB_DATE = 3;
    public static final int TITLE = 4;
    public static final int ID = 5;
    public static final int DATE = 6;
    public static final int DESCRIPTION = 7;
    public static final int THUMBNAIL_URL = 8;
    public static final int LENGTH = 9;
    public static final int VIEW_COUNTER = 10;
    public static final int COMMENT_COUNTER = 11;
    public static final int MY_LIST_COUNTER = 12;
    public static final int TAGS = 13;
    public static final int TAG = 14;
    public static final int THREAD_ID = 15;
    public static final int MESSAGE_SERVER_URL = 16;
    public static final int FLV_URL = 17;
    public static final int CONTRIBUTOR_ID = 18;
    public static final int CONTRIBUTOR_NAME = 19;
    public static final int CONTRIBUTOR_ICON_URL = 20;

    protected VideoInfo(){}
    public String getString(int key){
        switch ( key ){
            case GENRE:
                return genre;
            case RANK_KIND:
                return rankKind;
            case PERIOD:
                return period;
            case PUB_DATE:
                return pubDate;
            case TITLE:
                return title;
            case ID:
                return id;
            case DATE:
                return date;
            case DESCRIPTION:
                return description;
            case THREAD_ID:
                return threadID;
            case MESSAGE_SERVER_URL:
                return messageServerUrl;
            case FLV_URL:
                return flvUrl;
            case CONTRIBUTOR_NAME:
                return contributorName;
            case CONTRIBUTOR_ICON_URL:
                return contributorIconUrl;
            default:
                return null;
        }
    }
    public int getInt(int key){
        switch ( key ){
            case LENGTH:
                return length;
            case VIEW_COUNTER:
                return viewCounter;
            case COMMENT_COUNTER:
                return commentCounter;
            case MY_LIST_COUNTER:
                return myListCounter;
            case CONTRIBUTOR_ID:
                return contributorID;
            default:
                return 0;
        }
    }
    protected void setTags(String[] tags){
        if ( this.tags == null ){
            this.tags = new ArrayList<String>();
            for ( String tag : tags){
                this.tags.add(tag);
            }
        }
    }
    protected void setTags(List<String> tags){
        if ( tags != null ){
            this.tags = tags;
        }
    }
    public List<String> getTagsList(){
        return tags;
    }
    public String[] getTags(){
        String[] tags = new String[this.tags.size()];
        for ( int i=0 ; i<tags.length ; i++){
            tags[i] = this.tags.get(i);
        }
        return tags;
    }
    protected void setThumbnailUrl (String[] thumbnailUrl){
        this.thumbnailUrl = new ArrayList<String>();
        for ( String url : thumbnailUrl ){
            this.thumbnailUrl.add(url);
        }
    }
    protected void setThumbnailUrl (List<String> thumbnailUrl){
        this.thumbnailUrl = thumbnailUrl;
    }
    protected void setThumbnailUrl(String url){
        if ( thumbnailUrl == null){
            thumbnailUrl = new ArrayList<String>();
        }
        thumbnailUrl.add(url);
    }
    public String getThumbnailUrl (boolean isHigh){
        if ( thumbnailUrl == null || thumbnailUrl.isEmpty() ){
            return null;
        }
        if ( isHigh ){
            return thumbnailUrl.get(thumbnailUrl.size()-1);
        }else{
            return thumbnailUrl.get(0);
        }
    }
    public String getThumbnailUrl (){
        return getThumbnailUrl(false);
    }
    public String[] getThumbnailUrlArray() {
        if ( thumbnailUrl == null || thumbnailUrl.isEmpty() ){
            return null;
        }
        String[] array = new String[thumbnailUrl.size()];
        for ( int i=0 ; i<array.length ; i++){
            array[i] = thumbnailUrl.get(i);
        }
        return array;
    }
    public float getPoint(){
        return point;
    }

    public VideoInfoPackage pack(){
        return new VideoInfoPackage(this);
    }

    //safe down cast
    /*
    public VideoInfoManager downCast(){
        if (  this instanceof VideoInfo ) {
            VideoInfoManager info = new VideoInfoManager();
            info.setString(VideoInfo.GENRE, genre);
            info.setString(VideoInfo.RANK_KIND, rankKind);
            info.setString(VideoInfo.PERIOD, period);
            info.setString(VideoInfo.PUB_DATE, pubDate);
            info.setString(VideoInfo.TITLE, title);
            info.setString(VideoInfo.ID, id);
            info.setString(VideoInfo.DATE, date);
            info.setString(VideoInfo.DESCRIPTION, description);
            info.setThumbnailUrl(thumbnailUrl);
            info.setInt(VideoInfo.LENGTH, length);
            info.setInt(VideoInfo.VIEW_COUNTER, viewCounter);
            info.setInt(VideoInfo.COMMENT_COUNTER, commentCounter);
            info.setInt(VideoInfo.MY_LIST_COUNTER, myListCounter);
            info.setTags(tags);
            return info;
        }
        return (VideoInfoManager)this;
    }*/

}

