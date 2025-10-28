package com.wdiscute.starcatcher.networkandcodecs;

import com.wdiscute.starcatcher.items.ColorfulBobber;
import com.wdiscute.starcatcher.secretnotes.SecretNote;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;

public class ModDataComponents
{
    public static final String BOBBER = "bobber";
    public static final String BAIT = "bait";
    public static final String HOOK = "hook";

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

    }

    public static SecretNote.Note getSecretNote(ItemStack is)
    {
        return SecretNote.Note.SAMPLE_NOTE;
    }



    //fish properties data component
    public static FishProperties getFishProperties(ItemStack is)
    {
        return FishProperties.DEFAULT;
    }

    public static void setFishProperties(ItemStack is, FishProperties tp)
    {

    }



    //trophy properties data component
    public static void setTrophyProperties(ItemStack is, TrophyProperties tp)
    {

    }

    public static TrophyProperties getTrophyProperties(ItemStack is)
    {
        return TrophyProperties.DEFAULT;
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
