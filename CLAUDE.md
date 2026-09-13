# CLAUDE.md

이 파일은 Claude Code(및 다른 환경의 Claude)가 이 저장소에서 작업을 이어가기 위한 컨텍스트입니다.
사람이 읽는 소개는 `README.md`, 테스트 절차는 `docs/TESTING.md`를 참고하세요.

## 프로젝트 개요
- **kmemo** = Kotlin Multiplatform + Compose Multiplatform 메모 앱. **iOS + Android** 둘 다 타겟.
- 기능: 메모 생성/편집/삭제, 색상 6종, 상단 고정(pin), 로컬 저장(SQLDelight).
- **홈 화면 위젯**: iOS=WidgetKit(SwiftUI), Android=Glance. 메모는 공유 코드로 관리하고,
  앱이 변경 시 위젯용 JSON 스냅샷을 공유 저장소에 기록(iOS=App Group UserDefaults, Android=SharedPreferences).
- **위젯 딥링크**: `kmemo://memo/{id}` — 위젯의 메모를 탭하면 앱이 해당 메모 편집화면으로 진입.

## 현재 상태 (이어서 하기)
검증 완료:
- Android: `./gradlew :composeApp:assembleDebug` 빌드 성공.
- iOS: 시뮬레이터 실행 + Compose UI 렌더링 + SQLite 메모 목록 표시 + `kmemo://` URL 스킴 라우팅 확인.
- 앱 아이콘(iOS 1024 PNG, Android adaptive vector) 적용됨.

아직 안 된 것 / 다음 후보:
- **아직 git 커밋 안 함** (초기 커밋 1개만 존재, 모든 작업이 워킹 트리에 있음).
- iOS 위젯의 **실제 데이터 표시**는 App Group 서명이 필요해 실기기에서만 확인 가능 (아래 주의 참고).
- Android 라이브 실행 검증은 미완 (당시 에뮬레이터 adb sync가 깨져 설치 실패 — 앱 문제 아님. 에뮬레이터 재부팅 후 `./gradlew :composeApp:installDebug`).
- 아이디어: iCloud/서버 동기화, 검색/정렬, 위젯에서 새 메모 바로 추가, 다크테마.

## 빌드 / 실행 명령
```bash
# Android (JDK 17 필수 — 아래 '환경' 참고)
export JAVA_HOME=<JDK17_HOME>
./gradlew :composeApp:assembleDebug          # APK 빌드
./gradlew :composeApp:installDebug           # 기기/에뮬레이터 설치
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64   # iOS 프레임워크만 컴파일 검증

# iOS
brew install xcodegen                          # 최초 1회
cd iosApp && xcodegen generate                 # project.yml -> kmemo.xcodeproj (xcodeproj는 gitignore)
xcodebuild -project kmemo.xcodeproj -scheme kmemo \
  -destination 'platform=iOS Simulator,name=iPhone 17' \
  -configuration Debug CODE_SIGNING_ALLOWED=NO build
```
iOS 앱 빌드 시 Gradle이 `embedAndSignAppleFrameworkForXcode`로 `ComposeApp.framework`를 자동 컴파일/임베드
(빌드 페이즈 "Compile Kotlin Framework", `iosApp/project.yml`에 정의).

## 아키텍처 / 소스 맵
```
composeApp/
  src/commonMain/kotlin/com/kmemo/
    AppModule.kt            # 간단 DI: DatabaseDriverFactory+WidgetSnapshotPublisher -> Repository -> ViewModel
    DeepLink.kt             # 위젯 탭 딥링크 버스 (StateFlow<Long?>)
    data/Memo.kt            # Memo 모델 + MemoColors 팔레트
    data/MemoRepository.kt  # SQLDelight CRUD + 변경 시 위젯 스냅샷 발행. expect fun nowMillis()
    data/DatabaseDriverFactory.kt  # expect
    data/WidgetSnapshot.kt  # expect WidgetSnapshotPublisher + JSON 인코딩(encodeDefaults=true), APP_GROUP/KEY 상수
    ui/App.kt               # Compose UI: 목록(스태거드 그리드)/편집/색상/핀 + 딥링크 반응
    ui/MemoViewModel.kt     # androidx.lifecycle ViewModel (KMP)
  src/commonMain/sqldelight/com/kmemo/db/Memo.sq   # 스키마 + 쿼리 (테이블 MemoEntity)
  src/androidMain/kotlin/com/kmemo/
    Platform.android.kt     # actual: nowMillis, DatabaseDriverFactory(Context), WidgetSnapshotPublisher(Context)
    KmemoApp.kt             # Application, 싱글턴 AppModule 보관
    MainActivity.kt         # Compose 호스팅 + 딥링크 인텐트 처리
    widget/MemoWidget.kt    # Glance 위젯 + Receiver, 카드마다 딥링크 인텐트
  src/androidMain/AndroidManifest.xml, res/  # 매니페스트, adaptive 아이콘, 위젯 provider xml
  src/iosMain/kotlin/com/kmemo/
    Platform.ios.kt         # actual: NativeSqliteDriver, App Group UserDefaults 기록, IosWidgetReloader 브릿지
    MainViewController.kt   # ComposeUIViewController 진입점 + IosApp 싱글턴
iosApp/
  project.yml               # XcodeGen 정의 (source of truth). kmemo 앱 + MemoWidgetExtension
  kmemo/                    # SwiftUI 앱: iOSApp.swift(딥링크/위젯reload), ContentView.swift, Info.plist, .entitlements, Assets.xcassets(AppIcon)
  MemoWidget/               # WidgetKit: MemoWidget.swift, MemoWidgetBundle.swift, Info.plist, .entitlements
```

## ⚠️ 비자명한 함정 (반드시 숙지)
1. **Gradle은 JDK 17로 실행**. brew가 JDK 26을 깔아둬서 기본 java가 너무 최신 → AGP 8.9와 안 맞음.
   이 머신 경로: `/Users/dohyunkim/Library/Java/JavaVirtualMachines/corretto-17.0.14/Contents/Home`.
   다른 머신에선 `/usr/libexec/java_home -v 17`로 찾아 `JAVA_HOME` 지정.
2. **expect/actual은 같은 패키지**(`com.kmemo.data`)에 둘 것. 안 그러면 "no corresponding expected declaration".
3. **iOS 정적 프레임워크**라 앱 `OTHER_LDFLAGS`에 `-lsqlite3` 필요 (SQLDelight native driver 링크). `project.yml`에 있음.
4. **App Group 이름 `group.com.kmemo.app`은 3곳이 일치**해야 함: Kotlin `WidgetSnapshot.APP_GROUP`,
   iOS `*.entitlements`, 위젯 Swift `appGroup` 상수.
5. **iOS URL 스킴 등록 필수**: `iosApp/kmemo/Info.plist`의 `CFBundleURLTypes`에 `kmemo`. 없으면 위젯 탭이 앱을 못 엶.
6. **실기기/위젯 데이터 공유엔 서명 필요**: Xcode에서 `kmemo`·`MemoWidgetExtension` 두 타겟 모두 Team 선택 +
   App Groups capability 체크. 시뮬레이터 무서명 빌드는 위젯에 데이터가 안 보일 수 있음.
7. `kmemo.xcodeproj`는 **생성물** — `project.yml`을 고치고 `xcodegen generate` 재실행. (xcodeproj는 gitignore)
8. iOS 시뮬레이터 DB 직접 seed: `xcrun simctl get_app_container <dev> com.kmemo data` →
   `Library/Application Support/databases/kmemo.db`에 `sqlite3`로 INSERT. `simctl openurl`은 외부열기 확인창이 떠서 자동 탭 불가(실제 위젯 탭엔 확인창 없음).

## 버전 매트릭스 (gradle/libs.versions.toml)
Kotlin 2.1.21 · Compose MP 1.8.2 · AGP 8.9.2 · Gradle 8.13(wrapper) · SQLDelight 2.0.2 · Glance 1.1.1 ·
coroutines 1.10.2 · serialization 1.8.1 · compileSdk 35 · minSdk 26.

## 환경 (이 머신 기준, 다른 환경에선 확인 필요)
- macOS + Xcode(시뮬레이터: iPhone 17 등), Android SDK(API 31~36), CocoaPods, XcodeGen(brew).
- `local.properties`에 `sdk.dir` 필요(gitignore됨). 예: `sdk.dir=/Users/<you>/Library/Android/sdk`.
- Android SDK 경로가 다르면 `local.properties` 갱신.
