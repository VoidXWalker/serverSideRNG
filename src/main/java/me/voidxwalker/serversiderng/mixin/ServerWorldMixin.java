package me.voidxwalker.serversiderng.mixin;

import me.voidxwalker.serversiderng.RNGHandler;
import me.voidxwalker.serversiderng.ServerSideRng;
import net.minecraft.server.world.ServerWorld;
import java.util.Random;
import java.util.function.Supplier;

import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.ServerWorldProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {
    private int serverSideRNG_savedThunderTime;
    @Shadow @Final private ServerWorldProperties worldProperties;

    protected ServerWorldMixin(MutableWorldProperties mutableWorldProperties, RegistryKey<World> registryKey, RegistryKey<DimensionType> registryKey2, DimensionType dimensionType, Supplier<Profiler> profiler, boolean bl, boolean bl2, long l) {
        super(mutableWorldProperties, registryKey, registryKey2, dimensionType, profiler, bl, bl2, l);
    }
    /**
     * Obtains the first generated random {@code Integer}. This saved value is then later used in the {@link ServerWorldMixin#modifyThunderRandom(CallbackInfo)} method to decide whether the thunder time has been updated.
     * @author Void_X_Walker
     */
    @Redirect(method = "tick",at = @At(value = "INVOKE",target = "Ljava/util/Random;nextInt(I)I"))
    public int getRandom(Random instance,int i){
        int returnValue=instance.nextInt();
        if(ServerSideRng.inSpeedrun()){
            if(serverSideRNG_savedThunderTime ==0){
                if(i ==12000){
                    serverSideRNG_savedThunderTime =returnValue+3600;
                }
                else {
                    serverSideRNG_savedThunderTime =returnValue+12000;
                }
            }
        }
        return returnValue;
    }
    /**
     * Uses the from {@link RNGHandler#getRngValue(RNGHandler.RNGTypes)} obtained random {@code Long}, that has been generated by the verification server, as a seed for the {@link RNGHandler.RNGTypes#THUNDER} RNG.
     * @author Void_X_Walker
     */
    @Inject(method = "tick",at = @At(value = "INVOKE",target = "Lnet/minecraft/world/level/ServerWorldProperties;setRaining(Z)V"))
    public void modifyThunderRandom(CallbackInfo ci){
        if(ServerSideRng.inSpeedrun()){
            if(this.worldProperties.getThunderTime()== serverSideRNG_savedThunderTime){
                Random random=new Random(ServerSideRng.currentSpeedrun.getCurrentRNGHandler().getRngValue(RNGHandler.RNGTypes.THUNDER));
                int j =this.worldProperties.isThundering() ? random.nextInt(12000) + 3600 : random.nextInt(168000) + 12000;
                this.worldProperties.setThunderTime(j);
            }
            serverSideRNG_savedThunderTime =0;
        }
    }
}
