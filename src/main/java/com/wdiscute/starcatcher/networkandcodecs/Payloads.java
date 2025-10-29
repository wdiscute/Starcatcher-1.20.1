package com.wdiscute.starcatcher.networkandcodecs;

import com.wdiscute.starcatcher.ModItems;
import com.wdiscute.starcatcher.Starcatcher;
import com.wdiscute.starcatcher.bob.FishingBobEntity;
import com.wdiscute.starcatcher.minigame.FishingMinigameScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.joml.Math;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;


public class Payloads
{

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            Starcatcher.rl("fishing"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int id()
    {
        return packetId++;
    }

    public static void register()
    {
        CHANNEL.registerMessage(
                id(), FishingPayload.class,
                FishingPayload::encode,
                FishingPayload::decode,
                FishingPayload::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        CHANNEL.registerMessage(
                id(), FishingCompletedPayload.class,
                FishingCompletedPayload::encode,
                FishingCompletedPayload::decode,
                FishingCompletedPayload::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));


    }


    //send fishing start to client
    public static class FishingPayload
    {
        private final FishProperties fp;
        private final ItemStack rod;

        public FishingPayload(FishProperties fp, ItemStack rod)
        {
            this.fp = fp;
            this.rod = rod;
        }

        public static void encode(FishingPayload fishingPayload, FriendlyByteBuf buf)
        {
            buf.writeJsonWithCodec(FishProperties.CODEC, fishingPayload.fp);
            buf.writeNbt(fishingPayload.rod.serializeNBT());
        }

        public static FishingPayload decode(FriendlyByteBuf buf)
        {
            FishProperties fp = buf.readJsonWithCodec(FishProperties.CODEC);

            CompoundTag tag = buf.readNbt();

            ItemStack rod = ItemStack.of(tag);

            return new FishingPayload(fp, rod);
        }

        public static void handle(FishingPayload fishingPayload, Supplier<NetworkEvent.Context> context)
        {
            context.get().enqueueWork(() ->
            {
                Minecraft.getInstance().setScreen(new FishingMinigameScreen(fishingPayload.fp, fishingPayload.rod));

            });
            context.get().setPacketHandled(true);
        }
    }


    //send fishing minigame completed to server
    public static class FishingCompletedPayload
    {
        private final int tickCount;
        private final boolean awardTreasure;
        private final boolean perfectCatch;
        private final int consecutiveHits;

        public FishingCompletedPayload(int tickCount, boolean awardTreasure, boolean perfectCatch, int consecutiveHits)
        {
            this.tickCount = tickCount;
            this.awardTreasure = awardTreasure;
            this.perfectCatch = perfectCatch;
            this.consecutiveHits = consecutiveHits;
        }

        public static void encode(FishingCompletedPayload fishingCompletedPayload, FriendlyByteBuf buf)
        {
            buf.writeInt(fishingCompletedPayload.tickCount);
            buf.writeBoolean(fishingCompletedPayload.awardTreasure);
            buf.writeBoolean(fishingCompletedPayload.perfectCatch);
            buf.writeInt(fishingCompletedPayload.consecutiveHits);
        }

        public static FishingCompletedPayload decode(FriendlyByteBuf buf)
        {
            int tickCount = buf.readInt();
            boolean awardTreasure = buf.readBoolean();
            boolean perfectCatch = buf.readBoolean();
            int consecutiveHits = buf.readInt();

            return new FishingCompletedPayload(tickCount, awardTreasure, perfectCatch, consecutiveHits);
        }

        public static void handle(FishingCompletedPayload data, Supplier<NetworkEvent.Context> context)
        {
            context.get().enqueueWork(() ->
            {

                ServerPlayer player = context.get().getSender();
                ServerLevel level = ((ServerLevel) player.level());

                List<Entity> entities = level.getEntities(null, new AABB(-25, -65, -25, 25, 65, 25).move(player.position()));

                for (Entity entity : entities)
                {
                    if (entity.getUUID().toString().equals(ModDataAttachments.getFishingUUID(player)))
                    {
                        if (entity instanceof FishingBobEntity fbe)
                        {
                            if (data.tickCount != -1)
                            {
                                FishProperties fp = fbe.fpToFish;

                                //MAKE THIS DATA DRIVEN
//                        if (fbe.stack.is(ModItems.THUNDERCHARGED_EEL))
//                        {
//                            LightningBolt strike = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
//                            strike.setPos(fbe.position());
//                            strike.setVisualOnly(true);
//                            level.addFreshEntity(strike);
//                        }

                                //create itemStacks
                                ItemStack is = new ItemStack(fbe.fpToFish.fish());
                                ItemStack treasure = new ItemStack(BuiltInRegistries.ITEM.get(fbe.fpToFish.dif().treasure().loot()));

                                //assign custom name if fish has one
                                if (!fp.customName().isEmpty())
                                    is.setHoverName(Component.literal(fp.customName()));

                                //store fish properties in itemstack
                                ModDataComponents.setFishProperties(is, fp);
                                //is.set(ModDataComponents.FISH_PROPERTIES, fp);

                                //split hook double drops
                                if (data.perfectCatch && fbe.hook.is(ModItems.SPLIT_HOOK.get())) is.setCount(2);

                                //make ItemEntities for fish and treasure
                                ItemEntity itemFished = new ItemEntity(level, fbe.position().x, fbe.position().y + 1.2f, fbe.position().z, is);
                                ItemEntity treasureFished = new ItemEntity(level, fbe.position().x, fbe.position().y + 1.2f, fbe.position().z, treasure);

                                //assign delta movement so fish flies towards player
                                double x = Math.clamp(-1, 1, (player.position().x - fbe.position().x) / 25);
                                double y = Math.clamp(-1, 1, (player.position().y - fbe.position().y) / 20);
                                double z = Math.clamp(-1, 1, (player.position().z - fbe.position().z) / 25);
                                Vec3 vec3 = new Vec3(x, 0.7 + y, z);
                                itemFished.setDeltaMovement(vec3);
                                treasureFished.setDeltaMovement(vec3);

                                //add itemEntities to level
                                level.addFreshEntity(itemFished);
                                if (data.awardTreasure) level.addFreshEntity(treasureFished);

                                //play sound
                                Vec3 p = player.position();
                                level.playSound(null, p.x, p.y, p.z, SoundEvents.VILLAGER_CELEBRATE, SoundSource.AMBIENT, 1f, 1f);

                                //award fish counter
//                                if (FishCaughtCounter.AwardFishCaughtCounter(fbe.fpToFish, player, data.tickCount))
//                                    PacketDistributor.sendToPlayer(sp, new Payloads.FishCaughtPayload(fp));
                                //TODO SEND PACKET OF FISH CAUGHT TO PLAYER

                                //award fish counter
                                List<FishProperties> list = new ArrayList<>(ModDataAttachments.getFishesNotification(player));
                                list.add(fbe.fpToFish);
                                ModDataAttachments.setFishesNotification(player, list);
                                //player.setData(ModDataAttachments.FISHES_NOTIFICATION, list);

                                //award exp
                                int exp = 4;
                                if (fp.rarity() == FishProperties.Rarity.UNCOMMON) exp = 8;
                                if (fp.rarity() == FishProperties.Rarity.RARE) exp = 12;
                                if (fp.rarity() == FishProperties.Rarity.EPIC) exp = 20;
                                if (fp.rarity() == FishProperties.Rarity.LEGENDARY) exp = 35;
                                if (fbe.hook.is(ModItems.GOLD_HOOK.get()))
                                    exp *= (int) ((double) data.consecutiveHits / 3) + 1; //extra exp if gold hook is used
                                player.giveExperiencePoints(exp);

                                //todo trigger item fished event
                                //ItemFishedEvent event = new ItemFishedEvent(List.of(is), 0, null);
                                //NeoForge.EVENT_BUS.post(event);

                            }
                            else
                            {
                                //if fish minigame failed/canceled, play sound
                                Vec3 p = player.position();
                                level.playSound(null, p.x, p.y, p.z, SoundEvents.VILLAGER_NO, SoundSource.AMBIENT, 1f, 1f);
                            }

                            fbe.kill();
                        }
                    }

                }

                ModDataAttachments.setFishingUUID(player, "");

                context.get().setPacketHandled(true);
            });
        }


//    public record FishCaughtPayload(FishProperties fp) implements CustomPacketPayload
//    {
//
//        public static final Type<FishCaughtPayload> TYPE = new Type<>(Starcatcher.rl("fish_caught"));
//
//        public static final StreamCodec<ByteBuf, FishCaughtPayload> STREAM_CODEC = StreamCodec.composite(
//                ByteBufCodecs.fromCodec(FishProperties.CODEC),
//                FishCaughtPayload::fp,
//                FishCaughtPayload::new
//        );
//
//        @Override
//        public Type<? extends CustomPacketPayload> type() {
//            return TYPE;
//        }
//    }
//
//    public record FPsSeen(List<FishProperties> fps) implements CustomPacketPayload
//    {
//
//        public static final Type<FPsSeen> TYPE = new Type<>(Starcatcher.rl("fps_seen"));
//
//        public static final StreamCodec<ByteBuf, FPsSeen> STREAM_CODEC = StreamCodec.composite(
//                ByteBufCodecs.fromCodec(FishProperties.LIST_CODEC),
//                FPsSeen::fps,
//                FPsSeen::new
//        );
//
//        @Override
//        public Type<? extends CustomPacketPayload> type() {
//            return TYPE;
//        }
//    }
    }
}
