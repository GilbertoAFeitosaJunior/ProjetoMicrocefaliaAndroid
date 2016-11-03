package br.com.mobi.redemicro.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import br.com.mobi.redemicro.exception.NoInternetException;


public class HttpAsyncTask {

    private static final String TAG = HttpAsyncTask.class.getSimpleName();
    private HttpURLConnection connection;
    private URL url;
    private Map<String, Object> params;
    private Util util;

    public HttpAsyncTask(String url, Context context) throws MalformedURLException {
        this.url = new URL(url);
        this.params = new HashMap<>();
        util = new Util(context);
    }

    private void execute(METHOD method, final FutureCallback futureCallback) throws Exception {
        if (util.isOnline()) {
            try {
                connection = (HttpURLConnection) url.openConnection();
                //connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setInstanceFollowRedirects(false);
                connection.setRequestMethod(String.valueOf(method));
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("charset", "utf-8");
                connection.setUseCaches(false);

                if (params != null && !params.isEmpty()) {
                    JSONObject json = new JSONObject();
                    for (Map.Entry<String, Object> map : params.entrySet()) {
                        if (map.getValue() instanceof Map) {
                            JSONObject iJson = new JSONObject();
                            Map<String, Object> innerMap = (Map<String, Object>) map.getValue();
                            for (Map.Entry<String, Object> inner : innerMap.entrySet()) {
                                iJson.put(inner.getKey(), String.valueOf(inner.getValue()));
                            }
                            json.put(map.getKey(), iJson);
                        } else if (map.getValue() instanceof JSONObject) {
                            json.put(map.getKey(), map.getValue());
                        } else if (map.getValue() instanceof JSONArray) {
                            json.put(map.getKey(), map.getValue());
                        } else {
                            json.put(map.getKey(), String.valueOf(map.getValue()));
                        }
                    }
                    Log.v(TAG, json.toString());
                    byte[] bytes = json.toString().getBytes("UTF-8");
                    connection.setRequestProperty("Content-Length", Integer.toString(bytes.length));

                    DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                    wr.write(bytes, 0, bytes.length);
                    wr.flush();
                    wr.close();
                }

                int responseCode = connection.getResponseCode();
                if (futureCallback != null) {
                    InputStream is;
                    if (responseCode < 300) {
                        is = connection.getInputStream();
                    } else {
                        is = connection.getErrorStream();
                    }
                    String jsonString = util.inputStreamToString(is);
                    if (!TextUtils.isEmpty(jsonString)) {
                        Object preJson = new JSONTokener(jsonString).nextValue();
                        if (preJson instanceof JSONObject) {
                            futureCallback.onCallback(new JSONObject(jsonString), responseCode);
                        } else if (preJson instanceof JSONArray) {
                            futureCallback.onCallback(new JSONArray(jsonString), responseCode);
                        } else {
                            futureCallback.onCallback(jsonString, responseCode);
                        }
                    } else {
                        futureCallback.onCallback(jsonString, responseCode);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        } else {
            throw new NoInternetException();
        }
    }

    public void upload(String filePath, String fileField, String contentType, final FutureCallback futureCallback) {
        try {
            final String twoHyphens = "--";
            final String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
            final String lineEnd = "\r\n";

            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;

            String[] q = filePath.split("/");
            int idx = q.length - 1;

            File file = new File(filePath);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + fileField + "\"; filename=\"" + q[idx] + "\"" + lineEnd);
            outputStream.writeBytes("Content-Type: " + contentType + "" + lineEnd);
            outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
            outputStream.writeBytes(lineEnd);

            FileInputStream fileInputStream = new FileInputStream(file);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }
            outputStream.writeBytes(lineEnd);

            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"contentType\"" + lineEnd);
            outputStream.writeBytes("Content-Type: text/plain" + lineEnd);
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(contentType);
            outputStream.writeBytes(lineEnd);


            if (params != null && !params.isEmpty()) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    String key = entry.getKey();
                    String value = String.valueOf(entry.getValue());

                    outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd);
                    outputStream.writeBytes("Content-Type: application/json" + lineEnd);
                    outputStream.writeBytes(lineEnd);
                    outputStream.writeBytes(value);
                    outputStream.writeBytes(lineEnd);
                }
            }
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            fileInputStream.close();
            outputStream.flush();
            outputStream.close();

            int responseCode = connection.getResponseCode();
            Log.v(TAG, "Response Code: " + responseCode);
            if (responseCode < 500) {
                InputStream is = connection.getInputStream();
                String jsonString = util.inputStreamToString(is);
                if (!TextUtils.isEmpty(jsonString)) {
                    Object preJson = new JSONTokener(jsonString).nextValue();
                    if (preJson instanceof JSONObject) {
                        futureCallback.onCallback(new JSONObject(jsonString), responseCode);
                    } else if (preJson instanceof JSONArray) {
                        futureCallback.onCallback(new JSONArray(jsonString), responseCode);
                    }
                }
            } else {
                futureCallback.onCallback(null, responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public void post(final FutureCallback futureCallback) throws Exception {
        execute(METHOD.POST, futureCallback);
    }

    public void get(final FutureCallback futureCallback) throws Exception {
        execute(METHOD.GET, futureCallback);
    }

    public void put(final FutureCallback futureCallback) throws Exception {
        execute(METHOD.PUT, futureCallback);
    }

    public void delete(final FutureCallback futureCallback) throws Exception {
        execute(METHOD.DELETE, futureCallback);
    }

    public void addParams(String key, Object value) {
        this.params.put(key, value);
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public enum METHOD {
        GET,
        POST,
        PUT,
        DELETE
    }

    public interface FutureCallback {
        void onCallback(final Object jsonObject, final int responseCode) throws JSONException;
    }
}
