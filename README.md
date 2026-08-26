# FeedTracker

FeedTracker è un’app **Kotlin Multiplatform** per registrare e consultare sessioni di monitoraggio associate a una persona: nome, cognome, data di nascita, orario di inizio e fine, e note opzionali.

Il client (Android, iOS e Desktop) è scritto in **Compose Multiplatform**. Il backend è un server **Ktor** che espone un’API REST e persiste i dati su **PostgreSQL**. Il modulo `shared` contiene dominio, repository e client HTTP usati da tutte le piattaforme.

## Architettura

| Modulo | Ruolo |
| --- | --- |
| [`composeApp`](./composeApp/src) | UI Compose condivisa (home, nuova sessione, timer) e codice specifico Android / iOS / JVM |
| [`iosApp`](./iosApp) | Entry point nativo iOS |
| [`server`](./server/src/main/kotlin) | API Ktor (`/health`, `/api/tracking-sessions`) e accesso al database |
| [`shared`](./shared/src) | Modelli, data source, repository e configurazione di rete |

La UI elenca le sessioni salvate, consente di avviarne una nuova con un timer e sincronizza i dati con il server quando la rete è disponibile.

## Requisiti

- JDK 21 (il server usa il toolchain 21; Android è compilato per JVM 11)
- Android SDK (compile SDK 36) per l’app Android
- Xcode per iOS
- PostgreSQL per eseguire il server in locale (default: `localhost:5433`, database `feedtracker`)

## Build e avvio

### Android

```shell
./gradlew :composeApp:assembleDebug
```

### Desktop (JVM)

```shell
./gradlew :composeApp:run
```

### Server

```shell
./gradlew :server:run
```

Su Windows usa `.\gradlew.bat` al posto di `./gradlew`.

### iOS

Apri la cartella [`iosApp`](./iosApp) in Xcode e lancia lo schema dell’app, oppure usa la run configuration dell’IDE.

## Qualità del codice

- **Ktlint** (stile IntelliJ / Kotlin official): `./gradlew ktlintCheck` — per riformattare: `./gradlew ktlintFormat`
- **Android Lint**: `./gradlew :composeApp:lintDebug :shared:lintDebug`
- **Test** (server, shared e composeApp, target disponibili sulla macchina):  
  `./gradlew :server:test :shared:allTests :composeApp:allTests`

Su ogni push e pull request, GitHub Actions esegue ktlint, Android Lint e i test del server, JVM e Android su Linux, più i test iOS sul simulatore su macOS.

Quando la CI su `main` (o `master`) è verde, il workflow **Release** produce gli artifact (conservati 30 giorni): AAB Android firmato, installer Linux (`.deb`), Windows (`.msi`) e macOS (`.dmg`). Si scaricano da Actions → run **Release** → Artifacts. Non vengono caricati sugli store.

DMG e MSI non sono firmati con certificati Apple/Authenticode: il sistema operativo può mostrare un avviso. L’IPA iOS non è incluso.

### Keystore Android (una tantum)

Genera un keystore in locale (non committarlo; `*.jks` e `*.keystore` sono in `.gitignore`):

```shell
keytool -genkeypair -v \
  -keystore upload-keystore.jks \
  -alias upload \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

Codifica il file in base64:

```shell
base64 -i upload-keystore.jks | pbcopy
```

Su Linux usa `base64 -w 0 upload-keystore.jks`. Imposta questi **secret** nel repository GitHub (`Settings` → `Secrets and variables` → `Actions`):

| Secret | Contenuto |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | Keystore in base64 |
| `ANDROID_KEYSTORE_PASSWORD` | Password del keystore |
| `ANDROID_KEY_ALIAS` | Alias della chiave (es. `upload`) |
| `ANDROID_KEY_PASSWORD` | Password della chiave |

`versionCode` e `versionName` (`1.0.<n>`) sono presi dal numero di run di GitHub Actions. In locale restano `1` / `1.0.0`, oppure si possono passare `-Pfeedtracker.versionCode=…` e `-Pfeedtracker.versionName=…`.

## Deploy Railway

Il backend si costruisce con il [`Dockerfile`](./Dockerfile) di root (fat jar, solo `server` + `shared`). [`railway.toml`](./railway.toml) limita i **watch path**: Railway ridistribuisce solo se cambiano Dockerfile, Gradle, `server/` o `shared/`. Un merge che tocca solo `composeApp/`, `iosApp/` o `.github/` non deve far partire un deploy.

In dashboard: lascia vuoto il Custom Start Command (l’entrypoint del Dockerfile è già `java -jar /app/server.jar`) e verifica che il servizio usi il Dockerfile di root.

---

Documentazione Kotlin Multiplatform: [get started](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html).
