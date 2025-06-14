package com.liaoyunan.englishapp;

import static org.junit.Assert.assertNotNull;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class ApplicationTest{

    @Test
    public void testAppContext() {
        Application app = ApplicationProvider.getApplicationContext();
        assertNotNull(app);
    }
}