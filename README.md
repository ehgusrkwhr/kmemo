# kmemo

Kotlin Multiplatform (Compose Multiplatform) 메모 앱. iOS와 Android에서 동작하며,
홈 화면 위젯으로 최근 메모를 바로 볼 수 있습니다.

## 기능
- 메모 생성 / 편집 / 삭제, 색상 선택, 상단 고정(pin)
- 로컬 저장 (SQLDelight)
- 홈 화면 위젯
  - iOS: WidgetKit (SwiftUI)
  - Android: Glance
  - 메모 데이터는 KMP 공유 코드로 관리하고, 앱이 변경 시 위젯용 스냅샷(JSON)을 공유 저장소에 기록합니다.

## 구조
```
composeApp/
  src/commonMain/   # Memo 모델, SQLDelight DB, Repository, ViewModel, Compose UI
  src/androidMain/  # Android 진입점(MainActivity) + Glance 위젯
  src/iosMain/      # iOS actual 구현 + Compose 진입점(MainViewController)
iosApp/
  project.yml       # XcodeGen 프로젝트 정의 (kmemo.xcodeproj는 여기서 생성)
  kmemo/            # SwiftUI 앱 (Compose UI 호스팅)
  MemoWidget/       # WidgetKit 위젯 확장
```

## 빌드 / 실행

사전 준비: JDK 17, Android SDK, Xcode. (이 저장소는 `local.properties`에 Android SDK
경로가 필요합니다. 예: `sdk.dir=/Users/<you>/Library/Android/sdk`)

### Android
```bash
./gradlew :composeApp:assembleDebug
# 또는 Android Studio로 열어서 실행
```
위젯: 홈 화면 길게 누르기 → 위젯 → kmemo 추가.

### iOS
```bash
brew install xcodegen           # 최초 1회
cd iosApp && xcodegen generate  # project.yml → kmemo.xcodeproj
open kmemo.xcodeproj
```
Xcode에서 `kmemo` 스킴을 선택하고 실행. 앱 빌드 시 Gradle이 자동으로
`ComposeApp.framework`를 컴파일/임베드합니다(빌드 페이즈 "Compile Kotlin Framework").

명령줄 빌드:
```bash
cd iosApp
xcodebuild -project kmemo.xcodeproj -scheme kmemo \
  -destination 'platform=iOS Simulator,name=iPhone 17' build
```

## ⚠️ 실기기 / 위젯 데이터 공유를 위한 필수 설정 (App Group)

앱과 위젯이 메모 데이터를 공유하려면 **App Group** (`group.com.kmemo.app`)이 필요합니다.
시뮬레이터 컴파일에는 서명이 필요 없지만, 실기기(그리고 위젯 동작)에는 다음이 필요합니다.

1. Xcode에서 `kmemo`와 `MemoWidgetExtension` 두 타겟 모두:
   - **Signing & Capabilities** → Team 선택 (본인 Apple ID)
   - **+ Capability → App Groups** → `group.com.kmemo.app` 체크
2. Bundle ID가 본인 계정에서 사용 가능해야 합니다. 충돌 시 `com.kmemo`를
   본인 고유값(예: `com.<yourname>.kmemo`)으로 바꾸세요. 이때 `iosApp/project.yml`의
   `PRODUCT_BUNDLE_IDENTIFIER`와 App Group 이름
   (`iosApp/**/*.entitlements`, `WidgetSnapshot.APP_GROUP`)을 함께 맞춰야 합니다.

> App Group 이름은 Kotlin 쪽 `WidgetSnapshot.APP_GROUP`과 iOS `.entitlements`,
> 그리고 위젯 Swift 코드(`appGroup`)에서 모두 동일해야 합니다.

## 홈 화면 위젯 위치에 대해
iOS는 앱이 위젯을 임의 좌표에 직접 배치할 수 없습니다. 사용자가 홈 화면에서
위젯을 길게 눌러 원하는 자리에 끌어다 놓습니다(크기: 소/중/대 선택). Android도 동일하게
사용자가 위젯을 배치합니다.
