# Walkthrough — Pantallas de Privacidad, Consentimientos y Ayuda/FAQ (Oleada 1)

He implementado los módulos UI y ViewModels MVI restantes de la **Guía Oleada 1** (Aviso de Privacidad E015, Consentimientos E016/E017, FAQ E070 y Consejos Educativos E071) bajo la arquitectura MVI y el diseño **Aero-Futuristic**.

## Cambios Realizados

### Módulo Financiero / Privacidad y Ayuda UI (`mx.nexum.app.financial.ui`)

#### [NEW] [PrivacyNoticeViewModel.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/ui/PrivacyNoticeViewModel.kt), [PrivacyNoticeScreen.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/ui/PrivacyNoticeScreen.kt)
- ViewModel MVI y pantalla para consultar el aviso legal vigente y registrar la aceptación de términos con diseño *Glassmorphism*.

#### [NEW] [HelpEducationViewModel.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/ui/HelpEducationViewModel.kt), [HelpEducationScreen.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/ui/HelpEducationScreen.kt)
- ViewModel MVI y pantalla para listar preguntas frecuentes (FAQ) y consejos de educación financiera.

---

## Resultados de Verificación

- **Compilación:** `./gradlew assembleDebug` → **BUILD SUCCESSFUL**.
- **Pruebas Unitarias:** `./gradlew testDebugUnitTest` → **240 tests pasados en verde (0 fallos)**.
