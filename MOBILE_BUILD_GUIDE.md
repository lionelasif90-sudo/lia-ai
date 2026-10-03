# Build Lia AI from your phone (GitHub Actions)

1. Create an empty GitHub repo (e.g. `lia-ai`).
2. Push this folder (Termux): `git init && git add . && git commit -m "Lia AI" && git branch -M main && git remote add origin https://github.com/<you>/lia-ai.git && git push -u origin main`
3. GitHub -> Actions -> "Build Lia AI" -> wait for green. Download the artifact `lia-ai-debug-apks`.
4. Install the `direct` APK for full power (accessibility), the `play` APK for the Play-safe build.
5. In the app: Settings -> paste your Gemini API key. No key is stored in this repo.

If the build fails, open the failed step log and paste the first red error back to Claude.
Commands on a desktop: `gradle assembleDirectDebug assemblePlayDebug` (or `./gradlew` after running `gradle wrapper`).
