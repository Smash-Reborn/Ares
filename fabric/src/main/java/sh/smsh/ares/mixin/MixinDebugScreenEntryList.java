package sh.smsh.ares.mixin;

import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sh.smsh.ares.feature.impl.NativeBoundingBoxesFeature;

import java.util.List;

@Mixin(DebugScreenEntryList.class)
public abstract class MixinDebugScreenEntryList {

    @Shadow private List<Identifier> currentlyEnabled;

    @Shadow public abstract DebugScreenEntryStatus getStatus(Identifier id);

    @Inject(method = "rebuildCurrentList", at = @At("RETURN"))
    private void ares$keepForcedHitboxes(final CallbackInfo ci) {
        if (!NativeBoundingBoxesFeature.shouldApply()) {
            return;
        }

        final Identifier hitboxes = DebugScreenEntries.ENTITY_HITBOXES;
        final DebugScreenEntryStatus status = this.getStatus(hitboxes);
        if (status == null || status == DebugScreenEntryStatus.NEVER || this.currentlyEnabled.contains(hitboxes)) {
            return;
        }

        this.currentlyEnabled.add(hitboxes);
    }
}
