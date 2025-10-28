package com.wdiscute.starcatcher.items.cheater;

import com.wdiscute.starcatcher.networkandcodecs.FishCaughtCounter;
import com.wdiscute.starcatcher.networkandcodecs.FishProperties;
import com.wdiscute.starcatcher.networkandcodecs.ModDataAttachments;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class RevokeAllFishes extends Item
{
    public RevokeAllFishes()
    {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand)
    {
        //reset fishes caught
        ModDataAttachments.setFishCaught(player, List.of(FishCaughtCounter.DEFAULT));
        ModDataAttachments.setFishesNotification(player, List.of(FishProperties.DEFAULT));
        //player.removeData(ModDataAttachments.FISHES_CAUGHT);
        //player.removeData(ModDataAttachments.FISHES_NOTIFICATION);
        return InteractionResultHolder.success(player.getItemInHand(usedHand));
    }

}
