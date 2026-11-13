# Google Play Data safety formu için notlar

Uygulamanın kendisi sunucuya veri göndermez; aşağıdakilerin tamamı Google Mobile Ads (AdMob) SDK'sından gelir ve yalnız kullanıcı açılış diyaloğunda reklamlara açık rıza verdiyse toplanır. Kaynak: [Google'ın veri bildirimi](https://developers.google.com/admob/android/privacy/play-data-disclosure). Form her zaman doğru olmak zorundadır; göndermeden önce bu sayfayı SDK sürümüne göre yeniden kontrol et. Aşağıdaki form kategorisi eşlemesi benim yorumumdur, Google'ın ifadesi değildir.

| Formdaki veri türü | SDK'nın topladığı | Toplanır | Paylaşılır | Amaç |
| --- | --- | --- | --- | --- |
| Yaklaşık konum | IP adresi (yaklaşık konumu tahmin etmek için kullanılabilir) | Evet | Evet | Reklam, analiz, dolandırıcılığı önleme |
| Uygulama etkinliği: uygulama etkileşimleri | Uygulamanın açılması, dokunuşlar, video izlemeleri | Evet | Evet | Reklam, analiz, dolandırıcılığı önleme |
| Uygulama bilgileri ve performans: tanılama | Açılış süresi, takılma oranı, enerji kullanımı | Evet | Evet | Reklam, analiz, dolandırıcılığı önleme |
| Cihaz veya diğer kimlikler | Android reklam kimliği, uygulama grubu kimliği | Evet | Evet | Reklam, analiz, dolandırıcılığı önleme |

**Diğer sorular**

- Aktarımda şifreleme: Evet (TLS).
- Hesap oluşturma: Yok.
- Sayımlar, zikirler ve ayarlar cihazda kalır, forma girmez.
- Uygulamanın kendi sunucusu yok, kullanıcı verisi silme isteği için hesap da yok.
- Hedef kitle: çocuklara yönelik değil.
- Uygulama reklam içerir: Evet.
- Reklamlar yalnız ilk açılıştaki KVKK diyaloğunda "Kabul ediyorum" denirse açılır; "Reddediyorum" denirse reklam SDK'sı hiç başlatılmaz ve reklam istenmez, uygulamanın tüm özellikleri çalışır. Seçim Ayarlar > "Reklam tercihi"nden her an değiştirilebilir. Bu yüzden formdaki "kullanıcılar bu verinin toplanıp toplanmayacağını seçebilir" seçeneği uygun görünür. Bu eşleme benim yorumumdur; işaretlemeden önce [Data safety yardım sayfasından](https://support.google.com/googleplay/android-developer/answer/10787469) kontrol et.
- Reklam kartları yalnız Zikirler, Müezzinlik ana sayfası, Geçmiş ve Ayarlar ekranlarında görünür; reklamlar her zaman kişiselleştirilmemiştir (`npa=1`).
