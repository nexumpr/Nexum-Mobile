# Plan de Implementación — Unificación Visual Absoluta de Presupuestos, Analítica, Perfil y Ajustes (`SkeuomorphicCard`)

Este plan detalla la unificación estricta de las pantallas restantes (**Presupuestos**, **Analítica**, **Perfil** y **Ajustes**) utilizando el componente `SkeuomorphicCard` (chasis de aluminio cepillado y visor LCD empotrado) para lograr una identidad visual 100% uniforme en todo el sistema.

---

## User Review Required

> [!IMPORTANT]
> Revisa la unificación total en Presupuestos, Analítica, Perfil y Ajustes:

1. **Presupuestos (`BudgetsScreen`):**
   - Las tarjetas de uso y límite de presupuesto adoptan el chasis metálico hi-fi con visor LCD.
2. **Analítica e IA (`AnalyticsScreen`):**
   - Las tarjetas de distribución por categoría y recomendaciones se envuelven en `SkeuomorphicCard`.
3. **Perfil y Ajustes (`ProfileScreen`, `SettingsScreen`):**
   - Las tarjetas de identidad, preferencias, tema y seguridad adoptan el acabado metálico esqueumórfico.

---

## Proposed Changes

### Pantallas Restantes (`mx.nexum.app.financial.ui`)

#### [MODIFY] [BudgetsScreen.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/ui/BudgetsScreen.kt), [AnalyticsScreen.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/ui/AnalyticsScreen.kt), [ProfileScreen.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/ui/ProfileScreen.kt), [SettingsScreen.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/ui/SettingsScreen.kt)
- Envolver todas las superficies y tarjetas en `SkeuomorphicCard`.

---

## Verification Plan

### Automated Tests
- Compilación del proyecto (`./gradlew assembleDebug`).
- Pruebas unitarias (`./gradlew testDebugUnitTest` → 329+ tests en verde).
