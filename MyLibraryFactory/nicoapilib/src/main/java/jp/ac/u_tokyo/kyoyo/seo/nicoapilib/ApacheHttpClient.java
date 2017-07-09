package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Apache HTTP client を用いた{@link HttpClient}の実現クラスです。
 * @author Seo-4d696b75
 * @version on 2017/06/26.
 */

public class ApacheHttpClient implements HttpClient {

    public ApacheHttpClient(){}

    private CookieStore cookieStore;
    private String response;
    private int statusCode = 0;

    @Override
    public int getStatusCode(){
        return statusCode;
    }

    @Override
    public String getResponse(){
        return this.response;
    }

    @Override
    public boolean post(String path, String entity, CookieGroup cookies){
        DefaultHttpClient client = new DefaultHttpClient();
        response = null;
        cookieStore = null;
        try {
            HttpPost httpPost = new HttpPost(path);
            httpPost.setEntity(new StringEntity(entity,"UTF-8"));
            HttpResponse httpResponse = client.execute(httpPost);
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

    @Override
    public boolean post(String path, Map<String,String> entity, CookieGroup cookies){
        DefaultHttpClient client = new DefaultHttpClient();
        response = null;
        this.cookieStore = null;
        try {
            if ( cookies != null){
                client.setCookieStore(convertCookies(cookies));
            }
            HttpPost httpPost = new HttpPost(path);
            if ( cookieStore != null){
                client.setCookieStore(cookieStore);
            }
            if ( entity != null ) {
                ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
                for ( String key : entity.keySet() ) {
                    parameters.add(new BasicNameValuePair(key, entity.get(key)));
                }
                httpPost.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
            }

            HttpResponse httpResponse = client.execute(httpPost);
            /* レスポンスコードの取得（Success:200、Auth Error:403、Not Found:404、Internal Server Error:500）*/
            statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                this.cookieStore = client.getCookieStore();
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

    @Override
    public boolean get(String path, CookieGroup cookies){
        DefaultHttpClient client = new DefaultHttpClient();;
        response = null;
        try{
            //some characters cause IllegalArgumentException
            path = replaceMetaSymbol(path);
            HttpGet httpGet = new HttpGet(path);
            if ( cookies != null){
                client.setCookieStore(convertCookies(cookies));
            }
            //httpGet.setHeader("Connection", "keep-Alive");
            HttpResponse httpResponse = client.execute(httpGet);
            String res = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
            statusCode = httpResponse.getStatusLine().getStatusCode();
            if ( statusCode == 200 ) {
                this.cookieStore = client.getCookieStore();
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

    @Override
    public Bitmap getBitmap(String path, CookieGroup cookies){
        if ( path != null ){
            try{
                URL url = new URL(path);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                return bitmap;
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public boolean download(String path, File out, CookieGroup cookies){
        DefaultHttpClient client = new DefaultHttpClient();
        InputStream inputStream;
        OutputStream outputStream;
        try{
            path = replaceMetaSymbol(path);
            HttpGet httpGet = new HttpGet(path);
            if ( cookies != null){
                client.setCookieStore(convertCookies(cookies));
            }
            HttpResponse httpResponse = client.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            statusCode = httpResponse.getStatusLine().getStatusCode();
            if ( statusCode == 200 ) {
                if ( !out.exists() ){
                    if ( !out.createNewFile() ){
                        return false;
                    }
                }
                if ( !out.canWrite() ){
                    return false;
                }
                inputStream = new BufferedInputStream(entity.getContent());
                outputStream = new BufferedOutputStream(new FileOutputStream(out,false));
                int b;
                while ( (b = inputStream.read()) >= 0 ){
                    outputStream.write(b);
                }
                outputStream.close();
                inputStream.close();
                EntityUtils.consume(entity);
                entity.consumeContent();
                return true;
            }
        }catch ( Exception e){
            e.printStackTrace();
        }finally {
            client.getConnectionManager().shutdown();
        }
        return  false;
    }

    @Override
    public CookieGroup getCookies(){
        return new ApacheCookieGroup(cookieStore);
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

    private CookieStore convertCookies(CookieGroup cookies){
        CookieStore cookieStore = new DefaultHttpClient().getCookieStore();
        for(CookieInfo item : cookies ){
            BasicClientCookie cookie = new BasicClientCookie(item.getName(),item.getValue());
            cookie.setVersion(item.getVersion());
            cookie.setPath(item.getPath());
            cookie.setDomain(item.getDomain());
            cookieStore.addCookie(cookie);
        }
        return cookieStore;
    }

}
