// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()

        maven{
            setUrl("https://jitpack.io")
            setUrl("https://maven.google.com")

        }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.9.3")
        /*classpath("com.google.dagger:hilt-android-gradle-plugin:2.48.1")
        classpath("com.google.gms:google-services:4.4.0")
        classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")*/
        classpath("com.google.gms:google-services:4.4.3")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.48.1")

    }
}

plugins {
    id("com.android.application") version "8.9.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.gms.google-services") version "4.4.3" apply false
    id("com.google.dagger.hilt.android") version "2.48.1" apply false

}
