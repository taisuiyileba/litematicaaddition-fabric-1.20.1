package fi.dy.masa.litematica;

import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.scheduler.TaskScheduler;
import fi.dy.masa.litematica.selection.SelectionManager;
import fi.dy.masa.litematica.util.WorldUtils;
import fi.dy.masa.malilib.util.EntityUtils;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;


public class LitematicaServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        SchematicPlacementNetworkHandler.registerServerReceiver();
        System.out.println("已注册原理图放置数据接收器");
    }
    public void onServerTick(MinecraftServer mc)
    {
//        if (mc.getWorlds() != null && mc.getCurrentPlayerCount() > 0)
//        {
//            SelectionManager sm = DataManager.getSelectionManager();
//
//            if (sm.hasGrabbedElement())
//            {
//                sm.moveGrabbedElement(mc.player);
//            }
//
//            WorldUtils.easyPlaceOnUseTick(mc);
//
//            if (Configs.Generic.LAYER_MODE_DYNAMIC.getBooleanValue())
//            {
//                DataManager.getRenderLayerRange().setSingleBoundaryToPosition(EntityUtils.getCameraEntity());
//            }
//
//            DataManager.getSchematicPlacementManager().processQueuedChunks();
//            TaskScheduler.getInstanceServer().runTasks();
//        }
    }
}
