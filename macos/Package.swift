// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "GreenixStudio",
    platforms: [.macOS(.v12)],
    targets: [
        .executableTarget(name: "GreenixStudio", path: "Sources/GreenixStudio")
    ]
)
