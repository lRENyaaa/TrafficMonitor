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
package ltd.rymc.traffic.listener;

import io.netty.channel.Channel;
import ltd.rymc.traffic.TrafficMonitor;
import ltd.rymc.traffic.utils.PlayerChannelUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

public class PlayerListener implements Listener {

    private static final Method SET_PLAYER;
    private static final Logger LOGGER = TrafficMonitor.getInstance().getLogger();

    static {
        try {
            SET_PLAYER = UserConnection.class.getDeclaredMethod("setPlayer", Player.class);
            SET_PLAYER.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        TrafficMonitor.getMorePaperLib().scheduling().entitySpecificScheduler(player).run(() -> {
            Channel channel = PlayerChannelUtil.getChannel(player);
            if (channel == null) {
                LOGGER.warning("Can't get player %s's channel, skipping binding monitor");
                return;
            }

            UserConnection userConnection = UserConnection.getUserByChannel(channel);
            if (userConnection == null) {
                LOGGER.warning("Can't find the userConnection in map, skipping binding monitor for player %s");
                return;
            }

            try {
                SET_PLAYER.invoke(userConnection, player);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }, null);
    }


}
