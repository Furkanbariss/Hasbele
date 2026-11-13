# Gizlilik politikasını Google Sites'ta ücretsiz yayınlama

Alan adı almadan, `virata.software@gmail.com` hesabıyla, ücretsiz. Sonuç adres `https://sites.google.com/view/...` biçiminde olur.

## Adımlar

1. Tarayıcıda yalnız `virata.software@gmail.com` hesabıyla giriş yap (başka hesapla giriş yapılı kalmasın; kişisel adın site sahibi olarak görünmesin).
2. https://sites.google.com adresine git, "Boş site" ile yeni site oluştur.
3. Site adını kişisel ad içermeyecek biçimde ver: örneğin "Virata" ya da "Hasbele". Sol üstteki site adı alanına yaz. Sayfa başlığına ve alt bilgiye kendi adını yazma.
4. İlk sayfayı "Gizlilik politikası" olarak adlandır. `docs/gizlilik-politikasi.md` dosyasındaki Türkçe bölümü yapıştır. İngilizce bölümü aynı sayfanın altına ekleyebilir ya da ikinci bir sayfa ("Privacy policy") yapabilirsin. Başlıkları "Başlık" biçimiyle, madde işaretlerini liste olarak ayarla. Bakımcı notunu (en üstteki alıntı) yapıştırma.
5. "Destek / İletişim" adında yeni sayfa ekle. İçine yalnız şunu yaz: "Sorularınız ve KVKK başvurularınız için: virata.software@gmail.com". Form ekleme: ad ve e-posta toplayan bir form, ek veri işleme ve ek KVKK yükümlülüğü (yeni aydınlatma, saklama, silme) doğurur. Düz e-posta adresi yeterlidir.
6. Sağ üstten "Yayınla"ya bas, "web adresi" kutusuna kısa bir ad gir (örn. `virata-software`); adres `https://sites.google.com/view/virata-software` olur.
7. Yayın ayarlarında sitenin herkese açık olduğunu denetle. Yayın penceresindeki "Yayınlanan site" / "Görüntüleyenler" ayarında kısıtlama (yalnız belirli kişiler ya da kuruluş) olmamalı; herkes görebilmeli. Google'ın yardım sayfası: https://support.google.com/sites/answer/6372880 (arayüz adları değişmiş olabilir, yazıdan çok seçeneğin anlamına bak). İstersen "Arama motorlarının siteyi göstermemesini iste" kutusunu işaretleyebilirsin; bu yalnız bir istektir, garanti vermez.
8. Gizli (incognito) pencerede ya da oturum kapalı başka bir tarayıcıda adresi aç. Sayfaların görünmesini ve hiçbir yerde kişisel adının geçmediğini denetle (sayfa metni, sekme başlığı, alt bilgi, sahip bilgisi).
9. Play Console'da: Uygulama içeriği > Gizlilik politikası alanına gizlilik sayfasının adresini gir. Mağaza ayarları > Mağaza sayfası iletişim bilgilerinde "Web sitesi" olarak site adresini, "E-posta" olarak `virata.software@gmail.com` adresini gir. Geliştirici hesabının iletişim e-postası da `virata.software@gmail.com` olmalı.
10. Politika metni değişirse dört yeri birlikte güncelle: `docs/gizlilik-politikasi.md`, `privacy.html`, uygulamadaki `privacy_policy_text` ve Google Sites sayfası.

## app-ads.txt: mevcut GitHub Pages sitesi

AdMob, reklam satıcılarını doğrulamak için geliştirici web sitesinin kökünde (`alanadi/app-ads.txt`) bir `app-ads.txt` dosyası arar. Google Sites'ın köküne dosya konamadığı için bu iş için kullanılamaz.

Karar (13 Kasım 2026): Zaten yayında olan `https://furkanbariss.github.io/app-ads.txt` kullanılır. Dosya AdMob yayıncı kimliğine bağlıdır, uygulamaya değil; aynı AdMob hesabındaki Hasbele'yi de kapsar, yeni satır eklemek gerekmez.

- Play Console'da Hasbele'nin "Web sitesi" alanına `https://furkanbariss.github.io` yaz. Play'deki web sitesi ile app-ads.txt'nin bulunduğu alan adı aynı olmalıdır.
- Gizlilik politikası da aynı siteye konabilir: `docs/site/hasbele/privacy.html` dosyasını depoda `hasbele/privacy.html` olarak yayınla; adres `https://furkanbariss.github.io/hasbele/privacy.html` olur. Böyle yapılırsa Google Sites gerekmez; yukarıdaki Google Sites adımları yalnız istersen kullanılır.
- Bilinçli bir taviz: adres `furkanbariss` kullanıcı adını gösterir. Play kişisel hesaplarda yasal adı zaten gösterdiği için ek bir ifşa sayılmaz, ama bilinmeli.


## Kaynaklar

- Google Sites yayınlama ve paylaşma: https://support.google.com/sites/answer/6372880
- AdMob app-ads.txt: https://support.google.com/admob/answer/9776740
- AdMob app-ads geliştirici belgesi: https://developers.google.com/admob/android/app-ads
