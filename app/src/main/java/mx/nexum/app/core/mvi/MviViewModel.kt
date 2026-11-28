package mx.nexum.app.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel base para componentes con arquitectura MVI.
 * Garantiza:
 * 1. Flujo unidireccional de datos (Unidirectional Data Flow - UDF).
 * 2. Estado observable inmutable mediante [StateFlow].
 * 3. Eventos efímeros de un solo disparo desacoplados del estado mediante [SharedFlow].
 * 4. Procesamiento atómico de intenciones.
 */
abstract class MviViewModel<S : UiState, I : UiIntent, E : UiEffect>(
    initialState: S
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<E>(
        replay = 0,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<E> = _effects.asSharedFlow()

    val currentState: S
        get() = _uiState.value

    /**
     * Punto de entrada único para intenciones de UI.
     */
    abstract fun onIntent(intent: I)

    /**
     * Actualiza el estado actual de forma atómica e inmutable.
     */
    protected fun setState(reducer: S.() -> S) {
        _uiState.update { it.reducer() }
    }

    protected fun launchSafely(
        onError: (Throwable) -> Unit,
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewModelScope.launch {
            try {
                block()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Throwable) {
                onError(error)
            }
        }
    }

    /**
     * Emite un efecto efímero de disparo único fuera del estado persistente.
     */
    protected fun emitEffect(effect: E) {
        viewModelScope.launch {
            _effects.emit(effect)
        }
    }
}
