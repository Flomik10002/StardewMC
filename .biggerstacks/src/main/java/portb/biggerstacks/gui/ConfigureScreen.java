/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import portb.biggerstacks.Constants;
import portb.biggerstacks.net.ClientboundConfigureScreenOpenPacket;
import portb.biggerstacks.net.PacketHandler;
import portb.biggerstacks.net.ServerboundCreateConfigTemplatePacket;
import portb.configlib.template.TemplateOptions;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class ConfigureScreen extends Screen
{
    private static final int             WIDTH  = 200;
    private static final int             HEIGHT = 180;
    private final        TemplateOptions previousOptions;
    private final        boolean         isAlreadyUsingCustomFile;
    private              MultiLineLabel  OVERWRITE_WARNING_LABEL;
    private              EditBox         potionsBox;
    private              EditBox         enchBooksBox;
    private              EditBox         normalItemsBox;
    private              Button          confirmButton;
    
    protected ConfigureScreen(ClientboundConfigureScreenOpenPacket options)
    {
        super(Component.literal("Configure stack size"));
        
        previousOptions = options;
        isAlreadyUsingCustomFile = options.isAlreadyUsingCustomFile();
    }
    
    public static void open(ClientboundConfigureScreenOpenPacket packet)
    {
        Minecraft.getInstance().setScreen(new ConfigureScreen(packet));
    }
    
    private static boolean isEditBoxInputValid(String inputString)
    {
        if (inputString.matches("[0-9]+"))
        {
            try
            {
                int value = Integer.parseInt(inputString);
                
                return value > 0 && value <= (Integer.MAX_VALUE / 2);
            }
            catch (NumberFormatException e)
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }
    
    @Override
    protected void init()
    {
        int relX = (this.width - WIDTH) / 2, relY = (this.height - HEIGHT) / 2;
        
        int editBoxStartX = 120, editBoxStartY = 30;
        
        OVERWRITE_WARNING_LABEL = MultiLineLabel.create(font,
                                                        Component.translatable("biggerstacks.overwrite.warn").withStyle(
                                                                Style.EMPTY.withColor(0xffaaaa)),
                                                        WIDTH
        );
        
        normalItemsBox = new EditBoxWithADifferentBorderColour(
                font,
                relX + editBoxStartX,
                relY + editBoxStartY,
                60,
                20,
                Component.translatable("biggerstacks.normalbox.alt")
        );
        
        potionsBox = new EditBoxWithADifferentBorderColour(
                font,
                relX + editBoxStartX,
                relY + editBoxStartY + 30,
                60,
                20,
                Component.translatable("biggerstacks.potsbox.alt")
        );
        
        enchBooksBox = new EditBoxWithADifferentBorderColour(
                font,
                relX + editBoxStartX,
                relY + editBoxStartY + 60,
                60,
                20,
                Component.translatable("biggerstacks.enchbox.alt")
        );
        
        enchBooksBox.setValue(String.valueOf(previousOptions.getEnchBookLimit()));
        potionsBox.setValue(String.valueOf(previousOptions.getPotionStackLimit()));
        normalItemsBox.setValue(String.valueOf(previousOptions.getNormalStackLimit()));
        
        enchBooksBox.setResponder(verifyInputBoxNumber(enchBooksBox));
        potionsBox.setResponder(verifyInputBoxNumber(potionsBox));
        normalItemsBox.setResponder(verifyInputBoxNumber(normalItemsBox));
        
        /*
        * ,
                                   ,
                                   80,
                                   20,
                                   ,
                                   ,
                                   Supplier::get
        * */
        confirmButton = new Button.Builder(Component.translatable("biggerstacks.save"), this::onConfirmButtonClicked)
                                .bounds(relX + (WIDTH - 80) / 2, relY + HEIGHT - 30, 80, 20).build();
        
        addRenderableWidget(normalItemsBox);
        addRenderableWidget(potionsBox);
        addRenderableWidget(enchBooksBox);
        addRenderableWidget(confirmButton);
        
        super.init();
    }
    
    @Override
    public void render(@NotNull GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick)
    {
        renderBackground(graphics);
        
        int relX = (this.width - WIDTH) / 2, relY = (this.height - HEIGHT) / 2;
        
        graphics.drawCenteredString(
                font,
                Component.translatable("biggerstacks.config.title"),
                width / 2,
                relY + 10,
                0xffffff
        );
        
        int centreOffset = (20 - font.lineHeight) / 2;
        int labelStartX  = 20, labelStartY = 30;
        
        graphics.drawString(
                font,
                Component.translatable("biggerstacks.normalbox.label"),
                relX + labelStartX,
                centreOffset + relY + labelStartY,
                0xffffff
        );
        
        graphics.drawString(
                font,
                Component.translatable("biggerstacks.potsbox.label"),
                relX + labelStartX,
                centreOffset + relY + labelStartY + 30,
                0xffffff
        );
        
        graphics.drawString(
                font,
                Component.translatable("biggerstacks.enchbox.label"),
                relX + labelStartX,
                centreOffset + relY + labelStartY + 60,
                0xffffff
        );
        
        if (isAlreadyUsingCustomFile)
            OVERWRITE_WARNING_LABEL.renderCentered(graphics, width / 2, relY + 125);
        
        super.render(graphics, pMouseX, pMouseY, pPartialTick);
    }
    
    @Override
    public void renderBackground(GuiGraphics graphics)
    {
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;
        
        graphics.blitWithBorder(Constants.CONFIG_GUI_BG,
                                relX,
                                relY,
                                0,
                                0,
                                WIDTH,
                                HEIGHT,
                                256,
                                256,
                                4
        );
    }
    
    private void onConfirmButtonClicked(Button button)
    {
        if (isStateValid())
            submit();
    }
    
    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
    {
        if (pKeyCode == GLFW.GLFW_KEY_ENTER && isStateValid())
        {
            submit();
            
            return true;
        }
        
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }
    
    private boolean isStateValid()
    {
        return isEditBoxInputValid(normalItemsBox.getValue())
                       && isEditBoxInputValid(enchBooksBox.getValue())
                       && isEditBoxInputValid(potionsBox.getValue());
    }
    
    private void submit()
    {
        //send packet to server
        PacketHandler.INSTANCE.sendToServer(new ServerboundCreateConfigTemplatePacket(
                Integer.parseInt(normalItemsBox.getValue()),
                Integer.parseInt(potionsBox.getValue()),
                Integer.parseInt(enchBooksBox.getValue())
        ));
        
        //close screen
        Minecraft.getInstance().setScreen(null);
    }
    
    @Override
    public boolean isPauseScreen()
    {
        return true;
    }
    
    private Consumer<String> verifyInputBoxNumber(EditBox editBox)
    {
        return inputString -> {
            if (isEditBoxInputValid(inputString))
            {
                editBox.setTextColor(0xffffff);
                confirmButton.active = true;
            }
            else
            {
                editBox.setTextColor(0xff0000);
                confirmButton.active = false;
            }
        };
    }
}
