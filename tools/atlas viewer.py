import tkinter as tk
from tkinter import filedialog, simpledialog, messagebox
from PIL import Image, ImageTk
import os

CHUNK_SIZE = 16
PREVIEW_SIZE = 250  # Размер квадратного окна предпросмотра


class ExportManager(tk.Toplevel):
    """Окно для поштучного сохранения файлов."""

    def __init__(self, parent, export_queue, target_dir):
        super().__init__(parent)
        self.title("Export Manager")
        self.geometry("800x600")
        self.transient(parent)
        self.grab_set()

        self.queue = export_queue
        self.target_dir = target_dir
        self.current_index = 0

        # --- UI LAYOUT ---
        self.top_frame = tk.Frame(self)
        self.top_frame.pack(fill=tk.BOTH, expand=True, padx=10, pady=10)

        # Левая колонка: Квадратное превью
        self.left_col = tk.Frame(self.top_frame, width=320)
        self.left_col.pack(side=tk.LEFT, fill=tk.BOTH, expand=False)

        tk.Label(self.left_col, text="Current Sprite:", font=("Arial", 10, "bold")).pack()

        # Контейнер для превью фиксированного размера
        self.preview_container = tk.Frame(self.left_col, width=PREVIEW_SIZE, height=PREVIEW_SIZE, bg="#333")
        self.preview_container.pack_propagate(False)  # Запрещаем менять размер под контент
        self.preview_container.pack(pady=5)

        self.preview_lbl = tk.Label(self.preview_container, bg="#333")
        self.preview_lbl.pack(expand=True)  # Центрируем картинку

        self.info_lbl = tk.Label(self.left_col, text="Size: 0x0")
        self.info_lbl.pack()

        # Правая колонка: Файлы
        self.right_col = tk.Frame(self.top_frame)
        self.right_col.pack(side=tk.RIGHT, fill=tk.BOTH, expand=True, padx=(10, 0))

        tk.Label(self.right_col, text=f"Folder content:\n{os.path.basename(target_dir)}/", font=("Arial", 9)).pack()

        self.file_list = tk.Listbox(self.right_col, font=("Consolas", 10), bg="#eee")
        self.scroll = tk.Scrollbar(self.right_col, command=self.file_list.yview)
        self.file_list.config(yscrollcommand=self.scroll.set)

        self.scroll.pack(side=tk.RIGHT, fill=tk.Y)
        self.file_list.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)
        self.file_list.bind("<<ListboxSelect>>", self.on_file_click)

        # Нижняя часть: Управление
        self.bottom_frame = tk.Frame(self, pady=15, bg="#ddd")
        self.bottom_frame.pack(fill=tk.X, side=tk.BOTTOM)

        self.lbl_progress = tk.Label(self.bottom_frame, text="Item 1 of X", bg="#ddd", font=("Arial", 9, "bold"))
        self.lbl_progress.pack(pady=(0, 5))

        # Поля ввода
        entry_frame = tk.Frame(self.bottom_frame, bg="#ddd")
        entry_frame.pack()

        tk.Label(entry_frame, text="Filename:", bg="#ddd").pack(side=tk.LEFT, padx=5)
        self.name_entry = tk.Entry(entry_frame, font=("Arial", 12), width=30)
        self.name_entry.pack(side=tk.LEFT, padx=5)
        tk.Label(entry_frame, text=".png", bg="#ddd").pack(side=tk.LEFT)

        # Кнопки
        btn_frame = tk.Frame(self.bottom_frame, bg="#ddd", pady=10)
        btn_frame.pack()

        tk.Button(btn_frame, text="SKIP", command=self.skip_item, width=10).pack(side=tk.LEFT, padx=20)
        self.btn_save = tk.Button(btn_frame, text="SAVE & NEXT", command=self.save_current, bg="#cfc",
                                  font=("Arial", 10, "bold"), width=15)
        self.btn_save.pack(side=tk.LEFT, padx=20)

        self.bind("<Return>", lambda e: self.save_current())

        # Старт
        self.refresh_file_list()
        self.load_item(0)

    def refresh_file_list(self):
        self.file_list.delete(0, tk.END)
        try:
            files = sorted([f for f in os.listdir(self.target_dir) if f.lower().endswith(".png")])
            for f in files:
                self.file_list.insert(tk.END, f)
        except Exception as e:
            print(f"Error reading dir: {e}")

    def on_file_click(self, event):
        sel = self.file_list.curselection()
        if not sel: return
        filename = self.file_list.get(sel[0])
        name_no_ext = os.path.splitext(filename)[0]
        self.name_entry.delete(0, tk.END)
        self.name_entry.insert(0, name_no_ext)

    def load_item(self, index):
        # Проверка на конец списка
        if index >= len(self.queue):
            messagebox.showinfo("Done", "Export finished!")
            self.destroy()
            return

        self.current_index = index
        item = self.queue[index]

        # --- ЛОГИКА МАСШТАБИРОВАНИЯ ПОД КВАДРАТ ---
        img = item['img'].copy()
        w, h = img.size

        # Вычисляем коэффициент, чтобы вписать в PREVIEW_SIZE (250)
        scale = min(PREVIEW_SIZE / w, PREVIEW_SIZE / h)
        new_w = int(w * scale)
        new_h = int(h * scale)

        # Если картинка слишком мелкая, увеличиваем её минимум до заполнения (но сохраняя NEAREST)
        if new_w < 1: new_w = 1
        if new_h < 1: new_h = 1

        img = img.resize((new_w, new_h), Image.Resampling.NEAREST)

        self.tk_preview = ImageTk.PhotoImage(img)
        self.preview_lbl.config(image=self.tk_preview)
        self.info_lbl.config(text=f"Original Size: {w}x{h}")

        # Обновляем имя
        self.name_entry.delete(0, tk.END)
        self.name_entry.insert(0, item['name'])
        self.name_entry.focus_set()
        self.name_entry.select_range(0, tk.END)

        self.lbl_progress.config(text=f"Item {index + 1} of {len(self.queue)}")

    def save_current(self):
        # Защита от list index out of range
        if self.current_index >= len(self.queue):
            self.destroy()
            return

        name = self.name_entry.get().strip()
        if not name: return

        filename = name + ".png"
        full_path = os.path.join(self.target_dir, filename)

        try:
            item = self.queue[self.current_index]
            item['img'].save(full_path)
            print(f"Saved: {full_path}")

            self.refresh_file_list()
            self.load_item(self.current_index + 1)  # Переход к следующему

        except Exception as e:
            messagebox.showerror("Error", f"Failed to save:\n{e}")

    def skip_item(self):
        self.load_item(self.current_index + 1)


class ChunkSelector(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title("Atlas Slicer Final")
        self.geometry("1200x800")

        # --- DATA ---
        self.image_path = None
        self.original_img = None
        self.tk_img = None
        self.scale = 1.0

        self.selected_chunks = set()
        self.export_queue = []

        self.drag_start = None
        self.drag_rect_id = None
        self.is_erasing = False

        # --- GUI ---
        self.paned = tk.PanedWindow(self, orient=tk.HORIZONTAL)
        self.paned.pack(fill=tk.BOTH, expand=True)

        # LEFT
        self.left_frame = tk.Frame(self.paned)
        self.paned.add(self.left_frame, minsize=800, stretch="always")

        self.btn_frame = tk.Frame(self.left_frame, bg="#ddd", pady=5)
        self.btn_frame.pack(side=tk.TOP, fill=tk.X)
        tk.Button(self.btn_frame, text="📂 Load Image", command=self.load_image).pack(side=tk.LEFT, padx=5)
        tk.Button(self.btn_frame, text="➕ Add to Queue (Enter)", command=self.add_to_queue, bg="#cfc").pack(
            side=tk.LEFT, padx=5)
        tk.Label(self.btn_frame, text=" | LMB: Select | RMB: Erase | Wheel: Zoom", bg="#ddd").pack(side=tk.LEFT,
                                                                                                   padx=10)

        self.canvas_frame = tk.Frame(self.left_frame)
        self.canvas_frame.pack(fill=tk.BOTH, expand=True)
        self.canvas = tk.Canvas(self.canvas_frame, bg="#404040")
        self.h_scroll = tk.Scrollbar(self.canvas_frame, orient=tk.HORIZONTAL, command=self.canvas.xview)
        self.v_scroll = tk.Scrollbar(self.canvas_frame, orient=tk.VERTICAL, command=self.canvas.yview)
        self.canvas.config(xscrollcommand=self.h_scroll.set, yscrollcommand=self.v_scroll.set)
        self.v_scroll.pack(side=tk.RIGHT, fill=tk.Y)
        self.h_scroll.pack(side=tk.BOTTOM, fill=tk.X)
        self.canvas.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)

        # RIGHT
        self.right_frame = tk.Frame(self.paned, width=320, bg="#eee")
        self.paned.add(self.right_frame, minsize=280, stretch="never")

        tk.Label(self.right_frame, text="Export Queue", font=("Arial", 11, "bold"), bg="#eee").pack(pady=5)

        # --- КВАДРАТНОЕ ПРЕВЬЮ В САЙДБАРЕ ---
        self.preview_container = tk.Frame(self.right_frame, width=PREVIEW_SIZE, height=PREVIEW_SIZE, bg="gray")
        self.preview_container.pack_propagate(False)
        self.preview_container.pack(pady=5, padx=5)

        self.preview_lbl = tk.Label(self.preview_container, bg="gray", text="[No Selection]")
        self.preview_lbl.pack(expand=True)

        self.queue_listbox = tk.Listbox(self.right_frame, font=("Consolas", 10))
        self.queue_listbox.pack(fill=tk.BOTH, expand=True, padx=5)

        btn_right = tk.Frame(self.right_frame, bg="#eee")
        btn_right.pack(fill=tk.X, pady=5)
        tk.Button(btn_right, text="❌ Remove", command=self.remove_item).pack(side=tk.LEFT, padx=5)
        tk.Button(btn_right, text="📂 EXPORT...", command=self.open_export_manager, bg="#fcc").pack(side=tk.RIGHT,
                                                                                                   padx=5)

        # BINDS
        self.canvas.bind("<ButtonPress-1>", lambda e: self.on_mouse_down(e, erase=False))
        self.canvas.bind("<B1-Motion>", self.on_mouse_drag)
        self.canvas.bind("<ButtonRelease-1>", self.on_mouse_up)
        self.canvas.bind("<ButtonPress-3>", lambda e: self.on_mouse_down(e, erase=True))
        self.canvas.bind("<B3-Motion>", self.on_mouse_drag)
        self.canvas.bind("<ButtonRelease-3>", self.on_mouse_up)
        self.canvas.bind("<MouseWheel>", self.on_wheel)
        self.canvas.bind("<Button-4>", self.on_wheel)
        self.canvas.bind("<Button-5>", self.on_wheel)
        self.bind("<Return>", lambda e: self.add_to_queue())
        self.queue_listbox.bind("<Delete>", lambda e: self.remove_item())
        self.queue_listbox.bind("<<ListboxSelect>>", self.on_list_select)

    # --- CORE ---
    def load_image(self):
        path = filedialog.askopenfilename(filetypes=[("PNG", "*.png"), ("All", "*.*")])
        if not path: return
        self.image_path = path
        self.original_img = Image.open(path).convert("RGBA")
        self.scale = 1.0
        self.selected_chunks.clear()
        self.refresh_view(reset=True)

    def refresh_view(self, reset=False):
        if self.original_img is None: return
        w, h = self.original_img.size
        new_w = int(w * self.scale)
        new_h = int(h * self.scale)
        resized = self.original_img.resize((new_w, new_h), Image.Resampling.NEAREST)
        self.tk_img = ImageTk.PhotoImage(resized)

        self.canvas.delete("all")
        self.canvas.create_image(0, 0, anchor='nw', image=self.tk_img)
        self.canvas.config(scrollregion=(0, 0, new_w, new_h))
        if reset:
            self.canvas.yview_moveto(0)
            self.canvas.xview_moveto(0)
        self.draw_grid(new_w, new_h)
        self.draw_selection()

    def draw_grid(self, w, h):
        step = int(CHUNK_SIZE * self.scale)
        if step < 4: return
        for x in range(0, w, step):
            self.canvas.create_line(x, 0, x, h, fill="#666", width=1, tags="grid")
        for y in range(0, h, step):
            self.canvas.create_line(0, y, w, y, fill="#666", width=1, tags="grid")

    def draw_selection(self):
        self.canvas.delete("sel")
        step = CHUNK_SIZE * self.scale
        if len(self.selected_chunks) > 5000: return
        for (cx, cy) in self.selected_chunks:
            x1 = cx * step
            y1 = cy * step
            x2 = x1 + step
            y2 = y1 + step
            self.canvas.create_rectangle(x1, y1, x2, y2, fill="red", stipple="gray25", outline="", tags="sel")

    def on_mouse_down(self, event, erase):
        self.is_erasing = erase
        self.drag_start = (self.canvas.canvasx(event.x), self.canvas.canvasy(event.y))
        color = "red" if erase else "#0f0"
        self.drag_rect_id = self.canvas.create_rectangle(
            self.drag_start[0], self.drag_start[1],
            self.drag_start[0], self.drag_start[1],
            outline=color, width=2, tags="ui"
        )

    def on_mouse_drag(self, event):
        if not self.drag_start: return
        cur_x = self.canvas.canvasx(event.x)
        cur_y = self.canvas.canvasy(event.y)
        self.canvas.coords(self.drag_rect_id, self.drag_start[0], self.drag_start[1], cur_x, cur_y)

    def on_mouse_up(self, event):
        if not self.drag_start: return
        x1, y1, x2, y2 = self.canvas.coords(self.drag_rect_id)
        self.canvas.delete(self.drag_rect_id)
        self.drag_start = None
        self.drag_rect_id = None

        real_scale = CHUNK_SIZE * self.scale
        c_x1 = int(min(x1, x2) / real_scale)
        c_y1 = int(min(y1, y2) / real_scale)
        c_x2 = int(max(x1, x2) / real_scale)
        c_y2 = int(max(y1, y2) / real_scale)

        changed = False
        for cy in range(c_y1, c_y2 + 1):
            for cx in range(c_x1, c_x2 + 1):
                if 0 <= cx * CHUNK_SIZE < self.original_img.width and \
                        0 <= cy * CHUNK_SIZE < self.original_img.height:
                    coord = (cx, cy)
                    if self.is_erasing:
                        if coord in self.selected_chunks:
                            self.selected_chunks.remove(coord)
                            changed = True
                    else:
                        if coord not in self.selected_chunks:
                            self.selected_chunks.add(coord)
                            changed = True
        if changed:
            self.draw_selection()
            self.update_sidebar_preview(self.selected_chunks)

    def on_wheel(self, event):
        if not self.original_img: return
        if event.num == 5 or event.delta < 0:
            factor = 0.5 if self.scale > 1 else 0.9
        else:
            factor = 2.0 if self.scale >= 1 else 1.1
        new_scale = self.scale * factor
        if new_scale < 0.1: new_scale = 0.1
        if new_scale > 32.0: new_scale = 32.0
        if new_scale > 1:
            new_scale = round(new_scale)
            if new_scale == 0: new_scale = 1
        if new_scale != self.scale:
            self.scale = new_scale
            self.refresh_view()

    def get_crop(self, chunks):
        if not chunks: return None
        xs = [c[0] for c in chunks]
        ys = [c[1] for c in chunks]
        min_x, max_x = min(xs), max(xs)
        min_y, max_y = min(ys), max(ys)
        w = (max_x - min_x + 1) * CHUNK_SIZE
        h = (max_y - min_y + 1) * CHUNK_SIZE
        crop = Image.new("RGBA", (w, h))
        for (cx, cy) in chunks:
            box = (cx * CHUNK_SIZE, cy * CHUNK_SIZE, (cx + 1) * CHUNK_SIZE, (cy + 1) * CHUNK_SIZE)
            part = self.original_img.crop(box)
            crop.paste(part, ((cx - min_x) * CHUNK_SIZE, (cy - min_y) * CHUNK_SIZE))
        return crop

    def show_image_in_preview(self, img_pil):
        """Вписывает картинку в квадратное превью"""
        if not img_pil:
            self.preview_lbl.config(image='', text="[No Selection]")
            return

        w, h = img_pil.size
        # Скейлим, чтобы вписать в PREVIEW_SIZE
        scale = min(PREVIEW_SIZE / w, PREVIEW_SIZE / h)
        new_w = int(w * scale)
        new_h = int(h * scale)
        if new_w < 1: new_w = 1
        if new_h < 1: new_h = 1

        resized = img_pil.resize((new_w, new_h), Image.Resampling.NEAREST)
        self._tk_preview = ImageTk.PhotoImage(resized)
        self.preview_lbl.config(image=self._tk_preview, text="")

    def update_sidebar_preview(self, chunks):
        if not chunks:
            self.show_image_in_preview(None)
            return
        crop = self.get_crop(chunks)
        self.show_image_in_preview(crop)

    def add_to_queue(self):
        if not self.selected_chunks: return
        crop = self.get_crop(self.selected_chunks)
        name = simpledialog.askstring("Add", "Filename:", initialvalue=f"sprite_{len(self.export_queue):03d}",
                                      parent=self)
        if name:
            self.export_queue.append({'name': name, 'img': crop})
            self.queue_listbox.insert(tk.END, name)
            self.selected_chunks.clear()
            self.draw_selection()
            self.update_sidebar_preview(None)
            self.queue_listbox.see(tk.END)

    def remove_item(self):
        sel = self.queue_listbox.curselection()
        if not sel: return
        idx = sel[0]
        self.queue_listbox.delete(idx)
        self.export_queue.pop(idx)

    def on_list_select(self, event):
        sel = self.queue_listbox.curselection()
        if not sel: return
        item = self.export_queue[sel[0]]
        self.show_image_in_preview(item['img'])

    def open_export_manager(self):
        if not self.export_queue:
            messagebox.showinfo("Info", "Queue is empty")
            return
        target_dir = filedialog.askdirectory(title="Select folder")
        if not target_dir: return

        ExportManager(self, self.export_queue, target_dir)
        # Очередь пока не чистим автоматически, чтобы можно было экспортировать еще раз если надо
        # self.export_queue.clear()
        # self.queue_listbox.delete(0, tk.END)


if __name__ == "__main__":
    app = ChunkSelector()
    app.mainloop()