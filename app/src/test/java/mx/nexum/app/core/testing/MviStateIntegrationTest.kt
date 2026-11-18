package mx.nexum.app.core.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import mx.nexum.app.core.mvi.MviViewModel
import mx.nexum.app.core.mvi.UiEffect
import mx.nexum.app.core.mvi.UiIntent
import mx.nexum.app.core.mvi.UiState
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas de integración de flujo MVI, transiciones de estado y efectos efímeros (M080-R01, M080-R02, M080-R03).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MviStateIntegrationTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    // --- Modelos sintéticos para pruebas MVI ---

    sealed interface TestUiState : UiState {
        object Idle : TestUiState
        object Loading : TestUiState
        data class Content(val items: List<String>) : TestUiState
        object Empty : TestUiState
        data class Error(val message: String) : TestUiState
        object Offline : TestUiState
    }

    sealed interface TestUiIntent : UiIntent {
        object LoadData : TestUiIntent
        object LoadEmpty : TestUiIntent
        data class LoadError(val reason: String) : TestUiIntent
        object SimulateDisconnect : TestUiIntent
        data class EmitOneShotEffect(val message: String) : TestUiIntent
    }

    sealed interface TestUiEffect : UiEffect {
        data class ShowSnackbar(val text: String) : TestUiEffect
    }

    private class SampleMviViewModel : MviViewModel<TestUiState, TestUiIntent, TestUiEffect>(
        initialState = TestUiState.Idle
    ) {
        override fun onIntent(intent: TestUiIntent) {
            when (intent) {
                is TestUiIntent.LoadData -> {
                    setState { TestUiState.Loading }
                    // Simula respuesta sintética exitosa
                    setState { TestUiState.Content(listOf("Elemento sintético 1", "Elemento sintético 2")) }
                }
                is TestUiIntent.LoadEmpty -> {
                    setState { TestUiState.Loading }
                    setState { TestUiState.Empty }
                }
                is TestUiIntent.LoadError -> {
                    setState { TestUiState.Loading }
                    setState { TestUiState.Error(intent.reason) }
                }
                is TestUiIntent.SimulateDisconnect -> {
                    setState { TestUiState.Loading }
                    setState { TestUiState.Offline }
                }
                is TestUiIntent.EmitOneShotEffect -> {
                    emitEffect(TestUiEffect.ShowSnackbar(intent.message))
                }
            }
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- M080-R01: Transición MVI con estados sintéticos ---

    @Test
    fun mviTransitionsFromIdleToLoadingToContentSuccessfully() = runTest {
        val viewModel = SampleMviViewModel()
        assertEquals(TestUiState.Idle, viewModel.uiState.value)

        viewModel.onIntent(TestUiIntent.LoadData)

        val finalState = viewModel.uiState.value
        assertTrue(finalState is TestUiState.Content)
        val content = finalState as TestUiState.Content
        assertEquals(2, content.items.size)
        assertEquals("Elemento sintético 1", content.items.first())
    }

    // --- M080-R02: Validación de estados de carga, vacío, error y desconexión ---

    @Test
    fun mviTransitionsToEmptyStateWhenResultHasNoItems() = runTest {
        val viewModel = SampleMviViewModel()
        viewModel.onIntent(TestUiIntent.LoadEmpty)
        assertEquals(TestUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun mviTransitionsToErrorStateOnValidationOrServerFailure() = runTest {
        val viewModel = SampleMviViewModel()
        val errorReason = "Error sintético de prueba"
        viewModel.onIntent(TestUiIntent.LoadError(errorReason))

        val finalState = viewModel.uiState.value
        assertTrue(finalState is TestUiState.Error)
        assertEquals(errorReason, (finalState as TestUiState.Error).message)
    }

    @Test
    fun mviTransitionsToOfflineStateOnNetworkDisconnect() = runTest {
        val viewModel = SampleMviViewModel()
        viewModel.onIntent(TestUiIntent.SimulateDisconnect)
        assertEquals(TestUiState.Offline, viewModel.uiState.value)
    }

    // --- M080-R03: Efectos efímeros de disparo único y no repetición por recomposición ---

    @Test
    fun oneShotEffectsDoNotReplayToLateCollectors() = runTest {
        val viewModel = SampleMviViewModel()
        val receivedEffects = mutableListOf<TestUiEffect>()

        // 1. Colector activo recibe el efecto
        val job = launch(testDispatcher) {
            viewModel.effects.collect { receivedEffects.add(it) }
        }

        viewModel.onIntent(TestUiIntent.EmitOneShotEffect("Mensaje OneShot"))
        assertEquals(1, receivedEffects.size)
        assertEquals(TestUiEffect.ShowSnackbar("Mensaje OneShot"), receivedEffects.first())

        job.cancel()

        // 2. Un segundo colector (simulando recomposición tardía) NO debe recibir el efecto anterior
        val lateReceivedEffects = mutableListOf<TestUiEffect>()
        val lateJob = launch(testDispatcher) {
            viewModel.effects.collect { lateReceivedEffects.add(it) }
        }

        // Sin nuevas emisiones, lateReceivedEffects debe permanecer vacío
        assertTrue("Los efectos efímeros no deben repetirse en recomposición", lateReceivedEffects.isEmpty())

        lateJob.cancel()
    }

    @Test
    fun repeatedIdenticalIntentsProduceDeterministicStateWithoutDuplicateEffects() = runTest {
        val viewModel = SampleMviViewModel()
        val receivedEffects = mutableListOf<TestUiEffect>()

        val job = launch(testDispatcher) {
            viewModel.effects.collect { receivedEffects.add(it) }
        }

        // Se envía una sola intención de efecto
        viewModel.onIntent(TestUiIntent.EmitOneShotEffect("Efecto Único"))
        assertEquals(1, receivedEffects.size)

        // Se reintenta otra intención de carga sin disparar nuevo efecto
        viewModel.onIntent(TestUiIntent.LoadData)
        assertEquals(1, receivedEffects.size) // No hay efectos duplicados
        assertTrue(viewModel.uiState.value is TestUiState.Content)

        job.cancel()
    }
}
