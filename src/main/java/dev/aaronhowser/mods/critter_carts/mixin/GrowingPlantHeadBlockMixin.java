package dev.aaronhowser.mods.critter_carts.mixin;

import dev.aaronhowser.mods.critter_carts.block.DyeberryVinesBlock;
import dev.aaronhowser.mods.critter_carts.block.DyeberryVinesPlantBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GrowingPlantHeadBlock.class)
public abstract class GrowingPlantHeadBlockMixin {

	@Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
	private void critterCarts$connectDyeberryVine(
		BlockState state,
		Direction direction,
		BlockState neighborState,
		LevelAccessor level,
		BlockPos position,
		BlockPos neighborPosition,
		CallbackInfoReturnable<BlockState> callback
	) {
		if (!state.is(Blocks.CAVE_VINES) || direction != Direction.DOWN) return;
		if (!(neighborState.getBlock() instanceof DyeberryVinesBlock)
			&& !(neighborState.getBlock() instanceof DyeberryVinesPlantBlock)) return;

		BlockState bodyState = Blocks.CAVE_VINES_PLANT
			.defaultBlockState()
			.setValue(CaveVines.BERRIES, state.getValue(CaveVines.BERRIES));
		callback.setReturnValue(bodyState);
	}
}