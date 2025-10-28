package com.wdiscute.starcatcher.datagen;

import com.wdiscute.starcatcher.Starcatcher;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Starcatcher.MOD_ID)
public class DataGenerators
{

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event)
    {
        DataGenerator gen = event.getGenerator();

        //fish properties
        gen.addProvider(
                event.includeServer(),
                (DataProvider.Factory<FishAndTrophiesPropertiesProvider>) output -> new FishAndTrophiesPropertiesProvider(output,
                       event.getLookupProvider())
        );

        //item models
        gen.addProvider(event.includeServer(), new ModItemModelProvider(gen.getPackOutput(), event.getExistingFileHelper()));

        //block tags
        BlockTagsProvider btp = new ModBlocksTagProvider(gen.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper());
        gen.addProvider(event.includeServer(), btp);

        //item tags
        ItemTagsProvider itp = new ModItemsTagProvider(gen.getPackOutput(), event.getLookupProvider(), btp.contentsGetter(), event.getExistingFileHelper());
        gen.addProvider(event.includeServer(), itp);
    }


}
