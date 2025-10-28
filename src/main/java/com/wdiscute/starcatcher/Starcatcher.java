package com.wdiscute.starcatcher;

import com.mojang.logging.LogUtils;
import com.wdiscute.starcatcher.bob.FishingBobModel;
import com.wdiscute.starcatcher.bob.FishingBobRenderer;
import com.wdiscute.starcatcher.fishentity.FishEntity;
import com.wdiscute.starcatcher.fishentity.FishRenderer;
import com.wdiscute.starcatcher.fishspotter.FishTrackerLayer;
import com.wdiscute.starcatcher.guide.FishCaughtToast;
import com.wdiscute.starcatcher.networkandcodecs.*;
import com.wdiscute.starcatcher.particles.FishingBitingParticles;
import com.wdiscute.starcatcher.particles.FishingNotificationParticles;
import com.wdiscute.starcatcher.rod.FishingRodScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import javax.tools.Tool;
import java.util.List;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Starcatcher.MOD_ID)
public class Starcatcher
{
    public static final String MOD_ID = "starcatcher";

    public static final ResourceKey<Registry<FishProperties>> FISH_REGISTRY =
            ResourceKey.createRegistryKey(Starcatcher.rl("fish"));

    public static final ResourceKey<Registry<TrophyProperties>> TROPHY_REGISTRY =
            ResourceKey.createRegistryKey(Starcatcher.rl("trophy"));

    private static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation rl(String s)
    {
        return ResourceLocation.fromNamespaceAndPath(Starcatcher.MOD_ID, s);
    }

    @OnlyIn(Dist.CLIENT)
    public static void fishCaughtToast(FishProperties fp)
    {
        Minecraft.getInstance().getToasts().addToast(new FishCaughtToast(fp));
    }

    public Starcatcher(IEventBus eventBus, ModContainer modContainer)
    {
        ModCreativeModeTabs.register(eventBus);
        ModItems.register(eventBus);
        //ModDataComponents.register(eventBus);
        ModSounds.register(eventBus);
        ModEntities.register(eventBus);
        ModParticles.register(eventBus);
        ModMenuTypes.register(eventBus);
        //ModDataAttachments.register(eventBus);

        eventBus.addListener(Tooltips::modifyItemTooltip);
        eventBus.addListener(Tooltips::renderFrame);

        //modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }


    @Mod.EventBusSubscriber(modid = MOD_ID)
    public static class ModEvents
    {

        @SubscribeEvent
        public static void addRegistry(DataPackRegistryEvent.NewRegistry event)
        {
            event.dataPackRegistry(
                    FISH_REGISTRY, FishProperties.CODEC, FishProperties.CODEC);

            event.dataPackRegistry(
                    TROPHY_REGISTRY, TrophyProperties.CODEC, TrophyProperties.CODEC);
        }

        @SubscribeEvent
        public static void registerAttributed(EntityAttributeCreationEvent event)
        {
            event.put(ModEntities.FISH.get(), FishEntity.createAttributes().build());
        }

        @SubscribeEvent
        public static void registerPayloads(RegisterPayloadHandlersEvent event)
        {
            final PayloadRegistrar registrar = event.registrar("1");
            registrar.playToClient(
                    Payloads.FishingPayload.TYPE,
                    Payloads.FishingPayload.STREAM_CODEC,
                    PayloadReceiver::receiveFishingClient
            );

            registrar.playToServer(
                    Payloads.FishingCompletedPayload.TYPE,
                    Payloads.FishingCompletedPayload.STREAM_CODEC,
                    PayloadReceiver::receiveFishingCompletedServer
            );

            registrar.playToClient(
                    Payloads.FishCaughtPayload.TYPE,
                    Payloads.FishCaughtPayload.STREAM_CODEC,
                    PayloadReceiver::receiveFishCaught
            );

            registrar.playToServer(
                    Payloads.FPsSeen.TYPE,
                    Payloads.FPsSeen.STREAM_CODEC,
                    PayloadReceiver::receiveFPsSeen
            );

        }

    }

    @OnlyIn(Dist.CLIENT)
    @Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ModClientEvents
    {

//        @SubscribeEvent
//        public static void trophyTooltip(ItemTooltipEvent event)
//        {
//            List<Component> comp = event.getToolTip();
//            ItemStack stack = event.getItemStack();
//
//            if (stack.has(ModDataComponents.TROPHY))
//            {
//                TrophyProperties tp = stack.get(ModDataComponents.TROPHY);
//
//                if (tp.trophyType() == TrophyProperties.TrophyType.TROPHY)
//                    if (event.getFlags().hasShiftDown())
//                    {
//                        comp.add(Component.translatable("tooltip.libtooltips.generic.shift_down"));
//                        comp.add(Component.translatable("tooltip.libtooltips.generic.empty"));
//                        comp.add(Component.translatable("tooltip.starcatcher.trophy.0"));
//                        comp.add(Component.translatable("tooltip.starcatcher.trophy.1"));
//
//                        List<Component> list = new java.util.ArrayList<>();
//
//                        //all
//                        if (tp.all().total() != 0) list.add(Tooltips.decodeString(
//                                I18n.get("tooltip.starcatcher.trophy.total")
//                                        .replace("&", tp.all().total() + "")
//                                        .replace("$", I18n.get("tooltip.starcatcher.trophy.all"))
//                        ));
//
//                        if (tp.all().unique() != 0) list.add(
//                                Tooltips.decodeString(I18n.get("tooltip.starcatcher.trophy.unique")
//                                        .replace("&", tp.all().unique() + "")
//                                        .replace("$", I18n.get("tooltip.starcatcher.trophy.all"))));
//
//                        //common
//                        if (tp.common().total() != 0) list.add(
//                                Tooltips.decodeString(I18n.get("tooltip.starcatcher.trophy.total")
//                                        .replace("&", tp.common().total() + "")
//                                        .replace("$", I18n.get("tooltip.starcatcher.trophy.common"))));
//
//                        if (tp.common().unique() != 0) list.add(
//                                Tooltips.decodeString(I18n.get("tooltip.starcatcher.trophy.unique")
//                                        .replace("&", tp.common().unique() + "")
//                                        .replace("$", I18n.get("tooltip.starcatcher.trophy.common"))));
//
//                        //uncommon
//                        if (tp.uncommon().total() != 0) list.add(
//                                Tooltips.decodeString(I18n.get("tooltip.starcatcher.trophy.total")
//                                        .replace("&", tp.uncommon().total() + "")
//                                        .replace("$", I18n.get("tooltip.starcatcher.trophy.uncommon"))));
//
//                        if (tp.uncommon().unique() != 0) list.add(
//                                Tooltips.decodeString(I18n.get("tooltip.starcatcher.trophy.unique")
//                                        .replace("&", tp.uncommon().unique() + "")
//                                        .replace("$", I18n.get("tooltip.starcatcher.trophy.uncommon"))));
//
//                        //rare
//                        if (tp.rare().total() != 0) list.add(
//                                Tooltips.decodeString(I18n.get("tooltip.starcatcher.trophy.total")
//                                        .replace("&", tp.rare().total() + "")
//                                        .replace("$", I18n.get("tooltip.starcatcher.trophy.rare"))));
//
//                        if (tp.rare().unique() != 0) list.add(
//                                Tooltips.decodeString(I18n.get("tooltip.starcatcher.trophy.unique")
//                                        .replace("&", tp.rare().unique() + "")
//                                        .replace("$", I18n.get("tooltip.starcatcher.trophy.rare"))));
//
//                        //epic
//                        if (tp.epic().total() != 0) list.add(
//                                Tooltips.decodeString(I18n.get("tooltip.starcatcher.trophy.total")
//                                        .replace("&", tp.epic().total() + "")
//                                        .replace("$", I18n.get("tooltip.starcatcher.trophy.epic"))));
//
//                        if (tp.epic().unique() != 0) list.add(
//                                Tooltips.decodeString(I18n.get("tooltip.starcatcher.trophy.unique")
//                                        .replace("&", tp.epic().unique() + "")
//                                        .replace("$", I18n.get("tooltip.starcatcher.trophy.epic"))));
//
//                        //legendary
//                        if (tp.legendary().total() != 0) list.add(
//                                Tooltips.decodeString(I18n.get("tooltip.starcatcher.trophy.total")
//                                        .replace("&", tp.legendary().total() + "")
//                                        .replace("$", I18n.get("tooltip.starcatcher.trophy.legendary"))));
//
//                        if (tp.legendary().unique() != 0) list.add(
//                                Tooltips.decodeString(I18n.get("tooltip.starcatcher.trophy.unique")
//                                        .replace("&", tp.legendary().unique() + "")
//                                        .replace("$", I18n.get("tooltip.starcatcher.trophy.legendary"))));
//
//                        if (list.size() == 1)
//                        {
//                            comp.add(Component.translatable("tooltip.starcatcher.trophy.once")
//                                    .append(list.getFirst())
//                                    .append(Component.translatable("tooltip.starcatcher.trophy.have_been_caught")));
//                        }
//                        else
//                        {
//                            comp.add(Component.translatable("tooltip.starcatcher.trophy.2"));
//                            comp.addAll(list);
//                        }
//
//                    }
//                    else
//                    {
//                        comp.add(Component.translatable("tooltip.libtooltips.generic.shift_up"));
//                    }
//
//            }
//        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            EntityRenderers.register(ModEntities.FISHING_BOB.get(), FishingBobRenderer::new);
            EntityRenderers.register(ModEntities.BOTTLE.get(), ThrownItemRenderer::new);
            EntityRenderers.register(ModEntities.FISH.get(), FishRenderer::new);

            ModItemProperties.addCustomItemProperties();

            MenuScreens.register(ModMenuTypes.FISHING_ROD_MENU.get(), FishingRodScreen::new);
        }

        @SubscribeEvent
        public static void FishSpotterLayer(RegisterGuiOverlaysEvent event)
        {
            event.registerAboveAll("fish_tracker", new FishTrackerLayer());
        }

        @SubscribeEvent
        public static void registerParticleFactories(RegisterParticleProvidersEvent event)
        {
            event.registerSpriteSet(ModParticles.FISHING_NOTIFICATION.get(), FishingNotificationParticles.Provider::new);
            event.registerSpriteSet(ModParticles.FISHING_BITING.get(), FishingBitingParticles.Provider::new);
        }

        @SubscribeEvent
        public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event)
        {
            event.registerLayerDefinition(FishingBobModel.LAYER_LOCATION, FishingBobModel::createBodyLayer);
        }

    }

}
