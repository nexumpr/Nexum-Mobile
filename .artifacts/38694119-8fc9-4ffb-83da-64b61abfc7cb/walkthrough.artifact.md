# Walkthrough — Integración de Cerrar Sesión y Cambio de Contraseña

He integrado los botones y flujos funcionales de **Cerrar Sesión** (NEXUM-E004) y **Cambio de Contraseña** (NEXUM-E007) dentro de una nueva pestaña de **Ajustes** en el panel de control principal, utilizando la arquitectura MVI y el diseño **Aero-Futuristic**.

## Cambios Realizados

### Módulo Financiero / Ajustes UI (`mx.nexum.app.financial.ui`)

#### [NEW] [SettingsViewModel.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/ui/SettingsViewModel.kt)
- ViewModel MVI que gestiona el cambio de contraseña con validación local y la acción de cierre de sesión con purga incondicional de credenciales en `TokenStorage`.

#### [NEW] [SettingsScreen.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/ui/SettingsScreen.kt)
- Pantalla con estilo Aero-Futurista (*Glassmorphism* y brillo ambiental) con formularios para actualizar la contraseña y botón de cierre de sesión.

#### [MODIFY] [MainAppShell.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/ui/MainAppShell.kt)
- Añadida la 5ª pestaña de **Ajustes** con icono de engranaje a la barra de navegación inferior (*Bottom Navigation*), conectando la acción de logout para regresar al flujo de autenticación.

---

## Resultados de Verificación

- **Compilación:** `./gradlew assembleDebug` → **BUILD SUCCESSFUL**.
- **Pruebas Unitarias:** `./gradlew testDebugUnitTest` → **131 tests pasados en verde (0 fallos)**.
