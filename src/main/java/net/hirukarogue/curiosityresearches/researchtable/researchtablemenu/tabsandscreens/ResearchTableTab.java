package net.hirukarogue.curiosityresearches.researchtable.researchtablemenu.tabsandscreens;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class ResearchTableTab<M extends AbstractContainerMenu> {
    private final Component title;
    private final ScreenFactory<M> screenFactory;
    private final ResourceLocation tabImage;

    public ResearchTableTab(Component title, ScreenFactory<M> screenFactory, ResourceLocation tabImage) {
        this.title = title;
        this.screenFactory = screenFactory;
        this.tabImage = tabImage;
    }

    public Component getTitle() {
        return title;
    }

    public ResourceLocation getTabImage() {
        return tabImage;
    }

    public ScreenFactory<M> getScreenFactory() {
        return screenFactory;
    }

    public AbstractContainerScreen<M> createScreen(M menu, Inventory playerInventory, Component title) {
        return screenFactory.create(menu, playerInventory, title);
    }

    @FunctionalInterface
    public interface ScreenFactory<M extends AbstractContainerMenu> {
        AbstractContainerScreen<M> create(M menu, Inventory playerInventory, Component title);
    }
}