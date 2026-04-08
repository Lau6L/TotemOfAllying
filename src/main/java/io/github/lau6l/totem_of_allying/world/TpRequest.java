package io.github.lau6l.totem_of_allying.world;

import io.github.lau6l.totem_of_allying.component.AlliedEntityComponent;
import io.github.lau6l.totem_of_allying.component.ToAComponents;
import io.github.lau6l.totem_of_allying.item.TotemOfAllyingItem;
import io.github.lau6l.totem_of_allying.particle.ToAParticles;
import io.github.lau6l.totem_of_allying.sound.ToASounds;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

public class TpRequest {
    private final ServerWorld ownerWorld, allyWorld;
    private final ServerPlayerEntity serverUser;
    private final ItemStack totemStack;
    private final ServerChunkManager chunkManager;
    private final ChunkPos allyPos;
    private final AlliedEntityComponent alliedEntityComponent;
    private short chunkLoadedCount;
    private static final short REQUIRED_CHUNK_LOADED_COUNT = 10;

    public TpRequest(ServerWorld ownerWorld, ServerWorld allyWorld, ServerPlayerEntity serverUser, ItemStack totemStack, ServerChunkManager chunkManager, ChunkPos allyPos, AlliedEntityComponent alliedEntityComponent) {
        this.ownerWorld = ownerWorld;
        this.allyWorld = allyWorld;
        this.serverUser = serverUser;
        this.totemStack = totemStack;
        this.chunkManager = chunkManager;
        this.allyPos = allyPos;
        this.alliedEntityComponent = alliedEntityComponent;
        this.chunkLoadedCount = 0;
    }

    public boolean tick() {
        if (!chunkManager.isChunkLoaded(allyPos.x, allyPos.z)) return false;

        Entity loadedEntity = allyWorld.getEntity(alliedEntityComponent.uuid());
        if (loadedEntity == null && chunkLoadedCount++ < REQUIRED_CHUNK_LOADED_COUNT) {
            return false;
        }

        if (loadedEntity == null)
            onAlliedEntityDeath(totemStack, serverUser);
        else teleportAlliedEntityToPlayer(loadedEntity, ownerWorld, serverUser);

        chunkManager.removeTicket(
                ChunkTicketType.FORCED,
                allyPos,
                1
        );

        return true;
    }

    public static void onAlliedEntityDeath(ItemStack stack, ServerPlayerEntity serverUser) {
        serverUser.getEntityWorld().playSound(
                null,
                serverUser.getX(), serverUser.getY(), serverUser.getZ(),
                ToASounds.TOTEM_OF_ALLYING_RELEASE,
                SoundCategory.PLAYERS
        );
        stack.remove(ToAComponents.ALLIED_ENTITY_COMPONENT);
        stack.remove(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
        serverUser.networkHandler.sendPacket(new OverlayMessageS2CPacket(
                Text.translatable("totem_of_allying.use.died")
                        .setStyle(TotemOfAllyingItem.FAILURE)
        ));
    }

    public static void teleportAlliedEntityToPlayer(Entity entity, World world, PlayerEntity player) {
        spawnTotemOfAllyingParticles(entity);

        entity.fallDistance = 0;
        entity.dismountVehicle();
        if (entity.hasPassengers()) entity.removeAllPassengers();
        Entity newEntity = entity.teleportTo(new TeleportTarget(
                (ServerWorld) world,
                player.getEntityPos(),
                Vec3d.ZERO,
                0, 0,
                TeleportTarget.NO_OP
        ));

        if (newEntity != null) spawnTotemOfAllyingParticles(newEntity);
    }

    public static void spawnTotemOfAllyingParticles(Entity entity) {
        if (entity.getEntityWorld() instanceof ServerWorld serverEntityWorld) {
            Box boundingBox = entity.getBoundingBox();

            serverEntityWorld.spawnParticles(
                    ToAParticles.TOTEM_OF_ALLYING,
                    false,
                    false,
                    entity.getX(), entity.getY(), entity.getZ(),
                    (int) boundingBox.getAverageSideLength() * 5,
                    (boundingBox.maxX - boundingBox.minX) / 2, (boundingBox.maxY - boundingBox.minY) / 2, (boundingBox.maxZ - boundingBox.minZ) / 2,
                    0
            );
            serverEntityWorld.spawnParticles(
                    ParticleTypes.END_ROD,
                    false,
                    false,
                    entity.getX(), entity.getY(), entity.getZ(),
                    (int) boundingBox.getAverageSideLength() * 25,
                    (boundingBox.maxX - boundingBox.minX) / 2, (boundingBox.maxY - boundingBox.minY) / 2, (boundingBox.maxZ - boundingBox.minZ) / 2,
                    0.1
            );
            serverEntityWorld.spawnParticles(
                    ParticleTypes.REVERSE_PORTAL,
                    false,
                    false,
                    entity.getX(), entity.getY(), entity.getZ(),
                    (int) boundingBox.getAverageSideLength() * 50,
                    (boundingBox.maxX - boundingBox.minX) / 2, (boundingBox.maxY - boundingBox.minY) / 2, (boundingBox.maxZ - boundingBox.minZ) / 2,
                    1
            );

            serverEntityWorld.playSound(
                    null,
                    entity.getX(), entity.getY(), entity.getZ(),
                    ToASounds.TOTEM_OF_ALLYING_TP,
                    entity.isPlayer() ? SoundCategory.PLAYERS : SoundCategory.NEUTRAL
            );
        }
    }
}
