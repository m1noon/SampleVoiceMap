package com.example.mino.voicemap.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.example.mino.voicemap.R;
import com.example.mino.voicemap.util.TypeFaceUtil;

/**
 * アクションバーのタイトル表示用テキストビュー<br/>
 *
 * Created by mino-hiroki on 2014/11/12.
 */
public class SimbolTextView extends TextView {
    private static final String TAG = SimbolTextView.class.getSimpleName();

    private static final String VIEW_TAG = "FONT_LOCK";

    public SimbolTextView(Context context) {
        super(context);
    }

    public SimbolTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SimbolTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 初期化処理。
     */
    private void init() {
        if(isInEditMode()) {
            // 編集モードの場合はデフォルトフォント
            return;
        }

        // シンボルフォントをセット
        TypeFaceUtil.setTypefaceToViewInSynchronization(this, getContext().getString(R.string.assets_font_ameba_symbol));
        // 以降TypefaceUtilによってフォントが変更されないようにタグを追加
        setTag(VIEW_TAG);
    }
}
