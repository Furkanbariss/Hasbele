package com.furkanbarissonmezisik.tesbihatpro.core.content

import com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin.MuezzinStep
import com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin.PrayerFlow
import com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin.PrayerFlowId
import com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin.Recitation
import com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin.StepCondition
import com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin.StepKind

/**
 * Müezzinin vakit vakit yaptıkları. Sıralar Diyanet İslam İlmihali'ne (s.152–214), Din Hizmetleri
 * Uygulama Genelgesi'ne ve MEB Hitabet ve Mesleki Uygulama dersine göredir; Diyanet'te açık hükmü
 * bulunamayan sıralar kullanıcının kararıdır ve `docs/muezzin-icerik-kaynaklari.md`'de yazılıdır.
 *
 * - Sabah ve ikindide tesbihat farzdan hemen sonra, öğle, akşam, yatsı ve Cuma'da son sünnetten
 *   sonra yapılır (İlmihal s.162).
 * - Yatsıda sıra son sünnet, vitir, tesbihattır; Ramazan'da teravih son sünnetle vitir arasındadır.
 *
 * Adım id'leri kalıcıdır, değiştirilmez.
 */
object MuezzinFlows {

    private fun recite(id: String, instruction: String, text: Recitation? = null) =
        MuezzinStep(id, StepKind.RECITE, instruction, text)

    private fun pray(id: String, instruction: String) = MuezzinStep(id, StepKind.PRAY, instruction)

    private fun ezan(sabah: Boolean = false) = recite(
        id = "ezan",
        instruction = "Diyanet takvimine göre vakit girince ezanı oku.",
        text = if (sabah) MuezzinContent.sabahEzani else MuezzinContent.ezan,
    )

    private val ezanDuasi = recite(
        id = "ezan_duasi",
        instruction = "Ezandan sonra salavat getir ve ezan duasını içinden oku.",
        text = MuezzinContent.ezanDuasi,
    )

    private fun ilkSunnet(rekat: Int, gayriMuekked: Boolean = false) = pray(
        id = "ilk_sunnet",
        instruction = "İlk sünneti kıl ($rekat rekât" +
            (if (gayriMuekked) ", gayr-i müekked" else "") + ").",
    )

    private val kamet = recite(
        id = "kamet",
        instruction = "İmam namaza durmadan önce kameti getir. Kamet ezandan hızlı ve ara " +
            "vermeden okunur, ardından namaz başlar.",
        text = MuezzinContent.kamet,
    )

    private fun farz(rekat: Int) = pray(
        id = "farz",
        instruction = "İmama uyarak farzı kıl ($rekat rekât). İmamın sesi cemaate ulaşmıyorsa " +
            "tekbirleri yüksek sesle duyur.",
    )

    private val tesrik = MuezzinStep(
        id = "tesrik_tekbiri",
        kind = StepKind.RECITE,
        instruction = "Teşrik günlerindesin: selam verilir verilmez, ara vermeden teşrik tekbirini " +
            "getir.",
        recitation = MuezzinContent.tesrikTekbiri,
        condition = StepCondition.TESRIK,
    )

    private val enteSselam = recite(
        id = "ente_s_selam",
        instruction = "Selamdan sonra içinden üç kez \"Estağfirullâh\" de, ardından yüksek sesle oku.",
        text = MuezzinContent.enteSselam,
    )

    private fun sonSunnet(rekat: Int) = pray(
        id = "son_sunnet",
        instruction = "Son sünneti kıl ($rekat rekât). Tesbihat sünnetten sonra yapılır.",
    )

    private val vitir = pray(
        id = "vitir",
        instruction = "Vitir namazını kıl (3 rekât, vacip). Üçüncü rekâtta Fâtiha ve sûreden " +
            "sonra tekbir alınıp Kunut duaları okunur.",
    )

    private val kunut = recite(
        id = "kunut",
        instruction = "Vitirin üçüncü rekâtında Kunut dualarını oku.",
        text = MuezzinContent.kunut,
    )

    private val tesbihat = listOf(
        recite(
            id = "salavat",
            instruction = "Yüksek sesle \"Alâ Resûlinâ salavât\" de; cemaatle birlikte salavat getir.",
            text = MuezzinContent.salavatCagrisi,
        ),
        recite(
            id = "tesbih_duasi",
            instruction = "Tesbih duasını yüksek sesle oku.",
            text = MuezzinContent.tesbihDuasi,
        ),
        recite(
            id = "ayetel_kursi",
            instruction = "Eûzü-besmele çek; Âyetü'l-kürsî'yi cemaatle birlikte içinden oku.",
            text = MuezzinContent.ayetelKursi,
        ),
        MuezzinStep(
            id = "tesbihat",
            kind = StepKind.TESBIHAT,
            instruction = "Her tesbihi önce yüksek sesle bir kez söyle, sonra 33 kez çek: " +
                "Sübhânallâh, Elhamdülillâh, Allâhü ekber; ardından tehlili oku.",
        ),
        recite(
            id = "vehhab",
            instruction = "Tehlilden sonra yüksek sesle oku.",
            text = MuezzinContent.vehhab,
        ),
        MuezzinStep(
            id = "dua",
            kind = StepKind.LISTEN,
            instruction = "İmam dua eder; duaya \"âmin\" diyerek katıl.",
        ),
    )

    private val sabah = PrayerFlow(
        id = PrayerFlowId.SABAH,
        steps = listOf(
            MuezzinStep(
                id = "sabah_salasi",
                kind = StepKind.RECITE,
                instruction = "Cuma ve bayram sabahlarında sabah ezanından önce salâ ver.",
                recitation = MuezzinContent.sala,
                condition = StepCondition.FRIDAY_OR_BAYRAM_MORNING,
            ),
            ezan(sabah = true),
            ezanDuasi,
            ilkSunnet(rekat = 2),
            kamet,
            farz(rekat = 2),
            tesrik,
            enteSselam,
        ) + tesbihat,
    )

    private val ogle = PrayerFlow(
        id = PrayerFlowId.OGLE,
        steps = listOf(
            ezan(),
            ezanDuasi,
            ilkSunnet(rekat = 4),
            kamet,
            farz(rekat = 4),
            tesrik,
            enteSselam,
            sonSunnet(rekat = 2),
        ) + tesbihat,
    )

    private val ikindi = PrayerFlow(
        id = PrayerFlowId.IKINDI,
        steps = listOf(
            ezan(),
            ezanDuasi,
            ilkSunnet(rekat = 4, gayriMuekked = true),
            kamet,
            farz(rekat = 4),
            tesrik,
            enteSselam,
        ) + tesbihat,
    )

    private val aksam = PrayerFlow(
        id = PrayerFlowId.AKSAM,
        steps = listOf(
            ezan(),
            ezanDuasi,
            MuezzinStep(
                id = "bekle",
                kind = StepKind.WAIT,
                instruction = "Akşamın ilk sünneti yoktur. Cemaatin yetişmesi için iki rekât namaz " +
                    "kılınacak kadar bekle, sonra kameti getir.",
            ),
            kamet,
            farz(rekat = 3),
            tesrik,
            enteSselam,
            sonSunnet(rekat = 2),
        ) + tesbihat,
    )

    private val yatsiOncesi = listOf(
        ezan(),
        ezanDuasi,
        ilkSunnet(rekat = 4, gayriMuekked = true),
        kamet,
        farz(rekat = 4),
    )

    private val yatsi = PrayerFlow(
        id = PrayerFlowId.YATSI,
        steps = yatsiOncesi + listOf(
            tesrik,
            enteSselam,
            sonSunnet(rekat = 2),
            vitir,
            kunut,
        ) + tesbihat,
    )

    private val cuma = PrayerFlow(
        id = PrayerFlowId.CUMA,
        steps = listOf(
            recite(
                id = "cuma_salasi",
                instruction = "Öğle ezanından yaklaşık bir saat önce salâ ver.",
                text = MuezzinContent.sala,
            ),
            ezan(),
            ezanDuasi,
            ilkSunnet(rekat = 4),
            recite(
                id = "minber_ayeti",
                instruction = "Hatip minbere çıkarken bu ayeti oku.",
                text = MuezzinContent.ahzab56,
            ),
            recite(
                id = "ic_ezan",
                instruction = "Hatip minbere oturunca iç ezanı oku. İç ezan akşam ezanı gibi seri " +
                    "okunur.",
                text = MuezzinContent.ezan,
            ),
            MuezzinStep(
                id = "hutbe",
                kind = StepKind.LISTEN,
                instruction = "Hutbeyi dinle. Hatip minberden inince kamete hazırlan.",
            ),
            kamet,
            farz(rekat = 2),
            tesrik,
            enteSselam,
            sonSunnet(rekat = 4),
        ) + tesbihat,
    )

    /** 20 rekât, ikişer rekâtta selam; her dört rekâttan sonra tehlil ve salavat okunur. */
    private val teravihNamazi: List<MuezzinStep> = (1..10).flatMap { pair ->
        val first = pair * 2 - 1
        val prayer = pray(
            id = "teravih_$pair",
            instruction = "Teravihin $first. ve ${first + 1}. rekâtlarını kıl, selam ver " +
                "($pair / 10).",
        )
        if (pair % 2 == 0) {
            listOf(
                prayer,
                recite(
                    id = "teravih_ara_${pair / 2}",
                    instruction = "Dört rekât tamamlandı. Oturuşta tehlil ve salavat getir.",
                    text = MuezzinContent.teravihAra,
                ),
            )
        } else {
            listOf(prayer)
        }
    }

    private val teravih = PrayerFlow(
        id = PrayerFlowId.TERAVIH,
        steps = yatsiOncesi + listOf(
            enteSselam,
            sonSunnet(rekat = 2),
        ) + teravihNamazi + listOf(
            pray(
                id = "vitir",
                instruction = "Ramazan'da vitir cemaatle kılınır (3 rekât), kamet getirilmez. " +
                    "Üçüncü rekâtta Kunut duaları okunur.",
            ),
            kunut,
        ) + tesbihat,
    )

    private val bayram = PrayerFlow(
        id = PrayerFlowId.BAYRAM,
        steps = listOf(
            MuezzinStep(
                id = "bilgi",
                kind = StepKind.WAIT,
                instruction = "Bayram namazında ezan okunmaz, kamet getirilmez. Namaz vacip ve iki " +
                    "rekâttır, hutbe namazdan sonra okunur.",
            ),
            recite(
                id = "niyet",
                instruction = "Namazdan önce cemaate bayram namazının niyetini ve zevâid " +
                    "tekbirlerinin yerini hatırlat.",
            ),
            pray(
                id = "namaz",
                instruction = "İmama uyarak iki rekâtı kıl. Birinci rekâtta Sübhâneke'den sonra, " +
                    "ikinci rekâtta kıraatten sonra üçer zevâid tekbiri alınır; ikinci rekâtta " +
                    "dördüncü tekbirle rükûya gidilir. İmamın sesi yetmiyorsa tekbirleri duyur.",
            ),
            MuezzinStep(
                id = "tesrik_tekbiri",
                kind = StepKind.RECITE,
                instruction = "Kurban Bayramı'nda selam verilir verilmez teşrik tekbirini getir.",
                recitation = MuezzinContent.tesrikTekbiri,
                condition = StepCondition.TESRIK,
            ),
            recite(
                id = "minber_tekbiri",
                instruction = "Hatip minbere çıkınca cemaatle birlikte üç kez tekbir getir.",
                text = MuezzinContent.tesrikTekbiri,
            ),
            MuezzinStep(
                id = "hutbe",
                kind = StepKind.LISTEN,
                instruction = "Hutbeyi dinle.",
            ),
            recite(
                id = "inis_tekbiri",
                instruction = "Hatip minberden inerken cemaatle birlikte tekbir getir.",
                text = MuezzinContent.tesrikTekbiri,
            ),
        ),
    )

    val all: Map<PrayerFlowId, PrayerFlow> =
        listOf(sabah, ogle, ikindi, aksam, yatsi, cuma, teravih, bayram).associateBy { it.id }

    fun flow(id: PrayerFlowId): PrayerFlow = all.getValue(id)
}
