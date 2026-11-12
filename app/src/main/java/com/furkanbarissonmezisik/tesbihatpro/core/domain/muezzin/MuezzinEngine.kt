package com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin

/**
 * Akışta nerede olunduğu. [stepIndex] seçilen [options] ile görünen adımlar içindeki sıradır.
 * [finished] son adımda "Bitir"e basıldığını gösterir.
 */
data class MuezzinState(
    val flowId: PrayerFlowId,
    val options: FlowOptions = FlowOptions(),
    val stepIndex: Int = 0,
    val finished: Boolean = false,
)

sealed interface MuezzinEvent {
    data object Next : MuezzinEvent
    data object Back : MuezzinEvent
    data object Restart : MuezzinEvent
}

/** Müezzin akışının saf durum makinesi; ekran ve kayıt yalnızca onun çıktısını kullanır. */
object MuezzinEngine {

    fun visibleSteps(flow: PrayerFlow, options: FlowOptions): List<MuezzinStep> =
        flow.steps.filter { it.condition.isActive(options) }

    fun start(flowId: PrayerFlowId, options: FlowOptions): MuezzinState =
        MuezzinState(flowId = flowId, options = options)

    /** [stepCount] görünen adım sayısıdır, [visibleSteps] ile hesaplanır. */
    fun reduce(state: MuezzinState, event: MuezzinEvent, stepCount: Int): MuezzinState {
        require(stepCount > 0) { "Akışta görünen adım yok" }
        val last = stepCount - 1
        val index = state.stepIndex.coerceIn(0, last)
        return when (event) {
            MuezzinEvent.Next -> when {
                state.finished -> state.copy(stepIndex = last)
                index < last -> state.copy(stepIndex = index + 1)
                else -> state.copy(stepIndex = last, finished = true)
            }
            MuezzinEvent.Back -> when {
                state.finished -> state.copy(stepIndex = last, finished = false)
                else -> state.copy(stepIndex = (index - 1).coerceAtLeast(0))
            }
            MuezzinEvent.Restart -> state.copy(stepIndex = 0, finished = false)
        }
    }

    /**
     * Kayıtlı adım id'sinden kaldığı yeri bulur. Adım artık görünmüyorsa (içerik değişti ya da
     * koşulu kalktı) akış baştan başlar; yanlış adımda kalmaktansa baştan başlamak güvenlidir.
     */
    fun indexOf(steps: List<MuezzinStep>, stepId: String?): Int =
        steps.indexOfFirst { it.id == stepId }.coerceAtLeast(0)
}

internal fun StepCondition.isActive(options: FlowOptions): Boolean = when (this) {
    StepCondition.ALWAYS -> true
    StepCondition.TESRIK -> options.tesrik
    StepCondition.FRIDAY_OR_BAYRAM_MORNING -> options.fridayOrBayramMorning
}
