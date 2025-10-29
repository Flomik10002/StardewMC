import json
import os

INPUT_FOLDER = "split_farmland"
OUTPUT_FOLDER = "generated_models/block/farmland"

os.makedirs(OUTPUT_FOLDER, exist_ok=True)

for filename in os.listdir(INPUT_FOLDER):
    if not filename.endswith(".png"):
        continue

    name = filename.replace(".png", "")
    texture_path = f"stardew:block/farmland/{name}"

    model = {
        "parent": "block/cube_all",
        "textures": {
            "all": texture_path
        }
    }

    with open(os.path.join(OUTPUT_FOLDER, f"{name}.json"), "w") as f:
        json.dump(model, f, indent=2)

print("✅ Модели cube_all сгенерированы.")
