# Walkthrough — Orientación Horizontal Exclusiva y Gestos de Deslizamiento (Swipe Back)

He configurado la aplicación para funcionar **única y exclusivamente en modo horizontal** (*Sensor Landscape*) e integrado los **gestos táctiles de deslizamiento hacia atrás** (*Swipe Back / BackHandler*).

## Cambios Realizados

### Manifiesto y Actividad Principal (`app/src/main`)

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/AndroidManifest.xml)
- Configurado `android:screenOrientation="sensorLandscape"`, `android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"` y `android:enableOnBackInvokedCallback="true"`.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/MainActivity.kt)
- Forzada la orientación a nivel de actividad con `requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE`.

### Manejo de Gestos en UI (`mx.nexum.app.auth.ui`, `mx.nexum.app.financial.ui`)

#### [MODIFY] [AuthFlow.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/auth/ui/AuthFlow.kt) & [MainAppShell.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/ui/MainAppShell.kt)
- Incorporados interceptores `BackHandler` de Jetpack Compose:
  - Al realizar el gesto de retroceso en pantallas secundarias o modales (Privacidad, Ayuda), se cierra el modal.
  - Al realizar el gesto en una pestaña secundaria (Movimientos, Pagos, Metas, Perfil, Ajustes), regresa a la pestaña principal (**Resumen**).
  - En el flujo de login/registro, el gesto regresa a la pantalla inicial de login sin cerrar la app abruptamente.

---

## Resultados de Verificación

- **Compilación:** `./gradlew assembleDebug` → **BUILD SUCCESSFUL**.
- **Pruebas Unitarias:** `./gradlew testDebugUnitTest` → **240 tests pasados en verde (0 fallos)**.
