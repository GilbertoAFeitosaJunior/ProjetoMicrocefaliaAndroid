package br.com.mobi.redemicro.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class GeoLocation {

    private final Util util;
    private static final String TAG = GeoLocation.class.getSimpleName();

    private double lat;
    private double lng;
    private String address;

    public GeoLocation(Context context) {
        util = new Util(context);
    }

    public void setLatLng(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /***
     * Colocar a função dentro de uma asynctask para evitar erros de HTTP Security
     */
    public Map<TypeAddress, String> translate() {
        Map<TypeAddress, String> data = null;
        if (TextUtils.isEmpty(address)) {
            data = decodeLatLng(lat, lng);
        } else {
            data = encodeLatLng(address);
        }
        return data;
    }

    public enum TypeAddress {

        COMPLETE, NUMBER, ADDRESS, NEIGHBOR, CITY, STATE, COUNTRY, CEP, LATLNG, LAT, LNG
    }

    private Map<TypeAddress, String> encodeLatLng(final String address) {
        Map<TypeAddress, String> map = new HashMap<>();
        try {
            URL url = new URL("https://maps.googleapis.com/maps/api/geocode/json?address=" + URLEncoder.encode(address, "UTF8") + "&sensor=false");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("charset", "utf-8");
            connection.setUseCaches(false);
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                InputStream is = connection.getInputStream();

                JSONObject jsonObject = new JSONObject(util.inputStreamToString(is));
                JSONObject results = jsonObject.getJSONArray("results").getJSONObject(0);
                JSONObject geometry = results.getJSONObject("geometry");
                JSONObject location = geometry.getJSONObject("location");

                map.put(TypeAddress.LAT, String.valueOf(location.getDouble("lat")));
                map.put(TypeAddress.LNG, String.valueOf(location.getDouble("lng")));
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao tentar obter o endereço." + this.getClass().getName());
            e.printStackTrace();
        } finally {
            return map;
        }
    }

    private Map<TypeAddress, String> decodeLatLng(double latitude, double longitude) {
        Map<TypeAddress, String> map = new EnumMap<>(TypeAddress.class);
        map.put(TypeAddress.LATLNG, latitude + "," + longitude);
        try {
            if (latitude != 0 && longitude != 0) {

                URL url = new URL("https://maps.googleapis.com/maps/api/geocode/json?latlng=" + latitude + "," + longitude + "&sensor=false");
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setInstanceFollowRedirects(false);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("charset", "utf-8");
                connection.setUseCaches(false);
                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    InputStream is = connection.getInputStream();

                    JSONObject jsonObject = new JSONObject(util.inputStreamToString(is));
                    JSONObject results = jsonObject.getJSONArray("results").getJSONObject(0);
                    JSONArray addressComponents = results.getJSONArray("address_components");
                    for (int i = 0; i < addressComponents.length(); i++) {
                        JSONObject adrs = addressComponents.getJSONObject(i);

                        String s = adrs.getString("short_name");
                        String type = adrs.getJSONArray("types").getString(0);
                        switch (type) {
                            case "route":
                                map.put(TypeAddress.ADDRESS, s);
                                break;
                            case "sublocality":
                            case "political":
                            case "sublocality_level_1":
                                map.put(TypeAddress.NEIGHBOR, s);
                                break;
                            case "locality":
                                map.put(TypeAddress.CITY, s);
                                break;
                            case "administrative_area_level_1":
                                map.put(TypeAddress.STATE, s);
                                break;
                            case "country":
                                map.put(TypeAddress.COUNTRY, s);
                                break;
                            case "street_number":
                                map.put(TypeAddress.NUMBER, s);
                                break;
                            case "postal_code":
                                map.put(TypeAddress.CEP, s);
                                break;
                        }
                    }
                    map.put(TypeAddress.COMPLETE, results.getString("formatted_address"));
                }
            }
        } catch (Exception e) {
            Log.e(TAG,"Erro ao tentar obter o endereço." + this.getClass().getName());
            e.printStackTrace();
        } finally {
            return map;
        }
    }

}
