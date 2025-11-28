package net.hirukarogue.curiosityresearches.researchparches.researchitems;

import net.hirukarogue.curiosityresearches.CuriosityMod;
import net.hirukarogue.curiosityresearches.records.ResearchNotesData;
import net.hirukarogue.curiosityresearches.researchparches.ResearchItemsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.List;
import java.util.Objects;

public class InkandQuill extends Item {
    public InkandQuill(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (pPlayer.getItemInHand(InteractionHand.MAIN_HAND).is(this) && pPlayer.getItemInHand(InteractionHand.OFF_HAND).is(Items.PAPER)) {
            ItemStack researchNotes = new ItemStack(ResearchItemsRegistry.RESEARCH_NOTES.get());
            HitResult lookingAt = getPlayerPOVHitResult(pPlayer, 5.0D);
            List<ResearchNotesData> allNoteData = pLevel.registryAccess().registry(CuriosityMod.REGISTRED_RESEARCH_NOTES_DATA).get().stream().toList();
            boolean sucess = false;

            if (allNoteData.isEmpty()) {
                pPlayer.sendSystemMessage(Component.literal("You couldn't find any relevant information."));
                return super.use(pLevel, pPlayer, pUsedHand);
            }

            if (lookingAt.getType() == HitResult.Type.ENTITY) {
                Entity entity = ((EntityHitResult) lookingAt).getEntity();
                for (ResearchNotesData noteData : allNoteData) {
                    if (noteData.target() == null) {
                        continue;
                    }

                    if (entity instanceof ItemEntity) {
                        ItemStack itemStack = ((ItemEntity) entity).getItem();
                        if (itemStack.isEmpty()) {
                            continue;
                        }
                        ResourceLocation targetItemRL = itemStack.getItem().builtInRegistryHolder().key().location();
                        if (noteData.target().equals(targetItemRL)) {
                            ResearchNotesData.save(researchNotes, noteData);
                            giveOrTransformItem(pPlayer, researchNotes);
                            sucess = true;
                            break;
                        }
                        continue;
                    }

                    ResourceLocation targetEntityRL = entity.getType().builtInRegistryHolder().key().location();
                    if (noteData.target().equals(targetEntityRL)) {
                        ResearchNotesData.save(researchNotes, noteData);
                        giveOrTransformItem(pPlayer, researchNotes);
                        sucess = true;
                        break;
                    }
                }
            }

            if (lookingAt.getType() == HitResult.Type.BLOCK) {
                BlockPos blockPos = ((BlockHitResult) lookingAt).getBlockPos();
                for (ResearchNotesData noteData : allNoteData) {
                    BlockState block = pLevel.getBlockState(blockPos);
                    ResourceLocation targetBlockRL = block.getBlock().builtInRegistryHolder().key().location();
                    if (noteData.target() == null) {
                        continue;
                    }
                    if (noteData.target().equals(targetBlockRL)) {
                        ResearchNotesData.save(researchNotes, noteData);
                        giveOrTransformItem(pPlayer, researchNotes);
                        sucess = true;
                        break;
                    }
                }
            }

            if (lookingAt.getType() == HitResult.Type.MISS) {
                int[] playerPosition = new int[]{
                        (int) Math.floor(pPlayer.getX()),
                        (int) Math.floor(pPlayer.getY()),
                        (int) Math.floor(pPlayer.getZ())
                };

                Holder<Biome> biome = null;
                ResourceLocation dimensionId = pPlayer.level().dimension().location();

                if (!pPlayer.isShiftKeyDown()) {
                    biome = pLevel.getBiome(pPlayer.blockPosition());
                }

                for (ResearchNotesData noteData : allNoteData) {
                    if (noteData.target() == null && noteData.coordinates() == null) {
                        continue;
                    }

                    boolean hasCoordinates = false;
                    boolean coordinatesMatch = false;
                    if (noteData.coordinates() != null) {
                        hasCoordinates = true;
                        ResearchNotesData.Coordinates coords = noteData.coordinates();
                        boolean xInRange = coords.x().isEmpty() || coords.x2().isEmpty() ||
                                (playerPosition[0] >= coords.x().get() && playerPosition[0] <= coords.x2().get()) || playerPosition[0] == coords.x().get();
                        boolean yInRange = coords.y().isEmpty() || coords.y2().isEmpty() ||
                                (playerPosition[1] >= coords.y().get() && playerPosition[1] <= coords.y2().get()) || playerPosition[1] == coords.y().get();
                        boolean zInRange = coords.z().isEmpty() || coords.z2().isEmpty() ||
                                (playerPosition[2] >= coords.z().get() && playerPosition[2] <= coords.z2().get()) || playerPosition[2] == coords.z().get();

                        if (xInRange && yInRange && zInRange) {
                            coordinatesMatch = true;
                        }
                    }

                    boolean hasBiomeTarget = false;
                    if (noteData.target() != null) {
                        if (biome != null) {
                            hasBiomeTarget = true;
                        }

                        if (hasBiomeTarget) {
                            ResourceLocation targetBiomeRL = biome.unwrapKey().get().location();
                            if (noteData.target().equals(targetBiomeRL) && (!hasCoordinates || coordinatesMatch)) {
                                ResearchNotesData.save(researchNotes, noteData);
                                giveOrTransformItem(pPlayer, researchNotes);
                                sucess = true;
                                break;
                            }
                        }
                        if (noteData.target().equals(dimensionId) && (!hasCoordinates || coordinatesMatch)) {
                            ResearchNotesData.save(researchNotes, noteData);
                            giveOrTransformItem(pPlayer, researchNotes);
                            sucess = true;
                            break;
                        }
                        if (hasCoordinates && coordinatesMatch) {
                            ResearchNotesData.save(researchNotes, noteData);
                            giveOrTransformItem(pPlayer, researchNotes);
                            sucess = true;
                            break;
                        }
                    }
                }
            }

            if (sucess) {
                pPlayer.getItemInHand(pUsedHand).hurt(1, Objects.requireNonNull(pPlayer).getRandom(), null);
                if (pPlayer.getItemInHand(pUsedHand).getDamageValue() >= pPlayer.getItemInHand(pUsedHand).getMaxDamage()) {
                    pPlayer.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ResearchItemsRegistry.EMPTY_INK_AND_QUILL.get()));
                }
            } else {
                pPlayer.sendSystemMessage(Component.literal("You couldn't find any relevant information."));
            }
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    public static boolean giveOrTransformItem(Player player, ItemStack newStack) {
        ItemStack offHand = player.getOffhandItem();

        offHand.shrink(1);

        if (offHand.isEmpty()) {
            player.setItemInHand(InteractionHand.OFF_HAND, newStack.copy());
            return true;
        }

        if (player.getInventory().add(newStack.copy())) {
            player.inventoryMenu.broadcastChanges();
            return true;
        }

        player.drop(newStack.copy(), false);
        return true;
    }

    private static HitResult getPlayerPOVHitResult(Player player, double maxDistance) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookDir = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookDir.x * maxDistance, lookDir.y * maxDistance, lookDir.z * maxDistance);

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                player,
                eyePos,
                endPos,
                player.getBoundingBox().expandTowards(lookDir.scale(maxDistance)).inflate(1.0D),
                (entity) -> !entity.isSpectator() && entity.isPickable() && !entity.isInvisible(),
                maxDistance * maxDistance
        );

        if (entityHit != null) {
            return entityHit;
        }

        Entity closestItem = null;
        double closestDistSq = Double.MAX_VALUE;

        for (ItemEntity item : player.level().getEntitiesOfClass(ItemEntity.class,
                player.getBoundingBox().expandTowards(lookDir.scale(maxDistance)).inflate(2.0D))) {

            if (item.isInvisible() || item.getItem().isEmpty()) continue;

            var aabb = item.getBoundingBox().inflate(0.1D);
            var optionalHit = aabb.clip(eyePos, endPos);

            if (optionalHit.isPresent()) {
                Vec3 hitPos = optionalHit.get();
                double distSq = eyePos.distanceToSqr(hitPos);
                if (distSq < closestDistSq) {
                    closestDistSq = distSq;
                    closestItem = item;
                    // Não precisa continuar procurando se já tá muito perto
                    if (distSq < 0.25) break;
                }
            }
        }

        if (closestItem != null) {
            return new EntityHitResult(closestItem);
        }

        return player.level().clip(new ClipContext(
                eyePos,
                endPos,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.SOURCE_ONLY,
                player
        ));
    }
}
