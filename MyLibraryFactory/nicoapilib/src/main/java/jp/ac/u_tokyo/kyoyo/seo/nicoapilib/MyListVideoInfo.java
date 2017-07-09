package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import java.util.Date;

/**
 * @author Seo-4d696b75
 * @version  0.0 on 2017/06/26.
 */

public interface MyListVideoInfo extends VideoInfo {

    /**
     * この動画がマイリストに追加された日時を取得します Gets when this video was added to the myList.
     * @return Returns date, not {@code null}
     */
    Date getAddDate();

    /**
     * この動画のマイリス登録時に設定した説明文を取得します Gets the description set at the registration.<br>
     * この値は{@link VideoInfo#getDescription()}で取得される動画投稿者が設定した説明文とは異なり、
     * この動画をマイリスに登録したユーザが自由に設定できる説明文です。
     * 動画登録後もマイリス制作ユーザが自由に変更することができます。
     * ただし、何も設定されていない場合は空文字が返ります。<br>
     * This description is not that one which can be gotten at {@link VideoInfo#getDescription()} and which is edited by the contributor of this video.
     * The user who registered this video to his myList can edit it at the registration and after that.
     * If nothing is set, this returns empty string.
     * @return the description edited by the user who registered this video to his myList, not {@code null}
     */
    String getMyListItemDescription();

    /**
     * この動画のマイリス登録情報の最新更新日時を取得します Gets the last date when information about the registration to myList is updated.
     * {@link #getMyListItemDescription()}で取得される説明文などのこの動画のマイリスへの登録に関わる属性が更新された
     * 最新日時を返します。
     * @return Returns the date when information of registration is updated at the last time, not {@code null}
     */
    Date getUpdateDate();
}
