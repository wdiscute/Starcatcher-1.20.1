package com.wdiscute.starcatcher.networkandcodecs;

import net.minecraft.world.entity.player.Player;

import java.util.List;

public class ModDataAttachments
{

//    public static final Supplier<AttachmentType<String>> FISHING = ATTACHMENT_TYPES.register(
//            "fishing", () -> AttachmentType.builder(() -> "")
//                    .serialize(Codec.unit(""))
//                    .sync(ByteBufCodecs.STRING_UTF8)
//                    .build()
//    );

//    public static final Supplier<AttachmentType<List<FishCaughtCounter>>> FISHES_CAUGHT = ATTACHMENT_TYPES.register(
//            "fishes_caught", () ->
//                    AttachmentType.builder(() -> List.of(new FishCaughtCounter(FishProperties.DEFAULT, 0, 0, 0)))
//                            .serialize(FishCaughtCounter.LIST_CODEC)
//                            .sync(FishCaughtCounter.LIST_STREAM_CODEC)
//                            .copyOnDeath()
//                            .build()
//    );

//    public static final Supplier<AttachmentType<List<TrophyProperties>>> TROPHIES_CAUGHT = ATTACHMENT_TYPES.register(
//            "trophies_caught", () ->
//                    AttachmentType.builder(() -> List.of(TrophyProperties.DEFAULT))
//                            .serialize(TrophyProperties.LIST_CODEC)
//                            .sync(TrophyProperties.LIST_STREAM_CODEC)
//                            .copyOnDeath()
//                            .build()
//    );

    //todo maybe port this maybe not
//    public static final Supplier<AttachmentType<List<ResourceLocation>>> TREASURES_CAUGHT = ATTACHMENT_TYPES.register(
//            "treasures_caught", () ->
//                    AttachmentType.builder(() -> List.of(Starcatcher.rl("missingno")))
//                            .serialize(ResourceLocation.CODEC.listOf())
//                            .sync(ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()))
//                            .copyOnDeath()
//                            .build()
//    );

//    public static final Supplier<AttachmentType<List<FishProperties>>> FISHES_NOTIFICATION = ATTACHMENT_TYPES.register(
//            "fishes_notification", () ->
//                    AttachmentType.builder(() -> List.of(FishProperties.DEFAULT))
//                            .serialize(FishProperties.LIST_CODEC)
//                            .sync(FishProperties.STREAM_CODEC_LIST)
//                            .copyOnDeath()
//                            .build()
//    );

    //fishing uuid
    public static String getFishingUUID(Player player)
    {
        return "";
    }

    public static void setFishingUUID(Player player, String s)
    {
    }

    //fish caught counter
    public static List<FishCaughtCounter> getFishCaught(Player player)
    {
        return List.of(new FishCaughtCounter(FishProperties.DEFAULT, 0, 0, 0));
    }

    public static void setFishCaught(Player player, List<FishCaughtCounter> list)
    {
    }

    //trophies caught
    public static List<TrophyProperties> getTrophiesCaught(Player player)
    {
        return List.of(TrophyProperties.DEFAULT);
    }

    public static void setTrophiesCaught(Player player, List<TrophyProperties> list)
    {
    }

    //fish notifications
    public static List<FishProperties> getFishesNotification(Player player)
    {
        return List.of(FishProperties.DEFAULT);
    }

    public static void setFishesNotification(Player player, List<FishProperties> list)
    {
    }

}
