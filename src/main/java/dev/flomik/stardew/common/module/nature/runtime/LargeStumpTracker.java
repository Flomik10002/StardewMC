package dev.flomik.stardew.common.module.nature.runtime;

import dev.flomik.stardew.common.module.nature.blockentity.LargeStumpBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class LargeStumpTracker {
    private static final Map<ServerLevel, Set<BlockPos>> MAP = new WeakHashMap<>();

    public static void onLoad(LargeStumpBlockEntity be) {
        if (be.getLevel() instanceof ServerLevel sl) {
            MAP.computeIfAbsent(sl, k -> new HashSet<>()).add(be.getBlockPos().immutable());
        }
    }

    public static void onRemove(LargeStumpBlockEntity be) {
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
