import os
import json

BASE_PATH = "/home/flomik/IdeaProjects/StardewMC/tools/dirt"
SEASONS = ["spring", "summer", "fall", "winter"]

for season in SEASONS:
    path = os.path.join(BASE_PATH, season)
    files = sorted(f for f in os.listdir(path) if f.endswith(".png"))

    with open(os.path.join(path, "order.json"), "w") as f:
        json.dump(files, f, indent=2)

print("✅ order dumped")
