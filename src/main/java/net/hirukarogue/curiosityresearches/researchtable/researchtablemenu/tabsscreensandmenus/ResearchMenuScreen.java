package net.hirukarogue.curiosityresearches.researchtable.researchtablemenu.tabsscreensandmenus;

import com.mojang.blaze3d.systems.RenderSystem;
import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.miscellaneous.data.KnowledgeHelper;
import net.hirukarogue.curiosityresearches.network.PacketHandler;
import net.hirukarogue.curiosityresearches.network.ponderPackets.C2SPonderPacket;
import net.hirukarogue.curiosityresearches.network.researchPacket.C2SResearchPacket;
import net.hirukarogue.curiosityresearches.network.sharePacket.C2SSharePacket;
import net.hirukarogue.curiosityresearches.records.Knowledge.Knowledge;
import net.hirukarogue.curiosityresearches.researchtable.researchtablemenu.buttons.PonderButton;
import net.hirukarogue.curiosityresearches.researchtable.researchtablemenu.buttons.ResearchButton;
import net.hirukarogue.curiosityresearches.researchtable.researchtablemenu.buttons.ResearchTableTab;
import net.hirukarogue.curiosityresearches.researchtable.researchtablemenu.miscellaneous.KnowledgeList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class ResearchMenuScreen extends AbstractContainerScreen<GeneralResearchMenu> {
    private EditBox searchBox;
    private KnowledgeList knowledgeList;

    private static final ResourceLocation SHAREBG = new ResourceLocation(CuriosityMod.MOD_ID, "textures/gui/share_research_gui.png");
    private static final ResourceLocation RESEARCHBG = new ResourceLocation(CuriosityMod.MOD_ID, "textures/gui/research_gui.png");

    private static final int BG_TEXTURE_WIDTH = 256;
    private static final int BG_TEXTURE_HEIGHT = 256;

    private static final int TAB_WIDTH = 26;
    private static final int TAB_HEIGHT = 26;
    private static final int TAB_HORIZONTAL_STEP = 1;

    public ResearchMenuScreen(GeneralResearchMenu pMenu, Inventory pPlayerInventory, Component title) {
        super(pMenu, pPlayerInventory, Component.literal(""));
        this.imageWidth = 176;
        this.imageHeight = 197;
    }

    private static String ponderOutput = "";
    private static int ponderTimer = 0;

    public static void setPonderMessage(String message) {
        ponderOutput = message;
        ponderTimer = 20 * 5;
    }

    private void updatePonderTimer() {
        if (ponderTimer > 0) {
            ponderTimer--;
            if (ponderTimer == 0) {
                ponderOutput = "";
            }
        }
    }

    @Override
    protected void init() {
        super.init();

        // remove widgets antigos ao re-inicializar (evita que botões permaneçam entre abas)
        this.clearWidgets();

        // limpa referências locais para re-init seguro
        this.knowledgeList = null;
        this.searchBox = null;

        switch (this.menu.getSelectedTab()) {
            case RESEARCHING:
                this.addRenderableWidget(new PonderButton(
                        this.leftPos + 152,
                        this.topPos + 7,
                        16,
                        16,
                        176,
                        0,
                        16,
                        16,
                        16,
                        button -> {
                            try {
                                PacketHandler.sendToServer(new C2SPonderPacket(this.menu.blockEntity.getBlockPos()));
                            } catch (Exception e) {
                                System.err.println("[mod] Erro ao enviar C2SPonderPacket: " + e.getMessage());
                            }
                        }
                ));

                this.addRenderableWidget(new ResearchButton(
                        this.leftPos + 152,
                        this.topPos + 25,
                        16,
                        16,
                        0,
                        0,
                        16,
                        16,
                        16,
                        button -> {
                            try {
                                PacketHandler.sendToServer(new C2SResearchPacket(this.menu.blockEntity.getBlockPos()));
                            } catch (Exception e) {
                                System.err.println("[mod] Erro ao enviar C2SResearchPacket: " + e.getMessage());
                            }
                        }
                ));
                break;

            case SHARE_KNOWLEDGE:
                int areaLeft = this.leftPos + 9;
                int areaTop = this.topPos + 8;
                int areaWidth = 138;
                int searchHeight = 9;
                int searchX = areaLeft;
                int searchY = areaTop;
                int searchW = areaWidth - 16;

                this.searchBox = new EditBox(this.font, searchX, searchY, searchW, searchHeight, Component.literal(""));
                this.searchBox.setTextColor(0xFFFFFF);
                this.searchBox.setBordered(false);
                this.searchBox.setResponder(s -> {
                    if (this.knowledgeList != null) this.knowledgeList.setFilter(s);
                });
                this.addRenderableWidget(this.searchBox);

                int elementTop = searchY + searchHeight;
                int elementBottom = elementTop + 90;
                int elementLeft = areaLeft - 1;
                int elementWidth = searchW + 2;

                this.knowledgeList = new KnowledgeList(this.minecraft, elementWidth, elementLeft, elementTop, elementBottom, 20, entry -> {
                    try {
                        PacketHandler.sendToServer(new C2SSharePacket(this.menu.blockEntity.getBlockPos(), entry.getKnowledge()));
                    } catch (Exception e) {
                        System.err.println("[mod] Erro ao enviar C2SSharePacket: " + e.getMessage());
                    }
                });

                List<Knowledge> playerKnowledge = KnowledgeHelper.getPlayerKnowledge(Minecraft.getInstance().player);
                this.knowledgeList.addKnowledgeList(playerKnowledge);

                this.addRenderableWidget(this.knowledgeList);

                break;
        }

        // cria botões das tabs para as não selecionadas
        createTabButtons();
    }

    private void createTabButtons() {
        GeneralResearchMenu.Tabs[] tabs = GeneralResearchMenu.Tabs.values();
        int startX = this.leftPos + 4;

        int tabIndex = 0;
        for (GeneralResearchMenu.Tabs tab : tabs) {
            if (tab == this.menu.getSelectedTab()) {
                tabIndex++;
                continue;
            }
            int tabX = startX + (tabIndex * (TAB_WIDTH + 1) + TAB_HORIZONTAL_STEP);
            int tabY = this.topPos - TAB_HEIGHT + 4;

            ResearchTableTab tabButton = new ResearchTableTab(
                    this,
                    this.menu.getSelectedTab(),
                    tabX,
                    tabY,
                    TAB_WIDTH,
                    TAB_HEIGHT,
                    Component.literal(""),
                    b -> {
                        try {
                            this.menu.changeTab(tab);
                            this.init();
                        } catch (Exception e) {
                            System.err.println("[mod] Erro ao mudar tab: " + e.getMessage());
                        }
                    }

            );

            this.addRenderableWidget(tabButton);

            tabIndex++;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.menu.getSelectedTab() == GeneralResearchMenu.Tabs.SHARE_KNOWLEDGE) {
            if (this.knowledgeList != null && this.knowledgeList.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        switch (this.menu.getSelectedTab()) {
            case RESEARCHING:
                guiGraphics.drawString(this.font, Component.translatable("tabs.curiosity_researches.researching"), this.leftPos + 8, this.topPos + 6, 0x404040, false);
                this.updatePonderTimer();
                if (!ponderOutput.isEmpty() && this.ponderTimer > 0) {
                    int textX = this.leftPos + 6;
                    int textY = this.topPos + 105;
                    guiGraphics.drawString(this.font, this.ponderOutput, textX, textY, 0xFFFFFF, false);
                }
                break;

            case SHARE_KNOWLEDGE:
                guiGraphics.drawString(this.font, Component.translatable("tabs.curiosity_researches.share_knowledge"), this.leftPos + 8, this.topPos + 6, 0x404040, false);
                break;
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        // desenha background usando leftPos/topPos (mesmas coordenadas usadas pelas slots)
        switch (this.menu.getSelectedTab()) {
            case RESEARCHING: {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.setShaderTexture(0, RESEARCHBG);
                int x = this.leftPos;
                int y = this.topPos;
                // passa as dimensões reais da textura para evitar sampling/escala errada
                guiGraphics.blit(RESEARCHBG, x, y, 0, 0, this.imageWidth, this.imageHeight, BG_TEXTURE_WIDTH, BG_TEXTURE_HEIGHT);
                break;
            }
            case SHARE_KNOWLEDGE: {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.setShaderTexture(0, SHAREBG);
                int sx = this.leftPos;
                int sy = this.topPos;
                guiGraphics.blit(SHAREBG, sx, sy, 0, 0, this.imageWidth, this.imageHeight, BG_TEXTURE_WIDTH, BG_TEXTURE_HEIGHT);

                // desenha fundo preto atrás do EditBox para integrar visual
                if (this.searchBox != null) {
                    int relX = this.searchBox.getX() - 1;
                    int relY = this.searchBox.getY() - 1;
                    int relW = this.searchBox.getWidth() + 2;
                    int relH = this.searchBox.getHeight() + 2;
                    guiGraphics.fill(relX, relY, relX + relW, relY + relH, 0xFF000000);
                }
                break;
            }
        }
    }

    public Font getFont() {
        return this.font;
    }
}
