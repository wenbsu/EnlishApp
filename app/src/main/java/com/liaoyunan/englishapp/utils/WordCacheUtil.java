package com.liaoyunan.englishapp.utils;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.liaoyunan.englishapp.model.Word;

import java.io.*;

public class WordCacheUtil {

    private static final long MIN_VALID_FILE_SIZE = 100; // 字节数，防止空文件误用

    /**
     * 获取指定日期的缓存文件
     */
    public static File getCacheFile(Context context, String dateStr) {
        File cacheDir = context.getCacheDir(); // /data/data/<package>/cache
        return new File(cacheDir, dateStr + ".json");
    }

    /**
     * 判断缓存是否存在并且有效
     */
    public static boolean isCacheValid(File cacheFile) {
        return cacheFile.exists() && cacheFile.length() > MIN_VALID_FILE_SIZE;
    }

    /**
     * 从缓存文件读取 Word 对象（兼容 API 21）
     */
    public static Word loadWordFromCache(File cacheFile) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(cacheFile)));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return new Gson().fromJson(builder.toString(), Word.class);
        } catch (Exception e) {
            Log.e("WordCacheUtil", "读取缓存失败: " + e.getMessage());
            return null;
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException ignored) {}
        }
    }

    /**
     * 将 Word 对象写入缓存文件（兼容 API 21）
     */
    public static void saveWordToCache(File cacheFile, Word word) {
        BufferedWriter writer = null;
        try {
            String json = new Gson().toJson(word);
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cacheFile)));
            writer.write(json);
        } catch (Exception e) {
            Log.e("WordCacheUtil", "写入缓存失败: " + e.getMessage());
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (IOException ignored) {}
        }
    }
}
