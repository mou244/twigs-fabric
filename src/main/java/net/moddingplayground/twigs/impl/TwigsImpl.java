package net.moddingplayground.twigs.impl;

import com.google.common.reflect.Reflection;
import net.fabricmc.api.ModInitializer;
import net.moddingplayground.frame.api.util.InitializationLogger;
import net.moddingplayground.twigs.api.Twigs;
import net.moddingplayground.twigs.api.block.TwigsBlocks;
import net.moddingplayground.twigs.api.block.TwigsSignTypes;
import net.moddingplayground.twigs.api.entity.TwigsEntityType;
import net.moddingplayground.twigs.api.item.TwigsItemGroups;
import net.moddingplayground.twigs.api.item.TwigsItems;
import net.moddingplayground.twigs.api.particle.TwigsParticleTypes;
import net.moddingplayground.twigs.api.sound.TwigsSoundEvents;
import net.moddingplayground.twigs.api.world.gen.feature.TwigsConfiguredFeatures;
import net.moddingplayground.twigs.api.world.gen.feature.TwigsPlacedFeatures;

public final class TwigsImpl implements Twigs, ModInitializer {
    private final InitializationLogger initializer;

    public TwigsImpl() {
        this.initializer = new InitializationLogger(LOGGER, MOD_NAME);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void onInitialize() {
        this.initializer.start();

        Reflection.initialize(
            TwigsSoundEvents.class, TwigsItemGroups.class, TwigsSignTypes.class,
            TwigsBlocks.class, TwigsItems.class, TwigsEntityType.class,
            TwigsConfiguredFeatures.class, TwigsPlacedFeatures.class, TwigsParticleTypes.class
        );

        this.initializer.finish();
    }
}
