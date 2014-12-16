package com.example.mino.voicemap.helper;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.RecognizerIntent;
import android.util.Log;

import com.example.mino.voicemap.system.SystemData;

import java.util.List;

/**
 * Created by mino-hiroki on 2014/11/11.
 */
public class SpeechRecognitionHelper {
    private static final String TAG = SpeechRecognitionHelper.class.getSimpleName();


    /**
     * 音声認識プロセスを実行します。<br/>
     * 認識アクティビティの有無を確認し、アクティビティがない場合はGooglePlayに
     * 誘導してGoogle音声検索のインストールを行います。<br/>
     * アクティビティがある場合は、インテントを送信して実行します。<br/>
     * @param callingActivity
     */
    public static void run(Activity callingActivity)  {
        if(hasSpeechRecognitionActivity(callingActivity)) {
            // 音声認識がインストール済みの場合、音声認識処理を実行。
            startRecognition(callingActivity);
        } else {
            // 音声認識がインストールされていない場合、インストールの催促を行う。
            installGoogleVoiceSearch(callingActivity);
        }
    }

    /**
     * 音声認識要求のインテントを送信します。<br/>
     * @param callingActivity
     */
    public static void startRecognition(Activity callingActivity) {
        Log.d(TAG, "startRecognition started.");
        // 音声認識要求インテントの作成
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // 追加パラメータ
        // ユーザーへのヒント
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "目的の場所を述べよ。");
        // 認識モデル。これにより解析が最適化される。 WEB_SEARCH or FREE_FORM から選ぶ。
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        // 取得する結果の数。ここでは、最も関連性の高そうな結果のみを取得。
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        // 言語設定
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ja");

        // アクティビティの開始
        callingActivity.startActivityForResult(intent, SystemData.VOICE_RECOGNITION_REQUEST_CODE);
    }

    /**
     * 端末に音声認識用のアクティビティが存在するかをチェックします。<br/>
     * @return
     */
    public static boolean hasSpeechRecognitionActivity(Activity callingActivity) {
        Log.d(TAG, "hasSpeechRecognitionActivity started.");
        PackageManager pm = callingActivity.getPackageManager();
        List activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH),0);

        if(activities == null || activities.size() == 0) {
            return false;
        }
        return true;
    }


    /**
     * グーグル音声検索アプリをインストールします。<br/>
     *
     * @param ownerActivity
     */
    public static void installGoogleVoiceSearch(final Activity ownerActivity) {
        Log.d(TAG, "installGoogleVoiceSearch started.");
    }


    /**
     * 文字列が英数字のみからなるかチェックします。
     *
     * @param str
     * @return true=英数字のみ、 false=英数字以外が含まれる
     */
    public static boolean chekOnlyAlphabet(String str) {
        // 正規表現で英数字のみからなる文字列かをチェック
        return str.matches("[0-9a-zA-Z]+");
    }
}
