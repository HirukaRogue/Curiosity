package net.hirukarogue.curiosityresearches.researchtable.researchtablemenu.tabsscreensandmenus.screens;

import net.hirukarogue.curiosityresearches.researchtable.researchtablemenu.tabsscreensandmenus.menus.DiscoveryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class DiscoveryScreen extends AbstractContainerScreen<DiscoveryMenu> {
    public DiscoveryScreen(DiscoveryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {

    }
}
