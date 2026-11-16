package mx.nexum.app.core.mvi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MviCoreTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    // Fixtures sintéticos desacoplados de pantallas o finanzas
    data class TestState(
        val count: Int = 0,
        val text: String = "",
        val isLoading: Boolean = false
    ) : UiState

    sealed interface TestIntent : UiIntent {
        data object Increment : TestIntent
        data class SetText(val value: String) : TestIntent
        data class SetLoading(val loading: Boolean) : TestIntent
        data class RequestNotification(val message: String) : TestIntent
    }

    sealed interface TestEffect : UiEffect {
        data class ShowNotification(val message: String) : TestEffect
    }

    class TestReducer : Reducer<TestState, TestIntent> {
        override fun reduce(currentState: TestState, intent: TestIntent): TestState {
            return when (intent) {
                TestIntent.Increment -> currentState.copy(count = currentState.count + 1)
                is TestIntent.SetText -> currentState.copy(text = intent.value)
                is TestIntent.SetLoading -> currentState.copy(isLoading = intent.loading)
                is TestIntent.RequestNotification -> currentState // los efectos no mutan el estado persistente
            }
        }
    }

    class TestViewModel(
        private val reducer: TestReducer = TestReducer()
    ) : MviViewModel<TestState, TestIntent, TestEffect>(TestState()) {

        override fun onIntent(intent: TestIntent) {
            when (intent) {
                is TestIntent.RequestNotification -> {
                    emitEffect(TestEffect.ShowNotification(intent.message))
                }
                else -> {
                    setState { reducer.reduce(this, intent) }
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

    @Test
    fun initialStateIsExposedCorrectly() {
        val vm = TestViewModel()
        assertEquals(TestState(count = 0, text = "", isLoading = false), vm.uiState.value)
        assertEquals(0, vm.currentState.count)
    }

    @Test
    fun stateIsImmutableAndPureReducerReturnsNewInstance() {
        val reducer = TestReducer()
        val originalState = TestState(count = 10, text = "inicial")

        val newState = reducer.reduce(originalState, TestIntent.Increment)

        assertEquals(10, originalState.count)
        assertEquals(11, newState.count)
        assertNotSame(originalState, newState)
    }

    @Test
    fun unidirectionalDataFlowTransitionsStateDeterministically() = runTest {
        val vm = TestViewModel()

        vm.onIntent(TestIntent.Increment)
        assertEquals(1, vm.uiState.value.count)

        vm.onIntent(TestIntent.SetText("Nuevo valor"))
        assertEquals("Nuevo valor", vm.uiState.value.text)
        assertEquals(1, vm.uiState.value.count)

        vm.onIntent(TestIntent.SetLoading(true))
        assertTrue(vm.uiState.value.isLoading)
    }

    @Test
    fun sequentialIntentsProduceDeterministicAccumulatedState() = runTest {
        val vm = TestViewModel()

        repeat(5) {
            vm.onIntent(TestIntent.Increment)
        }

        assertEquals(5, vm.uiState.value.count)
    }

    @Test
    fun effectsAreEmittedViaSharedFlowAndDoNotMutateState() = runTest {
        val vm = TestViewModel()
        val collectedEffects = mutableListOf<TestEffect>()

        val collectJob = launch(testDispatcher) {
            vm.effects.collect { collectedEffects.add(it) }
        }

        vm.onIntent(TestIntent.RequestNotification("Alerta de prueba"))

        assertEquals(1, collectedEffects.size)
        assertEquals(TestEffect.ShowNotification("Alerta de prueba"), collectedEffects.first())
        assertEquals(TestState(), vm.uiState.value) // El estado permanece inalterado

        collectJob.cancel()
    }

    @Test
    fun effectsAreOneShotAndDoNotReplayToLateCollectors() = runTest {
        val vm = TestViewModel()

        // Disparo de efecto antes de que exista un observador activo
        vm.onIntent(TestIntent.RequestNotification("Mensaje previo"))

        // Un observador tardío (e.g. recomposición tras rotación) no debe recibir el efecto ya emitido
        val lateCollectedEffects = mutableListOf<TestEffect>()
        val lateJob = launch(testDispatcher) {
            vm.effects.collect { lateCollectedEffects.add(it) }
        }

        assertTrue(lateCollectedEffects.isEmpty())

        lateJob.cancel()
    }
}
