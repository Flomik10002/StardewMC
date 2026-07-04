import json
import csv

INPUT_JSON = "Objects_Strings.json"   # твой файл
OUTPUT_CSV = "Objects_Strings.csv"

with open(INPUT_JSON, "r", encoding="utf-8") as f:
    root = json.load(f)

strings = root["content"]

with open(OUTPUT_CSV, "w", newline="", encoding="utf-8") as f:
    writer = csv.writer(f)
    writer.writerow(["Key", "Text"])

    for key, text in strings.items():
        writer.writerow([key, text])

print("Strings CSV создан:", OUTPUT_CSV)