package com.github.flandre923.berrypouch.neoforge.mixinsa;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShapedRecipe.class)
public interface ShapedRecipeMixin {
    @Accessor("pattern")
    ShapedRecipePattern getPatternAccessor();

    @Accessor("result")
    ItemStack getResultAccess();
}
