package dev.flomik.stardew.client;

import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.core.registry.block.surface.BlockDirt;
import dev.flomik.stardew.core.registry.block.surface.BlockGrassSurface;
import dev.flomik.stardew.core.registry.block.BlockFarmland;
import dev.flomik.stardew.core.time.Season;
import dev.flomik.stardew.core.time.StardewDateData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StardewMod.MODID)
public class SeasonChunkSyncHandler {

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        LevelAccessor level = event.getLevel();
        if (level.isClientSide()) return;

        Season currentSeason = Season.SPRING;
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            currentSeason = StardewDateData.get(serverLevel).getSeason();
        }

        ChunkAccess chunk = event.getChunk();
        ChunkPos chunkPos = chunk.getPos();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
                    pos.set(chunkPos.getMinBlockX() + x, y, chunkPos.getMinBlockZ() + z);
                    var state = chunk.getBlockState(pos);

                    // grass
                    if (state.getBlock() instanceof BlockGrassSurface grass) {
                        Season oldSeason = state.getValue(BlockGrassSurface.SEASON);
                        if (oldSeason != currentSeason) {
                            var newState = state
                                    .setValue(BlockGrassSurface.SEASON, currentSeason)
                                    .setValue(BlockGrassSurface.SHAPE, grass.calculateShape(level, pos));
                            chunk.setBlockState(pos, newState, false);
                        }
                    }

                    // dirt
                    if (state.getBlock() instanceof BlockDirt dirt) {
                        Season oldSeason = state.getValue(BlockDirt.SEASON);
                        if (oldSeason != currentSeason) {
                            var newState = state.setValue(BlockDirt.SEASON, currentSeason);
                            chunk.setBlockState(pos, newState, false);
                        }
                    }

                    // farmland
                    if (state.getBlock() instanceof BlockFarmland farmland) {
                        Season oldSeason = state.getValue(BlockFarmland.SEASON);
                        if (oldSeason != currentSeason) {
                            var newState = state
                                    .setValue(BlockFarmland.SEASON, currentSeason)
                                    .setValue(BlockFarmland.SHAPE, farmland.calculateShape(level, pos))
                                    .setValue(BlockFarmland.WET_SHAPE, farmland.calculateWetShape(level, pos));
                            chunk.setBlockState(pos, newState, false);
                        }
                    }
                }
            }
        }
    }

    public static void syncChunk(LevelAccessor level, ChunkAccess chunk, Season currentSeason) {
        ChunkPos chunkPos = chunk.getPos();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
                    pos.set(chunkPos.getMinBlockX() + x, y, chunkPos.getMinBlockZ() + z);
                    BlockState state = chunk.getBlockState(pos);

                    if (state.getBlock() instanceof BlockGrassSurface grass) {
                        if (state.getValue(BlockGrassSurface.SEASON) != currentSeason) {
                            chunk.setBlockState(pos, state
                                            .setValue(BlockGrassSurface.SEASON, currentSeason)
                                            .setValue(BlockGrassSurface.SHAPE, grass.calculateShape(level, pos)),
                                    false);
                        }
                    } else if (state.getBlock() instanceof BlockDirt dirt) {
                        if (state.getValue(BlockDirt.SEASON) != currentSeason) {
                            chunk.setBlockState(pos, state.setValue(BlockDirt.SEASON, currentSeason), false);
                        }
                    } else if (state.getBlock() instanceof BlockFarmland farmland) {
                        if (state.getValue(BlockFarmland.SEASON) != currentSeason) {
                            chunk.setBlockState(pos, state
                                    .setValue(BlockFarmland.SEASON, currentSeason)
                                    .setValue(BlockFarmland.SHAPE, farmland.calculateShape(level, pos))
                                    .setValue(BlockFarmland.WET_SHAPE, farmland.calculateWetShape(level, pos)), false);
                        }
                    }
                }
            }
        }
    }

    public static void reloadWorldSeason(ServerLevel level, Season currentSeason) {
        ServerChunkCache cache = level.getChunkSource();
        int radius = level.getServer().getPlayerList().getViewDistance();

        for (ServerPlayer player : level.players()) {
            ChunkPos center = player.chunkPosition();

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int cx = center.x + dx;
                    int cz = center.z + dz;

                    LevelChunk chunk = cache.getChunkNow(cx, cz);
                    if (chunk == null) continue;

                    syncChunk(level, chunk, currentSeason);

                    ClientboundLevelChunkWithLightPacket packet =
                            new ClientboundLevelChunkWithLightPacket(chunk, level.getLightEngine(), null, null);

                    player.connection.send(packet);
                }
            }
        }
    }
}