import os
import shutil

DIR = "textures"  # ← укажи путь к папке с PNG

# проходим по всем .png файлам
files = [f for f in os.listdir(DIR) if f.endswith(".png")]

tmp_dir = os.path.join(DIR, "_tmp_inner_swap")
os.makedirs(tmp_dir, exist_ok=True)

for filename in files:
    name, ext = os.path.splitext(filename)
    if name.startswith("inner_"):
        # inner_top_left -> top_left
        new_name = name.replace("inner_", "", 1)
    else:
        # top_left -> inner_top_left
        new_name = f"inner_{name}"
    shutil.copy2(os.path.join(DIR, filename), os.path.join(tmp_dir, f"{new_name}{ext}"))

# очистим оригинальную папку и перенесём обратно
for filename in files:
    os.remove(os.path.join(DIR, filename))
for filename in os.listdir(tmp_dir):
    shutil.move(os.path.join(tmp_dir, filename), os.path.join(DIR, filename))

os.rmdir(tmp_dir)
print("✅ Inner и обычные версии успешно свопнуты.")
