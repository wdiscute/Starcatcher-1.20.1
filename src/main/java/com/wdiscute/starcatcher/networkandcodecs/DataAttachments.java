package com.wdiscute.starcatcher.networkandcodecs;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.network.PacketDistributor;
import org.slf4j.Logger;

import java.util.List;

public class DataAttachments implements DataAttachmentCapability, INBTSerializable<CompoundTag>
{
    private static final Logger LOGGER = LogUtils.getLogger();

    public String fishing = "";
    public List<FishCaughtCounter> fishesCaught = List.of(new FishCaughtCounter(FishProperties.DEFAULT, 0, 0, 0));
    public List<TrophyProperties> trophiesCaught = List.of(TrophyProperties.DEFAULT);
    public List<FishProperties> notifications = List.of(FishProperties.DEFAULT);

    private final Player player;

    public DataAttachments(Player player)
    {
        this.player = player;
    }

    public static DataAttachmentCapability get(Player player)
    {
        return player.getCapability(PLAYER_DATA).orElse(null);
    }

    @Override
    public String fishing()
    {
        return fishing;
    }

    @Override
    public void setFishing(String s)
    {
        this.fishing = s;

        if(this.player instanceof ServerPlayer sp)
        {
            Payloads.CHANNEL.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> sp),
                    new Payloads.FishingBobUUIDPayload(player, s)
            );
        }
    }

    @Override
    public List<FishCaughtCounter> fishesCaught()
    {
        return fishesCaught;
    }

    @Override
    public void setFishesCaught(List<FishCaughtCounter> list)
    {
        this.fishesCaught = list;

        if(this.player instanceof ServerPlayer sp)
        {
            Payloads.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> sp),
                    new Payloads.FishesCaughtPayload(this.fishesCaught)
            );
        }

    }

    @Override
    public List<TrophyProperties> trophiesCaught()
    {
        return this.trophiesCaught;
    }

    @Override
    public void setTrophiesCaught(List<TrophyProperties> list)
    {
        this.trophiesCaught = list;

        if(this.player instanceof ServerPlayer sp)
        {
            Payloads.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> sp),
                    new Payloads.TrophiesCaughtPayload(this.trophiesCaught)
            );
        }
    }

    @Override
    public List<FishProperties> fishNotifications()
    {
        return notifications;
    }

    @Override
    public void setFishNotifications(List<FishProperties> list)
    {
        this.notifications = list;

        if(this.player instanceof ServerPlayer sp)
        {
            Payloads.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> sp),
                    new Payloads.FishesNotificationPayload(this.notifications)
            );
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag compoundTag = new CompoundTag();

        FishCaughtCounter.LIST_CODEC.encode(this.fishesCaught, NbtOps.INSTANCE, new ListTag())
                .resultOrPartial(LOGGER::warn).ifPresent(tag -> compoundTag.put("fishes_caught", tag));

        TrophyProperties.LIST_CODEC.encode(this.trophiesCaught, NbtOps.INSTANCE, new ListTag())
                .resultOrPartial(LOGGER::warn).ifPresent(tag -> compoundTag.put("trophies_caught", tag));

        FishProperties.LIST_CODEC.encode(this.notifications, NbtOps.INSTANCE, new ListTag())
                .resultOrPartial(LOGGER::warn).ifPresent(tag -> compoundTag.put("notifications", tag));

        return compoundTag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag)
    {
        if(tag.contains("fishes_caught", 9))
            this.fishesCaught = FishCaughtCounter.LIST_CODEC.decode(NbtOps.INSTANCE, tag.getList("fishes_caught", Tag.TAG_COMPOUND)).result().get().getFirst();

        if(tag.contains("trophies_caught", 9))
            this.trophiesCaught = TrophyProperties.LIST_CODEC.decode(NbtOps.INSTANCE, tag.getList("trophies_caught", Tag.TAG_COMPOUND)).result().get().getFirst();

        if(tag.contains("notifications", 9))
            this.notifications = FishProperties.LIST_CODEC.decode(NbtOps.INSTANCE, tag.getList("notifications", Tag.TAG_COMPOUND)).result().get().getFirst();
    }
}
