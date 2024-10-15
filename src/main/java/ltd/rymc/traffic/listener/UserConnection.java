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
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserConnection {

    private static final Map<Channel, UserConnection> userConnectionMap = new ConcurrentHashMap<>();

    static {
        TrafficMonitor.getMorePaperLib().scheduling().asyncScheduler().runAtFixedRate(() -> {
            userConnectionMap.keySet().removeIf(channel -> !channel.isOpen());
        }, Duration.ofMillis(1), Duration.ofMinutes(5));
    }

    private Player player;

    public UserConnection(Channel channel){
        this(channel, null);
    }

    public UserConnection(Channel channel, Player player){
        this.player = player;

        userConnectionMap.put(channel, this);
    }

    @SuppressWarnings("unused")
    private void setPlayer(Player player){
        this.player = player;
    }

    public boolean checkPermission(String permission){
        return player != null && player.hasPermission(permission);
    }

    public Player getPlayer(){
        return player;
    }

    public static UserConnection getUserByChannel(Channel channel){
        return userConnectionMap.get(channel);
    }
}
