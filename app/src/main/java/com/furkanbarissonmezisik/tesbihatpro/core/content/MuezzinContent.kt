package com.furkanbarissonmezisik.tesbihatpro.core.content

import com.furkanbarissonmezisik.tesbihatpro.core.domain.muezzin.Recitation

/**
 * Müezzinlik Modu'nun okunan metinleri. Dini içerikte ölçü Diyanet'tir; her metnin kaynağı
 * [Recitation.source] alanında, ayrıntısı `docs/muezzin-icerik-kaynaklari.md` içindedir.
 *
 * - Arapça: Diyanet sitelerinde metin olarak bulunanlar oradan aynen alındı. Bulunmayanlar Diyanet
 *   Namaz İlmihali'nin metin katmanıyla karşılaştırılarak yazıldı; yayından önce bir hoca ya da
 *   müftülük kontrol etmelidir.
 * - Okunuş Diyanet'in yazım usulüne göredir.
 * - Kısa formüllerin anlamı yazıldı. Uzun duaların ve ayetlerin mealleri telifli olduğundan
 *   konmadı; [Recitation.meaningRef] nerede okunacağını gösterir.
 *
 * Id'ler kalıcıdır, değiştirilmez.
 */
object MuezzinContent {

    private object Sources {
        const val NAMAZ_ILMIHALI = "Diyanet Namaz İlmihali (DİB Yay. 703)"
        const val ISLAM_ILMIHALI = "Diyanet İslam İlmihali (DİB Yay. 334, 2019)"
        const val HADISLERLE_ISLAM = "Hadislerle İslâm, c.2 s.305 (Diyanet)"
        const val KURUL_TESBIHAT = "Din İşleri Yüksek Kurulu: Namazlardan sonra tesbihat nasıl yapılır"
        const val KURUL_SALA = "Din İşleri Yüksek Kurulu: Salânın anlamı ve dindeki yeri"
        const val KURUL_TERAVIH = "Din İşleri Yüksek Kurulu: Teravih namazının hükmü ve mahiyeti"
        const val KURAN = "Diyanet Kur'an-ı Kerim Portalı (kuran.diyanet.gov.tr)"
        const val MEB_HITABET = "MEB Hitabet ve Mesleki Uygulama 11. sınıf (2026)"
        const val TDV = "TDV İslâm Ansiklopedisi"
        const val ARABIC_REVIEW = "Arapçası hoca kontrolü bekliyor"
    }

    /** Ezanda her satır müezzinin bir nefeste okuduğu bölümdür. */
    private data class Line(val arabic: String, val transliteration: String, val meaning: String)

    private val tekbir = Line(
        arabic = "اللَّهُ أَكْبَرُ اللَّهُ أَكْبَرُ",
        transliteration = "Allâhü ekber, Allâhü ekber",
        meaning = "Allah en büyüktür, Allah en büyüktür.",
    )
    private val sehadetTevhid = Line(
        arabic = "أَشْهَدُ أَنْ لاَ إِلَهَ إِلاَّ اللَّهُ",
        transliteration = "Eşhedü en lâ ilâhe illallâh",
        meaning = "Şahitlik ederim ki Allah'tan başka ilâh yoktur.",
    )
    private val sehadetRisalet = Line(
        arabic = "أَشْهَدُ أَنَّ مُحَمَّدًا رَسُولُ اللَّهِ",
        transliteration = "Eşhedü enne Muhammeden Resûlullâh",
        meaning = "Şahitlik ederim ki Muhammed Allah'ın elçisidir.",
    )
    private val hayyaAlesSalah = Line(
        arabic = "حَىَّ عَلَى الصَّلاَةِ",
        transliteration = "Hayye ale's-salâh",
        meaning = "Haydi namaza!",
    )
    private val hayyaAlelFelah = Line(
        arabic = "حَىَّ عَلَى الْفَلاَحِ",
        transliteration = "Hayye ale'l-felâh",
        meaning = "Haydi kurtuluşa!",
    )
    private val salatuHayrun = Line(
        arabic = "الصَّلاَةُ خَيْرٌ مِنَ النَّوْمِ",
        transliteration = "Es-salâtü hayrün mine'n-nevm",
        meaning = "Namaz uykudan hayırlıdır.",
    )
    private val kadKametis = Line(
        arabic = "قَدْ قَامَتِ الصَّلاَةُ",
        transliteration = "Kad kâmeti's-salâh",
        meaning = "Namaz başladı.",
    )
    private val tevhid = Line(
        arabic = "لاَ إِلَهَ إِلاَّ اللَّهُ",
        transliteration = "Lâ ilâhe illallâh",
        meaning = "Allah'tan başka ilâh yoktur.",
    )

    private fun ezanLike(middle: List<Line>): List<Line> =
        listOf(tekbir, tekbir) +
            List(2) { sehadetTevhid } +
            List(2) { sehadetRisalet } +
            List(2) { hayyaAlesSalah } +
            List(2) { hayyaAlelFelah } +
            middle +
            listOf(tekbir, tevhid)

    private fun recitation(id: String, lines: List<Line>, source: String) = Recitation(
        id = id,
        arabic = lines.joinToString("\n") { it.arabic },
        transliteration = lines.joinToString("\n") { it.transliteration },
        meaning = lines.joinToString("\n") { it.meaning },
        source = source,
    )

    val ezan = recitation(
        id = "ezan",
        lines = ezanLike(middle = emptyList()),
        source = "${Sources.HADISLERLE_ISLAM}; okunuş ${Sources.NAMAZ_ILMIHALI} s.138",
    )

    /** Sabah ezanında "Hayye ale'l-felâh"tan sonra iki kez "es-salâtü hayrün mine'n-nevm" denir. */
    val sabahEzani = recitation(
        id = "sabah_ezani",
        lines = ezanLike(middle = List(2) { salatuHayrun }),
        source = "${Sources.HADISLERLE_ISLAM}; okunuş ${Sources.NAMAZ_ILMIHALI} s.138; " +
            "\"es-salâtü hayrün mine'n-nevm\" satırının ${Sources.ARABIC_REVIEW}",
    )

    /** Hanefî kamet: ezan sözleri ve "Hayye ale'l-felâh"tan sonra iki kez "kad kâmeti's-salâh". */
    val kamet = recitation(
        id = "kamet",
        lines = ezanLike(middle = List(2) { kadKametis }),
        source = "${Sources.HADISLERLE_ISLAM}; ${Sources.NAMAZ_ILMIHALI} s.139; " +
            "${Sources.TDV} \"İkāmet\"",
    )

    val ezanDuasi = Recitation(
        id = "ezan_duasi",
        arabic = "اللَّهُمَّ رَبَّ هَذِهِ الدَّعْوَةِ التَّامَّةِ وَالصَّلاَةِ الْقَائِمَةِ آتِ مُحَمَّدًا " +
            "الْوَسِيلَةَ وَالْفَضِيلَةَ وَابْعَثْهُ مَقَامًا مَحْمُودًا الَّذِى وَعَدْتَهُ",
        transliteration = "Allâhümme Rabbe hâzihi'd-da'veti't-tâmmeti ve's-salâti'l-kâimeti, " +
            "âti Muhammedeni'l-vesîlete ve'l-fazîlete, veb'ashü makâmen mahmûdenillezî veadteh.",
        meaning = null,
        source = "Hadislerle İslâm, c.2 s.320 (Diyanet); ${Sources.NAMAZ_ILMIHALI} s.240",
        meaningRef = "${Sources.NAMAZ_ILMIHALI} s.240–241",
    )

    val enteSselam = Recitation(
        id = "ente_s_selam",
        arabic = "اللَّهُمَّ أَنْتَ السَّلاَمُ وَمِنْكَ السَّلاَمُ تَبَارَكْتَ يَا ذَا الجَلاَلِ وَالإِكْرَامِ",
        transliteration = "Allâhümme ente's-selâmü ve minke's-selâm, " +
            "tebârekte yâ ze'l-celâli ve'l-ikrâm.",
        meaning = "Allah'ım! Sen selamsın, selamet sendendir. Ey celal ve ikram sahibi, " +
            "Sen ne yücesin!",
        source = "${Sources.KURUL_TESBIHAT}; ${Sources.NAMAZ_ILMIHALI} s.219",
    )

    val salavatCagrisi = Recitation(
        id = "salavat_cagrisi",
        arabic = "عَلٰى رَسُولِنَا صَلَوَاتٌ\nاَللّٰهُمَّ صَلِّ عَلٰى مُحَمَّدٍ",
        transliteration = "Alâ Resûlinâ salavât\nAllâhümme salli alâ Muhammed",
        meaning = "Peygamberimize salavat getirin.\nAllah'ım! Muhammed'e salât eyle.",
        source = "${Sources.NAMAZ_ILMIHALI} s.220; ${Sources.MEB_HITABET} (EBA 4. ünite s.8); " +
            Sources.ARABIC_REVIEW,
    )

    val tesbihDuasi = Recitation(
        id = "tesbih_duasi",
        arabic = "سُبْحَانَ اللّٰهِ وَالْحَمْدُ لِلّٰهِ وَلَا اِلٰهَ اِلَّا اللّٰهُ وَاللّٰهُ اَكْبَرُ\n" +
            "وَلَا حَوْلَ وَلَا قُوَّةَ اِلَّا بِاللّٰهِ الْعَلِيِّ الْعَظِيمِ",
        transliteration = "Sübhânallâhi ve'l-hamdü lillâhi ve lâ ilâhe illallâhü vallâhü ekber.\n" +
            "Ve lâ havle ve lâ kuvvete illâ billâhi'l-aliyyi'l-azîm.",
        meaning = "Allah'ı her türlü eksiklikten tenzih ederim, hamd Allah'a mahsustur, Allah'tan " +
            "başka ilâh yoktur, Allah en büyüktür.\n" +
            "Güç ve kuvvet ancak yüce ve büyük olan Allah iledir.",
        source = "${Sources.NAMAZ_ILMIHALI} s.220–221; ${Sources.ARABIC_REVIEW}",
    )

    val ayetelKursi = Recitation(
        id = "ayetel_kursi",
        arabic = "اَعُوذُ بِاللّٰهِ مِنَ الشَّيْطَانِ الرَّجِيمِ\n" +
            "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيمِ\n" +
            "اَللّٰهُ لَٓا اِلٰهَ اِلَّا هُوَۚ اَلْحَیُّ الْقَیُّومُۚ لَا تَاْخُذُهُ سِنَةٌ وَلَا نَوْمٌؕ " +
            "لَهُ مَا فِی السَّمٰوَاتِ وَمَا فِی الْاَرْضِؕ مَنْ ذَا الَّذٖی یَشْفَعُ عِنْدَهُٓ اِلَّا " +
            "بِاِذْنِهٖؕ یَعْلَمُ مَا بَیْنَ اَیْدٖیهِمْ وَمَا خَلْفَهُمْۚ وَلَا یُحٖیطُونَ بِشَیْءٍ " +
            "مِنْ عِلْمِهٖٓ اِلَّا بِمَا شَٓاءَۚ وَسِعَ كُرْسِیُّهُ السَّمٰوَاتِ وَالْاَرْضَۚ وَلَا " +
            "یَؤُ۫دُهُ حِفْظُهُمَاۚ وَهُوَ الْعَلِیُّ الْعَظٖیمُ",
        transliteration = "E'ûzü billâhi mine'ş-şeytâni'r-racîm.\n" +
            "Bismillâhirrahmânirrahîm.\n" +
            "Allâhü lâ ilâhe illâ hüve'l-hayyü'l-kayyûm, lâ te'huzühû sinetün ve lâ nevm, " +
            "lehû mâ fi's-semâvâti ve mâ fi'l-ard, men zellezî yeşfeu indehû illâ bi-iznih, " +
            "ya'lemü mâ beyne eydîhim ve mâ halfehüm, ve lâ yuhîtûne bi-şey'in min ilmihî illâ " +
            "bimâ şâ', vesia kürsiyyühü's-semâvâti ve'l-ard, ve lâ yeûdühû hıfzuhümâ " +
            "ve hüve'l-aliyyü'l-azîm.",
        meaning = null,
        // Portal "yeûdühû"da Android'in çizemediği U+10EFF işaretini kullanıyor; Namaz İlmihali'nin
        // (s.221) aynı yerde kullandığı U+06EB alındı.
        source = "Bakara 2/255: ${Sources.KURAN} (\"yeûdühû\"daki işaret ${Sources.NAMAZ_ILMIHALI} " +
            "s.221 yazımıyla); Eûzü-besmele ${Sources.ARABIC_REVIEW}",
        meaningRef = "Kur'an Yolu meali: kuran.diyanet.gov.tr/tefsir/sure/2/ayet/255",
    )

    val vehhab = Recitation(
        id = "vehhab",
        arabic = "سُبْحَانَ رَبِّيَ الْعَلِيِّ الْاَعْلَى الْوَهَّابِ",
        transliteration = "Sübhâne Rabbiye'l-aliyyi'l-a'le'l-vehhâb.",
        meaning = "Yüce, en yüce ve lütfu bol olan Rabbimi tenzih ederim.",
        source = "${Sources.MEB_HITABET} s.93; ${Sources.ARABIC_REVIEW}",
    )

    val kunut = Recitation(
        id = "kunut",
        arabic = "اَللّٰهُمَّ اِنَّا نَسْتَعِينُكَ وَنَسْتَغْفِرُكَ وَنَسْتَهْدِيكَ وَنُؤْمِنُ بِكَ " +
            "وَنَتُوبُ اِلَيْكَ وَنَتَوَكَّلُ عَلَيْكَ وَنُثْن۪ى عَلَيْكَ الْخَيْرَ كُلَّهُ نَشْكُرُكَ " +
            "وَلَا نَكْفُرُكَ وَنَخْلَعُ وَنَتْرُكُ مَنْ يَفْجُرُكَ\n" +
            "اَللّٰهُمَّ اِيَّاكَ نَعْبُدُ وَلَكَ نُصَلّ۪ى وَنَسْجُدُ وَاِلَيْكَ نَسْعٰى وَنَحْفِدُ " +
            "نَرْجُو رَحْمَتَكَ وَنَخْشٰى عَذَابَكَ اِنَّ عَذَابَكَ بِالْكُفَّارِ مُلْحِقٌ",
        transliteration = "Allâhümme innâ neste'înüke ve nesteğfirüke ve nestehdîke ve nü'minü " +
            "bike ve netûbü ileyke ve netevekkelü aleyke ve nüsnî aleyke'l-hayra küllehû " +
            "neşkürüke ve lâ nekfürüke ve nahleu ve netrükü men yefcürük.\n" +
            "Allâhümme iyyâke na'büdü ve leke nüsallî ve nescüdü ve ileyke nes'â ve nahfidü " +
            "nercû rahmeteke ve nahşâ azâbeke inne azâbeke bi'l-küffâri mülhık.",
        meaning = null,
        source = "${Sources.NAMAZ_ILMIHALI} s.215–216; ${Sources.ARABIC_REVIEW}",
        meaningRef = "${Sources.NAMAZ_ILMIHALI} s.216",
    )

    val tesrikTekbiri = Recitation(
        id = "tesrik_tekbiri",
        arabic = "اَللّٰهُ اَكْبَرُ اَللّٰهُ اَكْبَرُ لَا اِلٰهَ اِلَّا اللّٰهُ وَاللّٰهُ اَكْبَرُ " +
            "اَللّٰهُ اَكْبَرُ وَلِلّٰهِ الْحَمْدُ",
        transliteration = "Allâhü ekber, Allâhü ekber, lâ ilâhe illallâhü vallâhü ekber, " +
            "Allâhü ekber ve lillâhi'l-hamd.",
        meaning = "Allah en büyüktür, Allah en büyüktür. Allah'tan başka ilâh yoktur. Allah en " +
            "büyüktür, Allah en büyüktür ve hamd Allah'a mahsustur.",
        source = "${Sources.NAMAZ_ILMIHALI} s.151; Kırklareli Müftülüğü; ${Sources.ARABIC_REVIEW}",
    )

    /** Cuma salâsı ve Cuma, bayram sabahlarında sabah ezanından önce verilen salâ. */
    val sala = Recitation(
        id = "sala",
        arabic = "اَلصَّلَاةُ وَالسَّلَامُ عَلَيْكَ يَا رَسُولَ اللّٰهِ\n" +
            "اَلصَّلَاةُ وَالسَّلَامُ عَلَيْكَ يَا حَبِيبَ اللّٰهِ\n" +
            "اَلصَّلَاةُ وَالسَّلَامُ عَلَيْكَ يَا سَيِّدَ الْاَوَّلِينَ وَالْاٰخِرِينَ\n" +
            "وَسَلَامٌ عَلَى الْمُرْسَلِينَ وَالْحَمْدُ لِلّٰهِ رَبِّ الْعَالَمِينَ",
        transliteration = "Es-salâtü ve's-selâmü aleyke yâ Resûlallâh\n" +
            "Es-salâtü ve's-selâmü aleyke yâ Habîballâh\n" +
            "Es-salâtü ve's-selâmü aleyke yâ seyyide'l-evvelîne ve'l-âhirîn\n" +
            "Ve selâmün ale'l-mürselîn ve'l-hamdü lillâhi Rabbi'l-âlemîn",
        meaning = null,
        source = "${Sources.KURUL_SALA}; ${Sources.ARABIC_REVIEW}",
        meaningRef = Sources.KURUL_SALA,
    )

    /** Cuma'da hatip minbere çıkarken müezzinin okuduğu ayet. */
    val ahzab56 = Recitation(
        id = "ahzab_56",
        arabic = "اِنَّ اللّٰهَ وَمَلٰٓئِكَتَهُ یُصَلُّونَ عَلَى النَّبِیِّؕ یَٓا اَیُّهَا الَّذٖینَ " +
            "اٰمَنُوا صَلُّوا عَلَیْهِ وَسَلِّمُوا تَسْلٖیمًا",
        transliteration = "İnnallâhe ve melâiketehû yusallûne ale'n-nebiyy, yâ eyyühe'llezîne " +
            "âmenû sallû aleyhi ve sellimû teslîmâ.",
        meaning = null,
        source = "Ahzâb 33/56: ${Sources.KURAN}; okunacağı yer ${Sources.MEB_HITABET} " +
            "(EBA 4. ünite s.7)",
        meaningRef = "Kur'an Yolu meali: kuran.diyanet.gov.tr/tefsir/sure/33/ayet/56",
    )

    /** Teravihte her dört rekâttan sonraki oturuşta okunan tehlil ve salât-ı ümmiyye. */
    val teravihAra = Recitation(
        id = "teravih_ara",
        arabic = "لَا إِلٰهَ إِلَّا اللّٰهُ\n" +
            "اَللّٰهُمَّ صَلِّ عَلٰى سَيِّدِنَا مُحَمَّدٍ النَّبِيِّ الْاُمِّيِّ وَعَلٰى اٰلِهِ وَصَحْبِهِ " +
            "وَسَلِّمْ",
        transliteration = "Lâ ilâhe illallâh\n" +
            "Allâhümme salli alâ seyyidinâ Muhammedini'n-nebiyyi'l-ümmiyyi ve alâ âlihî ve " +
            "sahbihî ve sellim",
        meaning = null,
        source = "${Sources.KURUL_TERAVIH}; ${Sources.TDV} \"Salât-ı ümmiyye\"; " +
            "${Sources.ISLAM_ILMIHALI} s.212; ${Sources.ARABIC_REVIEW}",
        meaningRef = "${Sources.MEB_HITABET} s.91",
    )

    val all: List<Recitation> = listOf(
        ezan, sabahEzani, kamet, ezanDuasi, enteSselam, salavatCagrisi, tesbihDuasi,
        ayetelKursi, vehhab, kunut, tesrikTekbiri, sala, ahzab56, teravihAra,
    )
}
