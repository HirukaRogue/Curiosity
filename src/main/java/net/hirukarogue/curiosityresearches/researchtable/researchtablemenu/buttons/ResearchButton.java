package net.hirukarogue.curiosityresearches.researchtable.researchtablemenu.buttons;

import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ResearchButton extends Button {
    private static ResourceLocation RESEARCH_BUTTON_TEXTURE = new ResourceLocation(CuriosityMod.MOD_ID, "textures/gui/research_button.png");;

    protected final int xTexStart;
    protected final int yTexStart;
    protected final int yDiffTex;
    protected final int textureWidth;
    protected final int textureHeight;

    public ResearchButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int yDiffTex, int textureWidth, int textureHeight, net.minecraft.client.gui.components.Button.OnPress onPress) {
        super(x, y, width, height, Component.literal(""), onPress, DEFAULT_NARRATION);
        this.xTexStart = xTexStart;
        this.yTexStart = yTexStart;
        this.yDiffTex = yDiffTex;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTexture(guiGraphics, RESEARCH_BUTTON_TEXTURE, this.getX(), this.getY(), this.xTexStart, this.yTexStart, this.yDiffTex, this.width, this.height, this.textureWidth, this.textureHeight);
    }
}
