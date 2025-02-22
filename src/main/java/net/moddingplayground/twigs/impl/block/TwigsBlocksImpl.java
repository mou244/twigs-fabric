package net.moddingplayground.twigs.impl.block;

import com.google.common.collect.Maps;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.fabricmc.fabric.api.registry.TillableBlockRegistry;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BambooBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.BlockStatePropertyLootCondition;
import net.minecraft.loot.condition.InvertedLootCondition;
import net.minecraft.loot.condition.MatchToolLootCondition;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.predicate.item.EnchantmentPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.moddingplayground.frame.api.contentregistries.v0.StateRegistry;
import net.moddingplayground.twigs.api.block.StrippedBambooBlock;
import net.moddingplayground.twigs.api.block.TwigsBlocks;
import net.moddingplayground.twigs.api.sound.TwigsSoundEvents;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

public final class TwigsBlocksImpl implements TwigsBlocks, ModInitializer {
    @Override
    public void onInitialize() {
        StateRegistry.BLOCK_ENTITY_SUPPORTS.apply(BlockEntityType.SIGN).add(
            STRIPPED_BAMBOO_SIGN,
            STRIPPED_BAMBOO_WALL_SIGN
        );

        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            ItemStack stack = player.getStackInHand(hand);
            BlockPos pos = hit.getBlockPos();
            BlockState state = world.getBlockState(pos);

            Optional<BlockState> nu = Optional.empty();

            if (state.isOf(Blocks.FLOWERING_AZALEA) && stack.getItem() instanceof ShearsItem) {
                Block.dropStack(world, pos.up(), new ItemStack(AZALEA_FLOWERS, world.random.nextInt(2) + 1));
                world.playSound(player, pos, TwigsSoundEvents.BLOCK_FLOWERING_AZALEA_SHEAR, SoundCategory.BLOCKS, 1.0f, 1.0f);
                nu = Optional.of(Blocks.AZALEA.getDefaultState());
            } if (state.isOf(Blocks.BAMBOO) && stack.getItem() instanceof AxeItem) {
                if (!world.getBlockState(pos.up()).isOf(Blocks.BAMBOO)) {
                    int leaves = state.get(Properties.BAMBOO_LEAVES).ordinal();
                    if (leaves > 0) {
                        int drop = world.random.nextInt(leaves * (leaves + 1));
                        if (drop > 0) {
                            Block.dropStack(world, pos, new ItemStack(BAMBOO_LEAVES, drop));
                            world.playSound(player, pos, TwigsSoundEvents.BLOCK_BAMBOO_STRIP_SHEAR, SoundCategory.BLOCKS, 1.0f, 1.0f);
                        }
                    }

                    world.playSound(player, pos, SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    nu = Optional.of(STRIPPED_BAMBOO.getDefaultState().with(StrippedBambooBlock.FROM_BAMBOO, true));
                }
            }

            if (nu.isPresent()) {
                if (player instanceof ServerPlayerEntity serverPlayer) Criteria.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
                world.setBlockState(pos, nu.get(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
                stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
                return ActionResult.success(world.isClient);
            }

            return ActionResult.PASS;
        });

        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (this.equals(id, Blocks.BAMBOO)) {
                tableBuilder.pool(
                    LootPool.builder()
                            .with(
                                ItemEntry.builder(BAMBOO_LEAVES)
                                         .conditionally(
                                             InvertedLootCondition.builder(
                                                 BlockStatePropertyLootCondition.builder(Blocks.BAMBOO)
                                                                                .properties(StatePredicate.Builder.create().exactMatch(BambooBlock.LEAVES, BambooLeaves.NONE))
                                             )
                                         ).build()
                            )
                );
            } else if (this.equals(id, Blocks.GRAVEL)) {
                tableBuilder.pool(
                    LootPool.builder()
                            .with(
                                ItemEntry.builder(PEBBLE)
                                         .conditionally(InvertedLootCondition.builder(
                                             MatchToolLootCondition.builder(
                                                 ItemPredicate.Builder.create()
                                                                      .enchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, NumberRange.IntRange.ANY)))
                                         ))
                                         .conditionally(RandomChanceLootCondition.builder(0.2F))
                                         .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0F, 3.0F)))
                            ).build()
                );
            }
        });

        FlammableBlockRegistry flammableRegistry = FlammableBlockRegistry.getDefaultInstance();
        flammableRegistry.add(AZALEA_FLOWERS,30, 60);
        flammableRegistry.add(TWIG,30, 60);
        flammableRegistry.add(BAMBOO_LEAVES,30, 60);
        flammableRegistry.add(BAMBOO_THATCH,30, 60);
        flammableRegistry.add(BAMBOO_THATCH_SLAB, 30, 60);
        flammableRegistry.add(BAMBOO_THATCH_STAIRS, 30, 60);
        flammableRegistry.add(STRIPPED_BAMBOO, 5, 20);
        flammableRegistry.add(STRIPPED_BAMBOO_PLANKS, 5, 20);
        flammableRegistry.add(STRIPPED_BAMBOO_SLAB, 5, 20);
        flammableRegistry.add(STRIPPED_BAMBOO_FENCE_GATE, 5, 20);
        flammableRegistry.add(STRIPPED_BAMBOO_FENCE, 5, 20);
        flammableRegistry.add(STRIPPED_BAMBOO_STAIRS, 5, 20);

        FuelRegistry fuelRegistry = FuelRegistry.INSTANCE;
        fuelRegistry.add(STRIPPED_BAMBOO_FENCE, 300);
        fuelRegistry.add(STRIPPED_BAMBOO_FENCE_GATE, 300);

        TillableBlockRegistry.register(ROCKY_DIRT, ctx -> true, Blocks.COARSE_DIRT.getDefaultState(), PEBBLE);
        StrippableBlockRegistry.register(BUNDLED_BAMBOO, STRIPPED_BUNDLED_BAMBOO);

        LinkedHashMap<Block, Block> copperPillars = Maps.newLinkedHashMap();
        copperPillars.put(COPPER_PILLAR, WAXED_COPPER_PILLAR);
        copperPillars.put(EXPOSED_COPPER_PILLAR, WAXED_EXPOSED_COPPER_PILLAR);
        copperPillars.put(WEATHERED_COPPER_PILLAR, WAXED_WEATHERED_COPPER_PILLAR);
        copperPillars.put(OXIDIZED_COPPER_PILLAR, WAXED_OXIDIZED_COPPER_PILLAR);

        copperPillars.forEach(OxidizableBlocksRegistry::registerWaxableBlockPair);

        List<Block> unwaxedCopperPillars = List.copyOf(copperPillars.keySet());
        for (int i = 0, l = copperPillars.size() - 1; i < l; i++) OxidizableBlocksRegistry.registerOxidizableBlockPair(unwaxedCopperPillars.get(i), unwaxedCopperPillars.get(i + 1));
    }

    public boolean equals(Identifier id, Block block) {
        return id.equals(block.getLootTableId());
    }
}
