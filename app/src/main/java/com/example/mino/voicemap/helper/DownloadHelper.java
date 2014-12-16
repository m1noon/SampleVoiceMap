package com.example.mino.voicemap.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by mino-hiroki on 2014/11/11.
 */
public class DownloadHelper {
    private static final String TAG = DownloadHelper.class.getSimpleName();

    /**
     * 指定されたURLとHTTP通信を行い、取得したデータを文字列で返します。<br>
     * @param url ダウンロードを行うURL
     * @return String 結果の文字列
     * @throws IOException
     */
    public static String download(URL url) throws IOException {
        String data = "";
        InputStream is = null;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection)url.openConnection();
            is = connection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            StringBuffer sb = new StringBuffer();
            String line = "";
            while ( (line=br.readLine()) != null ) {
                sb.append(line);
            }
            data = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(is != null) is.close();
            if(url != null) connection.disconnect();
        }
        return data;
    }
}
