package com.example.mino.voicemap.util;

import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * 書体の変更機能を提供するUtilクラス。<br/>
 *
 * Created by mino-hiroki on 2014/11/12.
 */
public class TypeFaceUtil {
    private static final String TAG = TypeFaceUtil.class.getSimpleName();

    /**
     * 排他制御のためのロックオブジェクト
     */
    private static final Object LOCK = new Object();

    private TypeFaceUtil() {
    }

    /**
     * 指定されたビューに、指定されたフォントをセットします。<br/>
     * その際、アセットへのアクセスを排他制御します。<br/>
     *
     * @param view
     * @param fontoFileName
     */
    public static void setTypefaceToViewInSynchronization(final View view, final String fontoFileName) {
        Typeface typeface = null;

        // assetsのフォントファイルへのアクセスをスレッドセーフにするため、排他制御用オブジェクトに対して同期をとる
        synchronized (LOCK) {
            typeface = Typeface.createFromAsset(view.getContext().getAssets(),fontoFileName);
        }
        // ビューに対して書体をセット
        setTypefaceToView(view,typeface);
    }

    /**
     * 指定されたビュー（およびその子要素のビュー）に書体をセットする。<br/>
     *
     * @param view
     * @param typeface
     */
    private static void setTypefaceToView(final View view, final Typeface typeface) {
        if(view instanceof TextView && view.getTag() == null) {
            // テキストビューなら書体をセット
            ((TextView)view).setTypeface(typeface);
        } else if(view instanceof ViewGroup){
            // ビューグループなら、各子要素に対してフォントをセット
            final ViewGroup vg = (ViewGroup)view;
            final int childCount = vg.getChildCount();
            for (int i = 0; i < childCount; i++) {
                setTypefaceToView(vg.getChildAt(i),typeface);
            }
        }
    }
}
