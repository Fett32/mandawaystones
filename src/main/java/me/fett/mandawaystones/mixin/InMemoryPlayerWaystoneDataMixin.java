package me.fett.mandawaystones.mixin;

import net.blay09.mods.waystones.api.Waystone;
import net.blay09.mods.waystones.api.WaystoneOrigin;
import net.blay09.mods.waystones.core.InMemoryPlayerWaystoneData;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.UUID;

@Mixin(InMemoryPlayerWaystoneData.class)
public abstract class InMemoryPlayerWaystoneDataMixin {

    /**
     * Redirects sortingIndex.add(uuid) inside activateWaystone. For PLAYER-origin
     * waystones, inserts at index 0 (top of sort list) instead of appending.
     * Wild/village/dungeon/unknown origins keep vanilla append behavior so the
     * existing 223 auto-named world-gen waystones don't churn through players'
     * sorted lists.
     */
    @Redirect(
        method = "activateWaystone",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
        )
    )
    private boolean mandawaystones$insertPlayerWaystoneAtTop(
        List<UUID> sortingIndex,
        Object uuid,
        PlayerEntity player,
        Waystone waystone
    ) {
        if (waystone.getOrigin() == WaystoneOrigin.PLAYER) {
            sortingIndex.add(0, (UUID) uuid);
            return true;
        }
        return sortingIndex.add((UUID) uuid);
    }
}
