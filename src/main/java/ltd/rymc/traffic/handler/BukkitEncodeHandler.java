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
import io.netty.handler.codec.MessageToMessageEncoder;
import ltd.rymc.traffic.TrafficMonitor;

import java.net.InetSocketAddress;
import java.util.List;

public class BukkitEncodeHandler extends MessageToMessageEncoder<ByteBuf> {
    private long totalReadBytes = 0;

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> out) {
        InetSocketAddress remoteAddress = (InetSocketAddress) channelHandlerContext.channel().remoteAddress();
        String ipAddress = remoteAddress.getAddress().getHostAddress();

        int readableBytes = byteBuf.copy().readableBytes();
        totalReadBytes += readableBytes;

        String format = String.format("IP: %s, Send bytes: %d, Total send bytes: %d", ipAddress, readableBytes, totalReadBytes);
        TrafficMonitor.getInstance().getLogger().info(format);

        out.add(byteBuf.retain());
    }
}
