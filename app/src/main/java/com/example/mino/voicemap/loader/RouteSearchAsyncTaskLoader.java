package com.example.mino.voicemap.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.example.mino.voicemap.helper.DownloadHelper;

import java.io.IOException;
import java.net.URL;

/**
 * Created by mino-hiroki on 2014/11/11.
 */
public class RouteSearchAsyncTaskLoader extends AsyncTaskLoader<String> {
    private static final String TAG = RouteSearchAsyncTaskLoader.class.getSimpleName();

    URL mUrl;

    public RouteSearchAsyncTaskLoader(Context context, URL url) {
        super(context);
        mUrl = url;
    }

    /**
     * コンストラクタで指定されたURLとHttp通信を行い、結果の文字列を返す。
     * @return
     */
    @Override
    public String loadInBackground() {
        Log.d(TAG, "loadInBackground started.");
        String data = null;
        if(mUrl == null) {
            Log.w(TAG, "URL is null.");
            return null;
        }
        try {
            data = DownloadHelper.download(mUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // １リクエストに対して１度だけ処理するよう、URLをnullにしておく。
        mUrl = null;
        return data;
    }

    @Override
    protected void onStartLoading() {
        Log.d(TAG, "onStartLoading starterd.");
        super.onStartLoading();

        // urlがnullなら実行せず終了。
        if(mUrl == null) {
            Log.d(TAG, "do not forceLoad. URL is null.");
            cancelLoad();
            return;
        }

        forceLoad();
    }
}
