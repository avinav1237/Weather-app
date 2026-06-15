# Metro Weather App

A native Android app that lets you search for a city and see a ranked list of activities suitable for that location over the next 7 days, based on weather forecast data from [Open-Meteo](https://open-meteo.com/).

## Project overview

Users search for a city using the Open-Meteo Geocoding API. After selecting a result, the app fetches a 7-day daily forecast and ranks four activities:

1. Skiing
2. Surfing
3. Outdoor sightseeing
4. Indoor sightseeing

Each activity receives a score from 0–100 and a short rationale. The list is sorted best-to-worst.

## Platform and tooling

| Choice | Version / notes |
|--------|-----------------|
| Language | Kotlin 2.2 |
| UI | Jetpack Compose + Material 3 |
| Architecture | Clean Architecture + MVVM |
| Async | Coroutines + StateFlow |
| Networking | Retrofit 3 + OkHttp + kotlinx.serialization |
| DI | Hilt 2.59 (KSP) |
| Min SDK | 24 |
| Target SDK | 36 |
| Compile SDK | 37 |

Built with Android Gradle Plugin 9.x and Gradle 9.4.

## Architecture and technical decisions

The app is split into three layers with a unidirectional data flow:

```
UI (Compose) → ViewModel → Use Cases → Repository → Retrofit APIs
                              ↓
                   ActivityRecommendationEngine (pure Kotlin)
```

### Layer responsibilities

- **Presentation** — `ActivitiesScreen`, `ActivitiesViewModel`, explicit `ActivitiesUiState`. The ViewModel debounces search input (300 ms) and maps domain errors to UI actions.
- **Domain** — models, `WeatherRepository` interface, use cases, and `ActivityRecommendationEngine`. No Android or Retrofit dependencies.
- **Data** — Retrofit service interfaces, DTOs, mappers, and `WeatherRepositoryImpl`. Maps HTTP/IO failures to `AppError`.

### Key decisions

- **Single screen** — search, suggestions, and ranked results on one screen to keep scope focused.
- **`DomainResult<T>`** — a small sealed type (`Success` / `Error`) instead of throwing exceptions into ViewModels.
- **Repository pattern** — API details are hidden behind `WeatherRepository` for mockability.
- **Pure recommendation engine** — scoring logic lives in testable Kotlin with no framework coupling.
- **Hilt** — standard Android DI; upgraded to 2.59 for AGP 9 compatibility.

## How to build and run

### Prerequisites

- Android Studio (recent version with AGP 9 support) or JDK 11+
- Android SDK with API 37

### Command line

```bash
./gradlew assembleDebug
```

Install on a connected device/emulator:

```bash
./gradlew installDebug
```

### Android Studio

1. Open the project root.
2. Sync Gradle.
3. Run the `app` configuration on an emulator or device.

The app requires network access to call Open-Meteo.

## How to run tests and testing strategy

**Tests are not implemented in this pass** (per project scope). The codebase is structured for testing:

| Target | Tooling | What to verify |
|--------|---------|----------------|
| `ActivityRecommendationEngine` | JUnit | Table-driven scores for cold/snowy, windy, mild, and rainy forecasts |
| `GetActivityRecommendationsUseCase` | JUnit + MockK | Repository success/error paths |
| `WeatherRepositoryImpl` | JUnit + MockWebServer | DTO mapping and HTTP error mapping |
| `ActivitiesViewModel` | JUnit + Turbine | Debounced search, city selection, loading/error states |

Run tests (once added):

```bash
./gradlew test
```

## API usage notes

No API key is required for non-commercial use. Data is licensed under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/); attribution to Open-Meteo is required for production use.

### Geocoding API

- Base URL: `https://geocoding-api.open-meteo.com/`
- Endpoint: `GET /v1/search?name={query}&count=10&language=en&format=json`
- Used for city autocomplete (minimum 2 characters before calling)

### Forecast API

- Base URL: `https://api.open-meteo.com/v1/`
- Endpoint: `GET /forecast` with:
  - `latitude`, `longitude`
  - `daily=temperature_2m_max,temperature_2m_min,precipitation_sum,snowfall_sum,wind_speed_10m_max,weather_code`
  - `timezone=auto`
  - `forecast_days=7`

### Chosen weather fields

| Field | Unit | Used for |
|-------|------|----------|
| `temperature_2m_max` / `temperature_2m_min` | °C | Skiing, surfing, outdoor comfort |
| `precipitation_sum` | mm | Dry vs wet days |
| `snowfall_sum` | cm | Skiing |
| `wind_speed_10m_max` | km/h | Surfing (proxy for conditions) |
| `weather_code` | WMO code | Outdoor sightseeing (clear vs storm) |

## Activity recommendation logic

Each activity is scored **per day (0–100)**, then **averaged across 7 days**. Activities are sorted by average score descending.

### Skiing

- Weight: 55% cold score + 45% snow score
- Cold: best when daily max ≤ 0°C; poor above 5°C
- Snow: best with ≥ 1 cm `snowfall_sum`; some credit for cold + precipitation without snow

### Surfing

- Weight: 45% wind + 30% temperature + 25% dryness
- Wind: ideal 15–35 km/h (moderate onshore/offshore proxy; no wave model)
- Temperature: ideal 12–28°C
- Precipitation: lower is better

### Outdoor sightseeing

- Weight: 35% temperature + 30% precipitation + 15% wind + 20% weather code
- Temperature: ideal average 15–26°C
- Precipitation: minimal preferred
- Wind: ≤ 20 km/h preferred
- Weather code: clear/partly cloudy preferred; thunderstorms penalized

### Indoor sightseeing

- Computed as `100 - outdoorSightseeingScore`, clamped to 10–100
- Ranks higher when outdoor conditions are poor (rain, extremes, storms)
- Never scores as “perfect” on consistently nice weather (floor of 10)

Summaries (e.g. “Cold with snowfall on 3 of 7 days”) are derived from threshold counts across the week.

## Assumptions

- **Surfing** — wind speed proxies surf suitability; no wave height, swell, or coastal proximity data.
- **Skiing** — uses forecast snowfall, not resort operations, snow depth, or elevation-adjusted temperature (elevation from geocoding is fetched but not used in scoring yet).
- **Geocoding** — user picks from API results; no GPS/current-location flow.
- **Units** — Open-Meteo defaults (°C, mm, cm, km/h) are used as returned.
- **Timezone** — `timezone=auto` aligns daily buckets with the selected coordinates.
- **Search** — queries under 2 characters do not hit the network.

## Trade-offs and omissions

- Single-screen UX; no detail/hourly breakdown
- No local caching or offline support
- No unit/instrumented tests in this delivery
- Minimal visual polish; functional Material 3 layout
- No analytics, crash reporting, or retry backoff
- OkHttp logging enabled at BASIC level (consider debug-only build variant for production)

## Production-readiness notes

Before shipping:

- Add Open-Meteo attribution in-app (Settings/About)
- Respect rate limits (10,000 free calls/day for non-commercial use)
- Gate HTTP logging to debug builds
- Add response caching (e.g. OkHttp cache) to reduce API usage
- Handle process death / configuration changes explicitly if navigation grows
- Add ProGuard rules if minification is enabled
- Consider commercial API plan if call volume exceeds free tier

## Cross-platform delivery notes

The **domain layer** (`domain.model`, `domain.recommendation`, `domain.usecase`, `domain.repository`) is Android-free and could be extracted into a Kotlin Multiplatform shared module. Presentation and data layers would remain platform-specific (Compose on Android, SwiftUI on iOS, etc.).

## AI usage disclosure

This project was implemented with AI assistance (Cursor Agent) for scaffolding, architecture setup, scoring logic, and documentation. All code was verified by a successful `./gradlew assembleDebug` build. API behavior and scoring assumptions were cross-checked against Open-Meteo documentation.
