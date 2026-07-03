package net.hirukarogue.curiosityresearches.researchtable.researchtablemenu.buttons;

import com.mojang.blaze3d.systems.RenderSystem;
import net.hirukarogue.curiosityresearches.researchtable.researchtablemenu.tabsscreensandmenus.GeneralResearchMenu;
import net.hirukarogue.curiosityresearches.researchtable.researchtablemenu.tabsscreensandmenus.ResearchMenuScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ResearchTableTab extends AbstractButton {
    private final ResearchMenuScreen parentScreen;
    private final GeneralResearchMenu.Tabs targetTab;
    private final OnPress onPress; // Guardamos o clique aqui

    private final ResourceLocation TAB_BACKGROUND = new ResourceLocation("minecraft", "textures/gui/container/creative_inventory/tabs.png");

    // Olha que construtor lindo, limpo e sem NADA de narrator ou protected chato:
    public ResearchTableTab(ResearchMenuScreen parentScreen, GeneralResearchMenu.Tabs targetTab, int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message); // AbstractButton só pede isso!
        this.parentScreen = parentScreen;
        this.targetTab = targetTab;
        this.onPress = onPress;
    }

    @Override
    public void onPress() {
        // Executa a lógica de clique passada no Screen
        this.onPress.onPress(this);
    }

    // Na 1.20.1, mude para renderWidget para garantir que o hover/clique funcione perfeito
    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TAB_BACKGROUND);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Desenha a aba arredondada
        guiGraphics.blit(TAB_BACKGROUND, this.getX(), this.getY(), 0, 0, this.width, this.height);

        // Desenha o ícone usando a reflexão
        try {
            java.lang.reflect.Method m = targetTab.getClass().getMethod("getIcon");
            Object iconObj = m.invoke(targetTab);
            net.minecraft.world.item.ItemStack stack = null;

            if (iconObj instanceof net.minecraft.world.item.Item) {
                stack = new net.minecraft.world.item.ItemStack((net.minecraft.world.item.Item) iconObj);
            } else if (iconObj instanceof net.minecraft.world.item.ItemStack) {
                stack = (net.minecraft.world.item.ItemStack) iconObj;
            }
            else if (iconObj instanceof java.util.function.Supplier) {
                try {
                    Object supplied = ((java.util.function.Supplier<?>) iconObj).get();
                    if (supplied instanceof net.minecraft.world.item.Item) {
                        stack = new net.minecraft.world.item.ItemStack((net.minecraft.world.item.Item) supplied);
                    } else if (supplied instanceof net.minecraft.world.item.ItemStack) {
                        stack = (net.minecraft.world.item.ItemStack) supplied;
                    }
                } catch (Exception ignored) {}
            }
            else {
                try {
                    Class<?> regClass = Class.forName("net.minecraftforge.registries.RegistryObject");
                    if (regClass.isInstance(iconObj)) {
                        Object got = regClass.getMethod("get").invoke(iconObj);
                        if (got instanceof net.minecraft.world.item.Item) {
                            stack = new net.minecraft.world.item.ItemStack((net.minecraft.world.item.Item) got);
                        } else if (got instanceof net.minecraft.world.item.ItemStack) {
                            stack = (net.minecraft.world.item.ItemStack) got;
                        }
                    }
                } catch (ClassNotFoundException ignored) {
                } catch (Exception e) {
                    System.err.println("[mod] Erro ao resolver RegistryObject do ícone da tab: " + e.getMessage());
                }
            }

            if (stack != null) {
                int iconX = this.getX() + (this.width - 16) / 2;
                int iconY = this.getY() + (this.height - 16) / 2;
                guiGraphics.renderItem(stack, iconX, iconY);
                guiGraphics.renderItemDecorations(this.parentScreen.getFont(), stack, iconX, iconY, null);
            }
        } catch (NoSuchMethodException ignored) {
        } catch (Exception e) {
            System.err.println("[mod] Erro ao desenhar ícone da tab: " + e.getMessage());
        }

        // Reseta a cor para evitar interferir em outros widgets
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    // A Mojang exige o override desse cara, mas deixamos ele COMPLETAMENTE VAZIO.
    // Ignorado com sucesso!
    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    // Interface funcional para podermos passar o clique no seu createTabButtons do Screen
    public interface OnPress {
        void onPress(ResearchTableTab button);
    }
}