package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import org.apache.http.client.CookieStore;


/**
 * この例外クラスはライブラリ内で発生したエラーを区分するものです<br>
 * This class extending Exception helps you recognize what king of error happened.<br><br>
 *
 *
 * @author Seo-4d696b75
 * @version 0.0 on 2017/01/31.
 */
/*
NotInitializedException 対象の動画フィールドが初期化されていない
DrawableFailureException    画像の取得に失敗
ParseException  ＡＰＩからのレスポンスのパースに失敗
NoLoginException    ログインが必要な機能
InvalidParamsException  渡されたパラメータが不正
APIUnexpectedException 予期されないＡＰＩからのレスポンス
*/

class NicoAPIException extends Exception {

    private NicoAPIException (String message){
        super(message);
    }

    /**
     * 対象の動画フィールドが初期化されていない場合に投げられます<br>
     * Thrown if target video field is not initialized.<br><br>
     *
     * 各ＡＰＩから取得できるフィールドは以下から参照してください。<br>
     *     {@link RankingVideoInfo ランキング・マイリスト}<br>
     *     {@link RecommendVideoInfo おすすめ動画}<br>
     *     {@link SearchVideoInfo 動画検索}<br>
     *     {@link TempMyListVideoInfo とりあえずマイリスト}<br>
     * {@link VideoInfoManager#complete()},{@link VideoInfoManager#getFlv(CookieStore)}を呼ぶことで
     * 欠損したフィールド値を取得できます。<br>
     * You can get following fields from each API;<br>
     *     {@link RankingVideoInfo ranking and myList}<br>
     *     {@link RecommendVideoInfo reccomend}<br>
     *     {@link SearchVideoInfo search}<br>
     *     {@link TempMyListVideoInfo temp myList}<br>
     * You can get lacking field by calling
     * {@link VideoInfoManager#complete()},{@link VideoInfoManager#getFlv(CookieStore)}
     */
    static class NotInitializedException extends NicoAPIException{
        NotInitializedException (String message){
            super(message);
        }
    }

    /**
     * 画像の取得に失敗すると投げられます<br>
     * Thrown if fail to get image.
     */
    static class DrawableFailureException extends NicoAPIException{
        DrawableFailureException (String message){
            super(message);
        }
    }

    /**
     * APIからのレスポンスのパースに失敗すると投げられます<br>
     * Thrown if fail to parse API response.<br><br>
     *
     * APIのレスポンスが想定されたフォーマットと異なることを意味します。
     * 通常は投げられません。APIの仕様変更が考えられます。<br>
     * This means that API response does not follow expected format.
     * This exception is not usually thrown. Change in API may be to blame.
     */
    static class ParseException extends NicoAPIException{
        private String target;
        ParseException(String message,String target){
            super(message);
            this.target = target;
        }
        public String getTarget(){return target;}
    }

    /**
     * 不正な引数を渡すと投げられます<br>
     * Thrown if invalid argument is passed.
     */
    static class InvalidParamsException extends NicoAPIException{
        InvalidParamsException(String message){
            super(message);
        }
    }

    /**
     * ログインしていない状態でログイン必須の機能を使おうとすると投げます<br>
     * Thrown if try to use login-required method without login.
     */
    static class NoLoginException extends NicoAPIException{
        NoLoginException(String message){
            super(message);
        }
    }

    /**
     * APIからのレスポンスの状態が想定外の場合に投げます<br>
     * Thrown if status of API response is not expected.<br>
     * 各種不正なパラメータやユーザアカウントの設定、APIのアクセス制限などが考えられます。<br>
     * Various things may be a cause; invalid params, setting of user account in Nico, access limit ro API....
     */
    static class APIUnexpectedException extends NicoAPIException{
        APIUnexpectedException(String message){
            super(message);
        }
    }

}
