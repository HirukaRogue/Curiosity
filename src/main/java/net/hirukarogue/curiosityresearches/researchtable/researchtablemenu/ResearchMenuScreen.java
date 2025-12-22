package net.hirukarogue.curiosityresearches.researchtable.researchtablemenu;

import com.mojang.blaze3d.systems.RenderSystem;
import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.concurrent.atomic.AtomicReference;

public class ResearchMenuScreen extends AbstractContainerScreen<ResearchMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(CuriosityMod.MOD_ID, "textures/gui/research_gui.png");

    private String ponderOutput = "";
    private int ponderTimer = 0;

    public ResearchMenuScreen(ResearchMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageHeight = 197;
    }

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget(new PonderButton(
                this.leftPos + 152,
                this.topPos + 7,
                16,
                16,
                176,
                0,
                16,
                32,
                32,
                button -> {
                    this.ponderOutput = this.menu.blockEntity.ponder();
                    this.ponderTimer = 20 * 30;
                }
        ));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    private void updatePonderTimer() {
        if (this.ponderTimer > 0) {
            this.ponderTimer--;
            if (this.ponderTimer == 0) {
                this.ponderOutput = "";
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.updatePonderTimer();
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        if (!this.ponderOutput.isEmpty() && this.ponderTimer > 0) {
            int textX = this.leftPos + 10;
            int textY = this.topPos + 6;
            guiGraphics.drawString(this.font, this.ponderOutput, textX, textY, 0xFFFFFF, false);
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
