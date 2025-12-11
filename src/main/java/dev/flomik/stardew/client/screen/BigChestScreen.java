package dev.flomik.stardew.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.common.module.craftables.menu.ModBigChestMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BigChestScreen extends ChestScreen<ModBigChestMenu> {

    private static final ResourceLocation WIDE_TEXTURE = new ResourceLocation(StardewMod.MODID, "textures/gui/container/container_70.png");

    public BigChestScreen(ModBigChestMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.imageWidth = 194;
        this.imageHeight = 256;

        this.inventoryLabelX = (this.imageWidth - 162) / 2;
        this.inventoryLabelY = 145;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        int visualWidth = this.imageWidth + 17;

        guiGraphics.blit(WIDE_TEXTURE, x, y, 0, 0, visualWidth, this.imageHeight);

        drawColorButtonPlatform(guiGraphics, x, y);
        drawColorButtons(guiGraphics, x, y, mouseX, mouseY);
    }
}