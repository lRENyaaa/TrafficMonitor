/*
 * This file is from ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Modified by RENaa_FD - https://github.com/lRENyaaa
 *
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
package ltd.rymc.traffic.initializer;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import ltd.rymc.traffic.handler.BukkitDecodeHandler;
import ltd.rymc.traffic.handler.BukkitEncodeHandler;
import ltd.rymc.traffic.listener.UserConnection;

import java.lang.reflect.Method;

public final class BukkitChannelInitializer extends ChannelInitializer<Channel> {


    public static final String TRAFFIC_MONITOR_ENCODER = "traffic-monitor-encoder";
    public static final String TRAFFIC_MONITOR_DECODER = "traffic-monitor-decoder";
    public static final String MINECRAFT_ENCODER = "encoder";
    public static final String MINECRAFT_DECODER = "decoder";
    public static final String MINECRAFT_OUTBOUND_CONFIG = "outbound_config";
    private static final Method INIT_CHANNEL_METHOD;
    private final ChannelInitializer<Channel> original;

    static {
        try {
            INIT_CHANNEL_METHOD = ChannelInitializer.class.getDeclaredMethod("initChannel", Channel.class);
            INIT_CHANNEL_METHOD.setAccessible(true);
        } catch (final ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }


    public BukkitChannelInitializer(ChannelInitializer<Channel> oldInit) {
        this.original = oldInit;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        // Add originals
        INIT_CHANNEL_METHOD.invoke(this.original, channel);

        afterChannelInitialize(channel);
    }

    public static void afterChannelInitialize(Channel channel) {
        UserConnection userConnection = new UserConnection(channel);

        // Add our transformers
        final ChannelPipeline pipeline = channel.pipeline();
        final String encoderName = pipeline.get(MINECRAFT_OUTBOUND_CONFIG) != null ? MINECRAFT_OUTBOUND_CONFIG : MINECRAFT_ENCODER;
        pipeline.addBefore(encoderName, TRAFFIC_MONITOR_ENCODER, new BukkitEncodeHandler(userConnection));
        pipeline.addBefore(MINECRAFT_DECODER, TRAFFIC_MONITOR_DECODER, new BukkitDecodeHandler(userConnection));
    }

}
