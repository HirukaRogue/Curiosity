package net.hirukarogue.curiosityresearches.researchtable.researchtablemenu;

import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PonderButton extends Button {
    private static ResourceLocation ACTUAL_TEXTURE = null;
    private static final ResourceLocation PONDER_TEXTURE =
            new ResourceLocation(CuriosityMod.MOD_ID, "textures/gui/ponder_button.png");
    private static final ResourceLocation PONDER_TEXTURE_HOVER =
            new ResourceLocation(CuriosityMod.MOD_ID, "textures/gui/ponder_button_hover.png");

    protected final int xTexStart;
    protected final int yTexStart;
    protected final int yDiffTex;
    protected final int textureWidth;
    protected final int textureHeight;

    protected PonderButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int yDiffTex, int textureWidth, int textureHeight, Button.OnPress onPress) {
        super(x, y, width, height, Component.literal(""), onPress, DEFAULT_NARRATION);
        this.xTexStart = xTexStart;
        this.yTexStart = yTexStart;
        this.yDiffTex = yDiffTex;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        if (ACTUAL_TEXTURE == null) {
            ACTUAL_TEXTURE = PONDER_TEXTURE;
        }
        this.renderTexture(guiGraphics, ACTUAL_TEXTURE, this.getX(), this.getY(), this.xTexStart, this.yTexStart, this.yDiffTex, this.width, this.height, this.textureWidth, this.textureHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.getX() < mouseX && this.getWidth() + this.getX() > mouseX
                && this.getY() < mouseY && this.getHeight() + this.getY() > mouseY) {
            ACTUAL_TEXTURE = PONDER_TEXTURE_HOVER;
        } else {
            ACTUAL_TEXTURE = PONDER_TEXTURE;
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}
