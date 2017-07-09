package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * このクラスはとりあえずマイリストを保持・管理します
 * This class stores and manages temp myList.<br>
 * このオブジェクトが一ユーザのとりあえずマイリスに対応します。
 * ログインした状態で{@link NicoClient#getTempMyList()}から取得できます。
 * とりあえずマイリスに登録した動画の取得やとりあえずマイリスへ変更を加えるには、
 * このオブジェクトを操作します。<br>
 * This object corresponds to one user's temp myList.
 * You can get this at {@link NicoClient#getTempMyList()} with login session.
 * When you get videos belonging to this myList or make changes to the myList,
 * you call appropriate methods in this.<br>
 * @author Seo-4d696b75
 * @version 0.0 on 2017/02/28.
 */

public class TempMyListVideoGroup extends MyListEditor implements Parcelable{

    /**
     * とりあえずマイリスのオブジェクトを取得します
     * Gets object corresponding to temp myList.<br>
     * ログイン状態の{@link LoginInfo}オブジェクトを渡します。
     * <strong>ＵＩスレッド禁止</strong> HTTP通信を行うのでバックグランドで処理して下さい。<br>
     * Pass {@link LoginInfo} with login session.
     * <strong>No UI thread</strong>: HTTP communication is done
     * @param cookieGroup the login session
     * @param userID
     * @throws NicoAPIException if not login or fail to get temp myList
     */
    protected TempMyListVideoGroup(CookieGroup cookieGroup, int userID) throws NicoAPIException{
        super(cookieGroup);
        this.userID = userID;
        loadVideos();
    }

    private int userID;
    private List<MyListVideoInfo> videoInfoList;

    /* <implementation of parcelable> */

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(cookieGroup, flags);
        out.writeInt(userID);
        out.writeList(videoInfoList);
    }

    public static final Parcelable.Creator<TempMyListVideoGroup> CREATOR = new Parcelable.Creator<TempMyListVideoGroup>() {
        public TempMyListVideoGroup createFromParcel(Parcel in) {
            return new TempMyListVideoGroup(in);
        }
        public TempMyListVideoGroup[] newArray(int size) {
            return new TempMyListVideoGroup[size];
        }
    };

    private TempMyListVideoGroup(Parcel in) {
        super((CookieGroup)in.readParcelable(CookieGroup.class.getClassLoader()));
        this.userID = in.readInt();
        this.videoInfoList = new ArrayList<>();
        in.readList(this.videoInfoList,List.class.getClassLoader());
    }

    /**
     * とりあえずマイリスのユーザＩＤを取得します
     * @return the user ID
     */
    public int getUserID(){
        return userID;
    }

    /**
     * とりあえずマイリスに登録されている動画を取得します
     * Gets videos belonging to this temp myList.<br>
     * 返り値の動画を格納した{@code List}オブジェクトに変更を加えても問題はありません。
     * ただし、とりあえずマイリスに変更を加えても先に取得したこのリストおよび動画オブジェクトには反映されません。
     * 再取得してください。<br>
     * Making changes to returned {@code List} object does not matter.
     * But, when any change is made to this temp myList, the change is not applied to the List and its video objects.
     *
     * @return the videos in temp myList
     */
    public synchronized List<MyListVideoInfo> getVideos(){
        List<MyListVideoInfo> list = new ArrayList<MyListVideoInfo>();
        for (MyListVideoInfo info : videoInfoList) {
            list.add(info);
        }
        return list;
    }

    private void loadVideos() throws NicoAPIException{
        ResourceStore res = ResourceStore.getInstance();
        HttpClient client = res.getHttpClient();
        String tempMyListUrl = res.getURL(R.string.url_tempList_load);
        if ( client.get(tempMyListUrl, cookieGroup) ){
            JSONObject root = checkStatusCode(client.getResponse());
            List<MyListVideoInfo> list = NicoMyListVideoInfo.parse(cookieGroup,root);
            synchronized ( this ) {
                this.videoInfoList = list;
            }
        }else{
            throw new NicoAPIException.HttpException(
                    "HTTP failure > tempMyList",
                    NicoAPIException.EXCEPTION_HTTP_TEMP_MYLIST_GET,
                    client.getStatusCode(), tempMyListUrl, "GET"
            );
        }
    }

    /**
     * とりあえずマイリストに動画を追加登録します　Adds new video to this temp myList.<br>
     * 対象の動画と登録時に添えるコメント・説明を渡します。
     * 説明は空文字でも可能ですが、{@code null}は渡せません。
     * すでに登録済みの動画を追加することはできず、例外を投げます。<br>
     * You pass target video and comment/description about it.
     * The description can be empty {@code String}, but cannot be {@code null}.
     * That video which already exists in this temp myList cannot be added, or an exception is thrown.<br>
     * <strong>ＵＩスレッド禁止</strong> HTTP通信を行うのでバックグランドで処理して下さい。<br>
     * <strong>No UI thread</strong>: HTTP communication is done<br>
     * @param target the target video
     * @param description the description with the video
     * @throws NicoAPIException if fail to add the video
     */
    public void add(VideoInfo target, String description) throws NicoAPIException{
        addVideo(
                target,description,null,
                ResourceStore.getInstance().getURL(R.string.url_tempList_add)
        );
        loadVideos();
    }

    /**
     * 登録動画の説明文を編集します Edits the description of registered video.<br>
     * 説明は空文字でも可能ですが、{@code null}は渡せません。
     * とりあえずマイリスに存在しない動画を渡すと例外を投げます。<br>
     * The description can be empty {@code String}, but cannot be {@code null}.
     * If video which does not exist in this temp myList is passed, an exception is thrown.<br>
     * <strong>ＵＩスレッド禁止</strong> HTTP通信を行うのでバックグランドで処理して下さい。<br>
     * <strong>No UI thread</strong>: HTTP communication is done<br>
     * @param target the target video
     * @param description the new description
     * @throws NicoAPIException if fail to update
     */
    public void update(MyListVideoInfo target, String description) throws NicoAPIException{
        updateVideo(
                target,description,null,
                ResourceStore.getInstance().getURL(R.string.url_tempList_update)
        );
        loadVideos();
    }

    /**
     * 動画をとりあえずマイリスから削除します　Deletes video of this temp myList.<br>
     * 対象の動画を渡します。とりあえずマイリスに存在しない動画は渡せず、例外を投げます。<br>
     * Pass the target video. That video which does not exist in this temp myList cannot be passed,
     * or an exception is thrown.<br>
     * <strong>ＵＩスレッド禁止</strong> HTTP通信を行うのでバックグランドで処理して下さい。<br>
     * <strong>No UI thread</strong>: HTTP communication is done<br>
     * @param video the target
     * @throws NicoAPIException if fail to delete the target
     */
    public void delete(MyListVideoInfo video) throws NicoAPIException {
        delete(new MyListVideoInfo[]{video});
    }
    /**
     * @deprecated API response is ambiguous if try to delete plural videos at once.
     * @param videoList
     * @throws NicoAPIException
     */
    public void delete(MyListVideoInfo[] videoList) throws NicoAPIException{
        deleteVideo(
                videoList,null,
                ResourceStore.getInstance().getURL(R.string.url_tempList_delete)
        );
        loadVideos();
    }

    /**
     * 動画を別のマイリスに移動します
     * Moves videos to another myList.<br>
     * 対象動画を配列で渡し、一度に複数の動画をまとめて移動できます。
     * ただし、とりあえずマイリスに存在しない動画は移動させられず、例外を投げます。
     * また、他ユーザのマイリスへ移動させることもできません。<br>
     * Pass target videos in array and plural videos can be moved at once.
     * However those videos which do not exist in this temp myList cannot be moved, or an exception is thrown.
     * Also moving any video to other user's myList is not possible.<br>
     * <strong>ＵＩスレッド禁止</strong> HTTP通信を行うのでバックグランドで処理して下さい。<br>
     * <strong>No UI thread</strong>: HTTP communication is done<br>
     * @param videoList the target videos
     * @param target the target myList
     * @throws NicoAPIException if fail to move the targets
     */
    public void move(MyListVideoInfo[] videoList, MyListVideoGroup target) throws NicoAPIException{
        moveVideo(
                videoList,null,target,
                ResourceStore.getInstance().getURL(R.string.url_tempList_move)
        );
        loadVideos();
        target.loadVideos();
    }

    /**
     * 動画を別のマイリスに複製します
     * Copies videos to another myList.<br>
     * 対象動画を配列で渡し、一度に複数の動画をまとめて複製できます。
     * ただし、とりあえずマイリスに存在しない動画は複製できず、例外を投げます。
     * また、他ユーザのマイリスへ複製することもできません。<br>
     * Pass target videos in array and plural videos can be copied at once.
     * However those videos which do not exist in this temp myList cannot be copied, or an exception is thrown.
     * Also copying any video to other user's myList is not possible.<br>
     * <strong>ＵＩスレッド禁止</strong> HTTP通信を行うのでバックグランドで処理して下さい。<br>
     * <strong>No UI thread</strong>: HTTP communication is done<br>
     * @param videoList the target videos
     * @param target the target myList
     * @throws NicoAPIException if fail to copy the targets
     */
    public void copy(MyListVideoInfo[] videoList, MyListVideoGroup target) throws NicoAPIException{
        copyVideo(
                videoList,null,target,
                ResourceStore.getInstance().getURL(R.string.url_tempList_copy)
        );
        target.loadVideos();
    }

}
