package com.dersnotu.myapplication

// 👇 İŞTE EKSİK OLAN SİHİRLİ SATIRLAR BUNLAR
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InputValidatorTest {

    @Test
    fun girisBilgileri_DogruIse_TrueDonmeli() {
        // Senaryo 1: Email dolu, Şifre 6 karakterden uzun.
        // Beklenen: True
        val sonuc = InputValidator.isLoginInputValid(email = "ali@veli.com", pass = "123456")
        assertTrue(sonuc)
    }

    @Test
    fun email_BosIse_FalseDonmeli() {
        // Senaryo 2: Email boş, Şifre doğru.
        // Beklenen: False
        val sonuc = InputValidator.isLoginInputValid(email = "", pass = "123456")
        assertFalse(sonuc)
    }

    @Test
    fun sifre_KisaIse_FalseDonmeli() {
        // Senaryo 3: Email dolu, Şifre 6 karakterden kısa.
        // Beklenen: False
        val sonuc = InputValidator.isLoginInputValid(email = "ali@veli.com", pass = "12345")
        assertFalse(sonuc)
    }
}