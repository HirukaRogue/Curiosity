package net.hirukarogue.curiosityresearches.researchtable.researchtablemenu.tabsscreensandmenus;

import net.hirukarogue.curiosityresearches.miscellaneous.RomanConversor;
import net.hirukarogue.curiosityresearches.records.Knowledge.Knowledge;
import net.hirukarogue.curiosityresearches.records.ResearchParchmentData;
import net.hirukarogue.curiosityresearches.researchparches.ResearchItemsRegistry;
import net.hirukarogue.curiosityresearches.researchtable.ResearchTableRegistry;
import net.hirukarogue.curiosityresearches.researchtable.researchtableblock.ResearchTableBlockEntity;
import net.hirukarogue.curiosityresearches.researchtable.researchtablemenu.types.ResearchMenuType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

import java.util.function.Supplier;

public class GeneralResearchMenu extends AbstractContainerMenu {
    public final ResearchTableBlockEntity blockEntity;
    public final Level level;
    private final Inventory playerInventory;
    private Tabs selectedTab;

    public GeneralResearchMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, (ResearchTableBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public GeneralResearchMenu(int pContainerId, Inventory inv, ResearchTableBlockEntity entity) {
        this(pContainerId, inv, entity, Tabs.RESEARCHING);
    }

    public GeneralResearchMenu(int pContainerId, Inventory inv, ResearchTableBlockEntity entity, Tabs selectedTab) {
        super(ResearchMenuType.RESEARCH_MENU.get(), pContainerId); // Pass null for the menu type since it's not used
        blockEntity = entity;
        this.level = inv.player.level();
        this.playerInventory = inv;

        addPlayerInventory(this.playerInventory);
        addPlayerHotbar(this.playerInventory);

        this.selectedTab = selectedTab;

        grabActualTab();
    }

    public void grabActualTab() {
        switch (selectedTab) {
            case RESEARCHING -> grabResearch();
            //case DISCOVERY -> grabDiscovery();
            case SHARE_KNOWLEDGE -> grabShareKnowledge();
        }
    }

    public void changeTab(Tabs newTab) {
        if (newTab == selectedTab) return;

        // Limpar os slots atuais
        this.slots.clear();

        // Atualiza o tab selecionado e reconstrói os slots
        this.selectedTab = newTab;
        addPlayerInventory(this.playerInventory);
        addPlayerHotbar(this.playerInventory);

        grabActualTab();

        this.broadcastChanges();
    }

    public Tabs getSelectedTab() {
        return selectedTab;
    }

    public void grabResearch() {
        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
            this.addSlot(new SlotItemHandler(iItemHandler,0,8,86){
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.is(Items.PAPER);
                }
            });
            this.addSlot(new SlotItemHandler(iItemHandler,1,149,88){
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
            this.addSlot(new SlotItemHandler(iItemHandler,2,8,68){
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.is(ResearchItemsRegistry.INK_AND_QUILL.get()) || stack.is(ResearchItemsRegistry.EMPTY_INK_AND_QUILL.get());
                }
            });

            this.addSlot(new SlotItemHandler(iItemHandler,3,61,16));
            this.addSlot(new SlotItemHandler(iItemHandler,4,103,16));
            this.addSlot(new SlotItemHandler(iItemHandler,5,48,45));
            this.addSlot(new SlotItemHandler(iItemHandler,6,82,46));
            this.addSlot(new SlotItemHandler(iItemHandler,7,116,45));
            this.addSlot(new SlotItemHandler(iItemHandler,8,61,77));
            this.addSlot(new SlotItemHandler(iItemHandler,9,103,76));
        });
    }

    public void grabShareKnowledge() {
        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
            this.addSlot(new SlotItemHandler(iItemHandler,0,135,29){
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.is(Items.PAPER);
                }
            });
            this.addSlot(new SlotItemHandler(iItemHandler,1,144,83){
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
            this.addSlot(new SlotItemHandler(iItemHandler,2,153,29){
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.is(ResearchItemsRegistry.INK_AND_QUILL.get()) || stack.is(ResearchItemsRegistry.EMPTY_INK_AND_QUILL.get());
                }
            });
        });
    }

    // CREDIT GOES TO: diesieben07 | https://github.com/diesieben07/SevenCommons
    // must assign a slot number to each of the slots used by the GUI.
    // For this container, we can see both the tile inventory's slots as well as the player inventory slots and the hotbar.
    // Each time we add a Slot to the container, it automatically increases the slotIndex, which means
    //  0 - 8 = hotbar slots (which will map to the InventoryPlayer slot numbers 0 - 8)
    //  9 - 35 = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)
    //  36 - 44 = TileInventory slots, which map to our TileEntity slot numbers 0 - 8)
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    // THIS YOU HAVE TO DEFINE!
    private static final int TE_INVENTORY_SLOT_COUNT = 9;  // must be the number of slots you have!
    @Override
    public ItemStack quickMoveStack(Player playerIn, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (!sourceSlot.hasItem()) return ItemStack.EMPTY;  //EMPTY_ITEM
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots
        if (pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // This is a vanilla container slot so merge the stack into the tile inventory
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX
                    + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;  // EMPTY_ITEM
            }
        } else if (pIndex < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            // This is a TE slot so merge the stack into the players inventory
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            System.out.println("Invalid slotIndex:" + pIndex);
            return ItemStack.EMPTY;
        }
        // If stack size == 0 (the entire stack was moved) set slot contents to null

        sourceSlot.setChanged();

        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, ResearchTableRegistry.RESEARCH_TABLE.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 115 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 173));
        }
    }

    public enum Tabs {
        RESEARCHING(ResearchItemsRegistry.INK_AND_QUILL),
        //DISCOVERY(() -> Items.COMPASS),
        SHARE_KNOWLEDGE(ResearchItemsRegistry.COMMON_RESEARCH);

        private final Supplier<?> iconSupplier;

        Tabs(Supplier<?> iconSupplier) {
            this.iconSupplier = iconSupplier;
        }

        // método que o ResearchMenuScreen procura por reflexão
        public Supplier<?> getIcon() {
            return iconSupplier;
        }
    }
}