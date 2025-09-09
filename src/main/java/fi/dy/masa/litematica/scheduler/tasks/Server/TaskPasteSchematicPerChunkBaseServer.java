package fi.dy.masa.litematica.scheduler.tasks.Server;

import java.util.Collection;
import java.util.Set;
import com.google.common.collect.ImmutableList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkPos;
import fi.dy.masa.malilib.util.IntBoundingBox;
import fi.dy.masa.malilib.util.LayerRange;
import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.util.PositionUtils;
import fi.dy.masa.litematica.util.ReplaceBehavior;

public abstract class TaskPasteSchematicPerChunkBaseServer extends TaskProcessChunkMultiPhaseServer
{
    protected final ImmutableList<SchematicPlacement> placements;
    protected final LayerRange layerRange;
    protected final ReplaceBehavior replace;
    protected final boolean changedBlockOnly;
    protected boolean ignoreBlocks;
    protected boolean ignoreEntities;

    public TaskPasteSchematicPerChunkBaseServer(Collection<SchematicPlacement> placements,
                                                LayerRange range,
                                                boolean changedBlocksOnly, ServerPlayerEntity player, ReplaceBehavior replace)
    {
        super(player);
        this.placements = ImmutableList.copyOf(placements);
        this.layerRange = range;
        this.changedBlockOnly = changedBlocksOnly;
        this.replace = replace;
    }

    @Override
    public void init()
    {
        for (SchematicPlacement placement : this.placements)
        {
            this.addPlacement(placement, this.layerRange);
        }
        player.sendMessage(Text.of("this.pendingChunks.size()="+ this.pendingChunks.size()));
        this.pendingChunks.clear();
        this.pendingChunks.addAll(this.boxesInChunks.keySet());
        player.sendMessage(Text.of("this.pendingChunks.size()="+ this.pendingChunks.size()));
        this.sortChunkList();
    }

    @Override
    public boolean canExecute()
    {
        return super.canExecute();
    }

    protected void addPlacement(SchematicPlacement placement, LayerRange range)
    {
        Set<ChunkPos> touchedChunks = placement.getTouchedChunks();


        for (ChunkPos pos : touchedChunks)
        {
            int count = 0;

            for (IntBoundingBox box : placement.getBoxesWithinChunk(pos.x, pos.z).values())
            {
                box = PositionUtils.getClampedBox(box, range);
                player.sendMessage(Text.of("box = " + box));
                if (box != null)
                {
                    // Clamp the box to the world bounds.
                    // This is also important for the fill-based strip generation code to not
                    // overflow the work array bounds.
                    box = PositionUtils.clampBoxToWorldHeightRange(box, this.serverWorld);
                    player.sendMessage(Text.of("box = " + box));
                    if (box != null)
                    {
                        this.boxesInChunks.put(pos, box);
                        ++count;
                    }
                }
            }

            if (count > 0)
            {
                this.onChunkAddedForHandling(pos, placement);
            }
        }
    }

    protected void onChunkAddedForHandling(ChunkPos pos, SchematicPlacement placement)
    {
    }

    @Override
    protected boolean canProcessChunk(ChunkPos pos)
    {
        // Chunk exists in the schematic world, and all the surrounding chunks are loaded in the client world, good to go
        return this.areSurroundingChunksLoaded(pos, this.serverWorld, 1);
    }
}
