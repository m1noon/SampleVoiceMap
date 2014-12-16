package com.example.mino.voicemap.helper;

import android.util.Log;

import com.example.mino.voicemap.exception.RouteNotFoundException;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mino-hiroki on 2014/11/11.
 */
public class GoogleDirectionAPIParser {
    private static final String TAG = GoogleDirectionAPIParser.class.getSimpleName();

    public static final String KEY_POINTS = "points";

    public static final String KEY_END_ADDRESS = "end_address";

    /**
     * GoogleDirectionsAPIで取得したJSON文字列をパースして、
     * 経路情報を表すLatLngのリストを返します。
     * @param jsonString
     * @return
     */
    public static Map<String,Object> parse(String jsonString) {
        Map<String,Object> retMap = new HashMap<String, Object>();
        // 経路情報を保持するリスト
        List<LatLng> returnList = new ArrayList<LatLng>();
        // 目的地の住所を表す文字列
        String endAddress = "";
        try {
            JSONObject jsonObject = new JSONObject(jsonString);

            String status = jsonObject.getString("status");
            if( !"OK".equals(status) ) {
                Log.d(TAG, "route download was not OK. status=" + status);
                return null;
            }

            // routesの中から最初のrouteを取得
            JSONArray routes = (JSONArray)jsonObject.getJSONArray("routes");
            if(routes == null || routes.length() == 0) {
                throw new RouteNotFoundException();
            }

            JSONObject route = (JSONObject)routes.get(0);
            JSONArray legs = route.getJSONArray("legs");
            JSONArray steps;
            String points;

            int legsLentgh = legs.length();
            for(int i=0; i < legsLentgh; i++) {
                // 各legからステップのリストを取得
                steps = ((JSONObject)legs.get(i)).getJSONArray("steps");
                int stepsLength = steps.length();
                for (int j=0; j < stepsLength; j++) {
                    // stepsからstepを取得
                    JSONObject step = steps.getJSONObject(j);
                    // 各stepのpolylineからpointsを取得
                    points = step.getJSONObject("polyline").getString("points");
                    Log.d(TAG, "points=" +points);
                    // polyline文字列を位置情報を表すLatLngのリストにデコード
                    List<LatLng> latLngList = PolylineUtil.decode(points);
                    // 返却用リストに追加
                    returnList.addAll(latLngList);
                }

            }
            // ルートの目的地を取得
            endAddress = legs.getJSONObject(legsLentgh -1).getString("end_address");
        } catch (JSONException e) {
            Log.e(TAG, "JSONのパースに失敗しました。" + e.toString());
            e.printStackTrace();
            return null;
        }
        retMap.put(KEY_POINTS,returnList);
        retMap.put(KEY_END_ADDRESS,endAddress);
        return retMap;
    }

    static class PolylineUtil {

        /**
         * デコードを行う処理。ネットから拝借。<br/>
         * http://foonyan.sakura.ne.jp/wisteriahill/gmap_androidapiv2II_memo5/index.html
         * @param encoded
         * @return
         */
        public static List<LatLng> decode(String encoded) {
            List<LatLng> poly = new ArrayList<LatLng>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }

            return poly;
        }
    }
}
