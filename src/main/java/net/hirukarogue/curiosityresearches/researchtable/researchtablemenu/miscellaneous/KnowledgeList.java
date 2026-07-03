package net.hirukarogue.curiosityresearches.researchtable.researchtablemenu.miscellaneous;

import net.hirukarogue.curiosityresearches.miscellaneous.RomanConversor;
import net.hirukarogue.curiosityresearches.records.Knowledge.Knowledge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

public class KnowledgeList extends AbstractSelectionList<KnowledgeList.KnowledgeEntry> {
    public static final ResourceLocation BACKGROUND_LOCATION = new ResourceLocation("textures/block/dirt.png");

    private final Consumer<KnowledgeEntry> onClick;

    private final List<Knowledge> allKnowledge = new ArrayList<>();

    public KnowledgeList(Minecraft minecraft, int width, int left, int top, int bottom, int itemHeight, Consumer<KnowledgeEntry> onClick) {
        super(minecraft, width, bottom - top, top, bottom, itemHeight);
        this.setLeftPos(left);
        this.onClick = onClick;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {

    }

    public void setFilter(String search) {
        this.clearEntries();

        String lowerQuery = search.toLowerCase();
        for (Knowledge k : this.allKnowledge) {
            String knowledgeDisplay = (k.knowledgeName() + " " + RomanConversor.toRoman(k.level())).toLowerCase(Locale.ROOT);
            if (knowledgeDisplay.contains(lowerQuery)) {
                this.addEntry(new KnowledgeEntry(k));
            }
        }

        this.setScrollAmount(0);
    }

    public void addKnowledge(Knowledge knowledge) {
        KnowledgeEntry entry = new KnowledgeEntry(knowledge);
        this.addEntry(entry);
        this.allKnowledge.add(knowledge);
    }

    public void addKnowledgeList(List<Knowledge> knowledgeList) {
        for (Knowledge k : knowledgeList) {
            addKnowledge(k);
        }
    }

    public class KnowledgeEntry extends AbstractSelectionList.Entry<KnowledgeEntry> {
        private final Knowledge knowledge;

        public KnowledgeEntry(Knowledge knowledge) {
            this.knowledge = knowledge;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                KnowledgeList.this.onClick.accept(this);

                return true;
            }

            return false;
        }

        public Knowledge getKnowledge() {
            return knowledge;
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            int entryIndex = KnowledgeList.this.children().indexOf(this);

            if (entryIndex < 0) return false;

            int entryTop = KnowledgeList.this.getTop() + 4 - (int)KnowledgeList.this.getScrollAmount() + entryIndex * KnowledgeList.this.itemHeight;
            int entryLeft = KnowledgeList.this.getLeft();

            return mouseX >= entryLeft && mouseX <= entryLeft + KnowledgeList.this.getWidth() &&
                    mouseY >= entryTop && mouseY <= entryTop + KnowledgeList.this.itemHeight;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean isHovered, float partialTicks) {
            boolean realHovered = this.isMouseOver(mouseX, mouseY);

            int innerMargin = 4;
            int iconSize = 16;
            int spaceBetweenIconAndText = 6;

            int backgroundColor = 0x12AAAAAA;

            if (realHovered) {
                guiGraphics.renderOutline(left, top, rowWidth, rowHeight, 0xFFFFFFFF);
            }
            guiGraphics.fill(left, top, left + rowWidth, top + rowHeight, backgroundColor);

            int iconX = left + innerMargin;
            int iconY = top + (rowHeight - iconSize) / 2;

            if (this.knowledge.icon() != null) {
                guiGraphics.renderFakeItem(new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(this.knowledge.icon()))), iconX, iconY);
            } else {
                guiGraphics.renderFakeItem(new ItemStack(Blocks.BEDROCK), iconX, iconY);
            }

            int textX = iconX + iconSize + spaceBetweenIconAndText;
            int fontHeight = Minecraft.getInstance().font.lineHeight;
            int textY = top + (rowHeight - fontHeight) / 2;

            int titleColor = realHovered ? 0xFFFFFF : 0xAAAAAA;

            String displayName = knowledge.knowledgeName() + " " + RomanConversor.toRoman(knowledge.level());

            guiGraphics.drawString(Minecraft.getInstance().font, displayName, textX, textY, titleColor, false);
        }
    }

    @Override
    public int getRowLeft() {
        // Força o início horizontal dos itens a ser exatamente o lado esquerdo da lista
        return this.getLeft();
    }

    @Override
    public int getRowWidth() {
        // Força a largura da área de clique a ser o tamanho total da sua lista
        return this.getWidth();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (KnowledgeEntry entry : this.children()) {
            if (entry.isMouseOver(mouseX, mouseY)) {
                return entry.mouseClicked(mouseX, mouseY, button);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.enableScissor(this.getLeft(), this.getTop(), this.getLeft() + this.getWidth(), this.getTop() + this.getHeight());

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.disableScissor();
    }
}
