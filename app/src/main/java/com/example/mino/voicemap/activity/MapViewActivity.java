package com.example.mino.voicemap.activity;

import android.app.AlertDialog;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.mino.voicemap.R;
import com.example.mino.voicemap.fragment.LoadingFragment;
import com.example.mino.voicemap.helper.GoogleDirectionAPIParser;
import com.example.mino.voicemap.loader.RouteParseAsyncTaskLoader;
import com.example.mino.voicemap.loader.RouteSearchAsyncTaskLoader;
import com.example.mino.voicemap.system.SystemData;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

public class MapViewActivity extends FragmentActivity implements android.support.v4.app.LoaderManager.LoaderCallbacks{

    private static final String TAG = MapViewActivity.class.getSimpleName();

    private GoogleMap mMap;

    private static final int REQUEST_ID_ROUTE_SEARCH = 1;

    private static final int REQUEST_ID_PARSE_ROUTE = 2;


    private static final String BUNDLE_KEY_URL = "bundle_key_url";

    private static final String BUNDLE_KEY_JSON_STRING = "bundle_key_josn_string";

    private static final float DEFAULT_ZOOM_RATIO = 0.7f;

    private LatLng currentLatLng;

    private LatLng destLatLng;

    private String mDestAddress;

    private AlertDialog.Builder alertDialog;

    private LoadingFragment mLoadingFragment;

    PolylineOptions polylineOptions;

    /* ルート検索APIに用いるURL、設定など */
    private static final String URL_ROUTE_SEARCH_BASE = "https://maps.googleapis.com/maps/api/directions/";
    private static final String URL_ROUTE_SEARCH_CONTENT_TYPE = "json";
    private String travelMode = "driving";//default
    private static String region = "jp"; // 優先する地域

    // SaveInstanceState 用のキー
    private static final String SAVE_KEY_CURRENT_LATLNG = "save_key_current_latlng";
    private static final String SAVE_KEY_DEST_LATLNG = "save_key_dest_latlng";
    private static final String SAVE_KEY_DEST_ADDRESS = "save_key_dest_address";
    private static final String SAVE_KEY_POLYLINE_OPTIONS = "save_key_polyline_options";

    private static final String FRAGMENT_LOADING_TAG = "fragment_loading_tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        if(savedInstanceState == null) {
            // ルート検索中の黒画面をかぶせる
            mLoadingFragment = new LoadingFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.loading_container, mLoadingFragment, FRAGMENT_LOADING_TAG)
                    .commit();

            // インテントから目的地、住所を受け取ってフィールドにセット
            destLatLng = getIntent().getParcelableExtra(SystemData.KEY_DEST_LATLNG);
            mDestAddress = getIntent().getStringExtra(SystemData.KEY_DEST_ADDRESS);
        } else {
            currentLatLng = savedInstanceState.getParcelable(SAVE_KEY_CURRENT_LATLNG);
            destLatLng = savedInstanceState.getParcelable(SAVE_KEY_DEST_LATLNG);
            mDestAddress = savedInstanceState.getString(SAVE_KEY_DEST_ADDRESS);
        }

        // GoogleMapオブジェクトをフィールドにセット
        MapFragment mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map);
        mMap = mapFragment.getMap();

        // ロケーションを有効に設定
        mMap.setMyLocationEnabled(true);


        // 経路の取得に失敗した際のアラート
        alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(getString(R.string.alert_route_search_failed));
        alertDialog.setNeutralButton(R.string.close,null);

        // 現在地が変化したら、現在位置をカメラの中心に据える。
        // このリスナーは最初の呼び出しを処理した後、解除される。
        if(savedInstanceState == null) {
            mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location location) {

                    // 現在地を取得
                    currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    // 現在地と目的地の２地点のLatLngBoundsを作成
                    LatLngBounds.Builder latLngBuilder = LatLngBounds.builder();
                    latLngBuilder.include(currentLatLng);
                    latLngBuilder.include(destLatLng);
                    LatLngBounds bounds = latLngBuilder.build();
                    // boundsを元にカメラの更新設定を作成（２地点がカメラに収まるサイズ）
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 70);

                    // マップのカメラ位置をセット
                    mMap.moveCamera(cameraUpdate);

                    // ルート検索開始
                    startRouteSearch(currentLatLng, destLatLng);

                    // 初回だけ処理してリスナーを解除する
                    mMap.setOnMyLocationChangeListener(null);
                }
            });
        } else {
            polylineOptions = savedInstanceState.getParcelable(SAVE_KEY_POLYLINE_OPTIONS);
            mMap.addPolyline(polylineOptions);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map_view, menu);
        getActionBar().setHomeButtonEnabled(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Log.d(TAG, "onOptionsItemSelected. id=" + id);

        switch (id) {
            case android.R.id.home:
                Log.d(TAG, "icon was clicked.");
                MapViewActivity.this.finish();
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onStart() {
        super.onStart();

        // 目的地にマーカーをセット
        if(destLatLng != null) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(destLatLng);
            markerOptions.title(mDestAddress);
            mMap.addMarker(markerOptions);
        }
    }

    /**
     * ルート検索のLoaderを作成します。<br/>
     * @param origin
     * @param dest
     */
    private void startRouteSearch(LatLng origin, LatLng dest) {
        Log.d(TAG, "startRouteSearch started.");
        Bundle bundle = new Bundle();
        URL url = generateRouteSearchURL(origin, dest);
        if(url == null) {
            Log.d(TAG, "URLの生成に失敗しました。処理を中断します。");
            return;
        }
        bundle.putSerializable(BUNDLE_KEY_URL, url);
        getSupportLoaderManager().initLoader(REQUEST_ID_ROUTE_SEARCH,bundle,this);
    }

    /**
     * ルート検索のURLを生成します。<br/>
     *
     * @param origin 出発地の緯度経度
     * @param dest 目的地の文字列
     * @return
     */
    private URL generateRouteSearchURL(LatLng origin, LatLng dest) {
        String str_origin = "origin="+origin.latitude+","+origin.longitude;
        String str_dest = "destination="+dest.latitude+","+dest.longitude;
        String sensor = "sensor=false";
        //パラメータ
        String parameters = str_origin+"&"+str_dest+"&"+sensor + "&language=ja" +"&region="+ region + "&mode=" + travelMode;
        // リクエストURLの作成
        String urlStr = URL_ROUTE_SEARCH_BASE + URL_ROUTE_SEARCH_CONTENT_TYPE + "?" + parameters;
        URL url = null;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            Log.d(TAG, "URLの形式が不正です。");
            e.printStackTrace();
        }
        Log.d(TAG, "routeSearch url=" + url);
        return url;
    }

    /**
     * APIの結果のJSON文字列のパースを行うLoaderを生成します。<br/>
     * @param jsonText
     */
    private void startParseRoute(String jsonText) {
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY_JSON_STRING, jsonText);
        getSupportLoaderManager().initLoader(REQUEST_ID_PARSE_ROUTE,bundle,this);
    }

    @Override
    public android.support.v4.content.Loader onCreateLoader(int id, Bundle bundle) {
        Log.d(TAG, "onCreateLoader.");

        if(id == REQUEST_ID_ROUTE_SEARCH) {
            // ルート検索を実行する
            URL url = (URL) bundle.getSerializable(BUNDLE_KEY_URL);
            return new RouteSearchAsyncTaskLoader(this,url);
        } else if (id == REQUEST_ID_PARSE_ROUTE) {
            // 結果のパースを行う
            String jsonText = bundle.getString(BUNDLE_KEY_JSON_STRING);
            return new RouteParseAsyncTaskLoader(this, jsonText);
        }
        return null;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader loader, Object o) {
        Log.d(TAG, "onLoadFinished.");
        int id = loader.getId();

        if(id == REQUEST_ID_ROUTE_SEARCH) {
            // ルート検索結果
            Log.d(TAG, o.toString());
            String jsonText = String.valueOf(o);
            // ルート表示タスクの実行
            startParseRoute(jsonText);
        } else if( id == REQUEST_ID_PARSE_ROUTE ) {
            // ルートの表示結果
            Log.d(TAG, "PARSE_REQUEST succeed. object=" + o);
            if(o != null) {

                HashMap<String, Object> map = (HashMap<String, Object>) o;

                // 結果のMapから経路情報、目的地住所を取得
                List<LatLng> points = (List<LatLng>) map.get(GoogleDirectionAPIParser.KEY_POINTS);
                String endAddress = (String) map.get(GoogleDirectionAPIParser.KEY_END_ADDRESS);

                // 位置情報から表示用polylineの設定を生成
                polylineOptions = new PolylineOptions();
                polylineOptions.addAll(points);

                // 色など細かい設定
                polylineOptions.color(getResources().getColor(R.color.route_color));

                // ルートパース結果
                mMap.addPolyline(polylineOptions);
                Log.d(TAG, "RouteParse finished. end_address=" + endAddress);
            } else {
                // 経路取得失敗 -> アラート表示　（レスポンスコードがOK以外、もしくは通信そのものが失敗）
                Log.d(TAG, "経路の取得に失敗。");
                alertDialog.show();
            }

            getFragmentManager().beginTransaction()
                    .remove(mLoadingFragment)
                    .commit();
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader loader) {
        Log.d(TAG, "onLoaderReset.");
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState started.");
        super.onSaveInstanceState(outState);

        outState.putParcelable(SAVE_KEY_CURRENT_LATLNG,currentLatLng);
        outState.putParcelable(SAVE_KEY_DEST_LATLNG,destLatLng);
        outState.putParcelable(SAVE_KEY_POLYLINE_OPTIONS, polylineOptions);
        outState.putString(SAVE_KEY_DEST_ADDRESS,mDestAddress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        Log.d(TAG, "onRestoreInstanceState started.");
//        super.onRestoreInstanceState(savedInstanceState);
//
//        // 保存データ取得
//        currentLatLng = savedInstanceState.getParcelable(SAVE_KEY_CURRENT_LATLNG);
//        destLatLng = savedInstanceState.getParcelable(SAVE_KEY_DEST_LATLNG);
//        polylineOptions = savedInstanceState.getParcelable(SAVE_KEY_POLYLINE_OPTIONS);
//        // カメラの位置、ルートをセット
//        setCameraPosition(currentLatLng, destLatLng);
//        mMap.addPolyline(polylineOptions);
    }

    private void setCameraPosition(LatLng cur, LatLng dest) {
        // 現在地と目的地の２地点のLatLngBoundsを作成
        LatLngBounds.Builder latLngBuilder = LatLngBounds.builder();
        latLngBuilder.include(cur);
        latLngBuilder.include(dest);
        LatLngBounds bounds = latLngBuilder.build();
        // boundsを元にカメラの更新設定を作成（２地点がカメラに収まるサイズ）
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,70);
        // カメラの位置をセット
        mMap.moveCamera(cameraUpdate);
    }
}