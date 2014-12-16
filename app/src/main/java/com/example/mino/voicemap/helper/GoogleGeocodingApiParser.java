package com.example.mino.voicemap.helper;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mino-hiroki on 2014/11/11.
 */
public class GoogleGeocodingApiParser {
    private static final String TAG = GoogleGeocodingApiParser.class.getSimpleName();

    public static final String MAP_KEY_ADDRESS = "map_key_address";

    public static final String MAP_KEY_LARLNG = "map_key_latlng";

    public static final String MAP_KEY_STATUS = "map_key_status";

    /**
     * Google Geocoding API で取得したJSONテキストをパースしてマップに格納します。
     *
     * @param jsonText
     * @return
     */
    public static Map<String,Object> parse(String jsonText){
        Log.d(TAG, "parse started.");
        Map<String,Object> retMap = new HashMap<String, Object>();

        String status = null;
        String address = null;
        LatLng latLng = null;
        try {
            JSONObject jsonObject = new JSONObject(jsonText);

            status = jsonObject.getString("status");
            // ステータスがOKでなければ終了
            if( !"OK".equals(status) ) {
                Log.w(TAG, "API request failed. status = " + status);
                retMap.put(MAP_KEY_STATUS, status);
                return retMap;
            }

            // 住所の取得
            JSONObject result = jsonObject.getJSONArray("results").getJSONObject(0);
            address = result.getString("formatted_address");

            // Locationの取得
            JSONObject location = result.getJSONObject("geometry").getJSONObject("location");
            float lat = Float.parseFloat(location.getString("lat"));
            float lng = Float.parseFloat(location.getString("lng"));
            latLng = new LatLng(lat,lng);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        retMap.put(MAP_KEY_STATUS, status);
        retMap.put(MAP_KEY_ADDRESS,address);
        retMap.put(MAP_KEY_LARLNG,latLng);
        return retMap;
    }
}
