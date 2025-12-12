#!/usr/bin/env bash
set -e

MOD_ID="stardew"
JAVA_FILE="src/main/java/dev/flomik/stardew/common/registry/ModItems.java"
OUT_DIR="src/generated/resources/assets/$MOD_ID/models/item"

echo ">> Scanning $JAVA_FILE"
mkdir -p "$OUT_DIR"

ITEMS=$(tr '\n' ' ' < "$JAVA_FILE" \
  | grep -oP '\.create\s*\(\s*"\K[^"]+' \
  | sort -u)

for ITEM in $ITEMS; do
  # ---- ЖЁСТКИЙ ФИЛЬТР ----
  if [[ "$ITEM" == *hoe* || "$ITEM" == *watering_can* ]]; then
    echo "-- skip tool $ITEM"
    continue
  fi

  OUT="$OUT_DIR/$ITEM.json"

  # ---- НЕ ПЕРЕЗАПИСЫВАЕМ ----
  if [[ -f "$OUT" ]]; then
    echo "== skip exists $ITEM"
    continue
  fi

  # ---- PARENT ----
  PARENT="minecraft:item/generated"
  if [[ "$ITEM" == *pickaxe* || "$ITEM" == *axe* || "$ITEM" == *sword* ]]; then
    PARENT="minecraft:item/handheld"
  fi

  echo "++ gen  $ITEM ($PARENT)"

  cat > "$OUT" <<EOF
{
  "parent": "$PARENT",
  "textures": {
    "layer0": "$MOD_ID:item/$ITEM"
  }
}
EOF
done

echo ">> DONE"
