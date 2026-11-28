# Plan de Implementación — Módulo de Aportaciones y Progreso de Metas de Ahorro (Endpoint E044)

Este plan detalla la incorporación del flujo de **Aportaciones a Metas de Ahorro (E044 - `POST /goal-contributions`)** en `GoalsScreen.kt` y `GoalsViewModel.kt`, permitiendo al usuario visualizar el detalle de avance y abonar fondos directamente a sus metas activas.

---

## User Review Required

> [!IMPORTANT]
> Revisa las nuevas opciones interactivas para metas de ahorro:

1. **Botón de Aportación en Tarjetas de Meta (`+ $`):**
   - Cada tarjeta de meta incluye un botón interactivo de aporte económico.
   - Al presionarlo, abre un diálogo modal para ingresar el monto a abonar (ej. `500.00`).
2. **Registro Transaccional con E044 (`createGoalContribution`):**
   - Envía la aportación al backend utilizando el contrato de asignaciones (`GoalAllocationDto`) y clave de idempotencia.
   - Actualiza automáticamente el progreso en tiempo real al refrescar las metas.

---

## Proposed Changes

### Módulo de Metas de Ahorro (`mx.nexum.app.financial.ui`)

#### [MODIFY] [GoalsViewModel.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/ui/GoalsViewModel.kt)
- Añadir la intención `ContributeToGoal(goalId, amount)` invocando `financialRepository.createGoalContribution`.

#### [MODIFY] [GoalsScreen.kt](file:///C:/Users/Rayqu/StudioProjects/Nexum-Mobile/app/src/main/java/mx/nexum/app/financial/ui/GoalsScreen.kt)
- Añadir el botón de aportación y el diálogo modal para ingresar fondos a una meta específica.

---

## Verification Plan

### Automated Tests
- Compilación del proyecto (`./gradlew assembleDebug`).
- Pruebas unitarias (`./gradlew testDebugUnitTest` → 329+ tests en verde).
