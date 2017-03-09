package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * このクラスはマイリストグループを保持・管理します
 * This class contains and manages myListGroup.<br>
 * このオブジェクトが１ユーザのマイリスグループに対応し、
 * ログイン状態で{@link NicoClient#getMyListGroup()}を呼ぶことで得られます。
 * グループに所属するマイリスの取得やマイリスグループに変更を加えるには、このオブジェクトを操作します。<br>
 * This object corresponds to myList of a user, and this object can be gotten at {@link NicoClient#getMyListGroup()}.
 * When myLists belonging to the myListGroup want to be gotten or any change want to be made to the myListGroup,
 * you call appropriate methods of this object.
 *
 * @author Seo-4d696b75
 * @version 0.0 on 2017/02/28.
 */

public class MyListGroup extends MyListEditor implements Parcelable {

    protected MyListGroup(LoginInfo info) throws NicoAPIException{
        super.info = info;
        loadGroups();
    }

    private final String rootURL = "http://www.nicovideo.jp/api/mylistgroup/";
    private List<MyListVideoGroup> groupList;

    /* <implementation of parcelable> */

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(super.info, flags);
        out.writeList(groupList);
    }

    public static final Parcelable.Creator<MyListGroup> CREATOR = new Parcelable.Creator<MyListGroup>() {
        public MyListGroup createFromParcel(Parcel in) {
            return new MyListGroup(in);
        }
        public MyListGroup[] newArray(int size) {
            return new MyListGroup[size];
        }
    };

    private MyListGroup(Parcel in) {
        super.info = in.readParcelable(LoginInfo.class.getClassLoader());
        this.groupList = in.readArrayList(ArrayList.class.getClassLoader());
    }

    /**
     * 登録されているマイリスを取得します　Gets myLists registered in the user's myListGroup.<br>
     * {@link MyListVideoGroup マイリス}を格納した{@code List}のオブジェクトを変更しても問題はありません。
     * ただし、マイリスグループに変更を加えても先に取得したこの{@code List}オブジェクトに変更は反映されません。
     * {@link #add(String, boolean, int, String) マイリスの追加}、
     * {@link #update(MyListVideoGroup, String, boolean, int, String)}  マイリス設定の編集}、
     * {@link #delete(MyListVideoGroup) マイリスの削除}　を行った後に再取得してください。<br>
     *  Making changes to {@code List} containing {@link MyListVideoGroup myLists} does not matter.
     *  Be careful that, when making any change to myListGroup, the change is not applied to this {@code List} Object.
     *  After {@link #add(String, boolean, int, String) adding new myList},
     * {@link #update(MyListVideoGroup, String, boolean, int, String)}  editing myList setting},
     * {@link #delete(MyListVideoGroup) deleting a myList}, it should be gotten again.
     * @return Returns empty {@code List} if no myList is registered, not {@code null}
     */
    public synchronized List<MyListVideoGroup> getMyListVideoGroup() {
        List<MyListVideoGroup> list = new ArrayList<MyListVideoGroup>();
        for (MyListVideoGroup group : groupList) {
            list.add(group);
        }
        return list;
    }
    public synchronized MyListVideoGroup getMyListVideoGroup(int id) throws NicoAPIException{
        for ( MyListVideoGroup group : groupList){
            if ( group.getMyListID() == id ){
                return group;
            }
        }
        throw new NicoAPIException.InvalidParamsException(
                "target myListVideoGroup not found",
                NicoAPIException.EXCEPTION_PARAM_MYLIST_GROUP_ID
        );
    }


    private void loadGroups () throws NicoAPIException{
        String myListGroupUrl = rootURL + "list";
        if ( tryGet(myListGroupUrl,info.getCookieStore()) ){
            JSONObject root = checkStatusCode(super.response);
            List<MyListVideoGroup> list = MyListVideoGroup.parse(root, info);
            synchronized (this) {
                if (groupList != null) {
                    //マイリスグループのリストがセット済みならなるべく元のオブジェクトを維持して差分のみ変更する
                    for (Iterator<MyListVideoGroup> iterator = groupList.iterator(); iterator.hasNext(); ) {
                        MyListVideoGroup before = iterator.next();
                        boolean isFind = false;
                        for (Iterator<MyListVideoGroup> iteratorAfter = list.iterator(); iteratorAfter.hasNext(); ) {
                            MyListVideoGroup after = iteratorAfter.next();
                            if (after.getMyListID() == before.getMyListID()) {
                                isFind = true;
                                synchronized (before) {
                                    before.name = after.name;
                                    before.description = after.description;
                                    before.isPublic = after.isPublic;
                                    before.defaultSort = after.defaultSort;
                                    before.createDate = after.createDate;
                                    before.updateDate = after.updateDate;
                                    before.iconID = after.iconID;
                                }
                                iteratorAfter.remove();
                                break;
                            }
                        }
                        if (!isFind) {
                            iterator.remove();
                        }
                    }
                    if (!list.isEmpty()) {
                        groupList.addAll(list);
                    }
                } else {
                    groupList = list;
                }
            }

        } else {
            throw new NicoAPIException.HttpException(
                    "HTTP failure > myList group",
                    NicoAPIException.EXCEPTION_HTTP_MYLIST_GROUP_GET,
                    super.statusCode, myListGroupUrl, "GET"
            );
        }
    }

    /**
     * マイリスを追加します Adds new myList.<br>
     * マイリスの名前は既存の名前と重複可能かつ空文字も可能です。
     * 公開設定を{@code true}に設定すると他ユーザがマイリスＩＤから参照できますが、{@code false}に設定すよう推奨されます。
     * マイリスの説明は空文字でも可能ですが{@code null}は渡せません。
     * ソートパラメータはマイリス内の動画を{@link #sort(MyListVideoGroup[]) 整列させる}ときの方法を規定し、
     * 定数MYLIST_SORT_****から選べます。用意された定数以外の値は初期値のMYLIST_SORT_REGISTER_LATEとして扱います。<br>
     * The myList name can be empty {@code String} and can duplicate that of existing myList.
     * If myList is set public, any user can be access this myList with its myListID.
     * The myList description can be empty {@code String}, but cannot be {@code null}.
     * The sort param defines how to sort videos in target myList at {@link #sort(MyListVideoGroup[])},
     * can be chosen from these constants; MYLIST_SORT_****.
     * If any other value different from these constants is passed, it is interpreted to be MYLIST_SORT_REGISTER_LATE as default.<br>
     * <strong>ＵＩスレッド禁止</strong> HTTP通信を行うのでバックグランドで処理して下さい。<br>
     * <strong>No UI thread</strong>: HTTP communication is done<br>
     * @param name the myList name, can be empty {@code String}, but cannot be {@code null}
     * @param isPublic whether or not the myList is public
     * @param sort the sort param
     * @param description the myList description, can be empty {@code String}, but cannot be {@code null}
     * @throws NicoAPIException if invalid param is passed or fail to add the new myList
     */
    public void add (String name, boolean isPublic, int sort, String description) throws NicoAPIException{
        if ( name == null ){
            throw new NicoAPIException.InvalidParamsException(
                    "name is null > add - myList",
                    NicoAPIException.EXCEPTION_PARAM_MYLIST_NAME
            );
        }
        if ( description == null ){
            throw new NicoAPIException.InvalidParamsException(
                    "description is null > add - myList",
                    NicoAPIException.EXCEPTION_PARAM_MYLIST_DESCRIPTION
            );
        }
        if ( sort < 0 || sort > 17 ){
            sort = MYLIST_SORT_REGISTER_LATE;
        }
        String path = rootURL + "add";
        String publicValue = "0";
        if ( isPublic ){
            publicValue = "1";
        }
        Map<String,String> params = new HashMap<String, String>();
        params.put("token", getToken(getVideoID()));
        params.put("name", name);
        params.put("public", publicValue);
        params.put("default_sort", String.valueOf(sort));
        params.put("icon_id","0");
        params.put("description", description);
        if ( tryPost(path,params,info.getCookieStore()) ){
            checkStatusCode(super.response);
            loadGroups();
        }else{
            throw new NicoAPIException.HttpException(
                    "http failure",
                    NicoAPIException.EXCEPTION_HTTP_MYLIST_ADD,
                    statusCode,path,"POST");
        }
    }

    /**
     * マイリスの設定を編集します　Edits myList setting.<br>
     * 対象のマイリスト新しい設定値を渡します。各値の内容は{@link #add(String, boolean, int, String) 追加時}と同様です。
     * ただし、設定を変更しない項目に関しては文字列なら{@code null}を、整数値なら負数を渡せます。<br>
     * Pass the target myList and new setting values. The parameters are the same as those passed to {@link #add(String, boolean, int, String)}.
     * But, when you want to make no change to some values, you can pass {@code null} in String or negative in Integer as these values.<br>
     * <strong>ＵＩスレッド禁止</strong> HTTP通信を行うのでバックグランドで処理して下さい。<br>
     * <strong>No UI thread</strong>: HTTP communication is done<br>
     * @param group the target myList, cannot be {@code null}
     * @param name the new name of myList, can be {@code null}
     * @param isPublic the new setting whether myList is public or not
     * @param sort the new sort param, negative is ignored
     * @param description the new description, can be {@code null}
     * @throws NicoAPIException if fail to update the target
     */
    public void update (MyListVideoGroup group,
                        String name,
                        boolean isPublic,
                        int sort,
                        String description) throws NicoAPIException{
        if ( group == null ){
            throw new NicoAPIException.InvalidParamsException(
                    "no target myList > update - myList",
                    NicoAPIException.EXCEPTION_PARAM_MYLIST_TARGET_MYLIST
            );
        }
        String path = rootURL + "update";
        String publicValue = "0";
        if ( isPublic ){
            publicValue = "1";
        }
        Map<String,String> params = new HashMap<String, String>();
        params.put("group_id",String.valueOf(group.getMyListID()) );
        params.put("token", getToken(getVideoID()));
        if ( name != null ) {
            params.put("name", name);
        }
        params.put("public", publicValue);
        if ( sort >=0 && sort <= 17 ) {
            params.put("default_sort", String.valueOf(sort));
        }
        //params.put("icon_id","0");
        if ( description != null ) {
            params.put("description", description);
        }
        if ( tryPost(path,params,info.getCookieStore()) ){
            checkStatusCode(super.response);
            loadGroups();
        }else{
            throw new NicoAPIException.HttpException(
                    "http failure",
                    NicoAPIException.EXCEPTION_HTTP_MYLIST_UPDATE,
                    statusCode,path,"POST");
        }
    }

    /**
     * マイリスを削除します Deletes a myList.<br>
     * <strong>ＵＩスレッド禁止</strong> HTTP通信を行うのでバックグランドで処理して下さい。<br>
     * <strong>No UI thread</strong>: HTTP communication is done<br>
     * @param group the target myList
     * @throws NicoAPIException if fail to delete the target
     */
    public void delete (MyListVideoGroup group) throws NicoAPIException{
        if ( group == null ){
            throw new NicoAPIException.InvalidParamsException(
                    "no target myList > delete - myList",
                    NicoAPIException.EXCEPTION_PARAM_MYLIST_TARGET_MYLIST
            );
        }
        String path = rootURL + "delete";
        Map<String,String> params = new HashMap<String, String>();
        params.put("group_id",String.valueOf(group.getMyListID()) );
        params.put("token", getToken(getVideoID()));
        if ( tryPost(path,params,info.getCookieStore()) ){
            checkStatusCode(super.response);
            loadGroups();
        }else{
            throw new NicoAPIException.HttpException(
                    "http failure",
                    NicoAPIException.EXCEPTION_HTTP_MYLIST_DELETE,
                    statusCode,path,"POST");
        }
    }

    public static final int MYLIST_SORT_REGISTER_EARLY = 0;
    public static final int MYLIST_SORT_REGISTER_LATE = 1;
    public static final int MYLIST_SORT_MEMO_UP = 2;
    public static final int MYLIST_SORT_MEMO_DOWN = 3;
    public static final int MYLIST_SORT_TITLE_UP = 4;
    public static final int MYLIST_SORT_TITLE_DOWN = 5;
    public static final int MYLIST_SORT_CONTRIBUTION_LATE = 6;
    public static final int MYLIST_SORT_CONTRIBUTION_EARLY = 7;
    public static final int MYLIST_SORT_VIEW_DOWN = 8;
    public static final int MYLIST_SORT_VIEW_UP = 9;
    public static final int MYLIST_SORT_COMMENT_NEW = 10;
    public static final int MYLIST_SORT_COMMENT_OLD = 11;
    public static final int MYLIST_SORT_COMMENT_DOWN = 12;
    public static final int MYLIST_SORT_COMMENT_UP = 13;
    public static final int MYLIST_SORT_MYLIST_DOWN = 14;
    public static final int MYLIST_SORT_MYLIST_UP = 15;
    public static final int MYLIST_SORT_LENGTH_DOWN = 16;
    public static final int MYLIST_SORT_LENGTH_UP = 17;

    /**
     * マイリス内の動画をソートします　Sorts videos in myList.<br>
     * このとき動画を整列する方法は各マイリスのソートパラメータで規定されます。
     * パラメータは{@link #update(MyListVideoGroup, String, boolean, int, String)　変更できます}。
     * 同時に複数のマイリス内をソートするのも可能ですので、対象マイリスを配列で渡してください。
     * Hot to sort videos is defined by sort parameter of the target myList,
     * which can be changed at {@link #update(MyListVideoGroup, String, boolean, int, String)}.
     * Sorting videos in plural myLists is possible, which can be passed in array.<br>
     * <strong>ＵＩスレッド禁止</strong> HTTP通信を行うのでバックグランドで処理して下さい。<br>
     * <strong>No UI thread</strong>: HTTP communication is done<br>
     * @param groups the target myList, cannot be {@code null} and any element cannot be {@code null}
     * @throws NicoAPIException if fail to sort videos in the target
     */
    public void sort (MyListVideoGroup[] groups) throws NicoAPIException{
        if ( groups == null || groups.length == 0 ){
            throw new NicoAPIException.InvalidParamsException(
                    "no target myList > sort - myList",
                    NicoAPIException.EXCEPTION_PARAM_MYLIST_TARGET_MYLIST
            );
        }
        String path = rootURL + "sort";
        Map<String,String> params = new HashMap<String, String>();
        params.put("token", getToken(getVideoID()));
        for ( int i=0 ; i<groups.length ; i++){
            if ( groups[i] == null ){
                throw new NicoAPIException.InvalidParamsException(
                        "no target myList > sort - myList",
                        NicoAPIException.EXCEPTION_PARAM_MYLIST_TARGET_MYLIST
                );
            }else {
                params.put(String.format("group_id_list[%d]", i), String.valueOf(groups[i].getMyListID()));
            }
        }
        if ( tryPost(path,params,info.getCookieStore()) ){
            checkStatusCode(super.response);
            //対象のマイリスにビデオリストがセット済みなら再読み込みさせる
            for ( MyListVideoGroup group : groups) {
                synchronized (group){
                    if ( group.videoInfoList != null ){
                        group.loadVideos();
                    }
                }
            }
        }else{
            throw new NicoAPIException.HttpException(
                    "http failure",
                    NicoAPIException.EXCEPTION_HTTP_MYLIST_SORT,
                    statusCode,path,"POST");
        }

    }

    private String getVideoID(){
        for ( MyListVideoGroup group : groupList ){
            if ( group.videoInfoList != null && !group.videoInfoList.isEmpty()){
                return group.videoInfoList.get(0).getID();
            }
        }
        try {
            for (MyListVideoGroup group : groupList) {
                group.getVideos();
                if ( !group.videoInfoList.isEmpty() ){
                    return group.videoInfoList.get(0).getID();
                }
            }
        }catch (NicoAPIException e){}
        return super.defaultVideoID;
    }

}
