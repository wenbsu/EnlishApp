package com.liaoyunan.englishapp.activity;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.liaoyunan.englishapp.R;
import com.liaoyunan.englishapp.db.WordDB;
import com.liaoyunan.englishapp.model.Word;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Quhaofeng on 16-5-1.
 */
public class LearnWordActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private ListView wordListView;
    private List<Word.RECORDSBean> wordList = new ArrayList<Word.RECORDSBean>();
    /**
     * list中的数据
     */
    private List<String> dataList = new ArrayList<String>();
    private ArrayAdapter<String> mAdapter;
    private WordDB wordDB;
    private WebView mWebView;
    private int mIndex = 0;
    private TextView wordView;
    private TextView meaningView;
    private TextView yinbiaoView;
    private Button showHideBtn;
    private ImageButton btnPlay; // 添加播放按钮引用
    private String readWord = "word";
    private Word.RECORDSBean defaultWord = new Word.RECORDSBean();

    // 添加TextToSpeech相关变量
    private TextToSpeech textToSpeech;
    private boolean isTTSInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_book);

        defaultWord.setWord("word");
        defaultWord.setYinbiao("「音标」");
        defaultWord.setMeaning("「单词释义」");

        // 初始化视图
        initViews();
        // 设置点击监听器
        setupClickListeners();

        // 保留原有的WebView作为备用方案
        mWebView = (WebView) findViewById(R.id.web_view);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                Log.w("TAG", "" + newProgress);
                if (newProgress == 100) {
                    mWebView.loadUrl("javascript:(function() { var videos = document.getElementsByTagName('video'); for(var i=0;i<videos.length;i++){videos[i].play();}})()");
                }
            }
        });

        // 初始化TextToSpeech
        textToSpeech = new TextToSpeech(this, this);

        init();
    }

    /**
     * 初始化视图组件
     */
    private void initViews() {
        wordView = (TextView) findViewById(R.id.word);
        meaningView = (TextView) findViewById(R.id.meaning);
        yinbiaoView = (TextView) findViewById(R.id.yinbiao);
        wordListView = (ListView) findViewById(R.id.word_list1);
        showHideBtn = (Button) findViewById(R.id.show_hide_btn);
        btnPlay = (ImageButton) findViewById(R.id.btn_play); // 初始化播放按钮

        mAdapter = new ArrayAdapter<String>(this, R.layout.listview_item, R.id.list_item, dataList);
        wordListView.setAdapter(mAdapter);
    }

    /**
     * 设置点击监听器
     */
    private void setupClickListeners() {
        wordListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mIndex = position;
                setWord(wordList.get(mIndex));
                wordListView.setVisibility(View.GONE);
            }
        });

        showHideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wordListView.getVisibility() == View.GONE) {
                    wordListView.setVisibility(View.VISIBLE);
                } else {
                    wordListView.setVisibility(View.GONE);
                }
            }
        });

        // 设置播放按钮点击监听器
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playWord();
            }
        });
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // TTS初始化成功，设置语言为美式英语
            int result = textToSpeech.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w("TTS", "不支持该语言，将使用备用方案");
                isTTSInitialized = false;
                Toast.makeText(this, "语音引擎不支持英语，将使用在线发音", Toast.LENGTH_SHORT).show();
            } else {
                // 设置语速和音调
                textToSpeech.setSpeechRate(0.8f); // 稍慢一点，便于学习
                textToSpeech.setPitch(1.0f);
                isTTSInitialized = true;
                Log.i("TTS", "语音引擎初始化成功");
            }
        } else {
            Log.e("TTS", "语音引擎初始化失败，将使用备用方案");
            isTTSInitialized = false;
            Toast.makeText(this, "语音引擎初始化失败，将使用在线发音", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 初始化函数
     */
    public void init() {
        wordDB = WordDB.getInstance(this);
        wordList = wordDB.loadWordLib();//从单词库表获取单词

        if (wordDB.loadIndex() < 0) {
            wordDB.saveIndex( -1 );
        } else {
            mIndex = wordDB.loadIndex();//获取Index
        }

        dataList.clear();
        for (Word.RECORDSBean recordsBean : wordList) {
            dataList.add(recordsBean.getWord());
        }
        mAdapter.notifyDataSetChanged();
        if (wordList.isEmpty()){
            setWord(defaultWord);
        }else {
            setWord(wordList.get(mIndex));
        }
    }

    /**
     * 显示单词
     */
    public void setWord(Word.RECORDSBean word) {
        try {
            String s = word.getWord();
            wordView.setText(s);
            readWord = s;
            s = word.getYinbiao();
            yinbiaoView.setText(s);
            s = word.getMeaning();
            meaningView.setText(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 下一个单词
     */
    public void nextWord(View view) {
        if (mIndex < wordList.size() - 1) {
            mIndex++;
            setWord(wordList.get(mIndex));
            wordListView.setVisibility(View.GONE);
        }
    }

    /**
     * 上一个单词
     */
    public void previousWord(View view) {
        if (mIndex > 0) {
            mIndex--;
            setWord(wordList.get(mIndex));
            wordListView.setVisibility(View.GONE);
        }
    }

    /**
     * 添加到单词本
     */
    public void addToCollect(View view) {
        if (!wordList.isEmpty()){
            wordDB.addToCollect(wordList.get(mIndex));
        }
    }

    /**
     * 播放声音 - 改进版本（保留原方法以兼容XML中的onClick）
     */
    public void play(View view) {
        playWord();
    }

    /**
     * 播放单词发音的核心方法
     */
    private void playWord() {
        if (isTTSInitialized && textToSpeech != null) {
            // 使用TTS播放
            speakWord(readWord);
        } else {
            // 备用方案：使用原来的在线发音
            mWebView.loadUrl("http://dict.youdao.com/dictvoice?type=2&audio=" + readWord);
        }
    }

    /**
     * 使用TTS播放单词
     */
    private void speakWord(String word) {
        if (textToSpeech != null && isTTSInitialized) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    /**
     * back时回调，存储浏览索引到数据库
     * */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (wordList.size() == 0){
            wordDB.saveIndex(-1);
        }else {
            wordDB.saveIndex(mIndex);
        }
    }

    @Override
    protected void onDestroy() {
        // 释放TTS资源
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}