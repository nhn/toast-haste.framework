![Logo](https://cloud.githubusercontent.com/assets/4951898/19913934/252fd2d8-a0ea-11e6-92e2-c4225e97a020.png)

# TOAST Haste framework
`TOAST Haste framework` is the asynchronous game server framework for easy development.

`Haste is a spell that boosts the player movement speed and attack speed.`

![Englsh](https://img.shields.io/badge/Language-English-lightgrey.svg) 
[![Korean](https://img.shields.io/badge/Language-Korean-blue.svg)](README_KR.md)


## Features
### Various QoS, and Multiplexing
- Provide the follow QoS that the real time multiplayer game needs.
    - Reliable-Sequenced, Unreliable-Sequenced, Reliable-Fragmented.
- Provide the multiplexing that minimizes interferences between domains.

### Cryptography
- Generate a unique key for encryption whenever the connection was established.
- Can encrypt selected data which you need according to the game characteristic.

### Wi-Fi Cellular handover
- Respond effectively to the IP address changes caused by switching between the cellular network and the Wi-Fi network in a mobile environment.

## Prerequisites
- Java, Maven version required to build TOAST Haste:
    - JDK 1.7+, Maven 3.3.0+
- JCE(Java Cryptography Extension) required to support AES-256.

## Versioning
- The version of TOAST Haste follows [Semantic Versioning 2.0](http://semver.org/).
- Given a version number MAJOR.MINOR.PATCH, increment the:
    1. MAJOR version when you make incompatible API changes,
    2. MINOR version when you add functionality in a backwards-compatible manner, and
    3. PATCH version when you make backwards-compatible bug fixes.
    - Additional labels for pre-release and build metadata are available as extensions to the MAJOR.MINOR.PATCH format.

## Documetation
- Reference to the [Wiki section of GitHub](https://github.com/nhnent/toast-haste.framework/wiki).

## Roadmap
- At NHN Entertainment, we service Toast Cloud Real-time Multiplayer(a.k.a. RTM) developed by TOAST Haste.
- So, We will try to improve performance and convenience according to this roadmap.

### Milestones

|Milestone|Release Date|
|---------|------------|
|1.0.0    |   Sept 2016|
|1.0.1    | 2017 |
|1.1.0    | 2017 |

### Planned 1.0.1 features
- Consider the performance test.

### Planned 1.1.0 features
- Support WebSocket for WebGL.
- Improve performance and convenience, and documentation.
- Support Gradle besides Maven.

## Contributing
- Source Code Contributions:
    - Please follow the [Contribution Guidelines for TOAST Haste](./CONTRIBUTING.md).

## Bug Reporting
If you find a bug, it is very important to report it. We would like to help you and smash the bug away.
If you can fix a bug, you can send pull request (Should register a issue before sending PR)

### Before Reporting
Look into our issue tracker to see if the bug was already reported and you can add more information of the bug.
 
### Creating new issue
A bug report should contain the following

- An useful description of the bug
- The steps to reproduce the bug
- Details of system environments (OS, JVM, etc)
- What actually happened?
- Which branch have you used?
    
**Thank you for reporting a bug!**

## Mailing list
- dl_haste@nhnent.com

## Contributor
- Ethan Kwon (Founder)
- Tae gyeong, Kim

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
