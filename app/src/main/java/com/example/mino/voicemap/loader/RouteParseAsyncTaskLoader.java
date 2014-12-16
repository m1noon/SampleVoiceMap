package com.example.mino.voicemap.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.example.mino.voicemap.exception.RouteNotFoundException;
import com.example.mino.voicemap.helper.GoogleDirectionAPIParser;

import java.util.Map;

/**
 * Created by mino-hiroki on 2014/11/11.
 */
public class RouteParseAsyncTaskLoader extends AsyncTaskLoader<Map<String,Object>>{
    private static final String TAG = RouteParseAsyncTaskLoader.class.getSimpleName();

    private String mJsonText;

    public RouteParseAsyncTaskLoader(Context context, String jsonText) {
        super(context);
        mJsonText = jsonText;
    }

    @Override
    public Map<String,Object> loadInBackground() {
        Log.d(TAG, "loadInBackground stated.");

        // JSON文字列をパースして経路情報（地点のリスト）を取得

        try {
            return GoogleDirectionAPIParser.parse(mJsonText);
        } catch (RouteNotFoundException e) {
            Log.w(TAG, "Route was not found.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 1リクエストに対し１度きりの処理とするため、textをnullにしておく。
        mJsonText = null;

        return null;
    }

    @Override
    protected void onStartLoading() {
        Log.d(TAG, "onStartLoading started.");
        super.onStartLoading();
        if(mJsonText == null || mJsonText.length()==0) {
            Log.d(TAG, "do not forceLoad. jsonText is null.");
            return;
        }

        forceLoad();;
    }
}
