package com.liaoyunan.englishapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.liaoyunan.englishapp.R;
import com.liaoyunan.englishapp.db.WordDB;
import com.liaoyunan.englishapp.model.Word;
import com.liaoyunan.englishapp.model.WordTest;
import com.liaoyunan.englishapp.utils.AudioPlayerUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 单词测试Activity
 */
public class WordTestActivity extends AppCompatActivity implements View.OnClickListener {

    private WordDB wordDB;
    private List<Word.RECORDSBean> mRECORDSBeanList = new ArrayList<>();
    private List<WordTest.Test> mTests = new ArrayList<>();
    private int mIndex;
    private int score = 0;
    private int testIndex = 0;
    private int rightChoose = 0;
    private String currectAnswer = "";
    private String currentWord = "";

    // UI控件
    private TextView wordView;
    private TextView chooseAText, chooseBText, chooseCText, chooseDText;
    private LinearLayout chooseA, chooseB, chooseC, chooseD;
    private TextView scoreView;
    private TextView questionProgress;
    private ProgressBar progressBar;
    private ImageView speakButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_test);

        initViews();
        initData();
        getTest();
    }

    private void initViews() {
        // 基本控件
        wordView = findViewById(R.id.test_word);
        scoreView = findViewById(R.id.score_view);
        questionProgress = findViewById(R.id.question_progress);
        progressBar = findViewById(R.id.progress_bar);
        speakButton = findViewById(R.id.speak_button);

        // 选项控件
        chooseA = findViewById(R.id.choose_a);
        chooseB = findViewById(R.id.choose_b);
        chooseC = findViewById(R.id.choose_c);
        chooseD = findViewById(R.id.choose_d);

        chooseAText = findViewById(R.id.choose_a_text);
        chooseBText = findViewById(R.id.choose_b_text);
        chooseCText = findViewById(R.id.choose_c_text);
        chooseDText = findViewById(R.id.choose_d_text);

        // 设置点击监听
        chooseA.setOnClickListener(this);
        chooseB.setOnClickListener(this);
        chooseC.setOnClickListener(this);
        chooseD.setOnClickListener(this);
        speakButton.setOnClickListener(this);

        // 初始化进度
        updateProgress();
        setScore(score);
    }

    private void initData() {
        wordDB = WordDB.getInstance(this);
        mRECORDSBeanList = wordDB.loadWordLib();
        mIndex = wordDB.loadIndex();
    }

    private void updateProgress() {
        questionProgress.setText("题目 " + (testIndex + 1) + "/" + mTests.size());
        progressBar.setMax(mTests.size());
        progressBar.setProgress(testIndex + 1);
    }

    private void setScore(int score) {
        scoreView.setText("Score: " + score);
    }

    public void getTest() {
        if (mRECORDSBeanList != null && !mRECORDSBeanList.isEmpty()) {
            for (int i = 0; i < mRECORDSBeanList.size(); i++) {
                WordTest wordTest = new WordTest(this, i, mRECORDSBeanList);
                List<WordTest.Test> tests = wordTest.getWordFromLib();
                if (!tests.isEmpty()) {
                    mTests.add(tests.get(0)); // 每个单词取一个测试题
                }
            }
        }

        if (!mTests.isEmpty()) {
            displayTest(mTests.get(testIndex));
        } else {
            Toast.makeText(this, "词库为空，无法进行测试", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void displayTest(WordTest.Test test) {
        setRightChoose();
        currentWord = test.getWord();
        wordView.setText(currentWord);
        currectAnswer = test.getRight();

        // 根据随机选择的正确答案位置设置选项
        if (rightChoose == R.id.choose_a) {
            chooseAText.setText(test.getRight());
            chooseBText.setText(test.getWrong1());
            chooseCText.setText(test.getWrong2());
            chooseDText.setText(test.getWrong3());
        } else if (rightChoose == R.id.choose_b) {
            chooseAText.setText(test.getWrong1());
            chooseBText.setText(test.getRight());
            chooseCText.setText(test.getWrong2());
            chooseDText.setText(test.getWrong3());
        } else if (rightChoose == R.id.choose_c) {
            chooseAText.setText(test.getWrong2());
            chooseBText.setText(test.getWrong1());
            chooseCText.setText(test.getRight());
            chooseDText.setText(test.getWrong3());
        } else if (rightChoose == R.id.choose_d) {
            chooseAText.setText(test.getWrong3());
            chooseBText.setText(test.getWrong1());
            chooseCText.setText(test.getWrong2());
            chooseDText.setText(test.getRight());
        }

        // 重置选项样式
        resetChoiceStyles();

        // 自动播放单词发音
        AudioPlayerUtil.playAudioFromUrl(this, currentWord);

        // 更新进度
        updateProgress();
    }

    private void resetChoiceStyles() {
        // 重置所有选项的背景样式
        chooseA.setBackgroundResource(R.drawable.option_selector);
        chooseB.setBackgroundResource(R.drawable.option_selector);
        chooseC.setBackgroundResource(R.drawable.option_selector);
        chooseD.setBackgroundResource(R.drawable.option_selector);
    }

    private void setRightChoose() {
        double random = Math.random() * 10;
        if (random >= 0 && random < 2.5) {
            rightChoose = R.id.choose_a;
        } else if (random >= 2.5 && random < 5.0) {
            rightChoose = R.id.choose_b;
        } else if (random >= 5.0 && random < 7.5) {
            rightChoose = R.id.choose_c;
        } else if (random >= 7.5 && random < 10.0) {
            rightChoose = R.id.choose_d;
        }
    }

    public void nextTest() {
        testIndex++;
        if (testIndex < mTests.size()) {
            displayTest(mTests.get(testIndex));
        } else {
            // 所有题目做完，跳转到结果页
            Intent intent = new Intent(WordTestActivity.this, CalScoreActivity.class);
            intent.putExtra("score", score);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        if (mTests.size() == 0) {
            return;
        }

        int id = v.getId();
        if (id == R.id.speak_button) {
            AudioPlayerUtil.playAudioFromUrl(this, currentWord);
        } else if (id == R.id.choose_a) {
            testAnswer(R.id.choose_a);
        } else if (id == R.id.choose_b) {
            testAnswer(R.id.choose_b);
        } else if (id == R.id.choose_c) {
            testAnswer(R.id.choose_c);
        } else if (id == R.id.choose_d) {
            testAnswer(R.id.choose_d);
        }
    }

    private void testAnswer(int choose) {
        // 显示答案反馈
        showAnswerFeedback(choose);

        if (choose == rightChoose) {
            score += 10;
            setScore(score);
            Toast.makeText(this, "✅ 答对了！+10分", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "❌ 正确答案：" + currectAnswer, Toast.LENGTH_LONG).show();
        }

        // 延迟跳转到下一题
        wordView.postDelayed(new Runnable() {
            @Override
            public void run() {
                nextTest();
            }
        }, 1500);
    }

    private void showAnswerFeedback(int choose) {
        // 高亮正确答案
        if (rightChoose == R.id.choose_a) {
            chooseA.setBackgroundColor(0xFF4CAF50); // 绿色
        } else if (rightChoose == R.id.choose_b) {
            chooseB.setBackgroundColor(0xFF4CAF50);
        } else if (rightChoose == R.id.choose_c) {
            chooseC.setBackgroundColor(0xFF4CAF50);
        } else if (rightChoose == R.id.choose_d) {
            chooseD.setBackgroundColor(0xFF4CAF50);
        }

        // 如果选错了，标红错误选项
        if (choose != rightChoose) {
            if (choose == R.id.choose_a) {
                chooseA.setBackgroundColor(0xFFF44336); // 红色
            } else if (choose == R.id.choose_b) {
                chooseB.setBackgroundColor(0xFFF44336);
            } else if (choose == R.id.choose_c) {
                chooseC.setBackgroundColor(0xFFF44336);
            } else if (choose == R.id.choose_d) {
                chooseD.setBackgroundColor(0xFFF44336);
            }
        }
    }
}