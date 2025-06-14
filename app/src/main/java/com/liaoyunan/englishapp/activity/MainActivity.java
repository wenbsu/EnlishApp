package com.liaoyunan.englishapp.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.liaoyunan.englishapp.RetrofitRequest;
import com.liaoyunan.englishapp.R;
import com.liaoyunan.englishapp.db.WordDB;
import com.liaoyunan.englishapp.model.Word;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mTextView;

    private String result = null;

    // GitHub Raw 文件的 baseUrl
    private String baseUrl = "https://raw.githubusercontent.com/";

    private String url4Path = "/wenbsu/vocabulary/master/2025_06_14.json";

    private String url6Path = "/wenbsu/vocabulary/master/2025_06_17.json";

    private String currentUrlPath = "/wenbsu/vocabulary/master/2025_06_14.json"; // 默认CET4

    private WordDB wordDB;

    private Word myWord;

    private List<Word.RECORDSBean> wordList = new ArrayList<Word.RECORDSBean>();

    private List<Word.RECORDSBean> wordBookList = new ArrayList<Word.RECORDSBean>();

    private Button wordBookBtn;

    private Button wordViewBtn;

    private Button wordTestBtn;

    private Button cet4Btn;

    private Button cet6Btn;

    private TextView learnMax;

    // RetrofitRequest 实例
    private RetrofitRequest<Word> retrofitRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化 RetrofitRequest
        retrofitRequest = new RetrofitRequest<>(baseUrl, Word.class);

        wordDB = WordDB.getInstance(this);

        mTextView = (TextView) findViewById(R.id.text_view);

        wordBookBtn = (Button) findViewById(R.id.word_book_btn);

        wordBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WordBookActivity.class);
                startActivity(intent);
            }
        });

        wordViewBtn = (Button) findViewById(R.id.view_word_btn);

        wordViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LearnWordActivity.class);
                startActivity(intent);
            }
        });

        wordTestBtn = (Button) findViewById(R.id.word_test_btn);

        wordTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WordTestActivity.class);
                startActivity(intent);
            }
        });

        cet4Btn = (Button) findViewById(R.id.get_CET4_btn);
        cet4Btn.setOnClickListener(this);
        cet6Btn = (Button) findViewById(R.id.get_CET6_btn);
        cet6Btn.setOnClickListener(this);

        learnMax = (TextView) findViewById(R.id.learn_max);
        init();
    }

    /**
     * 初始化函数
     */
    public void init() {
        wordList = wordDB.loadWordLib();
        if (wordList.size() <= 0) {
            //wordDB.createTestData();//添加了测试数据
            getWordLib();
        }
        if (wordDB.loadMaxIndex() >= 0){
            learnMax.setText("总共学习了：" + wordDB.loadMaxIndex() + "个单词");
        }
    }

    /**
     * 从网上获取获取单词库 - 使用 RetrofitRequest 替代 GsonRequest
     */
    public void getWordLib() {
        if (!isNetwork(this)) {
            Toast.makeText(this, "网络连接错误", Toast.LENGTH_SHORT).show();
            return;
        }

        // 使用 RetrofitRequest 发起网络请求
        retrofitRequest.get(currentUrlPath,
                new RetrofitRequest.ResponseListener<Word>() {
                    @Override
                    public void onResponse(Word word) {
                        // 请求成功
                        myWord = word;
                        wordDB.saveWordLib(myWord);
                        mTextView.setText("词库下载成功");

                        // 可选：记录日志
                        Log.d("MainActivity", "词库下载成功，单词数量: " +
                                (word.getRECORDS() != null ? word.getRECORDS().size() : 0));
                    }
                },
                new RetrofitRequest.ErrorListener() {
                    @Override
                    public void onError(String error) {
                        // 请求失败
                        Log.e("MainActivity", "词库下载失败: " + error);
                        mTextView.setText("词库下载出错: " + error);

                        // 显示错误提示
                        Toast.makeText(MainActivity.this, "下载失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 判断网络是否连接
     */
    public boolean isNetwork(Context context) {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                // 获取网络连接管理的对象
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null && info.isConnected()) {
                    // 判断当前网络是否已经连接
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Log.v("error", e.toString());
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        wordDB.deleteAll();
        int id = v.getId();

        if (id == R.id.get_CET4_btn) {
            currentUrlPath = url4Path;
            mTextView.setText("正在下载CET4词库...");
            Log.d("MainActivity", "开始下载CET4词库");
        } else if (id == R.id.get_CET6_btn) {
            currentUrlPath = url6Path;
            mTextView.setText("正在下载CET6词库...");
            Log.d("MainActivity", "开始下载CET6词库");
        }

        wordDB.saveIndex(0);
        getWordLib();
    }

    /**
     * 开始的时候更新学习进度索引
     * */
    @Override
    protected void onStart(){
        super.onStart();
        if (wordDB.loadMaxIndex() >= 0){
            int index = wordDB.loadMaxIndex();
            learnMax.setText("总共学习了：" + index + "个单词");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理资源，可选
        if (retrofitRequest != null) {
            // 如果 RetrofitRequest 有取消请求的方法，可以在这里调用
        }
    }
}