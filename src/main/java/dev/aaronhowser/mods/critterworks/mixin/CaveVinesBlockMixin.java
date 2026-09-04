package dev.aaronhowser.mods.critterworks.mixin;

import dev.aaronhowser.mods.critterworks.world.DyeberryVineReplacement;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.CaveVinesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CaveVinesBlock.class)
public abstract class CaveVinesBlockMixin {

	@Inject(method = "getGrowIntoState", at = @At("RETURN"), cancellable = true)
	private void critterworks$replaceDyeberryVine(
		BlockState state,
		RandomSource random,
		CallbackInfoReturnable<BlockState> callback
	) {
		callback.setReturnValue(DyeberryVineReplacement.replace(callback.getReturnValue(), random));
	}
}