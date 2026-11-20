# Walkthrough — Pantallas de Perfil y Preferencias (Oleada 1)

He implementado las pantallas y ViewModels de **Perfil y Preferencias de Usuario** (Unidades correspondientes a la Guía Oleada 1: E012, E013, E068, E069) bajo la arquitectura MVI y el diseño **Aero-Futuristic**.

## Cambios Realizados

### Módulo Financiero / Perfil UI (`mx.nexum.app.financial.ui`)

#### [NEW] [ProfileViewModel.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/ui/ProfileViewModel.kt)
- ViewModel MVI para consultar la información del perfil (`getUserProfile`) y las preferencias (`getPreferences`), así como actualizar ambos recursos con los endpoints de la Oleada 1.

#### [NEW] [ProfileScreen.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/ui/ProfileScreen.kt)
- Pantalla con estilo Aero-Futurista (*Glassmorphism* y brillo ambiental) para visualizar el correo electrónico (no editable), nombre mostrado editable, idioma y zona horaria.

---

## Resultados de Verificación

- **Compilación:** `./gradlew assembleDebug` → **BUILD SUCCESSFUL**.
- **Pruebas Unitarias:** `./gradlew testDebugUnitTest` → **240 tests pasados en verde (0 fallos)**.
