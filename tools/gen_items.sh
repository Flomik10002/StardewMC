#!/usr/bin/env bash
# Аудит моделей предметов (бывший генератор моделей).
#
# История: скрипт писал плоские item-модели в src/generated/resources до того,
# как появился Java-датаген (.visual(ModelPresets...) + ./gradlew runData).
# Теперь src/generated принадлежит runData: он перегенерирует свои файлы и
# УДАЛЯЕТ оттуда всё, что сам не создавал, — поэтому писать туда бессмысленно.
#
# Новый режим работы:
#   ./tools/gen_items.sh          — отчёт: у каких зарегистрированных предметов
#                                   нет модели и/или текстуры
#   ./tools/gen_items.sh --write  — дополнительно генерирует недостающие плоские
#                                   модели в src/main/resources (только если
#                                   текстура item/<name>.png существует)
set -e
cd "$(dirname "$0")/.."

MOD_ID="stardew"
ITEMS_JAVA="src/main/java/dev/flomik/stardew/common/registry/ModItems.java"
BLOCKS_JAVA="src/main/java/dev/flomik/stardew/common/registry/ModBlocks.java"
MAIN_MODELS="src/main/resources/assets/$MOD_ID/models/item"
GEN_MODELS="src/generated/resources/assets/$MOD_ID/models/item"
TEXTURES="src/main/resources/assets/$MOD_ID/textures/item"

WRITE=0
[[ "${1:-}" == "--write" ]] && WRITE=1

# .create("x", ...) и .createFood("x", ...) — старый regex ловил только .create(
extract_names() {
  tr '\n' ' ' < "$1" | grep -oP '\.create(?:Food)?\s*\(\s*"\K[^"]+' | sort -u
}

has_model()   { [[ -f "$MAIN_MODELS/$1.json" || -f "$GEN_MODELS/$1.json" ]]; }
has_texture() { [[ -f "$TEXTURES/$1.png" ]]; }

# true, если цепочка регистрации содержит .noItem() — у такого блока предмета нет
is_no_item() {
  tr '\n' ' ' < "$1" \
    | grep -oP "\.create(?:Food)?\s*\(\s*\"$2\".*?\.register\s*\(\s*\)" \
    | grep -q '\.noItem\s*('
}

MISSING=0

echo ">> Items ($ITEMS_JAVA)"
for ITEM in $(extract_names "$ITEMS_JAVA"); do
  if has_model "$ITEM"; then
    continue
  fi
  MISSING=1
  if ! has_texture "$ITEM"; then
    echo "!! $ITEM: нет ни модели, ни текстуры $TEXTURES/$ITEM.png"
    continue
  fi
  if [[ $WRITE == 1 ]]; then
    PARENT="minecraft:item/generated"
    case "$ITEM" in
      *pickaxe*|*axe*|*sword*|*scythe*|*hoe*) PARENT="minecraft:item/handheld" ;;
    esac
    mkdir -p "$MAIN_MODELS"
    printf '{\n  "parent": "%s",\n  "textures": {\n    "layer0": "%s:item/%s"\n  }\n}\n' \
      "$PARENT" "$MOD_ID" "$ITEM" > "$MAIN_MODELS/$ITEM.json"
    echo "++ $ITEM: сгенерирована модель ($PARENT)"
  else
    echo "-- $ITEM: нет модели (текстура есть; запусти с --write)"
  fi
done

# Для блоков модель предмета обычно ссылается на модель блока — угадать parent
# нельзя, поэтому только сообщаем о пропусках.
echo ">> Block items ($BLOCKS_JAVA)"
for ITEM in $(extract_names "$BLOCKS_JAVA"); do
  has_model "$ITEM" && continue
  is_no_item "$BLOCKS_JAVA" "$ITEM" && continue
  MISSING=1
  echo "!! $ITEM: нет item-модели (сделай вручную или через .visual())"
done

if [[ $MISSING == 0 ]]; then
  echo ">> OK: у всех зарегистрированных предметов есть модели"
fi
echo ">> DONE"
