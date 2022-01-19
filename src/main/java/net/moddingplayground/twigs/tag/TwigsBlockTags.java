package net.moddingplayground.twigs.tag;

import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.block.Block;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.moddingplayground.twigs.Twigs;

public class TwigsBlockTags {
    public static final Tag.Identified<Block> TABLES = register("tables");
    public static final Tag.Identified<Block> PAPER_LANTERNS = register("paper_lanterns");

    private static Tag.Identified<Block> register(String id) {
        return TagFactory.BLOCK.create(new Identifier(Twigs.MOD_ID, id));
    }
}
