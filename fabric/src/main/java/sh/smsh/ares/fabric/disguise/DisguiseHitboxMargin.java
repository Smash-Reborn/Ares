package sh.smsh.ares.fabric.disguise;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import sh.smsh.ares.disguise.DisguiseRegistry;
import sh.smsh.ares.feature.impl.DynamicDisguiseBoundingBoxFeature;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DisguiseHitboxMargin {

    private static final String COMPONENT_PATH = "hitbox_margin";

    public static AABB boxFor(@NotNull final Entity entity) {
        final AABB box = entity.getBoundingBox();
        final double margin = marginFor(entity);
        return margin == 0.0D ? box : box.inflate(margin);
    }

    public static double marginFor(@NotNull final Entity entity) {
        if (!DynamicDisguiseBoundingBoxFeature.shouldApply()
                || (!DisguiseRegistry.isDisguiseEntity(entity.getId()) && !DisguiseRegistry.isOwnedSquid(entity.getId()))) {
            return 0.0D;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return 0.0D;
        }

        return readMargin(minecraft.player.getMainHandItem());
    }

    private static double readMargin(@NotNull final ItemStack stack) {
        if (stack.isEmpty()) {
            return 0.0D;
        }

        for (final TypedDataComponent<?> component : stack.getComponents()) {
            final Identifier id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(component.type());
            if (id != null && COMPONENT_PATH.equals(id.getPath()) && component.value() instanceof Number number) {
                return number.doubleValue();
            }
        }

        final CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return 0.0D;
        }

        final CompoundTag tag = customData.copyTag();
        if (!tag.contains(COMPONENT_PATH)) {
            return 0.0D;
        }

        if (tag.getDouble(COMPONENT_PATH).isPresent()) {
            return tag.getDouble(COMPONENT_PATH).get();
        }
        if (tag.getFloat(COMPONENT_PATH).isPresent()) {
            return tag.getFloat(COMPONENT_PATH).get();
        }
        return tag.getIntOr(COMPONENT_PATH, 0);
    }
}
