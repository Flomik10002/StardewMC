import os
import re

BASE_PATH = "/home/flomik/IdeaProjects/StardewMC/tools/dirt"
SEASONS = ["spring", "summer", "fall", "winter"]
VARIANTS_COUNT = 14

pattern = re.compile(r"chunk_(\d+)_(\d+)\.png")

def sort_key(filename):
    m = pattern.search(filename)
    if not m:
        return (999, 999)
    return (int(m.group(1)), int(m.group(2)))

for season in SEASONS:
    season_path = os.path.join(BASE_PATH, season)

    files = sorted(
        (f for f in os.listdir(season_path) if f.endswith(".png")),
        key=sort_key
    )

    if len(files) != VARIANTS_COUNT:
        print(f"⚠ {season}: expected {VARIANTS_COUNT}, got {len(files)}")
        continue

    # tmp
    for i, f in enumerate(files):
        os.rename(
            os.path.join(season_path, f),
            os.path.join(season_path, f"__tmp_{i}.png")
        )

    # final
    for i in range(len(files)):
        os.rename(
            os.path.join(season_path, f"__tmp_{i}.png"),
            os.path.join(season_path, f"dirt_{i}.png")
        )

    print(f"✔ {season} renamed in correct numerical order")
