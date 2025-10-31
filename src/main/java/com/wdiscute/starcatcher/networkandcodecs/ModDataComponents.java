package com.wdiscute.starcatcher.networkandcodecs;

import com.mojang.logging.LogUtils;
import com.wdiscute.starcatcher.items.ColorfulBobber;
import com.wdiscute.starcatcher.secretnotes.SecretNote;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

public class ModDataComponents
{
    private static final Logger LOGGER = LogUtils.getLogger();

    public enum Slots implements StringRepresentable
    {
        BOBBER("bobber"),
        BAIT("bait"),
        HOOK("noon");

        private final String key;

        Slots(String key)
        {
            this.key = key;
        }

        public String getSerializedName()
        {
            return this.key;
        }
    }







    //secret note data component
    public static void setSecretNote(ItemStack is, SecretNote.Note note)
    {
        is.getOrCreateTag().putString("secret_note", note.getSerializedName());
    }

    public static SecretNote.Note getSecretNote(ItemStack is)
    {
        return SecretNote.Note.getBySerializedName(is.getTag().getString("secret_note"));
    }










    //fish properties data component
    public static FishProperties getFishProperties(ItemStack is)
    {
        if (is.getOrCreateTag().contains("fish_properties"))
            return FishProperties.CODEC.decode(NbtOps.INSTANCE, is.getTag().get("fish_properties")).result().get().getFirst();

        return FishProperties.DEFAULT;
    }

    public static void setFishProperties(ItemStack is, FishProperties fp)
    {
        CompoundTag compoundTag = new CompoundTag();

        FishProperties.CODEC.encode(fp, NbtOps.INSTANCE, new CompoundTag())
                .resultOrPartial(LOGGER::warn).ifPresent(tag -> compoundTag.put("fish_properties", tag));

        is.getOrCreateTag().put("fish_properties", compoundTag);
    }






    //trophy properties data component
    public static TrophyProperties getTrophyProperties(ItemStack is)
    {
        if (is.getOrCreateTag().contains("trophy_properties"))
            return TrophyProperties.CODEC.decode(NbtOps.INSTANCE, is.getOrCreateTag().get("trophy_properties")).result().get().getFirst();

        return TrophyProperties.DEFAULT;
    }

    public static void setTrophyProperties(ItemStack is, TrophyProperties tp)
    {
        CompoundTag compoundTag = new CompoundTag();

        TrophyProperties.CODEC.encode(tp, NbtOps.INSTANCE, new CompoundTag())
                .resultOrPartial(LOGGER::warn).ifPresent(tag -> compoundTag.put("trophy_properties", tag));

        is.getOrCreateTag().put("trophy_properties", compoundTag);
    }






    //bobber color data component
    public static void setBobberColor(ItemStack is, ColorfulBobber.BobberColor bobberColor)
    {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("starcatcher_r", bobberColor.r());
        tag.putFloat("starcatcher_g", bobberColor.g());
        tag.putFloat("starcatcher_b", bobberColor.b());
        is.getOrCreateTag().put("starcatcher_bobber_color", tag);
    }

    public static ColorfulBobber.BobberColor getBobberColor(ItemStack is)
    {
        if (is.hasTag())
        {
            CompoundTag compound = is.getTag().getCompound("starcatcher_bobber_color");

            return new ColorfulBobber.BobberColor(
                    compound.getFloat("starcatcher_r"),
                    compound.getFloat("starcatcher_g"),
                    compound.getFloat("starcatcher_b")
            );
        }
        return ColorfulBobber.BobberColor.DEFAULT;
    }






    //bobber, bait, hook data component
    public static void setItemInSlot(ItemStack is, Slots slotToSave, ItemStack stackToSave)
    {
        if (!stackToSave.isEmpty())
        {
            CompoundTag tag = new CompoundTag();
            stackToSave.save(tag);
            is.getOrCreateTag().put(slotToSave.getSerializedName(), tag);
        }
    }

    public static ItemStack getItemInSlot(ItemStack is, Slots slotToGet)
    {
        if (is.hasTag())
        {
            CompoundTag compound = is.getTag().getCompound(slotToGet.getSerializedName());
            return ItemStack.of(compound);
        }
        return ItemStack.EMPTY;
    }
}
