/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class EditBoxWithADifferentBorderColour extends EditBox
{
    private static final int BORDER_COLOUR = 0xff_c7c7c7;
    
    public EditBoxWithADifferentBorderColour(Font pFont, int pX, int pY, int pWidth, int pHeight, Component pMessage)
    {
        super(pFont, pX, pY, pWidth, pHeight, pMessage);
        //fix crash with modernui
        setFormatter((p_94147_, p_94148_) -> FormattedCharSequence.forward(p_94147_, Style.EMPTY));
    }
    
    public EditBoxWithADifferentBorderColour(Font pFont, int pX, int pY, int pWidth, int pHeight, @Nullable EditBox p_94111_, Component pMessage)
    {
        super(pFont, pX, pY, pWidth, pHeight, p_94111_, pMessage);
        //fix crash with modernui
        setFormatter((p_94147_, p_94148_) -> FormattedCharSequence.forward(p_94147_, Style.EMPTY));
    }
    
    @Override
    public void renderWidget(GuiGraphics p_283252_, int p_281594_, int p_282100_, float p_283101_)
    {
        if (this.isVisible())
        {
            if (this.isBordered())
            {
                int i = this.isFocused() ? -1 : BORDER_COLOUR;
                p_283252_.fill(this.getX() - 1,
                               this.getY() - 1,
                               this.getX() + this.width + 1,
                               this.getY() + this.height + 1,
                               i
                );
                p_283252_.fill(this.getX(),
                               this.getY(),
                               this.getX() + this.width,
                               this.getY() + this.height,
                               -16777216
                );
            }
            
            int     i2    = this.isEditable ? this.textColor : this.textColorUneditable;
            int     j     = this.cursorPos - this.displayPos;
            int     k     = this.highlightPos - this.displayPos;
            String  s     = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
            boolean flag  = j >= 0 && j <= s.length();
            boolean flag1 = this.isFocused() && this.frame / 6 % 2 == 0 && flag;
            int     l     = this.bordered ? this.getX() + 4 : this.getX();
            int     i1    = this.bordered ? this.getY() + (this.height - 8) / 2 : this.getY();
            int     j1    = l;
            if (k > s.length())
            {
                k = s.length();
            }
            
            if (!s.isEmpty())
            {
                String s1 = flag ? s.substring(0, j) : s;
                j1 = p_283252_.drawString(this.font, this.formatter.apply(s1, this.displayPos), l, i1, i2);
            }
            
            boolean flag2 = this.cursorPos < this.value.length() || this.value.length() >= this.getMaxLength();
            int     k1    = j1;
            if (!flag)
            {
                k1 = j > 0 ? l + this.width : l;
            }
            else if (flag2)
            {
                k1 = j1 - 1;
                --j1;
            }
            
            if (!s.isEmpty() && flag && j < s.length())
            {
                p_283252_.drawString(this.font, this.formatter.apply(s.substring(j), this.cursorPos), j1, i1, i2);
            }
            
            if (this.hint != null && s.isEmpty() && !this.isFocused())
            {
                p_283252_.drawString(this.font, this.hint, j1, i1, i2);
            }
            
            if (!flag2 && this.suggestion != null)
            {
                p_283252_.drawString(this.font, this.suggestion, k1 - 1, i1, -8355712);
            }
            
            if (flag1)
            {
                if (flag2)
                {
                    p_283252_.fill(RenderType.guiOverlay(), k1, i1 - 1, k1 + 1, i1 + 1 + 9, -3092272);
                }
                else
                {
                    p_283252_.drawString(this.font, "_", k1, i1, i2);
                }
            }
            
            if (k != j)
            {
                int l1 = l + this.font.width(s.substring(0, k));
                this.renderHighlight(p_283252_, k1, i1 - 1, l1 - 1, i1 + 1 + 9);
            }
            
        }
    }
}
