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
package ltd.rymc.traffic.utils;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PlayerChannelUtil {

    private static final Class<?> ENTITY_PLAYER_CLASS;
    private static final Class<?> PLAYER_CONNECTION_CLASS;
    private static final Class<?> NETWORK_MANAGER_CLASS;

    private static final Method GET_HANDLE_METHOD;

    private static final Field PLAYER_CONNECTION_FIELD;
    private static final Field NETWORK_MANAGER_FIELD;
    private static final Field CHANNEL_FIELD;

    private static boolean initState;

    static {
        try {
            ENTITY_PLAYER_CLASS = NMSUtil.nms(
                    "EntityPlayer",
                    "net.minecraft.server.level.EntityPlayer"
            );
            PLAYER_CONNECTION_CLASS = NMSUtil.nms(
                    "PlayerConnection",
                    "net.minecraft.server.network.PlayerConnection"
            );

            NETWORK_MANAGER_CLASS = NMSUtil.nms(
                    "NetworkManager",
                    "net.minecraft.network.NetworkManager"
            );

            Class<?> craftPlayerClazz = NMSUtil.obc("entity.CraftPlayer");
            GET_HANDLE_METHOD = craftPlayerClazz.getMethod("getHandle");
            GET_HANDLE_METHOD.setAccessible(true);

            PLAYER_CONNECTION_FIELD = getPlayerConnectionFieldInEntityPlayer();
            NETWORK_MANAGER_FIELD = getNetworkManagerFieldInPlayerConnection();
            CHANNEL_FIELD = getChannelFieldInPlayerConnection();

            initState = true;
        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException e) {
            initState = false;
            throw new RuntimeException(e);
        }
    }

    public static boolean getInitState(){
        return initState;
    }

    public static Channel getChannel(Player player) {
        if (!initState) throw new IllegalStateException("Player channel util hasn't been loaded!");
        try {

            Object nmsPlayer = GET_HANDLE_METHOD.invoke(player);
            Object playerConnection = PLAYER_CONNECTION_FIELD.get(nmsPlayer);
            Object networkManager = NETWORK_MANAGER_FIELD.get(playerConnection);

            return (Channel) CHANNEL_FIELD.get(networkManager);

        } catch (IllegalAccessException | InvocationTargetException | ClassCastException e) {
            throw new RuntimeException(e);
        }
    }

    private static Field getPlayerConnectionFieldInEntityPlayer() throws ClassNotFoundException, NoSuchFieldException {
        for (Field field : ENTITY_PLAYER_CLASS.getDeclaredFields()) {
            if (field.getType().equals(PLAYER_CONNECTION_CLASS)){
                field.setAccessible(true);
                return field;
            }
        }

        throw new NoSuchFieldException("Cannot find player connection field in entity player class");
    }

    private static Field getNetworkManagerFieldInPlayerConnection() throws ClassNotFoundException, NoSuchFieldException {

        for (Field field : PLAYER_CONNECTION_CLASS.getDeclaredFields()) {

            if (!field.getType().equals(NETWORK_MANAGER_CLASS)) {
                continue;
            }

            field.setAccessible(true);
            return field;
        }

        // For paper
        Class<?> superclass = PLAYER_CONNECTION_CLASS.getSuperclass();
        if (superclass != null) for (Field field : superclass.getDeclaredFields()) {

            if (!field.getType().equals(NETWORK_MANAGER_CLASS)) {
                continue;
            }

            field.setAccessible(true);
            return field;
        }

        throw new NoSuchFieldException("Cannot find network manager field in player connection class");
    }

    private static Field getChannelFieldInPlayerConnection() throws ClassNotFoundException, NoSuchFieldException {
        for (Field field : NETWORK_MANAGER_CLASS.getDeclaredFields()) {
            if (field.getType().equals(Channel.class)){
                field.setAccessible(true);
                return field;
            }
        }

        throw new NoSuchFieldException("Cannot find channel field in player network manager");
    }
}
