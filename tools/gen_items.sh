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
#
# Заодно проверяет мультиблоки (common/registry/framework/multiblock/): их
# blockstate и item-модель тоже датаген-генерируемые (StardewBlockStates),
# существуют только после ./gradlew runData — если runData не запущен, эти
# файлы отсутствуют и в src/main, и в src/generated, что легко принять за
# "модель пропала".
set -e
cd "$(dirname "$0")/.."

MOD_ID="stardew"
ITEMS_JAVA="src/main/java/dev/flomik/stardew/common/registry/ModItems.java"
BLOCKS_JAVA="src/main/java/dev/flomik/stardew/common/registry/ModBlocks.java"
MAIN_MODELS="src/main/resources/assets/$MOD_ID/models/item"
GEN_MODELS="src/generated/resources/assets/$MOD_ID/models/item"
TEXTURES="src/main/resources/assets/$MOD_ID/textures/item"

MAIN_BLOCK_MODELS="src/main/resources/assets/$MOD_ID/models/block"
GEN_BLOCK_MODELS="src/generated/resources/assets/$MOD_ID/models/block"
MAIN_BLOCKSTATES="src/main/resources/assets/$MOD_ID/blockstates"
GEN_BLOCKSTATES="src/generated/resources/assets/$MOD_ID/blockstates"

WRITE=0
[[ "${1:-}" == "--write" ]] && WRITE=1

# .create("x", ...) и .createFood("x", ...) — старый regex ловил только .create(
extract_names() {
  tr '\n' ' ' < "$1" | grep -oP '\.create(?:Food)?\s*\(\s*"\K[^"]+' | sort -u
}

has_model()   { [[ -f "$MAIN_MODELS/$1.json" || -f "$GEN_MODELS/$1.json" ]]; }
has_texture() { [[ -f "$TEXTURES/$1.png" ]]; }

has_block_model()  { [[ -f "$MAIN_BLOCK_MODELS/$1.json" || -f "$GEN_BLOCK_MODELS/$1.json" ]]; }
has_blockstate()   { [[ -f "$MAIN_BLOCKSTATES/$1.json" || -f "$GEN_BLOCKSTATES/$1.json" ]]; }

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

# Мультиблоки (см. common/registry/framework/multiblock/) — считаем их имена
# заранее, чтобы пропустить в общей проверке "Block items" ниже: для них
# отсутствие item-модели/blockstate до runData — норма, а не ошибка "сделай
# вручную", и разбирается отдельно, подробнее, в своей секции.
MULTIBLOCK_CLASSES=$(grep -rlP '\bextends\s+MultiblockBlock\b' src/main/java --include="*.java" 2>/dev/null | xargs -n1 basename -s .java)
MULTIBLOCK_NAMES=""
for CLASS in $MULTIBLOCK_CLASSES; do
  NAME=$(tr '\n' ' ' < "$BLOCKS_JAVA" | grep -oP "\.create\(\s*\"\K[^\"]+(?=\"\s*,\s*$CLASS::new)")
  [[ -n "$NAME" ]] && MULTIBLOCK_NAMES="$MULTIBLOCK_NAMES $NAME"
done
is_multiblock_name() {
  [[ " $MULTIBLOCK_NAMES " == *" $1 "* ]]
}

# Для блоков модель предмета обычно ссылается на модель блока — угадать parent
# нельзя, поэтому только сообщаем о пропусках.
echo ">> Block items ($BLOCKS_JAVA)"
for ITEM in $(extract_names "$BLOCKS_JAVA"); do
  has_model "$ITEM" && continue
  is_no_item "$BLOCKS_JAVA" "$ITEM" && continue
  is_multiblock_name "$ITEM" && continue
  MISSING=1
  echo "!! $ITEM: нет item-модели (сделай вручную или через .visual())"
done

# Мультиблоки: blockstate и item-модель для них генерирует датаген
# (StardewBlockStates/ModelPresets.useBlockModel()) и существуют только
# после ./gradlew runData — модель самой клетки-истока
# (models/block/<name>.json) по-прежнему рисуется руками.
echo ">> Multiblocks (StardewBlockStates datagen)"
for CLASS in $MULTIBLOCK_CLASSES; do
  NAME=$(tr '\n' ' ' < "$BLOCKS_JAVA" | grep -oP "\.create\(\s*\"\K[^\"]+(?=\"\s*,\s*$CLASS::new)")
  if [[ -z "$NAME" ]]; then
    echo "?? $CLASS: наследует MultiblockBlock, но не нашёл регистрацию в $BLOCKS_JAVA"
    continue
  fi

  if ! has_block_model "$NAME"; then
    MISSING=1
    echo "!! $NAME ($CLASS): нет модели истока $MAIN_BLOCK_MODELS/$NAME.json — её нужно нарисовать руками"
    continue
  fi
  if ! has_blockstate "$NAME"; then
    MISSING=1
    echo "-- $NAME ($CLASS): blockstate ещё не сгенерирован — запусти ./gradlew runData"
  fi
  if ! has_model "$NAME"; then
    MISSING=1
    echo "-- $NAME ($CLASS): item-модель ещё не сгенерирована — запусти ./gradlew runData"
  fi
done
if ! has_block_model "empty"; then
  MISSING=1
  echo "!! отсутствует общая пустая модель $MAIN_BLOCK_MODELS/empty.json (нужна для невидимых клеток любого мультиблока)"
fi

if [[ $MISSING == 0 ]]; then
  echo ">> OK: у всех зарегистрированных предметов есть модели"
fi
echo ">> DONE"
