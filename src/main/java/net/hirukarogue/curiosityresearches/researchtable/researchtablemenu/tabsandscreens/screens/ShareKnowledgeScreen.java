package net.hirukarogue.curiosityresearches.researchtable.researchtablemenu.tabsandscreens.screens;

import net.hirukarogue.curiosityresearches.researchtable.researchtablemenu.menus.ShareKnowledgeMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ShareKnowledgeScreen extends AbstractContainerScreen<ShareKnowledgeMenu> {
    public ShareKnowledgeScreen(ShareKnowledgeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {

    }
}
