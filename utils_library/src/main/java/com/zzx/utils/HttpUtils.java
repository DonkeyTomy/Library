package com.zzx.utils;


import android.util.Base64;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * @author Tomy
 * Created by Tomy on 13-12-26.
 */
public class HttpUtils {
    private static final String TAG = "HttpUtils";
    private static final String BD_MAP_CONVERT_URI = "http://api.map.baidu.com/ag/coord/convert?from=0&to=4";
    private static final String KEY_G = "g";
    private static final String KEY_M = "m";
    private static final String KEY_CMD = "a";
    private static final String KEY_IMEI = "imei";
    private static final String PUSH_IMG = "pushimg";
    private static final String PUSH_VIDEO = "pushvideo";
    private DefaultHttpClient mGpsClient;
//    private SchemeRegistry registry;
    private String mUri = null;
    private final Object mLockObj = new Object();
    private String mCovertCmd;
    private HttpParams mParams;
    private String mLonStr;
    private String mLatStr;

    public HttpUtils(String uri) {
        mUri = uri;
        mParams = new BasicHttpParams();
        HttpProtocolParams.setVersion(mParams, HttpVersion.HTTP_1_1);
        HttpConnectionParams.setConnectionTimeout(mParams, 5 * 1000);
        HttpConnectionParams.setSoTimeout(mParams, 5 * 1000);
        /*params.setParameter(CoreConnectionPNames.SO_TIMEOUT, this.soTimeout);
        params.setParameter(CoreConnectionPNames.SO_REUSEADDR, true);
        params.setParameter(CoreConnectionPNames.TCP_NODELAY, false);
        params.setParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, this.bufSize);
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
        HttpProtocolParams.setUseExpectContinue(params, true);
        HttpProtocolParams.setUserAgent(params, "");
        params.setParameter(CoreProtocolPNames.USER_AGENT, "");
        ConnManagerParams.setMaxTotalConnections(params, 20);
        params.setParameter(ClientPNames.CONNECTION_MANAGER_FACTORY_CLASS_NAME, "org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager");
        params.setParameter(ClientPNames.MAX_REDIRECTS, this.maxRedirect);
        registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));*/
    }

    public void release() {
        try {
//            registry.unregister("http");
            if (mGpsClient != null)
                mGpsClient = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String request(List<NameValuePair> valuePairs) {
        HttpPost httpPost = new HttpPost(mUri);
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        try {
            UrlEncodedFormEntity requestEntity = new UrlEncodedFormEntity(valuePairs, "utf-8");
            httpPost.setEntity(requestEntity);
            response = client.execute(httpPost);
            return readResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initGpsClient() {
        if (mGpsClient == null) {
            mGpsClient = new DefaultHttpClient(mParams);
        }
    }

    public boolean convertGps(String lon, String lat) {
        initGpsClient();
        String mCovertCmd = BD_MAP_CONVERT_URI + "&x=" + lon + "&y=" + lat;
        HttpGet get = new HttpGet(mCovertCmd);
        try {
            HttpResponse response = mGpsClient.execute(get);
            String cmd = readResponse(response);
            JSONObject object = new JSONObject(cmd);
            mLonStr = new String(Base64.decode(object.getString("x"), Base64.DEFAULT));
            mLatStr = new String(Base64.decode(object.getString("y"), Base64.DEFAULT));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String requestUri(String imei, String sign) {
        HttpClient client = new DefaultHttpClient(mParams);
        String cmd = mUri + "?g=Api&m=Paipai&imei=" + imei + "&sign=" + sign;
        Log.e(TAG, "msg = " + cmd);
        HttpGet get = new HttpGet(cmd);
        try {
            HttpResponse response = client.execute(get);
            return readResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sendResult(String imei, String type, String value, String sign) {
        HttpClient client = new DefaultHttpClient(mParams);
        String cmd = mUri + "?g=Api&m=Paipai&a=saveMsg&imei=" + imei + "&sign=" + sign + "&type=" + type + "&value=" + value;
        HttpGet get = new HttpGet(cmd);
        try {
            HttpResponse response = client.execute(get);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                get.abort();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendIccid(String imei, String iccid) {
        HttpClient client = new DefaultHttpClient(mParams);
        String cmd = mUri + "?g=Api&m=Paipai&a=online&imei=" + imei + "&iccid=" + iccid;
        HttpGet get = new HttpGet(cmd);
        try {
            client.execute(get);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                get.abort();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendGps(String imei, String sign, String lon, String lat, String accStatus, String iccid) {
        initGpsClient();
        if (lon == null || lon.equals("0") || !convertGps(lon, lat)) {
            mLonStr = "0";
            mLatStr = "0";
        }
        String cmd = mUri + "?g=Api&m=Paipai&a=geo&imei=" + imei + "&lng=" + lon + "&lat=" + lat
                + "&sign=" + sign + "&acc=" + accStatus + "&iccid=" + iccid
                + "&bd_longitude=" + mLonStr + "&bd_latitude=" + mLatStr;
        HttpGet get = new HttpGet(cmd);
        try {
            synchronized (mLockObj) {
                HttpResponse response = mGpsClient.execute(get);
                if (response != null) {
                    readResponse(response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                get.abort();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendFile(File file, int type, String imei, String sign) {
        String result;
        try {
            String uri = mUri + "?g=Wap&m=Intelligent&a=pushimg&imei=" + imei;
            /*List<NameValuePair> list = new ArrayList<>();
            NameValuePair gPair = new BasicNameValuePair(KEY_G, "Wap");
            NameValuePair mPair = new BasicNameValuePair(KEY_M, "Intelligent");
            NameValuePair cmdPair   = new BasicNameValuePair(KEY_CMD, PUSH_IMG);
            NameValuePair imeiPair  = new BasicNameValuePair(KEY_IMEI, imei);*/
            HttpPost post = new HttpPost(uri);
            FileEntity fileEntity = new FileEntity(file, ACCEPT_TYPE_JPEG);
            fileEntity.setContentEncoding(ENCODING_TYPE_BINARY);
            post.setEntity(fileEntity);
            HttpClient fileClient = new DefaultHttpClient();
            HttpResponse response = fileClient.execute(post);
            result = readResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*public void send(String filePath, String imei) {
        ClosableCli httpclient;
        try {
            httpclient = new DefaultHttpClient();
            String uri = mUri + "?g=Wap&m=Intelligent&a=pushimg&imei=" + imei;
            HttpPost httpPost = new HttpPost(uri);
            httpPost.setHeader(CONNECTION, CONN_KEEP_ALIVE);
            httpPost.setHeader("Accept-Charset", CHARSET_UTF8);
            MultipartEntity multipartEntity = new MultipartEntity();
            FileBody file = new FileBody(new File(filePath));
            multipartEntity.addPart("file", file);
            httpPost.setEntity(multipartEntity);
            HttpResponse response = httpclient.execute(httpPost);

            StatusLine statusLine = response.getStatusLine();
            HttpEntity resEntity = response.getEntity();
            readResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public final String BOUNDARY = "--------------------56423498738365";
    public final String ENDLINE = "--" + BOUNDARY + "--/r/n";

    public void sendImg(String path, String imei) {
        try {
            String uri = mUri + "?g=Wap&m=Intelligent&a=pushimg&imei=" + imei;
            URL url = new URL(uri);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setUseCaches(false);
            con.setRequestMethod(METHOD_POST);
            con.setRequestProperty(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);
            con.setRequestProperty(CHARSET, HTTP.UTF_8);
            con.setRequestProperty(HTTP.CONTENT_TYPE, HTTP.DEFAULT_CONTENT_TYPE);
            DataOutputStream output = new DataOutputStream(con.getOutputStream());
            InputStream input = con.getInputStream();
            FileInputStream inputStream = new FileInputStream(path);
            byte[] buffer = new byte[1024 * 5];
            int len;
            while ((len = inputStream.read(buffer, 0, buffer.length)) != -1) {
                output.write(buffer, 0, len);
            }
            output.flush();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            StringBuilder msgs = new StringBuilder();
            String msg;
            while ((msg = reader.readLine()) != null) {
                msgs.append(msg);
            }
//            Log.i(TAG, "msg ========= " + msgs.toString());
            output.close();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String readResponse(HttpResponse response) {
        String result = null;
        InputStream inputStream = null;
        try {
            if (response != null) {
                HttpEntity entity = response.getEntity();
                inputStream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder msgs = new StringBuilder();
                String msg;
                while ((msg = reader.readLine()) != null) {
                    msgs.append(msg);
                }
                Log.d(TAG, "msg = " + msgs.toString());
                result = msgs.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /** Accept-Language **/
    public static final String PROPERTY_ACCEPT_LANGUAGE = "Accept-Language";
    public static final String LANGUAGE_CHINESE = "zh-CN";

    /** Charset **/
    public static final String CHARSET = "Charset";
    public static final String CHARSET_UTF8     = "UTF-8";
    public static final String CHARSET_UTF16    = "UTF-16";

    /** Accept **/
    public static final String ACCEPT = "Accept";
    public static final String ACCEPT_TYPE_GIF  = "image/gif";
    public static final String ACCEPT_TYPE_JPEG = "image/jpeg";
    public static final String ACCEPT_TYPE_PEPEG    = "image/pjpeg";
    public static final String ACCEPT_TYPE_XML = "application/xaml+xml";
    public static final String ACCEPT_TYPE_OCTET_STREAM = "application/octet-stream";
    public static final String ACCEPT_TYPE_PLAIN_TEXT = "text/plain";
    public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    /** Accept-Encoding Content-Encoding **/
    public static final String ACCEPT_ENCODING  = "Accept-Encoding";
    public static final String CONTENT_ENCODING = "Content-Encoding";
    public static final String ENCODING_TYPE_COMPRESS   = "compress";
    public static final String ENCODING_TYPE_GZIP       = "gzip";
    public static final String ENCODING_TYPE_DEFAULT_IDENTITY   = "identity";
    public static final String ENCODING_TYPE_BINARY   = "binary/octet-stream";

    /** Range **/
    public static final String RANGE = "Range";

    /** Connection **/
    public static final String CONNECTION = "Connection";
    public static final String CONN_KEEP_ALIVE = "Keep-Alive";

    public static final String REFERER = "Referer";
    /** GET **/
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    /** User-Agent **/
    public static final String USER_AGENT = "User-Agent";
    public static final String TRANSFER_ENCODING = "Transfer-Encoding";

    /** Content **/
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String EXPECT_DIRECTIVE = "Expect";
    public static final String TARGET_HOST = "Host";
    public static final String DATE = "Date";
    public static final String SERVER = "Server";
    public static final String EXPECT_CONTINUE = "100-continue";
    public static final String CONN_CLOSE = "Close";
    public static final String CHUNK_CODING = "chunked";
    public static final String IDENTITY_CODING = "identity";
    public static final String US_ASCII = "US-ASCII";
    public static final String ASCII = "ASCII";
    public static final String ISO_8859_1 = "ISO-8859-1";
    public static final String DEFAULT_CONTENT_CHARSET = "ISO-8859-1";
    public static final String DEFAULT_PROTOCOL_CHARSET = "US-ASCII";
    public static final String CHARSET_PARAM = "; charset=";
}
