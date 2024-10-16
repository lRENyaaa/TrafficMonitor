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
package ltd.rymc.traffic.netty.process;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public class EncodeProcess extends MessageToMessageEncoder<ByteBuf> {

    private final PacketHandler handler;

    public EncodeProcess(PacketHandler handler){
        this.handler = handler;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> out) {
        handler.run(channelHandlerContext, byteBuf, out);
    }

}
