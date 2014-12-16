package com.example.mino.voicemap.activity;


import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.mino.voicemap.R;
import com.example.mino.voicemap.helper.GoogleGeocodingApiParser;
import com.example.mino.voicemap.helper.SpeechRecognitionHelper;
import com.example.mino.voicemap.loader.GeocodingAsyncTaskLoader;
import com.example.mino.voicemap.system.SystemData;
import com.google.android.gms.maps.model.LatLng;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;


/**
 * アプリ開始画面のActivity。<br/>
 * 音声認識で目的地の入力を受付け、住所検索APIで住所を検索する。<br/>
 * 目的地の住所検索が正常に終了したら、自動で地図用のActivityにその情報を含んだインテントを送る。
 */
public class MainActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Object> {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_ID_GEOCODING = 3;
    private static final String BUNDLE_KEY_URL = "bundle_key_url";
    private static final String URL_GEOCODING_BASE = "https://maps.googleapis.com/maps/api/geocode/";
    private static final String URL_GEOCODING_CONTENT_TYPE = "json";
    private static String region = "jp"; // 優先する地域

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate started.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Log.d(TAG, "onCreateOptionMenu started.");
//        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Log.d(TAG, "onOptionsItemSelected started.");
        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 音声認識の結果を処理する。<br/>
     * 音声認識で文字列の取得に成功した場合、そのままこれを目的地のキーワードとして住所検索APIを実行する。
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult started.");
        if(requestCode == SystemData.VOICE_RECOGNITION_REQUEST_CODE
                && resultCode == RESULT_OK) {
            // 音声認識のリクエストが成功した
            Log.d(TAG, "VOICE_RECOGNITION_REQUEST was Successful. ");
            // 結果のリストを取得。（今回のように結果を１つとしている場合もリストで返ってくる）
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            // 音声認識の結果文字列を取得
            String text = null;
            if(matches != null && matches.size() > 0 ) {
                text = matches.get(0);
            }

            // 音声認識の失敗 （結果リストがnullもしくは中身無し）
            if(text == null) {
                Toast.makeText(this, "識別に失敗しました。", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "結果がnull");
                return;
            }

            // 音声認識の結果をトーストで表示
            Toast.makeText(this, "「" + text + "」で住所検索を行います。", Toast.LENGTH_SHORT).show();

            // 住所検索APIの実行
            startGeocoding(text);
        } else {
            Log.d(TAG, "音声認識に失敗、もしくは音声認識以外のリクエストからの結果。 result=" + resultCode);
        }
//        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * 音声検索ボタンのクリックリスナー。<br/>
     * 音声検索プロセスを開始する。
     *
     * @param v 音声検索ボタン
     */
    public void onClickVoiceSearchButton(View v) {
        Log.d(TAG, "onClickVoiceSearchButton started.");
        SpeechRecognitionHelper.run(this);

        // 音声めんどくさいのでデバッグ用に直書き検索
//        String text = "名古屋城";
//        ArrayList<String> list = new ArrayList<String>();
//        list.add(text);
//        Intent intent = new Intent();
//        intent.putStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS, list);
//        onActivityResult(SystemData.VOICE_RECOGNITION_REQUEST_CODE, RESULT_OK, intent);
    }


    /**
     * Geocoding(住所検索)用Loaderを作成します。<br/>
     *
     * @param searchText 住所検索の対象となるテキスト
     */
    private void startGeocoding(String searchText) {
        Log.d(TAG, "startGeocoding started.");
        URL url = generateGeocodingURL(searchText);
        // Bundle作成
        Bundle bundle = new Bundle();
        bundle.putSerializable(BUNDLE_KEY_URL,url);
        Log.d(TAG, "initLoader.");

        LoaderManager lManager = getSupportLoaderManager();
        // Loaderの作成
        if(lManager.getLoader(REQUEST_ID_GEOCODING) == null) {
            Log.d(TAG, "initLoader.");
            getSupportLoaderManager().initLoader(REQUEST_ID_GEOCODING, bundle, this);
        } else {
            Log.d(TAG, "restartLoader");
            lManager.restartLoader(REQUEST_ID_GEOCODING,bundle,this);
        }
        Log.d(TAG, "startGeocoding finished.");
    }

    /**
     * 住所検索用APIのURLを生成します。<br/>
     *
     * @param searchText 住所検索の対象となるテキスト
     * @return パラメータを含めた住所検索APIのURL
     */
    private URL generateGeocodingURL(String searchText) {
        URL url = null;

        String urlString = URL_GEOCODING_BASE + URL_GEOCODING_CONTENT_TYPE + "?";
        try {
            // 検索文字をエンコード
            String address = URLEncoder.encode(searchText, "utf8");
            // パラメータの生成
            String parameters = "address=" + address + "&sensor=true&language=ja&region="+region;
            // URLの生成
            urlString = urlString + parameters;

            Log.d(TAG, "encodedURL =" + urlString);

            url = new URL(urlString);
        } catch (MalformedURLException e) {
            Log.d(TAG, "URLの生成に失敗しました。");
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            Log.d(TAG, "エンコードに失敗しました。");
            e.printStackTrace();
        }
        return url;
    }


    /**
     * TaskLoaderの作成後コールバック<br/>
     * ここでは、住所検索TaskLoaderをLoaderManagerに登録する。<br/>
     *
     * @param id
     * @param bundle
     * @return
     */
    @Override
    public android.support.v4.content.Loader onCreateLoader(int id, Bundle bundle) {
        Log.d(TAG, "onCreateLoader.");
        if (id == REQUEST_ID_GEOCODING) {
            URL url = (URL)bundle.getSerializable(BUNDLE_KEY_URL);
            return new GeocodingAsyncTaskLoader(this,url);
        }
        return null;
    }

    /**
     * TaskLoaderのタスク終了後コールバック。<br/>
     * ここでは、住所検索APIの完了後処理を担う。<br/>
     * APIの結果から「目的地の住所」、「目的地の緯度/軽度」の情報を取得し、
     * 地図用Activityにインテントを送る。
     *
     * @param loader
     * @param o
     */
    @Override
    public void onLoadFinished(Loader<Object> loader, Object o) {
        Log.d(TAG, "onLoadFinished.");
        int id = loader.getId();
        if(id == REQUEST_ID_GEOCODING) {
            // ロード後の返り値チェック
            if(o == null) {
                Toast.makeText(this,"住所の取得に失敗しました。",Toast.LENGTH_SHORT).show();
                return;
            }

            // Loaderから得た結果のMap
            Map<String, Object> map = (Map<String, Object>) o;

            // レスポンスステータスのチェック -> OKでなければトーストを表示して終了。
            String status = (String)map.get(GoogleGeocodingApiParser.MAP_KEY_STATUS);
            if(!"OK".equals(status)) {
                Toast.makeText(this,"住所の取得に失敗しました。",Toast.LENGTH_SHORT).show();
                Log.d(TAG, "住所取得失敗。status=" + status);
                return;
            }

            // 目的地の住所取得
            String address = (String) map.get(GoogleGeocodingApiParser.MAP_KEY_ADDRESS);
            Toast.makeText(this,"「 " + address + " 」に目的地を設定します。",Toast.LENGTH_SHORT).show();
            // 目的地の緯度経度取得
            LatLng latLng = (LatLng) map.get(GoogleGeocodingApiParser.MAP_KEY_LARLNG);

            // 地図アクティビティにインテント発信
            Intent intent = new Intent(this,MapViewActivity.class);
            intent.putExtra(SystemData.KEY_DEST_LATLNG,latLng);
            intent.putExtra(SystemData.KEY_DEST_ADDRESS,address);
            startActivity(intent);
        }
    }


    @Override
    public void onLoaderReset(android.support.v4.content.Loader loader) {
        Log.d(TAG, "onLoaderReset.");
    }

}
