package sh.smsh.ares.fabric.debug;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpawnPacketDebug {

    private static boolean enabled;
    private static final List<PendingSpawn> PENDING = new ArrayList<>();
    private static final Map<Integer, String> NOTED_TEXT = new HashMap<>();
    private static final Set<Integer> SPAWNED_INTERACTIONS = new HashSet<>();

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean toggle() {
        enabled = !enabled;
        if (!enabled) {
            PENDING.clear();
            NOTED_TEXT.clear();
            SPAWNED_INTERACTIONS.clear();
        }
        return enabled;
    }

    public static void report(final ClientboundAddEntityPacket packet) {
        if (!enabled) {
            return;
        }

        if (packet.getType() == EntityTypes.INTERACTION) {
            SPAWNED_INTERACTIONS.add(packet.getId());
        }

        PENDING.add(new PendingSpawn(
                packet.getId(),
                String.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(packet.getType())),
                packet.getType() == EntityTypes.TEXT_DISPLAY,
                packet.getX(),
                packet.getY(),
                packet.getZ()));
    }

    public static void onPassengers(final ClientboundSetPassengersPacket packet) {
        if (!enabled || SPAWNED_INTERACTIONS.isEmpty()) {
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        final Entity vehicle = minecraft.level.getEntity(packet.getVehicle());
        if (SPAWNED_INTERACTIONS.contains(packet.getVehicle())) {
            minecraft.player.sendSystemMessage(Component.literal(
                    "[Ares] Interaction " + describe(vehicle, packet.getVehicle())
                            + " carrying " + describePassengers(minecraft, packet.getPassengers())
            ));
        }

        for (final int passengerId : packet.getPassengers()) {
            if (!SPAWNED_INTERACTIONS.contains(passengerId)) {
                continue;
            }

            minecraft.player.sendSystemMessage(Component.literal(
                    "[Ares] Interaction id=" + passengerId
                            + " riding " + describe(vehicle, packet.getVehicle())
            ));
        }
    }

    public static void noteText(final int entityId, final String text) {
        if (enabled) {
            NOTED_TEXT.put(entityId, text);
        }
    }

    public static void onRemoved(final int[] entityIds) {
        for (final int entityId : entityIds) {
            SPAWNED_INTERACTIONS.remove(entityId);
        }
    }

    /**
     * Custom names arrive in the entity-data packet after the spawn packet, so the line is
     * printed at the end of the tick once that name is on the entity.
     */
    public static void flush() {
        if (PENDING.isEmpty()) {
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            PENDING.clear();
            return;
        }

        final List<PendingSpawn> pending = List.copyOf(PENDING);
        PENDING.clear();

        for (final PendingSpawn spawn : pending) {
            final Entity entity = minecraft.level == null ? null : minecraft.level.getEntity(spawn.entityId());
            final String name = entity == null ? "-" : entity.getName().getString();
            final String customName = entity == null || entity.getCustomName() == null
                    ? "-"
                    : entity.getCustomName().getString();
            final String noted = NOTED_TEXT.remove(spawn.entityId());
            final String text = noted != null
                    ? DamageIndicatorText.describe(noted)
                    : spawn.textDisplay() ? textOf(entity) : null;

            minecraft.player.sendSystemMessage(Component.literal(
                    "[Ares] Spawn " + spawn.type()
                            + " id=" + spawn.entityId()
                            + " name=" + name
                            + " custom=" + customName
                            + (text == null ? "" : " text=\"" + text + "\"")
                            + " at " + format(spawn.x()) + ", " + format(spawn.y()) + ", " + format(spawn.z())
            ));
        }
    }

    private static String textOf(final Entity entity) {
        if (!(entity instanceof Display.TextDisplay textDisplay) || textDisplay.textRenderState() == null) {
            return "-";
        }

        final Component text = textDisplay.textRenderState().text();
        return text == null ? "-" : DamageIndicatorText.describe(text.getString());
    }

    private static String describePassengers(final Minecraft minecraft, final int[] passengerIds) {
        if (passengerIds.length == 0) {
            return "none";
        }

        final StringBuilder passengers = new StringBuilder();
        for (int index = 0; index < passengerIds.length; index++) {
            if (index > 0) {
                passengers.append(", ");
            }
            passengers.append(describe(minecraft.level.getEntity(passengerIds[index]), passengerIds[index]));
        }
        return passengers.toString();
    }

    private static String describe(final Entity vehicle, final int vehicleId) {
        if (vehicle == null) {
            return "id=" + vehicleId;
        }

        final String customName = vehicle.getCustomName() == null ? "-" : vehicle.getCustomName().getString();
        return String.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(vehicle.getType()))
                + " id=" + vehicleId
                + " name=" + vehicle.getName().getString()
                + " custom=" + customName
                + " at " + format(vehicle.getX()) + ", " + format(vehicle.getY()) + ", " + format(vehicle.getZ());
    }

    private static String format(final double value) {
        return String.format("%.2f", value);
    }

    private record PendingSpawn(int entityId, String type, boolean textDisplay, double x, double y, double z) {
    }
}
