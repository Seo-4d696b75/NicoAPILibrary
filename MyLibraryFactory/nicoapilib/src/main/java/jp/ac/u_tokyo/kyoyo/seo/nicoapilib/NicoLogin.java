package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;



import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * ニコ動へのログインおよびユーザＩＤ・ニックネームを取得する<br>
 * This class try to login NicoNico and get userID, userName and userIconImage in background.<br><br>
 *
 * references;<br>
 * how to login with Apache : <a href=https://teratail.com/questions/31972>[teratail]Java_ニコニコ動画へのログイン</a><br>
 * <a href=http://c-loft.com/blog/?p=1196>[夏研ブログ]HTTP POST/GET クッキー認証によるWebサイトへのログイン (Android, Java)</a><br>
 * regular expression : <a href=java.keicode.com/lang/regexp-split.php>[Java 入門]正規表現による文字列のスプリット</a><br>
 * get image via http : <a href=http://logicalerror.seesaa.net/article/419965567.html>[SQLの窓]Android Studio : インターネット上の画像を取得して、Bitmap か Drawable で ImageView に表示する</a><br>
 * how to get user name : <a href=http://7cc.hatenadiary.jp/entry/nico-user-id-to-name>[Hatena Blog]ニコニコ動画で、ユーザーIDからニックネーム（ユーザーネーム）を取得する</a><br>
 *
 * @author Seo-4d696b75
 * @version 0.0 2016/12/10.
 */

class NicoLogin  {

    private LoginInfo loginInfo;
    private int userID;
    private String userName;
    private HttpClient client;

    /**
     * ログインを行うにはこのコンストラクタでインスタンスを取得する<br>
     *  Gets instance in order to login.<br>
     *         ログイン情報を格納するための{@link LoginInfo}を引数に渡す必要があります。<br>
     *        You need to pass {@link LoginInfo} to keep login session.
     * @param loginInfo cannot be {@code null}
     */
    protected NicoLogin(LoginInfo loginInfo){
        if ( loginInfo != null ) {
            this.loginInfo = loginInfo;
        }
    }

    /**
     * 指定したメールアドレスとパスワードでログインします<br>
     *  Tries to login with mail address and password passed.<br>
     * ログインに成功すると、{@link NicoLogin コンストラクタ}で渡した{@link LoginInfo}に
     * ログインセッションの情報を含むCookiesとユーザＩＤ、ユーザ名を登録します。
     * いずれかの取得に失敗すると例外を投げます。<br>
     * When succeed in login, this tries to get userID and its name,
     * then resisters them in {@link LoginInfo} passed at {@link NicoLogin constructor}.<br>
     * <strong>ＵＩスレッド禁止</strong>HTTP通信を行うのでバックグランド処理してください。<br>
     * <strong>No UI thread</strong> HTTP communication is done.
     * @param mail the mail address, cannot be {@code null}
     * @param pass the password, cannot be {@code null}
     * @throws NicoAPIException if fail to login
     */
    protected synchronized void login( final String mail, final String pass) throws NicoAPIException{
        if ( mail == null || pass == null ){
            throw new NicoAPIException.InvalidParamsException(
                    "mail and pass cannot be null > login",
                    NicoAPIException.EXCEPTION_PARAM_LOGIN
            );
        }
        final ResourceStore res = ResourceStore.getInstance();
        client = res.getHttpClient();
        String path = res.getURL(R.string.url_login) + res.getURL(R.string.url_myPage);
        Map<String,String> params = new HashMap<String,String>(){
            {
                put(res.getString(R.string.key_login_id), mail);
                put(res.getString(R.string.key_login_pass), pass);
            }
        };
        if ( client.post(path,params,null) ){
            loginInfo.setCookies(client.getCookies());
            if ( loginInfo.isLogin() ){
                getUserID();
                getUserName();
                getUserIconUrl();
                return;
            }
        }
        throw new NicoAPIException.InvalidParamsException(
                "mail and pass are invalid > login",
                NicoAPIException.EXCEPTION_PARAM_LOGIN
        );
    }

    private void getUserName() throws NicoAPIException{
        String response = client.getResponse();
        if ( response == null || userID <= 0 ){
            throw new NicoAPIException.IllegalStateException(
                    "userID in unknown > userName",
                    NicoAPIException.EXCEPTION_ILLEGAL_STATE_LOGIN_NAME
            );
        }
        ResourceStore res = ResourceStore.getInstance();
        Matcher matcher = res.getPattern(R.string.regex_myPage_userName).matcher(response);
        if ( matcher.find() ){
            userName =  matcher.group(1);
            loginInfo.setUserName(userName);
            return;
        }
        String path = String.format(Locale.US,res.getString(R.string.url_user_seiga),userID);
        if ( client.get(path,null) ){
            matcher = res.getPattern(R.string.regex_seiga_userName).matcher(client.getResponse());
            if ( matcher.find() ){
                userName = matcher.group(1);
                loginInfo.setUserName(userName);
                return;
            }else{
                throw new NicoAPIException.ParseException(
                        "cannot find user name ",client.getResponse(),
                        NicoAPIException.EXCEPTION_PARSE_LOGIN_USER_NAME
                );
            }
        }
    }

    private void getUserID() throws NicoAPIException{
        if ( client.getResponse() == null ){
            throw new NicoAPIException.IllegalStateException(
                    "not login yet",
                    NicoAPIException.EXCEPTION_ILLEGAL_STATE_LOGIN_USER_ID
            );
        }
        Matcher matcher = ResourceStore.getInstance().getPattern(R.string.regex_myPage_user_profile).matcher(client.getResponse());
        if ( matcher.find() ){
            userID = Integer.parseInt(matcher.group(1));
            String age = matcher.group(2);
            //following params also can be gotten, but not used in this app
            boolean isPremium = Boolean.valueOf(matcher.group(3));
            boolean isOver18 = Boolean.valueOf(matcher.group(4));
            boolean isMan = Boolean.valueOf(matcher.group(5));
            loginInfo.setUserID(userID);
            loginInfo.setPremium(isPremium);
        }else{
            throw new NicoAPIException.ParseException(
                    "cannot find userID ",client.getResponse(),
                    NicoAPIException.EXCEPTION_PARSE_LOGIN_USER_ID
            );
        }
    }

    private void getUserIconUrl() throws NicoAPIException{
        if ( client.getResponse() == null ){
            throw new NicoAPIException.IllegalStateException(
                    "not login yet",
                    NicoAPIException.EXCEPTION_ILLEGAL_STATE_LOGIN_USER_ICON
            );
        }
        Matcher matcher = ResourceStore.getInstance().getPattern(R.string.regex_myPage_user_icon).matcher(client.getResponse());
        if ( matcher.find() ){
            loginInfo.setUserIconUrl(matcher.group(1));
        }else{
            throw new NicoAPIException.ParseException(
                    "cannot find userIconUrl ",client.getResponse(),
                    NicoAPIException.EXCEPTION_PARSE_LOGIN_USER_ICON
            );
        }
    }

}
