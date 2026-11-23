# Walkthrough — Orientación Vertical Exclusiva (Portrait) y Gestos de Deslizamiento (Swipe Back)

He corregido la orientación de la aplicación para que funcione **única y exclusivamente en modo vertical** (*Sensor Portrait*), manteniendo activos los **gestos táctiles de deslizamiento hacia atrás** (*Swipe Back / BackHandler*).

## Cambios Realizados

### Manifiesto y Actividad Principal (`app/src/main`)

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/AndroidManifest.xml)
- Cambiado a `android:screenOrientation="sensorPortrait"`, conservando `android:enableOnBackInvokedCallback="true"`.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/MainActivity.kt)
- Forzada la orientación a nivel de actividad con `requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT`.

### Manejo de Gestos en UI (`mx.nexum.app.auth.ui`, `mx.nexum.app.financial.ui`)

#### [MODIFY] [AuthFlow.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/auth/ui/AuthFlow.kt) & [MainAppShell.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/ui/MainAppShell.kt)
- Conservados los interceptores `BackHandler` de Jetpack Compose:
  - Al realizar el gesto de retroceso en modales (Privacidad, Ayuda), se cierra el modal.
  - Al realizar el gesto en una pestaña secundaria (Movimientos, Pagos, Metas, Perfil, Ajustes), regresa a la pestaña principal (**Resumen**).
  - En el flujo de login/registro, el gesto regresa a la pantalla de acceso sin cerrar la app abruptamente.

---

## Resultados de Verificación

- **Compilación:** `./gradlew assembleDebug` → **BUILD SUCCESSFUL**.
- **Pruebas Unitarias:** `./gradlew testDebugUnitTest` → **240 tests pasados en verde (0 fallos)**.
