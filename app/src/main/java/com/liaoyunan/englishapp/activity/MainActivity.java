package com.liaoyunan.englishapp.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.liaoyunan.englishapp.RetrofitRequest;
import com.liaoyunan.englishapp.R;
import com.liaoyunan.englishapp.db.WordDB;
import com.liaoyunan.englishapp.model.Word;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mTextView;
    private CalendarView calendarView;

    private String result = null;

    // GitHub Raw 文件的 baseUrl
    private String baseUrl = "https://raw.githubusercontent.com/";

    // 动态生成的路径，基于选择的日期
    private String currentUrlPath;

    private WordDB wordDB;

    private Word myWord;

    private List<Word.RECORDSBean> wordList = new ArrayList<Word.RECORDSBean>();

    private List<Word.RECORDSBean> wordBookList = new ArrayList<Word.RECORDSBean>();

    private TextView learnMax;

    // RetrofitRequest 实例
    private RetrofitRequest<Word> retrofitRequest;

    // 日期格式化器
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy_MM_dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化 RetrofitRequest
        retrofitRequest = new RetrofitRequest<>(baseUrl, Word.class);

        wordDB = WordDB.getInstance(this);

        mTextView = (TextView) findViewById(R.id.text_view);

        // 初始化日历控件
        calendarView = (CalendarView) findViewById(R.id.calendar_view);

        // 设置日历选择监听器
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                // 注意：月份从0开始，所以需要+1
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(year, month, dayOfMonth);

                String selectedDateStr = dateFormatter.format(selectedDate.getTime());
                Log.d("MainActivity", "选择的日期: " + selectedDateStr);

                // 根据选择的日期加载单词
                loadWordsForDate(selectedDateStr);
            }
        });

        // 修改为LinearLayout的点击事件
        LinearLayout wordBookBtn = findViewById(R.id.word_book_btn);
        wordBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WordBookActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout wordViewBtn = findViewById(R.id.view_word_btn);
        wordViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LearnWordActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout wordTestBtn = findViewById(R.id.word_test_btn);
        wordTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WordTestActivity.class);
                startActivity(intent);
            }
        });

        learnMax = (TextView) findViewById(R.id.learn_max);
        init();

        // ✅ 立刻加载今天的单词（不等用户点击）
        String today = dateFormatter.format(new Date());
        loadWordsForDate(today);
    }

    /**
     * 初始化函数
     */
    public void init() {
        wordList = wordDB.loadWordLib();
        if (wordList.size() <= 0) {
            // 初始化时加载今天的单词
            String today = dateFormatter.format(new Date());
            loadWordsForDate(today);
        }
        if (wordDB.loadMaxIndex() >= 0){
            learnMax.setText("总共学习了：" + wordDB.loadMaxIndex() + "个单词");
        }
    }

    /**
     * 根据指定日期加载单词
     * @param dateStr 格式为 yyyy_MM_dd 的日期字符串
     */
    private void loadWordsForDate(String dateStr) {
        // 构建对应日期的URL路径
        currentUrlPath = "/wenbsu/vocabulary/master/" + dateStr + ".json";

        // 清空当前词库
        wordDB.deleteAll();

        // 显示加载状态
        mTextView.setText("⏳ 正在加载 " + dateStr + " 的单词库...");
        mTextView.setVisibility(View.VISIBLE);

        // 重置学习索引
        wordDB.saveIndex(0);

        // 获取单词库
        getWordLib();
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
                        mTextView.setText("✅ 词库下载成功");
                        mTextView.setVisibility(View.VISIBLE);

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
                        mTextView.setText("❌ 词库下载出错: " + error);
                        mTextView.setVisibility(View.VISIBLE);

                        // 显示错误提示
                        Toast.makeText(MainActivity.this, "下载失败，请检查网络连接或该日期的词库是否存在", Toast.LENGTH_SHORT).show();
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
        // 这个方法现在不再需要，因为已经移除了CET4和CET6按钮
        // 保留这个方法以防其他地方还有使用
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