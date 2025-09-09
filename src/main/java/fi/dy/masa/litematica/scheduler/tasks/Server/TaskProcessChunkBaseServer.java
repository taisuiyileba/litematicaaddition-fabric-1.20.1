package fi.dy.masa.litematica.scheduler.tasks.Server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import com.google.common.collect.ArrayListMultimap;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import fi.dy.masa.malilib.util.IntBoundingBox;
import fi.dy.masa.malilib.util.LayerMode;
import fi.dy.masa.malilib.util.LayerRange;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.malilib.util.WorldUtils;
import fi.dy.masa.litematica.render.infohud.InfoHud;
import fi.dy.masa.litematica.selection.Box;
import fi.dy.masa.litematica.util.PositionUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;

public abstract class TaskProcessChunkBaseServer extends TaskBaseServer
{
    protected final ArrayListMultimap<ChunkPos, IntBoundingBox> boxesInChunks = ArrayListMultimap.create();
    protected final ArrayList<ChunkPos> pendingChunks = new ArrayList<>();
    protected final ServerWorld serverWorld;
    protected final World world;
    protected final boolean isServerWorld;
    protected PositionUtils.ChunkPosComparator comparator = new PositionUtils.ChunkPosComparator();

    protected TaskProcessChunkBaseServer(ServerPlayerEntity player)
    {
        super(player);
        this.serverWorld = this.player.getServerWorld();
        this.world = this.player.getWorld();
        this.isServerWorld = (this.world == this.serverWorld);
        this.comparator.setClosestFirst(true);
    }

    @Override
    public boolean execute()
    {
        return this.executeForAllPendingChunks();
    }

    @Override
    public void stop()
    {
        // Multiplayer, just a client world
        if (this.isServerWorld)
        {
            this.onStop();
        }
        // Single player, operating in the integrated server world
        else
        {
            this.mc.execute(this::onStop);
        }
    }

    protected void onStop()
    {
        this.notifyListener();
    }

    protected abstract boolean canProcessChunk(ChunkPos pos);

    protected boolean processChunk(ChunkPos pos)
    {
        return true;
    }

    protected boolean executeForAllPendingChunks()
    {
        Iterator<ChunkPos> iterator = this.pendingChunks.iterator();
        int processed = 0;

        while (iterator.hasNext())
        {
            ChunkPos pos = iterator.next();

            if (this.canProcessChunk(pos) && this.processChunk(pos))
            {
                iterator.remove();
                ++processed;
            }
        }

        if (processed > 0)
        {
            this.updateInfoHudLinesPendingChunks(this.pendingChunks);
        }

        this.finished = this.pendingChunks.isEmpty();

        return this.finished;
    }

    protected void addPerChunkBoxes(Collection<Box> allBoxes)
    {
        this.addPerChunkBoxes(allBoxes, new LayerRange(null));
    }

    protected void addPerChunkBoxes(Collection<Box> allBoxes, LayerRange range)
    {
        this.boxesInChunks.clear();
        this.pendingChunks.clear();

        if (range.getLayerMode() == LayerMode.ALL)
        {
            PositionUtils.getPerChunkBoxes(allBoxes, this::clampToWorldHeightAndAddBox);
        }
        else
        {
            PositionUtils.getLayerRangeClampedPerChunkBoxes(allBoxes, range, this::clampToWorldHeightAndAddBox);
        }

        this.pendingChunks.addAll(this.boxesInChunks.keySet());

        this.sortChunkList();
    }

    protected void clampToWorldHeightAndAddBox(ChunkPos pos, IntBoundingBox box)
    {
        box = PositionUtils.clampBoxToWorldHeightRange(box, this.serverWorld);

        if (box != null)
        {
            this.boxesInChunks.put(pos, box);
        }
    }

    protected List<IntBoundingBox> getBoxesInChunk(ChunkPos pos)
    {
        return this.boxesInChunks.get(pos);
    }

    protected void sortChunkList()
    {
        if (this.pendingChunks.size() > 0)
        {
            if (this.player != null)
            {
                this.comparator.setReferencePosition(this.player.getBlockPos());
                this.pendingChunks.sort(this.comparator);
            }

            this.updateInfoHudLines();
            this.onChunkListSorted();
        }
    }

    protected void onChunkListSorted()
    {
    }

    protected void updateInfoHudLines()
    {
        this.updateInfoHudLinesPendingChunks(this.pendingChunks);
    }
}
