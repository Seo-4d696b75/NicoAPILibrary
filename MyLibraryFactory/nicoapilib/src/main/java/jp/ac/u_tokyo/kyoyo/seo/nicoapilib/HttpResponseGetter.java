package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Seo on 2016/12/17.
 *
 * this class defines the logic of http communication
 *
 * reference;
 * important point of URL encoder : http://weblabo.oscasierra.net/java-urlencode/
 * usage of regular expression : http://nobuo-create.net/seikihyougen/#i-13
 *
 */

public class HttpResponseGetter {

    protected CookieStore cookieStore;

    protected String response;

    public boolean tryPost(String path,Map<String,String>params){
        response = null;
        cookieStore = null;
        try {
            HttpPost httpPost = new HttpPost(new URI(path));
            DefaultHttpClient client = new DefaultHttpClient();

            if ( params != null ) {
                ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
                for ( String key : params.keySet() ) {
                    parameters.add(new BasicNameValuePair(key, params.get(key)));
                }
                httpPost.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
            }

            HttpResponse httpResponse = client.execute(httpPost);
            /* レスポンスコードの取得（Success:200、Auth Error:403、Not Found:404、Internal Server Error:500）*/
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                cookieStore = client.getCookieStore();
                response = EntityUtils.toString(httpResponse.getEntity());
                return true;
            }
            client.getConnectionManager().shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //TODO return boolean; make sure target object is not null, then return

    public boolean tryGet(String path){
        return tryGet(path,null);
    }

    public boolean tryGet(String path, CookieStore cookieStore){
        try{
            //some characters cause IllegalArgumentException
            path = replaceMetaSymbol(path);
            HttpGet httpGet = new HttpGet(path);
            DefaultHttpClient client = new DefaultHttpClient();
            if ( cookieStore != null){
                client.setCookieStore(cookieStore);
            }
            //httpGet.setHeader("Connection", "keep-Alive");
            HttpResponse httpResponse = client.execute(httpGet);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            client.getConnectionManager().shutdown();
            if ( statusCode == 200 ) {
                response = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
                return true;
            }
        }catch ( Exception e){
            e.printStackTrace();
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
