from PIL import Image

img = Image.open("farmland_dry_full_spring.png").convert("RGBA")
r,g,b = (255,230,200)
overlay = Image.new("RGBA", img.size, (r,g,b,0))

pixels = img.load()
for y in range(img.height):
    for x in range(img.width):
        pr,pg,pb,pa = pixels[x,y]
        pixels[x,y] = (
            int(pr * r/255),
            int(pg * g/255),
            int(pb * b/255),
            pa
        )
img.save("farmland_dry_full_summer.png")
