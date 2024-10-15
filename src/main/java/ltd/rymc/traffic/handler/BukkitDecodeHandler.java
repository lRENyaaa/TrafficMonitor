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
package ltd.rymc.traffic.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import ltd.rymc.traffic.TrafficMonitor;
import ltd.rymc.traffic.listener.UserConnection;
import org.bukkit.entity.Player;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.logging.Logger;

public class BukkitDecodeHandler extends MessageToMessageDecoder<ByteBuf> {

    private static final Logger logger = TrafficMonitor.getInstance().getLogger();

    private long totalReadBytes = 0;
    private final UserConnection userConnection;

    public BukkitDecodeHandler(UserConnection userConnection){
        this.userConnection = userConnection;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> out) {
        InetSocketAddress remoteAddress = (InetSocketAddress) channelHandlerContext.channel().remoteAddress();
        String ipAddress = remoteAddress.getAddress().getHostAddress();

        int readableBytes = byteBuf.copy().readableBytes();
        totalReadBytes += readableBytes;

        String format = String.format("IP: %s, get bytes: %d, Total get bytes: %d", ipAddress, readableBytes, totalReadBytes);
        Player player = userConnection.getPlayer();
        logger.info("Player: " + (player == null ? "null" : player.getName()) + ", " + format);

        out.add(byteBuf.retain());
    }
}
