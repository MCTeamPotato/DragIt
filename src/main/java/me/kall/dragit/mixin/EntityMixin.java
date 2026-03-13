package me.kall.dragit.mixin;

import me.kall.dragit.ext.Leaving;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public class EntityMixin implements Leaving {
    @Unique private boolean dragIt$keepData = true;

    @Override
    public boolean dragIt$keepData() {
        return this.dragIt$keepData;
    }

    @Override
    public void dragIt$setKeepData(boolean keepData) {
        this.dragIt$keepData = keepData;
    }
}
