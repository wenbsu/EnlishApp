package com.liaoyunan.englishapp.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

public class AudioPlayerUtil {
    private static final String TAG = "AudioPlayerUtil";
    private static final String AUDIO_PLAYER_URL = "http://dict.youdao.com/dictvoice?type=2&audio=";
    private static MediaPlayer mediaPlayer;

    /**
     * 播放网络音频
     * 播放音频，先查缓存，缓存存在直接播放，否则下载
     * @param context 应用上下文
     * @param word    单词
     */
    public static void playAudioFromUrl(Context context, String word) {
        String audioUrl = AUDIO_PLAYER_URL + word;
        File cacheFile = new File(context.getCacheDir(), getSafeFileName(word) + ".mp3");

        if (cacheFile.exists()) {
            Log.d(TAG, "使用本地缓存音频: " + cacheFile.getAbsolutePath());
            playLocal(cacheFile);
        } else {
            Log.d(TAG, "缓存未命中，开始下载音频: " + word);
            downloadAndPlay(audioUrl, cacheFile);
        }
    }


    /**
     * 下载音频并播放，保存到指定缓存文件
     */
    private static void downloadAndPlay(String url, File targetFile) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0") // 防止 403
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "下载失败: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    BufferedSink sink = Okio.buffer(Okio.sink(targetFile));
                    sink.writeAll(response.body().source());
                    sink.close();

                    Log.d(TAG, "音频下载完成: " + targetFile.getAbsolutePath());
                    playLocal(targetFile);
                } else {
                    Log.e(TAG, "音频下载响应无效");
                }
            }
        });
    }

    /**
     * 播放本地音频文件
     */
    private static void playLocal(File audioFile) {
        if (!audioFile.exists()) {
            Log.e(TAG, "音频文件不存在: " + audioFile.getAbsolutePath());
            return;
        }

        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioFile.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();

            mediaPlayer.setOnCompletionListener(mp -> {
                Log.d(TAG, "播放完成");
                mp.release();
            });

        } catch (IOException e) {
            Log.e(TAG, "播放失败: " + e.getMessage());
        }
    }

    /**
     * 将单词转换为合法的文件名（用MD5避免非法字符）
     */
    private static String getSafeFileName(String word) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(word.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(word.hashCode());
        }
    }

    /**
     * 释放播放器资源（可在 Activity.onDestroy() 调用）
     */
    public static void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
