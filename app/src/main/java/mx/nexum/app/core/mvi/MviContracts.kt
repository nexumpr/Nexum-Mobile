package mx.nexum.app.core.mvi

/**
 * Marcador para estados inmutables de UI.
 * Cada pantalla o componente expone un único UiState inmutable.
 */
interface UiState

/**
 * Marcador para intenciones explícitas de usuario o del sistema.
 */
interface UiIntent

/**
 * Marcador para efectos secundarios de un solo disparo (navegación, mensajes efímeros).
 * No persisten en el estado observable y se consumen una única vez.
 */
interface UiEffect

/**
 * Contrato funcional para un reductor determinista puro.
 * Transforma (EstadoActual, Intención) -> NuevoEstado sin efectos secundarios colaterales.
 */
fun interface Reducer<S : UiState, I : UiIntent> {
    fun reduce(currentState: S, intent: I): S
}
