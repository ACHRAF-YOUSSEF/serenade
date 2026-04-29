# Tech Context

Repository layout:
- `app/`: Android Gradle project, currently single `:app` module with package `com.serenade.app`.
- `backend/`: Maven Spring Boot project with package `com.serenade.backend`.
- `memory-bank/`: required project memory for future sessions.

Build commands:
- Android tests: `cd app && ./gradlew --gradle-user-home ../.gradle-user-home testDebugUnitTest`
- Backend tests: `cd backend && sh mvnw test`

Current constraints:
- Workspace sandbox cannot write to `/home/achraf/.gradle`, so Gradle needs `--gradle-user-home` inside repo or `/tmp`.
- Network may be needed for first dependency download.

