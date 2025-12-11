#!/bin/bash

INPUT="$1"
OUTPUT="${INPUT%.*}.ogg"

ffmpeg -y -i "$INPUT" -c:a libvorbis -qscale:a 5 "$OUTPUT"
echo "Готово: $OUTPUT"
