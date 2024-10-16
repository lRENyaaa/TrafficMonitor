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
package ltd.rymc.traffic.monitor;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import ltd.rymc.traffic.TrafficMonitor;
import ltd.rymc.traffic.netty.process.DecodeProcess;
import ltd.rymc.traffic.netty.process.EncodeProcess;
import ltd.rymc.traffic.utils.PlayerChannelUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class PlayerTrafficMonitor {

    private static final TrafficMonitor PLUGIN = TrafficMonitor.getInstance();
    private static final Logger LOGGER = PLUGIN.getLogger();
    private static final List<PlayerTrafficMonitor> MONITOR_LIST = new ArrayList<>();

    private Player player = null;
    private final Channel channel;

    private int totalDecodeBytes = 0;
    private int totalEncodeBytes = 0;

    private final DecodeProcess decodeProcess = new DecodeProcess(this::onPacketSend);
    private final EncodeProcess encodeProcess = new EncodeProcess(this::onPacketReceive);

    public static void init() throws Exception {
        try {
            if (!PlayerChannelUtil.getInitState()){
                throw new IllegalStateException("Failed to initialize player channel getter");
            }
            registerListener();
            TrafficMonitor.getMorePaperLib().scheduling().asyncScheduler().runAtFixedRate(
                    PlayerTrafficMonitor::refreshMonitor,
                    Duration.ofMillis(1),
                    Duration.ofMinutes(1)
            );
            LOGGER.info("Player traffic monitor initialized!");
        } catch (Exception e){
            throw new Exception("Encountered an issue while initializing player traffic monitor", e);
        }
    }


    private static final Listener playerListener = new Listener() {
        @EventHandler
        private void onPlayerJoin(PlayerJoinEvent event){
            Player player = event.getPlayer();
            TrafficMonitor.getMorePaperLib().scheduling().entitySpecificScheduler(player).run(() -> {
                Channel channel = PlayerChannelUtil.getChannel(player);

                if (channel == null) {
                    LOGGER.warning("Can't get player %s's channel, skipping binding monitor");
                    return;
                }

                for (PlayerTrafficMonitor monitor : MONITOR_LIST) {
                    if (monitor.channel == channel) monitor.player = player;
                }

            }, null);


        }

        @EventHandler
        private void onPlayerQuit(PlayerQuitEvent event) {
            MONITOR_LIST.removeIf(monitor -> monitor.player == event.getPlayer());
        }
    };

    private static void registerListener(){
        Bukkit.getPluginManager().registerEvents(playerListener, PLUGIN);
    }

    private static void refreshMonitor(){
        MONITOR_LIST.removeIf(monitor -> !monitor.channel.isOpen());
    }

    public static PlayerTrafficMonitor of(Channel channel){
        for (PlayerTrafficMonitor monitor : MONITOR_LIST) {
            if (monitor.channel == channel) return monitor;
        }

        PlayerTrafficMonitor monitor = new PlayerTrafficMonitor(channel);
        MONITOR_LIST.add(monitor);
        return monitor;
    }

    private PlayerTrafficMonitor(Channel channel){
        this.channel = channel;
    }

    public DecodeProcess getDecodeProcess(){
        return decodeProcess;
    }

    public EncodeProcess getEncodeProcess(){
        return encodeProcess;
    }

    private void onPacketSend(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> out){
        InetSocketAddress remoteAddress = (InetSocketAddress) channelHandlerContext.channel().remoteAddress();
        String ipAddress = remoteAddress.getAddress().getHostAddress();

        int readableBytes = byteBuf.copy().readableBytes();
        totalEncodeBytes += readableBytes;

        String format = String.format("IP: %s, send bytes: %d, Total get bytes: %d", ipAddress, readableBytes, totalEncodeBytes);
        LOGGER.info("Player: " + (player == null ? "null" : player.getName()) + ", " + format);

        out.add(byteBuf.retain());
    }

    private void onPacketReceive(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> out){
        InetSocketAddress remoteAddress = (InetSocketAddress) channelHandlerContext.channel().remoteAddress();
        String ipAddress = remoteAddress.getAddress().getHostAddress();

        int readableBytes = byteBuf.copy().readableBytes();
        totalDecodeBytes += readableBytes;

        String format = String.format("IP: %s, get bytes: %d, Total get bytes: %d", ipAddress, readableBytes, totalDecodeBytes);
        LOGGER.info("Player: " + (player == null ? "null" : player.getName()) + ", " + format);

        out.add(byteBuf.retain());
    }

}
