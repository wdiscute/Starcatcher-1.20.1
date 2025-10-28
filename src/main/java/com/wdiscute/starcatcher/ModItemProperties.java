package com.wdiscute.starcatcher;

import com.wdiscute.starcatcher.networkandcodecs.ModDataAttachments;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.entity.player.Player;

public class ModItemProperties
{

    public static void addCustomItemProperties()
    {
        ItemProperties.register(
                ModItems.ROD.get(),
                Starcatcher.rl("cast"),
                (stack, level, entity, seed) ->
                {
                    if (entity == null) return 0.0f;

                    if (entity instanceof Player player)
                        return !ModDataAttachments.getFishingUUID(player).isEmpty() && (entity.getMainHandItem() == stack || (entity.getOffhandItem() == stack)) ? 1.0f : 0.0f;

                    return 1.0f;
                }
        );
    }


}
