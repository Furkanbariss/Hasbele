package com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin

/** Müezzinlik Modu'nda seçilebilen akışlar. Kayıtlı oturumda adıyla saklanır, ad değiştirilmez. */
enum class PrayerFlowId { SABAH, OGLE, IKINDI, AKSAM, YATSI, CUMA, TERAVIH, BAYRAM }

enum class StepKind {
    /** Müezzin okur. */
    RECITE,

    /** Namaz kılınır. */
    PRAY,

    /** Beklenir. */
    WAIT,

    /** İmam ya da hatip okur, müezzin dinler. */
    LISTEN,

    /** Tesbihat Tesbih sayacıyla sayılır. */
    TESBIHAT,
}

/** Adımın akışa hangi durumda girdiği. */
enum class StepCondition {
    ALWAYS,

    /** Yalnız teşrik günlerinde (arefe sabahından bayramın 4. günü ikindiye kadar). */
    TESRIK,

    /** Yalnız Cuma ve bayram sabahlarında. */
    FRIDAY_OR_BAYRAM_MORNING,
}

/** Kullanıcının akışı başlatırken seçtiği durumlar. */
data class FlowOptions(
    val tesrik: Boolean = false,
    val fridayOrBayramMorning: Boolean = false,
)

/**
 * Müezzinin okuduğu ya da dinlediği metin. Dini metinler yalnız `core/content` içinde tanımlanır.
 *
 * Çok satırlı metinlerde Arapça, okunuş ve anlam satır satır birbirine karşılık gelir.
 *
 * @property arabic null ise Arapçası henüz kaynağından doğrulanmamıştır ve gösterilmez.
 * @property meaning null ise meal telifli olduğu için konmamıştır; [meaningRef] nerede okunacağını
 * söyler.
 * @property source Metnin alındığı Diyanet kaynağı ve sayfası.
 */
data class Recitation(
    val id: String,
    val arabic: String?,
    val transliteration: String,
    val meaning: String?,
    val source: String,
    val meaningRef: String? = null,
)

/**
 * Akıştaki bir adım.
 *
 * @property id Akış içinde tekildir ve kaldığı yeri bulmak için saklanır; içerik değişse de
 * aynı adımın id'si korunur.
 * @property instruction Kullanıcıya o an ne yapacağını söyleyen kısa yönlendirme.
 */
data class MuezzinStep(
    val id: String,
    val kind: StepKind,
    val instruction: String,
    val recitation: Recitation? = null,
    val condition: StepCondition = StepCondition.ALWAYS,
)

data class PrayerFlow(val id: PrayerFlowId, val steps: List<MuezzinStep>) {
    init {
        require(steps.isNotEmpty()) { "$id akışında adım yok" }
        val duplicates = steps.groupBy { it.id }.filterValues { it.size > 1 }.keys
        require(duplicates.isEmpty()) { "$id akışında tekrarlanan adım id'leri: $duplicates" }
        require(steps.any { it.condition == StepCondition.ALWAYS }) {
            "$id akışı her koşulda en az bir adım göstermeli"
        }
    }
}
