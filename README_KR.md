# TOAST Haste framework
`TOAST Haste framework` 는 게임서버를 손쉽게 개발할 수 있도록 도와주는 비동기 게임 서버 프레임워크 입니다.

`Haste는 속도를 빠르게 만드는 효과를 가진 가속 마법을 의미한다.`

[![Englsh](https://img.shields.io/badge/Language-English-red.svg)](README.md)
![Korean](https://img.shields.io/badge/Language-Korean-lightgrey.svg)

## Features
### 다양한 QoS, 그리고 멀티플렉싱
- 실시간 멀티플레이 게임에 필요한 다음과 같은 QoS를 제공한다.
    - Reliable-Sequenced, Unreliable-Sequenced, Reliable-Fragmented.
- 도메인간 간섭이 최소화된 멀티플렉싱을 제공한다.

### 암호화
- 연결이 성립될 때마다 새로운 암호화 키를 생성한다.
- 게임특성에 따라서 필요한 데이터를 선택해서 암호화 할 수 있다.

### Wi-Fi Cellular handover
- 모바일 환경에서 Wi-Fi, 셀룰러 네트워크간 전환에 따른 IP 주소 변경에도 효율적으로 대응한다.

## Prerequisites
- 빌드에 필요한 Java 및 Maven 버전:
    - JDK 1.7+, Maven 3.3.0+
- AES-256 암호화를 위해서 JCE(Java Cryptography Extension) 설치가 필요하다.

## Versioning
- TOAST Haste의 버전은 [Semantic Versioning 2.0](http://semver.org/) 을 따른다.
- 버전을 MAJOR.MINOR.PATCH 로 표현하며:
    1. 기존 버전과 호환되지 않게 API가 바뀌면 “MAJOR 버전”을 올리고,
    2. 기존 버전과 호환되면서 새로운 기능을 추가할 때는 “MINOR 버전”을 올리고,
    3. 기존 버전과 호환되면서 버그를 수정한 것이라면 “BUILD 버전”을 올린다.
    - MAJOR.MINOR.BUILD 형식에 정식배포 전 버전이나 빌드 메타데이터를 위한 라벨을 덧붙이는 방법도 있다.

## Documetation
- 문서는 [GitHub의 Wiki](https://github.com/nhnent/toast-haste.framework/wiki) 를 참조한다.

## Roadmap
- NHN Entertainment 에서는 Toast Cloud Real-time Multiplayer(이하 RTM) 를 TOAST Haste를 이용해서 서비스하고 있다.
- 그래서 아래 로드맵에 따라서 성능을 최적화하고 사용성을 향상시키는 노력을 꾸준히 할 예정이다. 

### Milestones

|Milestone|Release Date|
|---------|------------|
|1.0.0    |   Sept 2016|
|1.0.1    | 2017 |
|1.1.0    | 2017 |

### Planned 1.0.1 features
- 성능 테스트에 대한 고민.

### Planned 1.1.0 features
- WebGL을 위한 WebSocket 지원.
- 성능 및 사용성 향상, 개발자 문서 보완.
- Gradle 지원.

## Contributing
- Source Code Contributions:
    - [Contribution Guidelines for TOAST Haste](./CONTRIBUTING.md) 문서를 참조하면 됩니다.

## Bug Reporting
버그를 발견했을때, 리포트하는건 매우 중요합니다. 다른 사람들에게도 도움이 될 수 있도록 발견된 버그를 알려주세요. 
가능하면 수정해서 Pull Request를 보내주셔도 됩니다(PR을 보내기 전에 버그를 Issues에 등록해주세요).

### Before Reporting
이미 알려진 버그일수도 있으니, Issues를 먼저 확인하고 리포트해주시기 바랍니다.
 
### Creating new issue
버그 리포팅은 다음을 포함하고 있어야 합니다.

- 버그에 관련된 자세한 내용
- 재현가능한 스텝
- 자세한 시스템 환경 (OS, JVM, etc)
- 실제 어떤 현상이 발생했는가?
- 어떤 브랜치를 사용하고 있는가?
    
**Thank you for reporting a bug!**

## Mailing list
- dl_haste@nhnent.com

## Contributor
- 권오범 (Founder)
- 김태경

## License
TOAST Haste is licensed under the Apache 2.0 license, see [LICENSE](LICENSE.txt) for details.
```
Copyright 2016 NHN Entertainment Corp.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

```
