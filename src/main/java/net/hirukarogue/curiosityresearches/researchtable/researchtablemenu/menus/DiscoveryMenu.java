package net.hirukarogue.curiosityresearches.researchtable.researchtablemenu.menus;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DiscoveryMenu extends AbstractContainerMenu {
    protected DiscoveryMenu(@Nullable MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }



    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }
}
