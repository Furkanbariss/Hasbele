#!/bin/sh
# Hasbele mağaza görsellerini HTML kaynaklarından PNG'ye çevirir (Git Bash, Windows).
# Kullanım:  sh docs/store/graphics/src/render.sh            # hepsi
#            sh docs/store/graphics/src/render.sh tr_01 tr_03 # yalnız bunlar (feature / feature_en de olur)
# Metinler shots.js'te, stil common.css'te, öne çıkan görsel feature.html'de.
set -e
CHROME="${CHROME:-/c/Program Files/Google/Chrome/Application/chrome.exe}"
SRC_DIR="$(cd "$(dirname "$0")" && pwd)"
OUT_DIR="$(dirname "$SRC_DIR")"
SRC_URL="file:///$(cygpath -m "$SRC_DIR")"
OUT_WIN="$(cygpath -w "$OUT_DIR")"

ALL="tr_01 tr_02 tr_02_tam tr_03 tr_04 tr_05 tr_06 tr_07 en_01 en_02 en_02_tam en_03 en_04 en_05 en_06 en_07 feature feature_en"
IDS="${*:-$ALL}"

shoot() { # $1 = url, $2 = çıktı adı, $3 = genişlik, $4 = yükseklik
  "$CHROME" --headless=new --disable-gpu --hide-scrollbars --force-device-scale-factor=1 \
    --allow-file-access-from-files --virtual-time-budget=3000 \
    --window-size="$3,$4" --screenshot="$OUT_WIN\\$2.png" "$1" >/dev/null 2>&1
  echo "$2.png"
}

for id in $IDS; do
  case "$id" in
    feature)    shoot "$SRC_URL/feature.html?lang=tr" feature_graphic 1024 500 ;;
    feature_en) shoot "$SRC_URL/feature.html?lang=en" feature_graphic_en 1024 500 ;;
    *)          shoot "$SRC_URL/shot.html?id=$id" "$id" 1080 1920 ;;
  esac
done
