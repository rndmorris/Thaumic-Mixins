package xyz.uniblood.thaumicmixins.mixins.late;

import net.minecraft.init.Blocks;
import net.minecraft.util.ChunkCoordinates;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.crafting.IInfusionStabiliser;
import thaumcraft.common.tiles.TileInfusionMatrix;
import xyz.uniblood.thaumicmixins.ThaumicMixins;

import java.util.ArrayList;

@Mixin(value = TileInfusionMatrix.class, remap = false)
public class MixinTileInfusionMatrix extends TileThaumcraft {

    @Shadow
    public int symmetry = 0;

    @Inject(
        method = "getSurroundings",
        at = @At(
            target = "Ljava/util/ArrayList;iterator()Ljava/util/Iterator;",
            ordinal = 0,
            value = "INVOKE"
        ),
        cancellable = true,
        remap = false,
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void calculateStabilizers(CallbackInfo ci, ArrayList<ChunkCoordinates> stuff) {

        var symmetry = 0;
        for (var coordinate: stuff) {
            if (thaumic_Mixins$isStabilizer(coordinate.posX, coordinate.posY, coordinate.posZ)) {
                symmetry += 1;
            }
            if (thaumic_Mixins$isStabilizer(xCoord + xCoord - coordinate.posX, coordinate.posY, zCoord + zCoord - coordinate.posZ)) {
                symmetry -= 2;
            }
        }
        this.symmetry += (symmetry / 10);
        ci.cancel();
    }

    @Unique
    private boolean thaumic_Mixins$isStabilizer(int x, int y, int z) {
        final var block = worldObj.getBlock(x, y, z);
        final var result =  block == Blocks.skull || (block instanceof IInfusionStabiliser stabiliser && stabiliser.canStabaliseInfusion(worldObj, x, y, z));
        ThaumicMixins.LOG.info("{} at ({}, {}, {}) is stabilizer? {}", block.getLocalizedName(), x, y, z, result);
        return result;
    }

}
