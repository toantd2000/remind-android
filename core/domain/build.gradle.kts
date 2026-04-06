plugins {
    kotlin("jvm")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(project(":core:model"))
}