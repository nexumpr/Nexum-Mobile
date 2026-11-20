# Walkthrough — Selector de Tema Claro / Oscuro (Estética Aero-Futurista)

He implementado un **Selector de Tema Claro / Oscuro** que mantiene la estética inmersiva **Aero-Futuristic** (con superficies translúcidas y acentos neón adaptados tanto para modo oscuro como luminoso).

## Cambios Realizados

### Sistema de Diseño y UI (`mx.nexum.app.ui.theme`, `mx.nexum.app.financial.ui`, `mx.nexum.app`)

#### [MODIFY] [Color.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/ui/theme/Color.kt), [Theme.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/ui/theme/Theme.kt)
- Definida la paleta clara aero-futurista (`LightBackground`, `LightSurface`, `LightGlassCard`, `AeroCyanDark`, `AeroPurpleDark`).
- Configurado `AeroLightColorScheme` y actualizado `NexumTheme` para conmutar dinámicamente entre modo oscuro y claro.

#### [MODIFY] [SettingsViewModel.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/ui/SettingsViewModel.kt), [SettingsScreen.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/ui/SettingsScreen.kt)
- Añadida tarjeta con un interruptor (*Switch*) de **"Apariencia Aero"** (Modo Oscuro Cyber vs. Modo Claro Luminoso).

#### [MODIFY] [MainAppShell.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/ui/MainAppShell.kt), [AuthFlow.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/auth/ui/AuthFlow.kt), [MainActivity.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/MainActivity.kt)
- Propagado el estado de tema (`isDarkTheme`) desde la raíz de la actividad para actualizar el tema de toda la aplicación en tiempo real al accionar el interruptor.

---

## Resultados de Verificación

- **Compilación:** `./gradlew assembleDebug` → **BUILD SUCCESSFUL**.
- **Pruebas Unitarias:** `./gradlew testDebugUnitTest` → **240 tests pasados en verde (0 fallos)**.
