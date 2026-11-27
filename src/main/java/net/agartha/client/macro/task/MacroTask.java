package net.agartha.client.macro.task;

import net.agartha.client.macro.task.TaskCancellation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface MacroTask {
   void run(TaskCancellation arg);
}
