import tkinter as tk
from tkinter import filedialog
from PIL import Image, ImageTk
import os

CHUNK_SIZE = 16
SELECTED = set()
IMAGE_PATH = None
IMG = None


class ChunkSelector(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title("Chunk Selector")
        self.geometry("1000x700")

        # --- TOP BUTTONS ---
        self.btn_frame = tk.Frame(self)
        self.btn_frame.pack(side=tk.TOP, fill=tk.X)

        tk.Button(self.btn_frame, text="Load image", command=self.load_image).pack(side=tk.LEFT)
        tk.Button(self.btn_frame, text="Save chunks", command=self.save_chunks).pack(side=tk.LEFT)

        # --- SCROLLABLE CANVAS SETUP ---
        self.scroll_frame = tk.Frame(self)
        self.scroll_frame.pack(fill=tk.BOTH, expand=True)

        self.canvas = tk.Canvas(self.scroll_frame, bg="white")
        self.canvas.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)

        self.h_scroll = tk.Scrollbar(self.scroll_frame, orient=tk.HORIZONTAL, command=self.canvas.xview)
        self.h_scroll.pack(side=tk.BOTTOM, fill=tk.X)
        self.v_scroll = tk.Scrollbar(self.scroll_frame, orient=tk.VERTICAL, command=self.canvas.yview)
        self.v_scroll.pack(side=tk.RIGHT, fill=tk.Y)

        self.canvas.config(xscrollcommand=self.h_scroll.set, yscrollcommand=self.v_scroll.set)

        # --- EVENTS ---
        self.canvas.bind("<Button-1>", self.on_left_click)    # выделить
        self.canvas.bind("<Button-3>", self.on_right_click)   # снять выделение

        self.tk_img = None
        self.canvas_img_id = None

    def load_image(self):
        global IMAGE_PATH, IMG, SELECTED
        SELECTED.clear()
        IMAGE_PATH = filedialog.askopenfilename(filetypes=[("PNG files", "*.png")])
        if not IMAGE_PATH:
            return

        IMG = Image.open(IMAGE_PATH)
        self.tk_img = ImageTk.PhotoImage(IMG)

        self.canvas.delete("all")
        self.canvas_img_id = self.canvas.create_image(0, 0, anchor='nw', image=self.tk_img)

        self.canvas.config(scrollregion=(0, 0, IMG.width, IMG.height))
        self.draw_grid()

    def draw_grid(self):
        self.canvas.delete("grid")
        if IMG is None:
            return
        for x in range(0, IMG.width, CHUNK_SIZE):
            self.canvas.create_line(x, 0, x, IMG.height, fill="gray", tags="grid")
        for y in range(0, IMG.height, CHUNK_SIZE):
            self.canvas.create_line(0, y, IMG.width, y, fill="gray", tags="grid")
        self.redraw_selection()

    def redraw_selection(self):
        self.canvas.delete("selection")
        for (x, y) in SELECTED:
            self.canvas.create_rectangle(
                x * CHUNK_SIZE, y * CHUNK_SIZE,
                (x + 1) * CHUNK_SIZE, (y + 1) * CHUNK_SIZE,
                fill="red", stipple="gray25", outline="black", tags="selection"
            )

    def canvas_to_chunk_coords(self, event):
        canvas_x = self.canvas.canvasx(event.x)
        canvas_y = self.canvas.canvasy(event.y)
        return int(canvas_x // CHUNK_SIZE), int(canvas_y // CHUNK_SIZE)

    def on_left_click(self, event):
        if IMG is None:
            return
        coord = self.canvas_to_chunk_coords(event)
        SELECTED.add(coord)
        self.redraw_selection()

    def on_right_click(self, event):
        if IMG is None:
            return
        coord = self.canvas_to_chunk_coords(event)
        if coord in SELECTED:
            SELECTED.remove(coord)
        self.redraw_selection()

    def save_chunks(self):
        if not SELECTED or IMG is None:
            print("❌ No chunks selected.")
            return

        out_dir = filedialog.askdirectory(title="Choose directory:")
        if not out_dir:
            return

        basename = os.path.splitext(os.path.basename(IMAGE_PATH))[0]
        count = 0

        for (x, y) in SELECTED:
            box = (
                x * CHUNK_SIZE,
                y * CHUNK_SIZE,
                (x + 1) * CHUNK_SIZE,
                (y + 1) * CHUNK_SIZE
            )

            if box[2] > IMG.width or box[3] > IMG.height:
                continue

            chunk = IMG.crop(box)
            filename = f"{basename}_chunk_{x}_{y}.png"
            chunk.save(os.path.join(out_dir, filename))
            count += 1

        print(f"✅ Saved: {count} chunks.")


if __name__ == "__main__":
    app = ChunkSelector()
    app.mainloop()
