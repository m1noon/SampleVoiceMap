package com.example.mino.voicemap.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.example.mino.voicemap.helper.DownloadHelper;
import com.example.mino.voicemap.helper.GoogleGeocodingApiParser;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Random;

/**
 * Created by mino-hiroki on 2014/11/11.
 */
public class GeocodingAsyncTaskLoader extends AsyncTaskLoader<Map<String,Object>>{
    private static final String TAG = GeocodingAsyncTaskLoader.class.getSimpleName();

    private URL mUrl;
    private int myid;

    public GeocodingAsyncTaskLoader(Context context, URL url) {
        super(context);
        mUrl = url;
        Random r = new Random();
        myid = r.nextInt();
        Log.d(TAG, "constructor finished. id=" + myid);
    }

    @Override
    public Map<String, Object> loadInBackground() {
        Log.d(TAG, "loadInBackground started. url=" + mUrl);
        String jsonText = null;

        // URLからデータを取得
        try {
            jsonText = DownloadHelper.download(mUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // API結果をパースして必要な情報をマップで取得
        Log.d(TAG, "jsonText=" + jsonText);
        Map<String ,Object> result = GoogleGeocodingApiParser.parse(jsonText);

//        処理が終わるときにURLを空にして、一度しか呼ばれないようにする
        mUrl = null;

        Log.d(TAG, "loadInBackground finished.");
        return result;
    }

    @Override
    protected void onStartLoading() {
        Log.d(TAG, "onStartLoading started.");

        super.onStartLoading();
        // URLが存在しないもしくはデータに変更がない場合、処理をしない。
        if( mUrl == null) {
            Log.d(TAG, "do not force load. URL is null.");
            cancelLoad();
            return;
        }

        // タスクの実行
        Log.d(TAG, "forceLoad.");
        forceLoad();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        Log.d(TAG, "onContentChanged started.");
    }



}
