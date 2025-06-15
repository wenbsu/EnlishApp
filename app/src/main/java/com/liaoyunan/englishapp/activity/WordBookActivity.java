package com.liaoyunan.englishapp.activity;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.liaoyunan.englishapp.R;
import com.liaoyunan.englishapp.SlideCutListView;
import com.liaoyunan.englishapp.SlideCutListView.RemoveDirection;
import com.liaoyunan.englishapp.SlideCutListView.RemoveListener;
import com.liaoyunan.englishapp.db.WordDB;
import com.liaoyunan.englishapp.model.Word;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Quhaofeng on 16-5-1.
 */
public class WordBookActivity extends AppCompatActivity implements RemoveListener, TextToSpeech.OnInitListener {
    private TextView wordView;
    private TextView meaningView;
    private TextView yinbiaoView;
    private SlideCutListView wordListView;
    private List<Word.RECORDSBean> wordList = new ArrayList<Word.RECORDSBean>();
    private List<String> dataList = new ArrayList<String>();
    private ArrayAdapter<String> mAdapter;
    private WordDB wordDB;
    private Button showHideBtn;
    private WebView mWebView;
    private ImageButton add;
    private ImageButton btnPlay; // 添加播放按钮引用
    private String readWord = "word";
    private Word.RECORDSBean defaultWord = new Word.RECORDSBean();
    /**
     * 单词索引
     */
    private int mIndex = 0;

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
        wordListView = (SlideCutListView) findViewById(R.id.word_list);
        add = (ImageButton) findViewById(R.id.add);
        add.setVisibility(View.GONE);
        showHideBtn = (Button) findViewById(R.id.show_hide_btn);
        btnPlay = (ImageButton) findViewById(R.id.btn_play); // 初始化播放按钮

        wordListView.setRemoveListener(this);
        mAdapter = new ArrayAdapter<String>(this, R.layout.listview_item, R.id.list_item, dataList);
        wordListView.setAdapter(mAdapter);
    }

    /**
     * 设置点击监听器
     */
    private void setupClickListeners() {
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

        wordListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mIndex = position;
                setWord(wordList.get(mIndex));
                wordListView.setVisibility(View.GONE);
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
        wordList = wordDB.loadWordCelect();//从单词本表获取单词
        if (wordList.isEmpty()) {
            Toast.makeText(this, "未添加任何单词", Toast.LENGTH_SHORT).show();
        } else {
            dataList.clear();
            for (Word.RECORDSBean recordsBean : wordList) {
                dataList.add(recordsBean.getWord());
            }
            mAdapter.notifyDataSetChanged();
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
        }
    }

    /**
     * 上一个单词
     */
    public void previousWord(View view) {
        if (mIndex > 0) {
            mIndex--;
            setWord(wordList.get(mIndex));
        }
    }

    /**
     * 播放单词发音的核心方法
     */
    private void playWord() {
        mWebView.loadUrl("http://dict.youdao.com/dictvoice?type=2&audio=" + readWord);
/*        if (isTTSInitialized && textToSpeech != null) {
            // 使用TTS播放
            speakWord(readWord);
        } else {
            // 备用方案：使用原来的在线发音
            mWebView.loadUrl("http://dict.youdao.com/dictvoice?type=2&audio=" + readWord);
        }*/
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
     * 删除单词
     */
    @Override
    public void removeItem(RemoveDirection direction, int position) {
        switch (direction) {
            case RIGHT:
                wordDB.deleteFromCol(mAdapter.getItem(position));
                break;
            case LEFT:
                wordDB.deleteFromCol(mAdapter.getItem(position));
                break;
            default:
                break;
        }
        mAdapter.remove(mAdapter.getItem(position));
        wordList.remove(wordList.get(position));
        if (wordList.size() != 0) {
            if ((wordList.size()) == mIndex) {
                mIndex = mIndex -1;
                setWord(wordList.get(mIndex));
            } else {
                setWord(wordList.get(mIndex));
            }
        } else {
            setWord(defaultWord);
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