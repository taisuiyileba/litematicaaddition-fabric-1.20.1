package fi.dy.masa.litematica.scheduler.tasks.Server;

import com.google.common.collect.ArrayListMultimap;
import fi.dy.masa.litematica.render.infohud.InfoHud;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.util.ReplaceBehavior;
import fi.dy.masa.litematica.util.SchematicPlacingUtils;
import fi.dy.masa.malilib.gui.Message.MessageType;
import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.malilib.util.LayerRange;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.Collection;

public class TaskPasteSchematicPerChunkDirectServer extends TaskPasteSchematicPerChunkBaseServer
{
    private final ArrayListMultimap<ChunkPos, SchematicPlacement> placementsPerChunk = ArrayListMultimap.create();


    public TaskPasteSchematicPerChunkDirectServer(Collection<SchematicPlacement> placements, LayerRange range, boolean changedBlocksOnly, ServerPlayerEntity player, ReplaceBehavior replace)
    {
        super(placements, range, changedBlocksOnly,player,replace);
    }

    @Override
    public boolean canExecute()
    {
        return super.canExecute() &&
               this.world != null && this.world.isClient == false;
    }

    @Override
    protected void onChunkAddedForHandling(ChunkPos pos, SchematicPlacement placement)
    {
        super.onChunkAddedForHandling(pos, placement);

        this.placementsPerChunk.put(pos, placement);
    }

    @Override
    public boolean execute()
    {
        // Nothing to do
        if (this.ignoreBlocks && this.ignoreEntities)
        {
            return true;
        }
        MinecraftServer server = this.mc;
        final long vanillaTickTime = server.lastTickLengths[server.getTicks() % 100];
        final long timeStart = Util.getMeasuringTimeNano();
        this.sortChunkList();
        for (int chunkIndex = 0; chunkIndex < this.pendingChunks.size(); ++chunkIndex)
        {
            long currentTime = Util.getMeasuringTimeNano();
            long elapsedTickTime = vanillaTickTime + (currentTime - timeStart);
            if (elapsedTickTime >= 60000000L)
            {
                break;
            }
            ChunkPos pos = this.pendingChunks.get(chunkIndex);

            if (this.canProcessChunk(pos) && this.processChunk(pos))
            {
                this.pendingChunks.remove(chunkIndex);
                --chunkIndex;
            }
        }

        if (this.pendingChunks.isEmpty())
        {
            this.finished = true;
            return true;
        }

        this.updateInfoHudLines();
        return false;
    }

    @Override
    protected boolean processChunk(ChunkPos pos)
    {
        // TODO ignoreBlocks and ignoreEntities

        // New list to avoid CME
        ArrayList<SchematicPlacement> placements = new ArrayList<>(this.placementsPerChunk.get(pos));

        for (SchematicPlacement placement : placements)
        {
            if (SchematicPlacingUtils.placeToWorldWithinChunk(this.world, pos, placement, this.replace, false))
            {
                this.placementsPerChunk.remove(pos, placement);
            }
        }

        return this.placementsPerChunk.containsKey(pos) == false;
    }

    @Override
    protected void onStop()
    {
        if (this.finished)
        {
            player.sendMessage(Text.translatable("litematica.message.schematic_pasted"));
        }
        else
        {
            player.sendMessage(Text.translatable("litematica.message.error.schematic_paste_failed"));
        }

//        InfoHud.getInstance().removeInfoHudRenderer(this, false);

        super.onStop();
    }
}
