package dev.flomik.stardew.client;

import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.core.registry.block.surface.BlockDirt;
import dev.flomik.stardew.core.registry.block.surface.BlockFarmland;
import dev.flomik.stardew.core.registry.block.surface.BlockGrassFull;
import dev.flomik.stardew.core.registry.block.surface.BlockGrassSurface;
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
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = StardewMod.MODID)
public class SeasonChunkSyncHandler {

    private record PendingChunk(ServerLevel level, ChunkPos pos) {}
    private static final Set<PendingChunk> PENDING_SYNC = ConcurrentHashMap.newKeySet();
    private static final Set<ChunkPos> CHECKED_CHUNKS = ConcurrentHashMap.newKeySet();
    private static int tickCounter = 0;
    private static final int CHECK_INTERVAL = 10;
    private static final int CHUNKS_PER_TICK = 50;

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        
        LevelChunk chunk = (LevelChunk) event.getChunk();
        
        if (needsSync(level, chunk)) {
            PENDING_SYNC.add(new PendingChunk(level, chunk.getPos()));
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        
        if (!PENDING_SYNC.isEmpty()) {
            var iterator = PENDING_SYNC.iterator();
            int processed = 0;
            
            while (iterator.hasNext() && processed < CHUNKS_PER_TICK) {
                PendingChunk pending = iterator.next();
                ServerLevel level = pending.level();
                ChunkPos chunkPos = pending.pos();
                
                ServerChunkCache cache = level.getChunkSource();
                LevelChunk chunk = cache.getChunkNow(chunkPos.x, chunkPos.z);
                
                if (chunk != null) {
                    Season currentSeason = StardewDateData.get(level).getSeason();
                    syncChunk(level, chunk, currentSeason);
                    sendChunkUpdate(level, chunk);
                }
                
                iterator.remove();
                processed++;
            }
        }
        
        tickCounter++;
        if (tickCounter >= CHECK_INTERVAL) {
            tickCounter = 0;
            checkLoadedChunks();
        }
    }
    
    private static boolean needsSync(ServerLevel level, LevelChunk chunk) {
        Season currentSeason = StardewDateData.get(level).getSeason();
        ChunkPos chunkPos = chunk.getPos();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        
        for (int x = 0; x < 16; x += 2) {
            for (int z = 0; z < 16; z += 2) {
                int wx = chunkPos.getMinBlockX() + x;
                int wz = chunkPos.getMinBlockZ() + z;
                int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                
                pos.set(wx, surfaceY, wz);
                BlockState state = chunk.getBlockState(pos);
                
                if (state.getBlock() instanceof BlockGrassSurface) {
                    if (state.getValue(BlockGrassSurface.SEASON) != currentSeason) {
                        return true;
                    }
                } else if (state.getBlock() instanceof BlockGrassFull) {
                    if (state.getValue(BlockGrassFull.SEASON) != currentSeason) {
                        return true;
                    }
                } else if (state.getBlock() instanceof BlockDirt) {
                    if (state.getValue(BlockDirt.SEASON) != currentSeason) {
                        return true;
                    }
                } else if (state.getBlock() instanceof BlockFarmland) {
                    if (state.getValue(BlockFarmland.SEASON) != currentSeason) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    private static void checkLoadedChunks() {
        for (ServerLevel level : net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer().getAllLevels()) {
            ServerChunkCache cache = level.getChunkSource();
            int radius = level.getServer().getPlayerList().getViewDistance();
            Season currentSeason = StardewDateData.get(level).getSeason();
            int processed = 0;
            
            for (ServerPlayer player : level.players()) {
                if (processed >= CHUNKS_PER_TICK) break;
                
                ChunkPos center = player.chunkPosition();
                
                for (int dx = -radius; dx <= radius && processed < CHUNKS_PER_TICK; dx++) {
                    for (int dz = -radius; dz <= radius && processed < CHUNKS_PER_TICK; dz++) {
                        int cx = center.x + dx;
                        int cz = center.z + dz;
                        
                        LevelChunk chunk = cache.getChunkNow(cx, cz);
                        if (chunk == null) continue;
                        
                        ChunkPos chunkPos = chunk.getPos();
                        if (CHECKED_CHUNKS.contains(chunkPos)) continue;
                        
                        if (needsSync(level, chunk)) {
                            syncChunk(level, chunk, currentSeason);
                            sendChunkUpdate(level, chunk);
                            processed++;
                        }
                        
                        CHECKED_CHUNKS.add(chunkPos);
                    }
                }
            }
            
            CHECKED_CHUNKS.clear();
        }
    }
    
    private static void sendChunkUpdate(ServerLevel level, LevelChunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        int radius = level.getServer().getPlayerList().getViewDistance();
        
        for (ServerPlayer player : level.players()) {
            ChunkPos playerChunk = player.chunkPosition();
            int dx = Math.abs(chunkPos.x - playerChunk.x);
            int dz = Math.abs(chunkPos.z - playerChunk.z);
            
            if (dx <= radius && dz <= radius) {
                ClientboundLevelChunkWithLightPacket packet =
                        new ClientboundLevelChunkWithLightPacket(chunk, level.getLightEngine(), null, null);
                player.connection.send(packet);
            }
        }
    }

    public static void syncChunk(LevelAccessor level, ChunkAccess chunk, Season currentSeason) {
        ChunkPos chunkPos = chunk.getPos();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int wx = chunkPos.getMinBlockX() + x;
                int wz = chunkPos.getMinBlockZ() + z;

                int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);

                for (int y = Math.max(level.getMinBuildHeight(), surfaceY - 32); y <= surfaceY; y++) {
                    pos.set(wx, y, wz);
                    BlockState state = chunk.getBlockState(pos);

                    if (state.getBlock() instanceof BlockGrassSurface grass) {
                        if (state.getValue(BlockGrassSurface.SEASON) != currentSeason) {
                            chunk.setBlockState(pos, state
                                    .setValue(BlockGrassSurface.SEASON, currentSeason)
                                    .setValue(BlockGrassSurface.SHAPE, grass.calculateShape(level, pos)), false);
                        }
                    } else if (state.getBlock() instanceof BlockGrassFull grass) {
                        if (state.getValue(BlockGrassFull.SEASON) != currentSeason) {
                            chunk.setBlockState(pos, state.setValue(BlockGrassFull.SEASON, currentSeason), false);
                        }
                    }
                    else if (state.getBlock() instanceof BlockDirt dirt) {
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