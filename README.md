[![CI](https://github.com/ranjithguggilla/Email-Client-Server/actions/workflows/ci.yml/badge.svg)](https://github.com/ranjithguggilla/Email-Client-Server/actions)
![Java](https://img.shields.io/badge/Java-17%2B-blue)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

# simple-mail
Beginner-friendly Java TCP email client–server with a tiny text protocol.

## Features
- Server: handles `REGISTER`, `LOGIN`, `SEND`, `INBOX`, `READ`, `QUIT`
- Client: console menu for common actions
- Protocol: newline-delimited, telnet-friendly
- Java 17+ (works on 21), Gradle (wrapper included; see note below)

## Quickstart

> ⚠️ **Gradle Wrapper note:** This zip was produced in an offline environment, so
> `gradle/wrapper/gradle-wrapper.jar` is not bundled. Generate it once locally:
>
> ```bash
> # If you have Gradle installed locally, from repo root run:
> gradle wrapper
> # This creates gradle/wrapper/gradle-wrapper.jar
> ```
> After that, you can use `./gradlew` normally.

### Build
```bash
./gradlew :server:build :client:build
```

### Run – Server (default port 2525)
```bash
./gradlew :server:run
# or
java -cp server/build/classes/java/main com.example.mail.ServerMain 2525
```

### Run – Client (second terminal)
```bash
./gradlew :client:run
# or
java -cp client/build/classes/java/main com.example.mail.ClientMain localhost 2525
```

### Demo flow
1. Register `alice/1234` and `bob/1234`
2. Login as `alice`, **SEND** to `bob` (end body with a single `.` line)
3. Login as `bob`, **INBOX**, then **READ m1** (or the printed id)

### Manual verification via telnet
```bash
telnet localhost 2525
# expect: OK SIMPLEMAIL 1.0
REGISTER a 1
LOGIN a 1
QUIT
```

## Project layout
```
simple-mail/
  settings.gradle.kts
  build.gradle.kts
  gradlew, gradlew.bat
  gradle/wrapper/gradle-wrapper.properties
  gradle/wrapper/gradle-wrapper.jar   # created by 'gradle wrapper'
  server/ ...  client/ ...
```

## License
MIT
