import json
import os

SHAPES = [
    "single",
    "horizontal_left",
    "horizontal_mid",
    "horizontal_right",
    "vertical_top",
    "vertical_mid",
    "vertical_bottom",
    "top_left",
    "top",
    "top_right",
    "left",
    "center",
    "right",
    "bottom_left",
    "bottom",
    "bottom_right"
]

output = {}

for hydrated in ["false", "true"]:
    for shape in SHAPES:
        model = f"stardew:block/farmland/{'wet' if hydrated == 'true' else 'dry'}_{shape}"
        key = f"hydrated={hydrated},shape={shape}"
        output[key] = { "model": model }

blockstate_json = { "variants": output }

os.makedirs("generated_models/blockstates", exist_ok=True)

with open("generated_models/blockstates/farmland.json", "w") as f:
    json.dump(blockstate_json, f, indent=2)

print("✅ farmland.json сгенерирован в blockstates/")
