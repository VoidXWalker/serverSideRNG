package me.voidxwalker.serversiderng.mixin.rng_verification;

import me.voidxwalker.serversiderng.RNGHandler;
import me.voidxwalker.serversiderng.RNGSession;
import net.minecraft.world.gen.GeneratorOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.OptionalLong;
import java.util.function.Supplier;

@Mixin(GeneratorOptions.class)
public class GeneratorOptionsMixin {
    @Mutable
    @Shadow @Final private long seed;
    /**
     * Uses the from {@link  RNGSession#getRngContext(RNGHandler.RNGTypes))} obtained random {@code Long}, that has been generated by the {@code Verification-Server}, for the {@link RNGHandler.RNGTypes#WORLD_SEED}.
     * Modifies the getField instead of just the parameter to ensure that other fabric mods, notably <a href="https://github.com/RedLime/SpeedRunIGT">SpeedRunIGT</a>, can still classify the world as "random seed".
     * @author Void_X_Walker
     * @see  RNGSession#getRngContext(RNGHandler.RNGTypes)
     */
    @Redirect(method = "withHardcore", at = @At(value = "INVOKE",target = "Ljava/util/OptionalLong;orElse(J)J"))
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private long modifySeedRandom(OptionalLong instance, long other) {
        RNGSession.getRngContext(RNGHandler.RNGTypes.WORLD_SEED)
            .map(Supplier::get)
            .ifPresent((it) -> this.seed = it);
        return instance.orElse(this.seed);
    }

    /**
     * Ensures that the {@link GeneratorOptions} use a {@link net.minecraft.world.gen.chunk.ChunkGenerator} created with the seed modified via {@link GeneratorOptionsMixin#modifySeedRandom(OptionalLong, long)}.
     * @author Void_X_Walker
     */
    @Redirect(
            method = "withHardcore",
            at = @At(value = "INVOKE", target = "Ljava/util/OptionalLong;isPresent()Z", ordinal = 0),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Ljava/util/OptionalLong;orElse(J)J", shift = At.Shift.AFTER),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/GeneratorOptions;isDebugWorld()Z", shift = At.Shift.BEFORE)
            )
    )
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private boolean ensureSeedConsistency(OptionalLong instance) {
        System.out.println("ensureSeedConsistency"+(RNGSession.inSession() || instance.isPresent()));
        return RNGSession.inSession() || instance.isPresent();
    }
    /**
     * Ensures that the {@link GeneratorOptions} use a {@link net.minecraft.world.gen.chunk.ChunkGenerator} created with the seed modified via {@link GeneratorOptionsMixin#modifySeedRandom(OptionalLong, long)}.
     * @author Void_X_Walker
     */
    @Redirect(
        method = "withHardcore",
        at = @At(value = "INVOKE", target = "Ljava/util/OptionalLong;getAsLong()J", ordinal = 0),
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Ljava/util/OptionalLong;orElse(J)J", shift = At.Shift.AFTER),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/GeneratorOptions;isDebugWorld()Z", shift = At.Shift.BEFORE)
        )
    )
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private long ensureSeedConsistency2(OptionalLong instance) {
        System.out.println("ensureSeedConsistency2"+( instance.isPresent()));
        return instance.orElse(this.seed);
    }
}
