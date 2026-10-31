package sh.smsh.ares.fabric.combat;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import sh.smsh.ares.fabric.debug.DamageIndicatorText;
import sh.smsh.ares.fabric.debug.SpawnPacketDebug;
import sh.smsh.ares.feature.impl.DisableDamageIndicatorsFeature;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DamageIndicators {

    public static void onEntityData(final ClientboundSetEntityDataPacket packet) {
        if (!DisableDamageIndicatorsFeature.shouldApply() || packet.packedItems() == null) {
            return;
        }

        final String text = damageText(packet);
        if (text == null) {
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        final ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        final Entity entity = level.getEntity(packet.id());
        if (!(entity instanceof Display.TextDisplay)) {
            return;
        }

        SpawnPacketDebug.noteText(packet.id(), text);
        level.removeEntity(packet.id(), Entity.RemovalReason.DISCARDED);
    }

    private static String damageText(final ClientboundSetEntityDataPacket packet) {
        for (final SynchedEntityData.DataValue<?> value : packet.packedItems()) {
            if (!(value.value() instanceof Component component)) {
                continue;
            }

            final String text = component.getString();
            if (DamageIndicatorText.damageValue(text) != null) {
                return text;
            }
        }

        return null;
    }
}
