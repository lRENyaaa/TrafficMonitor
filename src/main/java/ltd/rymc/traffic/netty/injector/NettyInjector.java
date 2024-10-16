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
package ltd.rymc.traffic.netty.injector;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import ltd.rymc.traffic.TrafficMonitor;
import ltd.rymc.traffic.utils.NMSUtil;
import ltd.rymc.traffic.utils.ReflectionUtil;
import ltd.rymc.traffic.utils.SynchronizedListWrapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Logger;

@SuppressWarnings({"SynchronizationOnLocalVariableOrMethodParameter", "unchecked"})
public class NettyInjector {

    private static Logger LOGGER = TrafficMonitor.getInstance().getLogger();

    public static void init() throws Exception {
        LOGGER.info("Injecting traffic monitor into Netty pipeline.");
        new NettyInjector().inject();
        LOGGER.info("Netty pipeline injected!");
    }

    public void inject() throws ReflectiveOperationException {

        // Get ServerConnection
        Object connection = getServerConnection();
        if (connection == null) {
            throw new RuntimeException("Failed to find the core component 'ServerConnection'");
        }

        // Inject into channels list
        for (Field field : connection.getClass().getDeclaredFields()) {
            // Check for list with the correct generic type
            if (!List.class.isAssignableFrom(field.getType()) || !field.getGenericType().getTypeName().contains(ChannelFuture.class.getName())) {
                continue;
            }

            field.setAccessible(true);
            List<ChannelFuture> list = (List<ChannelFuture>) field.get(connection);
            List<ChannelFuture> wrappedList = new SynchronizedListWrapper<>(list, o -> {
                // Inject newly added entries
                try {
                    injectChannelFuture(o);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            });

            // Synchronize over original list before setting the field
            synchronized (list) {
                // Iterate through current list
                for (ChannelFuture future : list) {
                    injectChannelFuture(future);
                }

                field.set(connection, wrappedList);
            }

        }

    }

    private void injectChannelFuture(ChannelFuture future) throws ReflectiveOperationException {
        List<String> names = future.channel().pipeline().names();
        ChannelHandler bootstrapAcceptor = null;
        // Find the right channelhandler
        for (String name : names) {
            ChannelHandler handler = future.channel().pipeline().get(name);
            try {
                ReflectionUtil.get(handler, "childHandler", ChannelInitializer.class);
                bootstrapAcceptor = handler;
                break;
            } catch (ReflectiveOperationException ignored) {
                // Not this one
            }
        }

        if (bootstrapAcceptor == null) {
            // Default to first (also allows blame to work)
            bootstrapAcceptor = future.channel().pipeline().first();
        }

        try {
            ChannelInitializer<Channel> oldInitializer = ReflectionUtil.get(bootstrapAcceptor, "childHandler", ChannelInitializer.class);
            ReflectionUtil.set(bootstrapAcceptor, "childHandler", new BukkitChannelInitializer(oldInitializer));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private Object getServerConnection() throws ReflectiveOperationException {
        Class<?> serverClass = NMSUtil.nms(
                "MinecraftServer",
                "net.minecraft.server.MinecraftServer"
        );
        Class<?> connectionClass = NMSUtil.nms(
                "ServerConnection",
                "net.minecraft.server.network.ServerConnection"
        );

        Object server = ReflectionUtil.invokeStatic(serverClass, "getServer");
        for (Method method : serverClass.getDeclaredMethods()) {
            if (method.getReturnType() != connectionClass || method.getParameterTypes().length != 0) {
                continue;
            }

            // We need the method that initiates the connection if not yet set
            Object connection = method.invoke(server);
            if (connection != null) {
                return connection;
            }
        }
        return null;
    }
}
