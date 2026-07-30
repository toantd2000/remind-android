plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "vn.io.litever.remind.core.testing"

    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }
}
