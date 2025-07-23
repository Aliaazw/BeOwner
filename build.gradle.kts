
plugins {
    id("com.android.application") version "8.10.1" apply false
    id("com.android.library") version "8.10.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false


    id("com.google.gms.google-services") version "4.4.1" apply false

}

buildscript {
    repositories {
        google()
        mavenCentral()

    }
    dependencies {

        classpath("com.google.gms:google-services:4.4.1")
    }
}

