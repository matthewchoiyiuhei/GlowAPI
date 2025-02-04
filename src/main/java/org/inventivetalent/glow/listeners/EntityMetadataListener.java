package org.inventivetalent.glow.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.inventivetalent.glow.GlowAPI;
import org.inventivetalent.glow.packetwrappers.WrapperPlayServerEntityMetadata;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EntityMetadataListener implements PacketListener {

    @Override
    public void onPacketSending(@NotNull PacketEvent packetEvent) {
        PacketContainer packet = packetEvent.getPacket();
        WrapperPlayServerEntityMetadata wrappedPacket = new WrapperPlayServerEntityMetadata(packet);

        final int entityId = wrappedPacket.getEntityID();
        if (entityId < 0) {//Our packet
            return;
        }

        final List<WrappedWatchableObject> metaData = wrappedPacket.getMetadata();
        if (metaData == null || metaData.isEmpty()) return;//Nothing to modify

        final Player player = packetEvent.getPlayer();
        final World world = player.getWorld();
        final Entity entity = wrappedPacket.getEntity(world);
        if (entity == null) return;
        if(entity.getType() == EntityType.ARMOR_STAND){
            ArmorStand armorStand = (ArmorStand) entity;
            if(!armorStand.isVisible()){
                return;
            }
        }

        //Check if the entity is glowing
        if (!GlowAPI.isGlowing(entity, player)) return;

        //Update the DataWatcher Item
        final WrappedWatchableObject wrappedEntityObj = metaData.get(0);
        final Object entityObj = wrappedEntityObj.getValue();
        if (!(entityObj instanceof Byte)) return;
        byte entityByte = (byte) entityObj;
        entityByte = (byte) (entityByte | GlowAPI.ENTITY_GLOWING_EFFECT);
        wrappedEntityObj.setValue(entityByte);

    }

    @Override
    public void onPacketReceiving(@NotNull PacketEvent packetEvent) { }

    @Override
    @NotNull
    public ListeningWhitelist getSendingWhitelist() {
        return ListeningWhitelist
            .newBuilder()
            .types(PacketType.Play.Server.ENTITY_METADATA)
            .priority(ListenerPriority.NORMAL)
            .gamePhase(GamePhase.PLAYING)
            .monitor()
            .build();
    }

    @Override
    @NotNull
    public ListeningWhitelist getReceivingWhitelist() {
        return ListeningWhitelist
            .newBuilder()
            .build();
    }

    @Override
    @NotNull
    public Plugin getPlugin() {
        return GlowAPI.getPlugin();
    }

}
