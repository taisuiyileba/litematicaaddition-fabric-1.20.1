package fi.dy.masa.litematica.scheduler;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

/**
 * Fabric 1.20.1 专用任务调度器集成
 * 自动将任务调度器绑定到服务器tick循环
 */
public class FabricTaskSchedulerIntegration {

    private static boolean initialized = false;

    /**
     * 初始化并注册到Fabric服务器tick事件
     */
    public static void initialize() {
        if (initialized) {
            return;
        }

        // 注册服务器tick结束事件
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            TaskScheduler.getInstanceServer().runTasks();
        });

        initialized = true;
    }

    /**
     * 获取服务器任务调度器实例
     */
    public static TaskScheduler getServerScheduler() {
        return TaskScheduler.getInstanceServer();
    }

    /**
     * 安全地调度服务器任务
     */
    public static void scheduleServerTask(ITask task, int interval) {
        // 确保已初始化
        initialize();

        // 获取服务器调度器并安排任务
        getServerScheduler().scheduleTask(task, interval);
    }
}