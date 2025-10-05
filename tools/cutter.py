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

        # --- Drag selection support ---
        self.drag_start = None
        self.drag_rect = None

        self.canvas.bind("<ButtonPress-1>", self.start_drag_left)
        self.canvas.bind("<B1-Motion>", self.update_drag)
        self.canvas.bind("<ButtonRelease-1>", self.end_drag_left)

        self.canvas.bind("<ButtonPress-3>", self.start_drag_right)
        self.canvas.bind("<B3-Motion>", self.update_drag)
        self.canvas.bind("<ButtonRelease-3>", self.end_drag_right)


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

    def start_drag_left(self, event):
        self.drag_start = (self.canvas.canvasx(event.x), self.canvas.canvasy(event.y))
        self.drag_rect = self.canvas.create_rectangle(
            self.drag_start[0], self.drag_start[1],
            self.drag_start[0], self.drag_start[1],
            outline="blue", dash=(2, 2), tags="drag"
        )

    def start_drag_right(self, event):
        self.drag_start = (self.canvas.canvasx(event.x), self.canvas.canvasy(event.y))
        self.drag_rect = self.canvas.create_rectangle(
            self.drag_start[0], self.drag_start[1],
            self.drag_start[0], self.drag_start[1],
            outline="red", dash=(2, 2), tags="drag"
        )

    def update_drag(self, event):
        if not self.drag_start or self.drag_rect is None:
            return
        x0, y0 = self.drag_start
        x1, y1 = self.canvas.canvasx(event.x), self.canvas.canvasy(event.y)
        self.canvas.coords(self.drag_rect, x0, y0, x1, y1)

    def end_drag_left(self, event):
        self.apply_drag_selection(add=True)

    def end_drag_right(self, event):
        self.apply_drag_selection(add=False)

    def apply_drag_selection(self, add=True):
        if not self.drag_start or self.drag_rect is None:
            return

        x0, y0, x1, y1 = self.canvas.coords(self.drag_rect)
        left = int(min(x0, x1) // CHUNK_SIZE)
        right = int(max(x0, x1) // CHUNK_SIZE)
        top = int(min(y0, y1) // CHUNK_SIZE)
        bottom = int(max(y0, y1) // CHUNK_SIZE)

        for x in range(left, right + 1):
            for y in range(top, bottom + 1):
                coord = (x, y)
                if add:
                    SELECTED.add(coord)
                else:
                    SELECTED.discard(coord)

        self.canvas.delete("drag")
        self.drag_start = None
        self.drag_rect = None
        self.redraw_selection()

    def save_chunks(self):
        if not SELECTED or IMG is None:
            print("❌ No chunks selected.")
            return

        out_path = filedialog.asksaveasfilename(
            defaultextension=".png",
            filetypes=[("PNG files", "*.png")],
            title="Save combined chunks as"
        )
        if not out_path:
            return

        xs = [x for (x, _) in SELECTED]
        ys = [y for (_, y) in SELECTED]
        min_x, max_x = min(xs), max(xs)
        min_y, max_y = min(ys), max(ys)
        width = (max_x - min_x + 1) * CHUNK_SIZE
        height = (max_y - min_y + 1) * CHUNK_SIZE

        combined_img = Image.new("RGBA", (width, height), (0, 0, 0, 0))

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
            pos_x = (x - min_x) * CHUNK_SIZE
            pos_y = (y - min_y) * CHUNK_SIZE
            combined_img.paste(chunk, (pos_x, pos_y))

        MAX_SIZE = 8192
        if combined_img.width > MAX_SIZE or combined_img.height > MAX_SIZE:
            combined_img.thumbnail((MAX_SIZE, MAX_SIZE), Image.NEAREST)

        combined_img.save(out_path)
        print(f"✅ Combined image saved as: {out_path}")

if __name__ == "__main__":
    app = ChunkSelector()
    app.mainloop()
