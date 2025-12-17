plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // Firebase Eklentisi (Burada versiyon yok)
    id("com.google.gms.google-services")
}
android {
    namespace = "com.dersnotu.myapplication"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.dersnotu.myapplication"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // --- TEMEL ANDROID ---
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0") // CardView için ŞART
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // --- FIREBASE (BOM sayesinde versiyonlar otomatik ayarlanır) ---
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    // --- RESİM YÜKLEME (Glide) ---
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // --- YUVARLAK PROFİL RESMİ (CircleImageView) ---
    // Uygulamanın çökmesinin ana sebebi muhtemelen bu eksiklikti!
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // --- TESTLER ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // ZOOM YAPILABİLEN RESİM (PhotoView)
    implementation("com.github.chrisbanes:PhotoView:2.3.0")

    implementation("androidx.cardview:cardview:1.0.0")

    testImplementation("junit:junit:4.13.2")
}