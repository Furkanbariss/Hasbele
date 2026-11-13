// Mağaza ekran görüntülerinin metinleri ve yerleşimi. Metni değiştirip render.sh ile yeniden üret.
// y aralıkları ham ekran görüntüsünün (1080x2424) pikselleridir; 0-100 durum çubuğu olduğu için kırpılır.
const RAW = "../../screenshots/raw/";
const FULL = [[100, 2424]];
// 04'teki yönlendirme satırı (y 557-637) "Diyanet takvimine göre..." diye başlıyor;
// görsellerde Diyanet adı geçmesin diye bu satır atlanır. Tam hâli: tr_02_tam / en_02_tam.
const MUEZZIN_NO_NAME = [[100, 557], [637, 2424]];

const SCREENS = {
  s01: { src: "01_tesbih.png", slices: FULL, bg: "#FFF8F5" },
  s02: { src: "02_tesbih_koyu.png", slices: FULL, bg: "#19120C" },
  s03: { src: "03_muezzin_vakitler.png", slices: FULL, bg: "#FFF8F5" },
  s04: { src: "04_muezzin_adim.png", slices: MUEZZIN_NO_NAME, bg: "#FFF8F5" },
  s04full: { src: "04_muezzin_adim.png", slices: FULL, bg: "#FFF8F5" },
  s05: { src: "05_zikirler.png", slices: FULL, bg: "#FFF8F5" },
  s06: { src: "06_gecmis.png", slices: FULL, bg: "#FFF8F5", cardCrop: [130, 800] },
  s08: { src: "08_karartma.png", slices: FULL, bg: "#000000" },
};

// layout: single (tek telefon), pair (önde solda + arkada sağda), float (telefon + üstünde başka ekrandan kesit kartı)
const SHOTS = {
  // ---------- Türkçe ----------
  tr_01: { layout: "single", screen: "s01",
    h1: "Dokun, <em>say</em>",
    sub: "Ekranın her yeri tesbih. Sayımın asla kaybolmaz.",
    chips: ["Titreşim", "Ses tuşları", "Sayarken reklam yok"] },
  tr_02: { layout: "single", screen: "s04",
    h1: "Müezzinlik <em>adım adım</em>",
    sub: "Her adımda ne yapılır, ne okunur;<br>Arapçası ve okunuşuyla." },
  tr_02_tam: { layout: "single", screen: "s04full",
    h1: "Müezzinlik <em>adım adım</em>",
    sub: "Her adımda ne yapılır, ne okunur;<br>Arapçası ve okunuşuyla." },
  tr_03: { layout: "single", screen: "s05",
    h1: "Namaz sonrası <em>tesbihat</em>",
    sub: "33 · 33 · 33 ve tehlil, kendiliğinden ilerler.<br>Kendi zikrini de hedefiyle ekle." },
  tr_04: { layout: "pair", dark: true, front: "s08", back: "s01",
    h1: "Karanlıkta da <em>say</em>",
    sub: "Ekran kararır, sayım sürer.<br>Ses tuşlarıyla da sayabilirsin." },
  tr_05: { layout: "single", dark: true, screen: "s02",
    h1: "Gece için <em>koyu tema</em>",
    sub: "Tesbih görünümünü sen seç.",
    chips: [{ bead: "amber", text: "Kehribar" }, { bead: "wood", text: "Koyu ahşap" }] },
  tr_06: { layout: "single", screen: "s03",
    h1: "Her vakit için <em>rehber</em>",
    sub: "Beş vakit, Cuma, teravih ve bayram namazı." },
  tr_07: { layout: "float", screen: "s01", card: "s06",
    h1: "Günlük <em>zikir geçmişin</em>",
    sub: "Saydıkların güne yazılır; son 30 günü gör.",
    chips: ["Hesap gerekmez", "Veriler telefonunda kalır"] },

  // ---------- English (ekran görüntüleri Türkçe arayüzlü) ----------
  en_01: { layout: "single", screen: "s01",
    h1: "Tap to <em>count</em>",
    sub: "The whole screen is your tasbih.<br>Your count is never lost.",
    chips: ["Vibration", "Volume keys", "No ads while counting"] },
  en_02: { layout: "single", screen: "s04",
    h1: "Muezzin, <em>step by step</em>",
    sub: "What to do and what to recite,<br>in Arabic with its reading." },
  en_02_tam: { layout: "single", screen: "s04full",
    h1: "Muezzin, <em>step by step</em>",
    sub: "What to do and what to recite,<br>in Arabic with its reading." },
  en_03: { layout: "single", screen: "s05",
    h1: "Tesbihat <em>after prayer</em>",
    sub: "33 · 33 · 33 and the tahlil, step by step.<br>Add your own dhikr with a target." },
  en_04: { layout: "pair", dark: true, front: "s08", back: "s01",
    h1: "Count in <em>the dark</em>",
    sub: "The screen goes black, the count goes on.<br>Volume keys count too." },
  en_05: { layout: "single", dark: true, screen: "s02",
    h1: "Dark theme <em>for the night</em>",
    sub: "Choose your bead style.",
    chips: [{ bead: "amber", text: "Amber" }, { bead: "wood", text: "Dark wood" }] },
  en_06: { layout: "single", screen: "s03",
    h1: "A guide for <em>every prayer</em>",
    sub: "Five daily prayers, Friday, Tarawih and Eid." },
  en_07: { layout: "float", screen: "s01", card: "s06",
    h1: "Your daily <em>dhikr history</em>",
    sub: "Every count is saved by day; see the last 30 days.",
    chips: ["No account needed", "Data stays on your phone"] },
};
