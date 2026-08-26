package dev.aaronhowser.mods.critter_carts.mixin;

import dev.aaronhowser.mods.critter_carts.block.DyeberryVinesBlock;
import dev.aaronhowser.mods.critter_carts.block.DyeberryVinesPlantBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GrowingPlantBlock.class)
public abstract class GrowingPlantBlockMixin {

	@Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
	private void critterCarts$attachToDyeberryVine(
		BlockState state,
		LevelReader level,
		BlockPos position,
		CallbackInfoReturnable<Boolean> callback
	) {
		if (!state.is(Blocks.CAVE_VINES) && !state.is(Blocks.CAVE_VINES_PLANT)) return;

		BlockState supportState = level.getBlockState(position.above());
		if (supportState.getBlock() instanceof DyeberryVinesBlock
			|| supportState.getBlock() instanceof DyeberryVinesPlantBlock) {
			callback.setReturnValue(true);
		}
	}
}