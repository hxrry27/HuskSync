/*
 * This file is part of HuskSync, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.husksync.util;

import net.william278.husksync.BukkitHuskSync;
import net.william278.husksync.HuskSync;
import net.william278.husksync.data.UserDataHolder;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BukkitTask extends Task {

    class Sync extends Task.Sync implements BukkitTask {

        private org.bukkit.scheduler.BukkitTask task;
        private final @Nullable UserDataHolder user;

        protected Sync(@NotNull HuskSync plugin, @NotNull Runnable runnable,
                       @Nullable UserDataHolder user, long delayTicks) {
            super(plugin, runnable, delayTicks);
            this.user = user;
        }

        @Override
        public void cancel() {
            if (task != null && !cancelled) {
                task.cancel();
            }
            super.cancel();
        }

        @Override
        public void run() {
            if (isPluginDisabled()) {
                runnable.run();
                return;
            }
            if (cancelled) {
                return;
            }

            final BukkitHuskSync bukkitPlugin = (BukkitHuskSync) getPlugin();
            if (delayTicks > 0) {
                this.task = Bukkit.getScheduler().runTaskLater(bukkitPlugin, runnable, delayTicks);
            } else {
                this.task = Bukkit.getScheduler().runTask(bukkitPlugin, runnable);
            }
        }
    }

    class Async extends Task.Async implements BukkitTask {

        private org.bukkit.scheduler.BukkitTask task;

        protected Async(@NotNull HuskSync plugin, @NotNull Runnable runnable, long delayTicks) {
            super(plugin, runnable, delayTicks);
        }

        @Override
        public void cancel() {
            if (task != null && !cancelled) {
                task.cancel();
            }
            super.cancel();
        }

        @Override
        public void run() {
            if (isPluginDisabled()) {
                runnable.run();
                return;
            }
            if (cancelled) {
                return;
            }

            final BukkitHuskSync bukkitPlugin = (BukkitHuskSync) getPlugin();
            if (delayTicks > 0) {
                plugin.debug("Running async task with delay of " + delayTicks + " ticks");
                this.task = Bukkit.getScheduler().runTaskLaterAsynchronously(bukkitPlugin, runnable, delayTicks);
            } else {
                this.task = Bukkit.getScheduler().runTaskAsynchronously(bukkitPlugin, runnable);
            }
        }
    }

    class Repeating extends Task.Repeating implements BukkitTask {

        private org.bukkit.scheduler.BukkitTask task;

        protected Repeating(@NotNull HuskSync plugin, @NotNull Runnable runnable, long repeatingTicks) {
            super(plugin, runnable, repeatingTicks);
        }

        @Override
        public void cancel() {
            if (task != null && !cancelled) {
                task.cancel();
            }
            super.cancel();
        }

        @Override
        public void run() {
            if (isPluginDisabled()) {
                return;
            }

            if (!cancelled) {
                final BukkitHuskSync bukkitPlugin = (BukkitHuskSync) getPlugin();
                this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(
                        bukkitPlugin, runnable, 0L, repeatingTicks
                );
            }
        }
    }

    // Returns if the Bukkit HuskSync plugin is disabled
    default boolean isPluginDisabled() {
        return !((BukkitHuskSync) getPlugin()).isEnabled();
    }

    interface Supplier extends Task.Supplier {

        @NotNull
        @Override
        default Task.Sync getSyncTask(@NotNull Runnable runnable, @Nullable UserDataHolder user, long delayTicks) {
            return new Sync(getPlugin(), runnable, user, delayTicks);
        }

        @NotNull
        @Override
        default Task.Async getAsyncTask(@NotNull Runnable runnable, long delayTicks) {
            return new Async(getPlugin(), runnable, delayTicks);
        }

        @NotNull
        @Override
        default Task.Repeating getRepeatingTask(@NotNull Runnable runnable, long repeatingTicks) {
            return new Repeating(getPlugin(), runnable, repeatingTicks);
        }

        @Override
        default void cancelTasks() {
            Bukkit.getScheduler().cancelTasks((BukkitHuskSync) getPlugin());
        }

    }

}
