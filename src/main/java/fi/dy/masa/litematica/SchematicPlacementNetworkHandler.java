package fi.dy.masa.litematica;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.scheduler.TaskScheduler;
import fi.dy.masa.litematica.scheduler.tasks.Server.LitematicaSchematicServer;
import fi.dy.masa.litematica.scheduler.tasks.Server.PositionUtilsServer;
import fi.dy.masa.litematica.scheduler.tasks.Server.TaskPasteSchematicPerChunkDirectServer;
import fi.dy.masa.litematica.scheduler.tasks.TaskPasteSchematicPerChunkBase;
import fi.dy.masa.litematica.scheduler.tasks.TaskPasteSchematicPerChunkDirect;
import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager;
import fi.dy.masa.litematica.util.FileType;
import fi.dy.masa.litematica.util.ReplaceBehavior;
import fi.dy.masa.malilib.util.LayerRange;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class SchematicPlacementNetworkHandler {

    // 自定义事件处理逻辑（替换成你的实际逻辑）
    public static Identifier getPacketId() {
        return new Identifier("litematica", "schematic_placement");
    }
    public static void sendSchematicPlacement(List<SchematicPlacement> list, LayerRange range, boolean changedBlocksOnly, ClientPlayerEntity player) {
        // 在这里调用你的自定义事件
        PacketByteBuf buf = PacketByteBufs.create();
        list.forEach(placement -> {
            // 获取 schematicPlacement 的信息
            buf.writeString(placement.getSchematicFile().toString());
            buf.writeBlockPos(placement.getOrigin());
            buf.writeString(placement.getName());
            buf.writeInt(placement.getSubRegionCount());
            buf.writeBoolean(placement.isEnabled());
            buf.writeBoolean(placement.isRenderingEnabled());
        });
        buf.writeString(range.toJson().toString());
        buf.writeBoolean(changedBlocksOnly);
        buf.writeString(Configs.Generic.PASTE_REPLACE_BEHAVIOR.getOptionListValue().toString());
        buf.writeInt(Configs.Generic.COMMAND_TASK_INTERVAL.getIntegerValue());
        ClientPlayNetworking.send(getPacketId(), buf);
    }
    public static void registerServerReceiver(){
        ServerPlayNetworking.registerGlobalReceiver(getPacketId(), (server, player, handler, buf, responseSender) -> {
            // 在事件循环上读取数据包数据
            File schematicFile = new File(buf.readString());
            BlockPos origin =buf.readBlockPos();
            String name = buf.readString();
            int subRegionCount = buf.readInt();
            boolean enabled = buf.readBoolean();
            boolean renderingEnabled = buf.readBoolean();
            String rangeJsonStr= buf.readString();
            JsonObject rangeJson = JsonParser.parseString(rangeJsonStr).getAsJsonObject();
            LayerRange range = new LayerRange(null);
            range.fromJson(rangeJson);
            boolean changedBlocksOnly = buf.readBoolean();
            ReplaceBehavior replace =  ReplaceBehavior.fromStringStatic(buf.readString());
            int taskInterval = buf.readInt();
            server.execute(() -> {
                // 此 lambda 中的所有内容都在渲染线程上运行
//                LitematicaSchematic schematic = LitematicaSchematic.createFromFile(new File("C:\\Users\\suiyi\\Documents\\litematica-pre-rewrite-fabric-1.20.1(1)\\run\\syncmatics"),
//                        "883f147c-59b9-3f41-9843-d0101692cc6f");
                String fileName = "883f147c-59b9-3f41-9843-d0101692cc6f";
                File dir = new File("C:\\Users\\suiyi\\Documents\\litematica-pre-rewrite-fabric-1.20.1(1)\\run\\syncmatics");
                FileType schematicType = FileType.LITEMATICA_SCHEMATIC;
                if (fileName.endsWith(LitematicaSchematic.FILE_EXTENSION) == false)
                {
                    fileName = fileName + LitematicaSchematic.FILE_EXTENSION;
                    File file = new File(dir, fileName);
//                    LitematicaSchematicServer schematic = new LitematicaSchematicServer(file, schematicType);
//                    schematic.readFromFile();
//                    player.sendMessage(Text.literal(String.valueOf(schematic.getMetadata().getEnclosingSize())));
//                    SchematicPlacement schematicPlacement = new SchematicPlacement(schematic.getLitematicaSchematic(), origin, name,  enabled, renderingEnabled);
//                    TaskPasteSchematicPerChunkDirectServer task = new TaskPasteSchematicPerChunkDirectServer(Collections.singletonList(schematicPlacement), range, changedBlocksOnly,player,replace);
//                    TaskScheduler.getInstanceServer().scheduleTask(task, taskInterval);
                }
            });
        });
    }
    public static SchematicPlacementManager execute(){
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            return DataManager.getSchematicPlacementManager();
        } else {
            return null; // 或创建服务器专用的简化管理器
        }
    }
}

