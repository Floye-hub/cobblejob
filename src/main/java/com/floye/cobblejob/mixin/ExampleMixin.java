package com.floye.cobblejob.mixin;

import com.floye.cobblejob.CobbleJob;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class ExampleMixin {
	private CobbleJob cobbleJob;

	@Inject(at = @At("HEAD"), method = "loadWorld")
	private void init(CallbackInfo info) {
		this.cobbleJob = new CobbleJob();
	}

	public CobbleJob getCobbleJob() {
		return this.cobbleJob;
	}
}