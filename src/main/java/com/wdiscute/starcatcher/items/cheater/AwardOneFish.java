package com.wdiscute.starcatcher.items.cheater;

import com.wdiscute.starcatcher.Starcatcher;
import com.wdiscute.starcatcher.networkandcodecs.FishCaughtCounter;
import com.wdiscute.starcatcher.networkandcodecs.FishProperties;
import com.wdiscute.starcatcher.networkandcodecs.ModDataAttachments;
import com.wdiscute.starcatcher.networkandcodecs.Payloads;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AwardOneFish extends Item
{
    public AwardOneFish()
    {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand)
    {
        if (!player.isCreative())
            return InteractionResultHolder.pass(player.getItemInHand(usedHand));

        List<FishCaughtCounter> fishCounter;

        List<FishProperties> fishesNotification = new ArrayList<>(ModDataAttachments.getFishesNotification(player));

        fishCounter = new ArrayList<>(ModDataAttachments.getFishCaught(player));

        Optional<Holder.Reference<FishProperties>> optional = level.registryAccess().registryOrThrow(Starcatcher.FISH_REGISTRY).getRandom(level.random);

        if (optional.isPresent())
        {
            FishProperties fp = optional.get().value();

            fishCounter.add(new FishCaughtCounter(fp, 1, Integer.MAX_VALUE, 99999));
            fishesNotification.add(fp);

            if (player instanceof ServerPlayer sp)
            {
                //todo send notification
                //PacketDistributor.sendToPlayer(sp, new Payloads.FishCaughtPayload(fp));
            }
        }

        ModDataAttachments.setFishCaught(player, fishCounter);
        //player.setData(ModDataAttachments.FISHES_CAUGHT, fishCounter);
        ModDataAttachments.setFishesNotification(player, fishesNotification);
        //player.setData(ModDataAttachments.FISHES_NOTIFICATION, fishesNotification);

        return InteractionResultHolder.success(player.getItemInHand(usedHand));
    }


}
