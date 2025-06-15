package com.liaoyunan.englishapp;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class AppContextTest {

    @Test
    public void useAppContext() {
        // 获取 Context
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        // 验证包名是否正确
        assertEquals("com.liaoyunan.englishapp", appContext.getPackageName());
    }

    @Test
    public void testMappings() {
//        DateFileMappingUtil.getInstance().testMappings();
    }
}