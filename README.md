# 숨 (SOOUM) - Android

**SOOUM**은 위치 기반 익명 커뮤니케이션 플랫폼입니다.<br/>
사용자들은 자신의 생각과 감정을 담은 '카드'를 작성하여 주변 사람들과 공유하고, 서로 공감하며 소통할 수 있습니다. 익명성을 바탕으로 한 솔직하고 자유로운 대화 공간을 제공합니다.

<br/>

## 📱 Screenshots
<div style="overflow-x: scroll; display: flex;">
  <img src="image/1-1.png" width="200" style="margin-right: 10px;" />
  <img src="image/1-2.png" width="200" style="margin-right: 10px;" />
  <img src="image/1-3.png" width="200" style="margin-right: 10px;" />
  <img src="image/1-4.png" width="200" style="margin-right: 10px;" />
  <img src="image/1-5.png" width="200" style="margin-right: 10px;" />
  <img src="image/1-6.png" width="200" style="margin-right: 10px;" />
  <img src="image/1-7.png" width="200" style="margin-right: 10px;" />
</div>

<br/>

## ✨ Key Features
*   **카드 피드 (Feed)**: 인기순, 최신순, 거리순으로 정렬된 다양한 카드를 탐색하여 사람들의 이야기를 확인합니다.
*   **카드 작성 (Write)**: 배경 이미지, 다양한 폰트, 태그를 활용하여 나만의 감성을 담은 카드를 작성합니다.
*   **소통 (Communication)**: 카드에 공감(좋아요)을 표현하고, 댓글을 통해 작성자 및 다른 사용자들과 대화를 나눕니다.
*   **태그 (Tags)**: 관심 있는 주제의 태그를 팔로우하고 관련 카드를 모아볼 수 있습니다.
*   **위치 기반 (Location-based)**: 현재 나의 위치를 기반으로 주변 사용자들의 이야기를 발견합니다.

<br/>

## 🛠 Tech Stack

### Android
- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose (Material3)
- **Architecture**:
    - Clean Architecture (Multi-module: Presentation, Domain, Data, Core)
    - MVVM Pattern
    - UDP (Unidirectional Data Flow)
- **Dependency Injection**: Hilt
- **Network**: Retrofit2, OkHttp3
- **Async Processing**: Coroutines, Flow
- **Image Loading**: Coil
- **Navigation**: Navigation Compose

### Backend & Core
- **API Communication**: RESTful API
- **Location Service**: Provide location-based feed content

<br/>

## 📂 Module Structure
프로젝트는 클린 아키텍처 원칙에 따라 멀티 모듈로 구성되어 있습니다.

- **app**: 애플리케이션의 진입점 및 의존성 주입 설정
- **presentation**: UI 및 사용자 상호작용 처리 (Screens, ViewModels, Compose UI)
- **domain**: 비즈니스 로직 및 유스케이스 정의 (순수 Kotlin 모듈)
- **data**: 데이터 소스 관리 및 레포지토리 구현 (API 호출, 로컬 DB)
- **core**: 공통 유틸리티, 디자인 시스템, 확장 함수 등
