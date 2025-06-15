package com.liaoyunan.englishapp.model;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 动态生成单词测试题
 */
public class WordTest {

    private List<Word.RECORDSBean> wordList;
    private List<Test> testList = new ArrayList<>();
    private Random random = new Random();

    public WordTest(Context context, List<Word.RECORDSBean> wordList) {
        this.wordList = wordList;
    }

    /**
     * 为每个单词生成一题，题目包括1个正确答案 + 3个错误选项（不重复）
     */
    public List<Test> generateTests() {
        if (wordList == null || wordList.size() < 4) return testList; // 至少需要4个词才能生成题

        for (Word.RECORDSBean record : wordList) {
            String correctMeaning = record.getMeaning();
            String word = record.getWord();

            // 从整个词库中选出3个错误答案
            List<String> wrongOptions = getWrongAnswers(correctMeaning, 3);

            Test test = new Test();
            test.setWord(word);
            test.setRight(correctMeaning);
            test.setWrong1(wrongOptions.get(0));
            test.setWrong2(wrongOptions.get(1));
            test.setWrong3(wrongOptions.get(2));

            testList.add(test);
        }

        return testList;
    }

    /**
     * 从词库中随机获取不同于正确答案的错误选项
     */
    private List<String> getWrongAnswers(String correct, int count) {
        List<String> candidates = new ArrayList<>();
        for (Word.RECORDSBean record : wordList) {
            String meaning = record.getMeaning();
            if (!meaning.equals(correct) && !candidates.contains(meaning)) {
                candidates.add(meaning);
            }
        }

        Collections.shuffle(candidates);
        return candidates.subList(0, Math.min(count, candidates.size()));
    }

    public static class Test {
        private String word;
        private String right;
        private String wrong1;
        private String wrong2;
        private String wrong3;

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public String getRight() {
            return right;
        }

        public void setRight(String right) {
            this.right = right;
        }

        public String getWrong1() {
            return wrong1;
        }

        public void setWrong1(String wrong1) {
            this.wrong1 = wrong1;
        }

        public String getWrong2() {
            return wrong2;
        }

        public void setWrong2(String wrong2) {
            this.wrong2 = wrong2;
        }

        public String getWrong3() {
            return wrong3;
        }

        public void setWrong3(String wrong3) {
            this.wrong3 = wrong3;
        }

        @Override
        public String toString() {
            return "Test{" +
                    "word='" + word + '\'' +
                    ", right='" + right + '\'' +
                    ", wrong1='" + wrong1 + '\'' +
                    ", wrong2='" + wrong2 + '\'' +
                    ", wrong3='" + wrong3 + '\'' +
                    '}';
        }
    }
}
