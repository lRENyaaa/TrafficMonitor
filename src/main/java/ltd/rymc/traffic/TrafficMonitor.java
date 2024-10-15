/*
 * This file is part of TrafficMonitor - https://github.com/lRENyaaa/TrafficMonitor
 *
 * Copyright (C) 2024 RenYuan Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ltd.rymc.traffic;

import ltd.rymc.traffic.injector.NettyInjector;
import ltd.rymc.traffic.listener.PlayerListener;
import ltd.rymc.traffic.utils.PlayerChannelUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import space.arim.morepaperlib.MorePaperLib;

public final class TrafficMonitor extends JavaPlugin {

    private static TrafficMonitor instance;
    private static MorePaperLib morePaperLib;

    @Override
    public void onEnable() {
        instance = this;
        morePaperLib = new MorePaperLib(this);
        if (!PlayerChannelUtil.getInitState()){
            getLogger().severe("Failed to initialize player channel getter");
        }
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        try {
            new NettyInjector().inject();
        } catch (ReflectiveOperationException e) {
            getLogger().severe("Failed to inject Netty pipeline: " + e.getMessage());
        }
    }

    public static TrafficMonitor getInstance(){
        return instance;
    }

    public static MorePaperLib getMorePaperLib(){
        return morePaperLib;
    }
}
