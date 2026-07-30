package vn.io.litever.remind.core.testing

enum class DeviceType(
    val directoryName: String,
    val qualifiers: String
) {
    PHONE("phoneScreenshots", "w412dp-h914dp-port-xxhdpi"), // Pixel 8a equivalent
    TABLET_7_INCH("sevenInchScreenshots", "w600dp-h960dp-port-xhdpi"), // 7-inch tablet equivalent
    TABLET_10_INCH("tenInchScreenshots", "w800dp-h1280dp-port-xhdpi") // 10-inch tablet equivalent
}

object StoreScreenshotHelper {
    fun getScreenshotPath(
        locale: String,
        deviceType: DeviceType,
        imageName: String
    ): String {
        return "../../fastlane/metadata/android/$locale/images/${deviceType.directoryName}/$imageName.png"
    }
}
