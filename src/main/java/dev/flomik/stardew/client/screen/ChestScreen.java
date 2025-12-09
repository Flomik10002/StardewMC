package dev.flomik.stardew.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.core.menu.ModChestMenu;
import dev.flomik.stardew.core.network.PacketChangeChestVariant;
import dev.flomik.stardew.core.network.PacketHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ChestScreen extends AbstractContainerScreen<ModChestMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(StardewMod.MODID, "textures/gui/container/container_36.png");
    private static final ResourceLocation PLATFORM_TEXTURE = new ResourceLocation(StardewMod.MODID, "textures/gui/container/color_platform.png");

    private static final int[] COLORS = {
            0x00000000, 0x5555FF, 0x77BFFF, 0x00AAAA, 0x00EAAF, 0x00AA00, 0x9FEC00,
            0xFFEA12, 0xFFA712, 0xFF6912, 0xFF0000, 0x870023, 0xFFADC7, 0xFF75C3,
            0xAC00C6, 0x8F00FF, 0x590B8E, 0x404040, 0x646464, 0xC8C8C8, 0xFEFEFE
    };

    private static final int BUTTON_SIZE = 12;
    private static final int SPACING = 2;
    private static final int BUTTON_FULL_SIZE = BUTTON_SIZE + SPACING;
    private static final int Y_OFFSET_FROM_TOP = -40;
    private static final int PLATFORM_PADDING = 3; // Отступ плашки от кнопок
    private static final int PLATFORM_HEIGHT = 40; // Высота плашки в пикселях
    private static final int PLATFORM_WIDTH = 166; // Ширина плашки в пикселях

    public ChestScreen(ModChestMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 185;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        drawColorButtonPlatform(guiGraphics, x, y);
        drawColorButtons(guiGraphics, x, y, mouseX, mouseY);
    }

    private void drawColorButtonPlatform(GuiGraphics guiGraphics, int guiLeft, int guiTop) {
        int startY = guiTop + Y_OFFSET_FROM_TOP;
        int row1Width = 11 * BUTTON_FULL_SIZE - SPACING;
        int row2Width = 11 * BUTTON_FULL_SIZE - SPACING;
        int maxWidth = Math.max(row1Width, row2Width);
        
        // Координаты плашки (центрирована относительно GUI)
        // Плашка центрируется по GUI, а не по кнопкам
        int platformX = guiLeft + (this.imageWidth - PLATFORM_WIDTH) / 2;
        int platformY = startY - (PLATFORM_HEIGHT - (2 * BUTTON_FULL_SIZE - SPACING)) / 2;
        
        // Рисуем текстуру плашки
        guiGraphics.blit(PLATFORM_TEXTURE, platformX, platformY, 0, 0, PLATFORM_WIDTH, PLATFORM_HEIGHT, PLATFORM_WIDTH, PLATFORM_HEIGHT);
    }

    private void drawColorButtons(GuiGraphics guiGraphics, int guiLeft, int guiTop, int mouseX, int mouseY) {
        int currentVariant = this.menu.getVariant();
        int startY = guiTop + Y_OFFSET_FROM_TOP;

        int row1Width = 11 * BUTTON_FULL_SIZE - SPACING;
        int row2Width = 11 * BUTTON_FULL_SIZE - SPACING;

        int startX1 = guiLeft + (this.imageWidth - row1Width) / 2;
        int startX2 = guiLeft + (this.imageWidth - row2Width) / 2;

        for (int i = 0; i < COLORS.length; i++) {
            int btnX, btnY;

            if (i <= 10) {
                btnX = startX1 + (i * BUTTON_FULL_SIZE);
                btnY = startY;
            } else {
                btnX = startX2 + ((i - 11) * BUTTON_FULL_SIZE);
                btnY = startY + BUTTON_FULL_SIZE;
            }

            int color = COLORS[i];

            if (i == 0) {
                guiGraphics.fill(btnX, btnY, btnX + BUTTON_SIZE, btnY + BUTTON_SIZE, 0x00000000);
                guiGraphics.renderOutline(btnX, btnY, BUTTON_SIZE, BUTTON_SIZE, 0xFF555555);
            } else {
                guiGraphics.fill(btnX, btnY, btnX + BUTTON_SIZE, btnY + BUTTON_SIZE, 0xFF000000 | color);
                guiGraphics.renderOutline(btnX, btnY, BUTTON_SIZE, BUTTON_SIZE, 0xFF000000);
            }

            if (i == currentVariant) {
                guiGraphics.renderOutline(btnX - 1, btnY - 1, BUTTON_SIZE + 2, BUTTON_SIZE + 2, 0xFFFFFFFF);
            }

            if (isHovering(btnX - guiLeft, btnY - guiTop, BUTTON_SIZE, BUTTON_SIZE, mouseX, mouseY)) {
                guiGraphics.fill(btnX, btnY, btnX + BUTTON_SIZE, btnY + BUTTON_SIZE, 0x30FFFFFF);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int x = (this.width - this.imageWidth) / 2;
            int y = (this.height - this.imageHeight) / 2;
            int startY = y + Y_OFFSET_FROM_TOP;
            int row1Width = 11 * BUTTON_FULL_SIZE - SPACING;
            int row2Width = 11 * BUTTON_FULL_SIZE - SPACING;
            int startX1 = x + (this.imageWidth - row1Width) / 2;
            int startX2 = x + (this.imageWidth - row2Width) / 2;

            for (int i = 0; i < COLORS.length; i++) {
                int btnX, btnY;
                if (i <= 10) {
                    btnX = startX1 + (i * BUTTON_FULL_SIZE);
                    btnY = startY;
                } else {
                    btnX = startX2 + ((i - 11) * BUTTON_FULL_SIZE);
                    btnY = startY + BUTTON_FULL_SIZE;
                }

                if (mouseX >= btnX && mouseX < btnX + BUTTON_SIZE && mouseY >= btnY && mouseY < btnY + BUTTON_SIZE) {
                    PacketHandler.CHANNEL.sendToServer(new PacketChangeChestVariant(this.menu.getPos(), i));
                    net.minecraft.client.Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}