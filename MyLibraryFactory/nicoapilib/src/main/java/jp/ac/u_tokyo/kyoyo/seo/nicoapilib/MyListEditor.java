package jp.ac.u_tokyo.kyoyo.seo.nicoapilib;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Seo-4d696b75 on 2017/02/27.
 */

public class MyListEditor extends HttpResponseGetter {

    protected MyListEditor(LoginInfo info){
        this.info = info;
    }
    private String tokenURL = "http://www.nicovideo.jp/mylist_add/video/";
    protected LoginInfo info;

    protected String getToken(String anyVideoID) throws NicoAPIException{
        String path = tokenURL + anyVideoID;
        try {
            if ( tryGet(path, info.getCookieStore()) ) {
                Matcher matcher = Pattern.compile("NicoAPI.token = '(.+?)';").matcher(super.response);
                if ( matcher.find() ){
                    return matcher.group(1);
                }else{
                    throw new NicoAPIException.ParseException(
                            "fail to find NicoAPI.token",super.response,
                            NicoAPIException.EXCEPTION_PARSE_MYLIST_TOKEN);
                }
            }else{
                throw new NicoAPIException.HttpException(
                        "fail to get token",
                        NicoAPIException.EXCEPTION_HTTP_MYLIST_TOKEN,
                        super.statusCode, path, "GET");
            }
        }catch (NicoAPIException e){
            throw e;
        }
    }

    protected String getThreadID(VideoInfo video) throws NicoAPIException{
        try {
            video.getThreadID();
        } catch (NicoAPIException e) {
            video.getFlv(info.getCookieStore());
        }
        return video.getThreadID();
    }

    protected int getDeleteCount(JSONObject response) throws NicoAPIException{
        try{
            return response.getInt("delete_count");
        }catch (JSONException e){
            throw new NicoAPIException.ParseException(
                    "fail to parse delete count",response.toString(),
                    NicoAPIException.EXCEPTION_PARSE_TEMP_MYLIST_DELETE_COUNT);
        }
    }

    private final Map<String,String> errorCodeMap = new HashMap<String, String>(){
        {
            put("NOAUTH","editing myList needs login");
            put("NONEXIST","specified video not found");
            put("INVALIDTOKEN","invalid token");
            put("EXIST","specified video already in target myList");
            put("PARAMERROR","invalid param or required param not found");
            put("EXPIRETOKEN","token too old");
        }
    };
    protected JSONObject checkStatusCode(String response) throws NicoAPIException{
        try{
            JSONObject root = new JSONObject(response);
            String status = root.getString("status");
            switch (status) {
                case "ok":
                    return root;
                case "fail":
                    JSONObject error = root.getJSONObject("error");
                    String code = error.getString("code");
                    if ( errorCodeMap.containsKey(code)){
                        String message = errorCodeMap.get(code);
                        switch ( code ){
                            case "NOAUTH":
                                throw new NicoAPIException.NoLoginException(
                                        message,
                                        NicoAPIException.EXCEPTION_NOT_LOGIN_MYLIST_EDIT);
                            case "NONEXIST":
                                throw new NicoAPIException.InvalidParamsException(
                                        message,
                                        NicoAPIException.EXCEPTION_PARAM_MYLIST_EDIT_VIDEO_NOT_FOUND);
                            case "INVALIDTOKEN":
                                throw new NicoAPIException.InvalidParamsException(
                                        message,
                                        NicoAPIException.EXCEPTION_PARAM_MYLIST_EDIT_INVALID_TOKEN);
                            case "EXIST":
                                throw new NicoAPIException.InvalidParamsException(
                                        message,
                                        NicoAPIException.EXCEPTION_PARAM_MYLIST_EDIT_VIDEO_ALREADY_EXIST);
                            case "PARAMERROR":
                                throw new NicoAPIException.InvalidParamsException(
                                        message,
                                        NicoAPIException.EXCEPTION_PARAM_MYLIST_EDIT_PARAM);
                            case "EXPIRETOKEN":
                                throw new NicoAPIException.InvalidParamsException(
                                        message,
                                        NicoAPIException.EXCEPTION_PARAM_MYLIST_EDIT_OLD_TOKEN);
                        }
                    }else{
                        throw new NicoAPIException.APIUnexpectedException(
                                "unexpected error code",
                                NicoAPIException.EXCEPTION_UNEXPECTED_MYLIST_EDIT_ERROR_CODE);
                    }
                default:
                    throw new NicoAPIException.APIUnexpectedException(
                            "unexpected status code",
                            NicoAPIException.EXCEPTION_UNEXPECTED_MYLIST_EDIT_STATUS_CODE);
            }
        }catch (JSONException e){
            throw new NicoAPIException.ParseException(e.getMessage(),response);
        }
    }
}
