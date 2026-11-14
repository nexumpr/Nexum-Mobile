---
project: Nexum-Mobile
unit: 350-Metas-Ahorro
kind: technical-plan
version: 1.0.0
status: ready
implementation: only_on_explicit_request
---

# PLAN — Gestión de Metas de Ahorro (Savings Goals CRUD & Details)

## Estrategia técnica
1. **Contratos DTO y Endpoints:**
   - `GoalDto`, `CreateGoalRequest`, `CreateGoalResponse`, `GoalsListResponse`.
   - Métodos en `GoalsApi` y `GoalsRepository`.
2. **Estados MVI y UI:**
   - `GoalsUiState`, `GoalDetailUiState`.
   - Pantalla de listado de metas (`GoalsScreen`), tarjeta de progreso con porcentaje limitado a 100%, y formulario de creación (`CreateGoalScreen`).
3. **Simulación en Mock:**
   - Colección de metas en memoria, cálculo de `remaining`, `progressPercent` (tope 100%), y persistencia de aportaciones iniciales.
