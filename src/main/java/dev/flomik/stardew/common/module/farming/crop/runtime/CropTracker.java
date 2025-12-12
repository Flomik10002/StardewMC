package dev.flomik.stardew.common.module.farming.crop.runtime;

import dev.flomik.stardew.common.module.farming.crop.blockentity.CropBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.*;

public final class CropTracker {
    private static final Map<ServerLevel, Set<BlockPos>> MAP = new WeakHashMap<>();

    public static void onLoad(CropBlockEntity be) {
        if (be.getLevel() instanceof ServerLevel sl) {
            MAP.computeIfAbsent(sl, k -> new HashSet<>()).add(be.getBlockPos().immutable());
        }
    }
    public static void onRemove(CropBlockEntity be) {
        if (be.getLevel() instanceof ServerLevel sl) {
            var set = MAP.get(sl);
            if (set != null) set.remove(be.getBlockPos());
        }
    }
    public static Iterable<BlockPos> all(ServerLevel sl) {
        var set = MAP.get(sl);
        return set != null ? set : Set.of();
    }
}