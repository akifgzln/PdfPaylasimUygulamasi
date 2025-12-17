package com.dersnotu.myapplication

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Test
    fun kullanici_GirisYapabilmeli() {
        // Biraz bekle ki ekranın açıldığını gör (1.5 saniye)
        Thread.sleep(1500)

        // 1. Email yaz
        onView(withId(R.id.etEmail))
            .perform(typeText("ogrenci@test.com"), closeSoftKeyboard())

        // Yazdığını gör diye bekle
        Thread.sleep(1000)

        // 2. Şifre yaz ve KLAVYEYİ KAPAT (Kritik nokta burası)
        onView(withId(R.id.etPassword))
            .perform(typeText("123456"), closeSoftKeyboard())

        // Yazdığını gör diye bekle
        Thread.sleep(1000)

        // 3. Giriş Yap butonuna tıkla
        onView(withId(R.id.btnLogin))
            .perform(click())

        // Tıklandığını gör diye bekle
        Thread.sleep(2000)
    }
}