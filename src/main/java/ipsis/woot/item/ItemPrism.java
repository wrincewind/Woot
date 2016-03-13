package ipsis.woot.item;

import ipsis.Woot;
import ipsis.oss.client.ModelHelper;
import ipsis.woot.init.ModItems;
import ipsis.woot.reference.Lang;
import ipsis.woot.tileentity.TileEntityMobFactoryController;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemPrism extends ItemWoot {

    public static final String BASENAME = "prism";

    public ItemPrism() {

        super(BASENAME);
        setMaxStackSize(1);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void initModel() {

        ModelHelper.registerItem(ModItems.itemPrism, BASENAME);
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {

        if (attacker.worldObj.isRemote)
            return false;

        if (hasMobName(stack))
            return false;

        String wootName = Woot.mobRegistry.onEntityLiving((EntityLiving)target);
        if (!Woot.mobRegistry.isValidMobName(wootName))
            return false;

        String displayName = Woot.mobRegistry.getDisplayName(wootName);
        setMobName(stack, wootName, displayName, ((EntityLiving) target).experienceValue);
        return true;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {

        if (worldIn.isRemote)
            return true;

        if (!hasMobName(stack))
            return false;

        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityMobFactoryController) {
            if (!Woot.mobRegistry.isValidMobName(((TileEntityMobFactoryController) te).getMobName())) {
                ((TileEntityMobFactoryController) te).setMobName(getMobName(stack), getDisplayName(stack), getXp(stack));
                if (!playerIn.capabilities.isCreativeMode)
                    stack.stackSize--;
                return true;
            }
        }

        return false;
    }

    /**
     * All we store in the prism is the name of the mob
     */
    static final String NBT_MOBNAME = "mobName";
    static final String NBT_DISPLAYNAME = "displayName";
    static final String NBT_XP_VALUE = "mobXpCost";
    public static void setMobName(ItemStack itemStack, String mobName, String displayName, int xp) {

        if (itemStack.getTagCompound() == null)
            itemStack.setTagCompound(new NBTTagCompound());

        itemStack.getTagCompound().setString(NBT_MOBNAME, mobName);
        itemStack.getTagCompound().setString(NBT_DISPLAYNAME, displayName);
        itemStack.getTagCompound().setInteger(NBT_XP_VALUE, xp);
    }

    public static String getMobName(ItemStack itemStack) {

        if (itemStack.getTagCompound() == null)
            return "";

        return itemStack.getTagCompound().getString(NBT_MOBNAME);
    }

    public static String getDisplayName(ItemStack itemStack) {

        if (itemStack.getTagCompound() == null)
            return "";

        return itemStack.getTagCompound().getString(NBT_DISPLAYNAME);
    }

    public static int getXp(ItemStack itemStack) {

        if (itemStack.getTagCompound() == null)
            return 1;

        return itemStack.getTagCompound().getInteger(NBT_XP_VALUE);
    }

    static boolean hasMobName(ItemStack itemStack) {

        if (itemStack.getItem() != ModItems.itemPrism)
            return false;

        return Woot.mobRegistry.isValidMobName(getMobName(itemStack));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {

        tooltip.add(StatCollector.translateToLocal(Lang.TAG_TOOLTIP + BASENAME + ".0"));
        tooltip.add(StatCollector.translateToLocal(Lang.TAG_TOOLTIP + BASENAME + ".1"));
        if (stack != null && hasMobName(stack) && Woot.mobRegistry.isValidMobName(getMobName(stack))) {
            String displayName = getDisplayName(stack);
            if (!displayName.equals(""))
                tooltip.add(EnumChatFormatting.GREEN + String.format("Mob: %s", StatCollector.translateToLocal(displayName)));

            tooltip.add(EnumChatFormatting.GREEN + String.format("Xp: %d", getXp(stack)));
        }
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        
        return hasMobName(stack);
    }
}
