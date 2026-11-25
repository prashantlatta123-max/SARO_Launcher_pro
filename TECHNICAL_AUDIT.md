# SAROTECH Launcher v1.0.2 — Engineering Audit

## Corrections in this package
- Added Android resource folders, strings, theme resources, vector icon, adaptive icon XML.
- Added R8/ProGuard keep rules for JNI methods.
- Split repository and ViewModel from the Activity.
- Added DataStore settings repository foundation.
- Added Kotlin unit test and Android instrumentation smoke test.
- Added package-change receiver class and Activity registration lifecycle.
- Kept package visibility scoped to launcher activities; no QUERY_ALL_PACKAGES.
- Preserved exact package + Activity component identity for app launch.
- Kept Rust normalization and sorting in Rust behind JNI.
- Added Rust panic containment at JNI entry points.
- Added CI stages for Rust tests, NDK JNI builds, Android debug/release APK builds.
- Added release version metadata and ABI filters.
- Explicitly documented unfinished features and unsigned release artifact status.

## Verification performed during packaging
- AndroidManifest.xml parsed as valid XML.
- Required source/config/test/resource files exist.
- ZIP archive integrity verified.

## Not verified in this environment
- `cargo test` and Rust cross-compilation.
- Gradle Kotlin DSL compilation.
- Android APK build and R8 execution.
- Emulator and physical-device tests.
- Runtime behavior of package-change receiver on OEM Android builds.

## Known remaining blockers / limitations
1. A real Cargo.lock must be generated with Cargo and committed. CI currently resolves it at runtime before locked builds; this is less reproducible than a committed lockfile.
2. Gradle wrapper properties are included, but wrapper JAR and generated scripts are not. Generate them using the official Gradle 8.9 wrapper task and commit all three.
3. CI release APK is unsigned. Configure secure signing secrets before public distribution.
4. Package change receiver callback is currently a lightweight hook; the ViewModel refresh on Activity resume remains the authoritative refresh.
5. DataStore settings repository exists but is not yet wired to the home UI.
6. Room dependencies are present for future persistence; database entities/DAO/migrations are not yet implemented.
7. Features listed as planned in README are not implemented yet.

## Release gate
Do not call this production-ready until CI passes, Cargo.lock and Gradle wrapper are committed, signed release build is verified, and API 29+ emulator/device tests pass.
