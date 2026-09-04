package dev.aaronhowser.mods.critterworks.mixin;

import dev.aaronhowser.mods.critterworks.block.DyeberryVinesBlock;
import dev.aaronhowser.mods.critterworks.block.DyeberryVinesPlantBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantBodyBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GrowingPlantBodyBlock.class)
public abstract class GrowingPlantBodyBlockMixin {

	@Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
	private void critterworks$connectDyeberryVine(
		BlockState state,
		Direction direction,
		BlockState neighborState,
		LevelAccessor level,
		BlockPos position,
		BlockPos neighborPosition,
		CallbackInfoReturnable<BlockState> callback
	) {
		if (!state.is(Blocks.CAVE_VINES_PLANT) || direction != Direction.DOWN) return;
		if (!(neighborState.getBlock() instanceof DyeberryVinesBlock)
			&& !(neighborState.getBlock() instanceof DyeberryVinesPlantBlock)) return;

		callback.setReturnValue(state);
	}
}