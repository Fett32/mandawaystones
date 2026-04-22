package me.fett.mandawaystones.mixin;

import net.blay09.mods.waystones.api.Waystone;
import net.blay09.mods.waystones.api.WaystoneOrigin;
import net.blay09.mods.waystones.core.PersistentPlayerWaystoneData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;
import java.util.UUID;

/**
 * Server-side path: the waystones mod uses PersistentPlayerWaystoneData on
 * dedicated servers (InMemory is client-only). New player activations end up
 * appended to the sortingIndex inside ensureSortingIndex via NbtList.add. We
 * redirect that add so PLAYER-origin waystones go to index 0 instead.
 */
@Mixin(PersistentPlayerWaystoneData.class)
public abstract class PersistentPlayerWaystoneDataMixin {

    @Redirect(
        method = "ensureSortingIndex",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/nbt/NbtList;add(Ljava/lang/Object;)Z"
        )
    )
    private boolean mandawaystones$insertPlayerWaystoneAtTop(
        NbtList nbtList,
        Object element,
        PlayerEntity player,
        Collection<Waystone> waystones
    ) {
        NbtString nbtString = (NbtString) element;
        UUID uuid;
        try {
            uuid = UUID.fromString(nbtString.asString());
        } catch (IllegalArgumentException e) {
            return nbtList.add(nbtString);
        }

        for (Waystone waystone : waystones) {
            if (waystone.getWaystoneUid().equals(uuid)) {
                if (waystone.getOrigin() == WaystoneOrigin.PLAYER) {
                    nbtList.add(0, nbtString);
                    return true;
                }
                break;
            }
        }
        return nbtList.add(nbtString);
    }
}
