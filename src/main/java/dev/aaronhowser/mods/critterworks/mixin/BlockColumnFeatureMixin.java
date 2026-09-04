package dev.aaronhowser.mods.critterworks.mixin;

import dev.aaronhowser.mods.critterworks.world.DyeberryVineReplacement;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.BlockColumnFeature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockColumnFeature.class)
public abstract class BlockColumnFeatureMixin {

	@Redirect(
		method = "place",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/WorldGenLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"
		)
	)
	private boolean critterworks$replaceDyeberryVine(
		WorldGenLevel level,
		BlockPos position,
		BlockState state,
		int flags,
		FeaturePlaceContext<BlockColumnConfiguration> context
	) {
		RandomSource random = context.random();
		return level.setBlock(position, DyeberryVineReplacement.replace(state, random), flags);
	}
}