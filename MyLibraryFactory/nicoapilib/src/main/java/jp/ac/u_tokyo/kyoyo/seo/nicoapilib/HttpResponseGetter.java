package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *  HTTP通信の実装クラス<br>
 * This class defines the logic of http communication.<br><br>
 *
 * reference;<br>
 * important point of URL encoder : <a href=http://weblabo.oscasierra.net/java-urlencode>[WEB ARCH LABO]JavaでURLエンコード/デコードする方法と注意点</a><br>
 * usage of regular expression : <a href=http://nobuo-create.net/seikihyougen/#i-13>[一番かんたんなJava入門]【Java】正規表現って何？</a><br>
 *
 * @author Seo-4d696b75
 * @version 0.0 on 2016/12/17.
 */

public class HttpResponseGetter {

    /**
     * {@link #tryPost(String, Map)} で取得したクッキー、失敗すると{@code null}<br>
     *     cookies gotten in {@link #tryPost(String, Map)}, can be {@code null} if fails
     */
    protected CookieStore cookieStore;
    /**
     * 通信のレスポンス,失敗すると{@code null}<br>
     *     response from HTTP communication, can be {@code null} if fails.
     */
    protected String response;

    protected int statusCode;

    /**
     * apacheでpostしてクッキーとレスポンスを取得<br>
     * Posts in apache and gets Cookie and response.
     * @param path target url, cannot be {@code null}
     * @param params map of name and param, corresponding to List of NameValuePair passed to HttpPost#setEntity
     * @return Returns {@code true} if success. Be sure to check this value before getting {@link #response} or {@link #cookieStore}.
     */
    public boolean tryPost(String path,Map<String,String>params){
        return tryPost(path,params,null);
    }
    public boolean tryPost(String path,Map<String,String>params, CookieStore cookieStore){
        DefaultHttpClient client = new DefaultHttpClient();
        response = null;
        cookieStore = null;
        try {
            HttpPost httpPost = new HttpPost(path);
            if ( cookieStore != null){
                client.setCookieStore(cookieStore);
            }
            if ( params != null ) {
                ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
                for ( String key : params.keySet() ) {
                    parameters.add(new BasicNameValuePair(key, params.get(key)));
                }
                httpPost.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
            }

            HttpResponse httpResponse = client.execute(httpPost);
            /* レスポンスコードの取得（Success:200、Auth Error:403、Not Found:404、Internal Server Error:500）*/
            statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                cookieStore = client.getCookieStore();
                response = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            client.getConnectionManager().shutdown();
        }
        return false;
    }

    public boolean tryPost(String path ,String post){
        DefaultHttpClient client = new DefaultHttpClient();
        response = null;
        cookieStore = null;
        try {
            HttpPost httpPost = new HttpPost(path);
            httpPost.setEntity(new StringEntity(post,"UTF-8"));
            HttpResponse httpResponse = client.execute(httpPost);
            statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                //cookieStore = client.getCookieStore();
                response = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            client.getConnectionManager().shutdown();
        }
        return false;
    }


    /**
     * 引数簡略したもの <br>
     * {@link #tryGet(String, CookieStore) The method} with omitted argument.
     * @param path target url, cannot be {@code null}
     * @return the same
     */
    public boolean tryGet(String path){
        return tryGet(path,null);
    }

    /**
     * apacheでgetしてクッキーとレスポンスを取得<br>
     * Gets in apache and catches response.
     * @param path target url, cannot be {@code null}
     * @param cookieStore cookies needed for access, can be {@code null}
     * @return Returns {@code true} if success. Be sure to check this value before getting {@link #response}
     */
    public boolean tryGet(String path, CookieStore cookieStore){
        DefaultHttpClient client = new DefaultHttpClient();;
        response = null;
        try{
            //some characters cause IllegalArgumentException
            path = replaceMetaSymbol(path);
            HttpGet httpGet = new HttpGet(path);
            if ( cookieStore != null){
                client.setCookieStore(cookieStore);
            }
            //httpGet.setHeader("Connection", "keep-Alive");
            HttpResponse httpResponse = client.execute(httpGet);
            String res = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
            statusCode = httpResponse.getStatusLine().getStatusCode();
            if ( statusCode == 200 ) {
                response = res;
                return true;
            }
        }catch ( Exception e){
            e.printStackTrace();
        }finally {
            client.getConnectionManager().shutdown();
        }
        return  false;
    }

    private final Map<String,String> symbolMap = new HashMap<String, String>(){
        {
            put("\\+","%2b");
            put("\\s","%20");
            put("\"","%22");
            put("\\|","%7c");
        }
    };
    private String replaceMetaSymbol(String str){
        for ( String key : symbolMap.keySet() ){
            str = str.replaceAll(key,symbolMap.get(key));
        }
        return str;
    }
}
