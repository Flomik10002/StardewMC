import json

def rotate_box(x1, y1, z1, x2, y2, z2, times):
    for _ in range(times):
        ox1, oz1, ox2, oz2 = x1, z1, x2, z2
        x1, z1 = 16 - oz2, ox1
        x2, z2 = 16 - oz1, ox2

    x_min, x_max = sorted((x1, x2))
    y_min, y_max = sorted((y1, y2))
    z_min, z_max = sorted((z1, z2))
    return f"Block.box({x_min:.3f}, {y_min:.3f}, {z_min:.3f}, {x_max:.3f}, {y_max:.3f}, {z_max:.3f})"

def generate_voxelshape_variants(model_path: str):
    with open(model_path, "r", encoding="utf-8") as f:
        model = json.load(f)

    elements = model.get("elements", [])
    out = []

    for direction, turns in (("NORTH",0),("EAST",1),("SOUTH",2),("WEST",3)):
        boxes = []
        for el in elements:
            f = el["from"]; t = el["to"]
            boxes.append(rotate_box(f[0], f[1], f[2], t[0], t[1], t[2], turns))
        code = (
            f"private static final VoxelShape SHAPE_{direction} = Shapes.or(\n"
            f"    " + ",\n    ".join(boxes) + "\n);"
        )
        out.append(code)

    return "\n\n".join(out)

if __name__ == "__main__":
    path = input("Enter path to model (.json): ").strip()
    print(generate_voxelshape_variants(path))
