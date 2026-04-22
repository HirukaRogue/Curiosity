package net.hirukarogue.curiosityresearches.researchtable.researchtablemenu.tabsandscreens;

import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.researchtable.researchtablemenu.menus.DiscoveryMenu;
import net.hirukarogue.curiosityresearches.researchtable.researchtablemenu.menus.ResearchMenu;
import net.hirukarogue.curiosityresearches.researchtable.researchtablemenu.menus.ShareKnowledgeMenu;
import net.hirukarogue.curiosityresearches.researchtable.researchtablemenu.tabsandscreens.screens.DiscoveryScreen;
import net.hirukarogue.curiosityresearches.researchtable.researchtablemenu.tabsandscreens.screens.ResearchingScreen;
import net.hirukarogue.curiosityresearches.researchtable.researchtablemenu.tabsandscreens.screens.ShareKnowledgeScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.CreativeModeTab;

import java.util.ArrayList;
import java.util.List;

public class ResearchMenuScreen extends AbstractContainerScreen<AbstractContainerMenu> {
    private final List<ResearchTableTab<? extends AbstractContainerMenu>> tabs = new ArrayList<>();
    private ResearchTableTab<? extends AbstractContainerMenu> selectedTab;
    private final ResourceLocation TAB_BACKGROUND = new ResourceLocation("minecraft", "textures/gui/container/creative_inventory/tabs.png");

    public ResearchMenuScreen(ResearchMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageHeight = 197;
    }

    @Override
    protected void init() {
        super.init();

        tabs.add(new ResearchTableTab<ResearchMenu>(
                Component.literal("Research"),
                ResearchingScreen::new,
                new ResourceLocation(CuriosityMod.MOD_ID, "textures/items/ink_and_quill.png")
        ));
        tabs.add(new ResearchTableTab<DiscoveryMenu>(
                Component.literal("Discovery"),
                DiscoveryScreen::new,
                new ResourceLocation(CuriosityMod.MOD_ID, "textures/gui/ponder_button.png")
        ));
        tabs.add(new ResearchTableTab<ShareKnowledgeMenu>(
                Component.literal("Share Knowledge"),
                ShareKnowledgeScreen::new,
                new ResourceLocation(CuriosityMod.MOD_ID, "textures/items/shared_research_book.png")
        ));

        if (selectedTab == null) {
            selectedTab = tabs.get(0);
        }
    }

    public void renderTabButton(GuiGraphics guiGraphics, ResearchTableTab<? extends AbstractContainerMenu> selectedTab) {
        boolean flag = this.selectedTab == selectedTab;
        int x = this.leftPos - 28;
        int y = this.topPos + 4 + tabs.indexOf(selectedTab) * 28;
        guiGraphics.blit(TAB_BACKGROUND, x, y, flag ? 28 : 0, 0, 28, 28);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x + 6, y + 6, 0);
        float scale = 0.5f;
        guiGraphics.pose().scale(scale, scale, scale);
        guiGraphics.blit(selectedTab.getTabImage(), 0, 0, 0, 0, 32, 32);
        guiGraphics.pose().popPose();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        for (ResearchTableTab<? extends AbstractContainerMenu> tab : tabs) {
            renderTabButton(guiGraphics, tab);
        }

        if (selectedTab != null) {
            int x = this.leftPos - 28;
            int y = this.topPos + 4 + tabs.indexOf(selectedTab) * 28;
            guiGraphics.blit(TAB_BACKGROUND, x, y, 28, 0, 28, 28);

            try {
                @SuppressWarnings("unchecked")
                ResearchTableTab.ScreenFactory<AbstractContainerMenu> factory =
                        (ResearchTableTab.ScreenFactory<AbstractContainerMenu>) selectedTab.getScreenFactory();
                if (factory != null) {
                    AbstractContainerScreen<?> tabScreen = factory.create((AbstractContainerMenu) this.menu,
                            this.minecraft.player.getInventory(), selectedTab.getTitle());
                    if (tabScreen != null) {
                        tabScreen.init(this.minecraft, this.width, this.height);
                        tabScreen.render(guiGraphics, i, i1, v);
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }
}