package ipsis.woot.loot.schools;

import ipsis.Woot;
import ipsis.woot.util.*;
import ipsis.woot.oss.LogHelper;
import ipsis.woot.farmstructure.IFarmSetup;
import ipsis.woot.farming.ITickTracker;
import ipsis.woot.loot.ILootLearner;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDropsEvent;

import java.util.List;

public class SkyBoxSchool implements ILootLearner {

    private boolean hasSkyBox = false;
    private AxisAlignedBB axisAlignedBB;

    /**
     * Creates a 3x3x3 box in the sky to capture the drops while learning
     */
    private void createSkybox(World world, BlockPos factoryPos) {

       Block b = Blocks.COBBLESTONE;

       int worldHeight = world.getHeight();
       int yMin = worldHeight - 5;
       int yMax = worldHeight - 1;

       for (int y = yMin; y <= yMax; y++) {
           for (int x = -2; x <= 2; x++) {
               for (int z = -2; z <= 2; z++) {

                   boolean change = false;
                   if (y == yMin || y == yMax || x == -2 || x == 2 || z == -2 || z == 2)
                       change = true;

                   if (change) {
                       BlockPos p = new BlockPos(factoryPos.getX() + x, y, factoryPos.getZ() + z);
                       if (world.getBlockState(p).getBlock() != b)
                           world.setBlockState(p, b.getDefaultState(), 3);
                   }
               }
           }
       }

       hasSkyBox = true;
    }

    private void destroySkyBox(World world, BlockPos factoryPos) {

        int worldHeight = world.getHeight();
        int yMin = worldHeight - 5;
        int yMax = worldHeight - 1;

        for (int y = yMax; y >= yMin; y--) {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos p = new BlockPos(factoryPos.getX() + x, y, factoryPos.getZ() + z);
                    if (world.getBlockState(p).getBlock() != Blocks.AIR)
                        world.setBlockToAir(p);
                }
            }
        }

        hasSkyBox = false;
    }

    private void checkSkybox(World world, BlockPos origin, IFarmSetup farmSetup) {

        WootMobName wootMobName = farmSetup.getWootMobName();
        EnumEnchantKey key = farmSetup.getEnchantKey();

        if (!Woot.lootRepository.isFull(wootMobName, key)) {

            if (axisAlignedBB == null) {
                BlockPos spawnPos = new BlockPos(origin.getX(), world.getHeight() - 3, origin.getZ());
                int range = 2;
                axisAlignedBB = new AxisAlignedBB(spawnPos).expand(range, 0, range);
            }

            List<EntityItem> items = world.getEntitiesWithinAABB(
                    EntityItem.class, axisAlignedBB, EntitySelectors.IS_ALIVE);

            if (!items.isEmpty()) {
                LogHelper.info("checkSkybox: learn for " + wootMobName + " - " + items);
                Woot.lootRepository.learn(wootMobName, key, items, false);
                for (EntityItem i : items)
                    ((EntityItem)i).setDead();
            }
        }
    }

    /**
     * ILootLearner
     */

    public void tick(ITickTracker tickTracker, World world, BlockPos origin, IFarmSetup farmSetup) {

        if (hasSkyBox)
            checkSkybox(world, origin, farmSetup);

        if (!tickTracker.hasLearnTickExpired())
            return;

        WootMobName wootMobName = farmSetup.getWootMobName();
        EnumEnchantKey key = farmSetup.getEnchantKey();
        if (!Woot.lootRepository.isFull(wootMobName, key)) {

            if (!hasSkyBox)
                createSkybox(world, origin);

            // Spawn in middle of the sky box
            BlockPos spawnPos = new BlockPos(origin.getX(), world.getHeight() - 3, origin.getZ());
            Woot.entitySpawner.spawn(wootMobName, key, world, spawnPos);

        } else if (hasSkyBox) {

            // Don't need it any more
            destroySkyBox(world, origin);
        }

        tickTracker.resetLearnTickCount();
    }

    /**
     * Only learns when it was generated by our farm
     */
    public void onLivingDropsEvent(LivingDropsEvent e) {

        Woot.debugSetup.trace(DebugSetup.EnumDebugType.LOOT_EVENTS, this, "onLivingDropsEvent", e);

        if (!(e.getEntity() instanceof EntityLiving))
            return;

        DamageSource damageSource = e.getSource();
        if (damageSource == null)
            return;

        if (!FakePlayerPool.isOurFakePlayer(damageSource.getTrueSource()))
            return;

        // Cancel our fake spawns
        e.setCanceled(true);

        WootMobName wootMobName = WootMobNameBuilder.create((EntityLiving)e.getEntity());
        if (wootMobName.isValid()) {

            EnumEnchantKey key = EnumEnchantKey.getEnchantKey(e.getLootingLevel());
            Woot.lootRepository.learn(wootMobName, key, e.getDrops(), true);

            Woot.debugSetup.trace(DebugSetup.EnumDebugType.LOOT_EVENTS, this, "onLivingDropsEvent", wootMobName + " " + key + " " + e.getDrops());
        } else {
            Woot.debugSetup.trace(DebugSetup.EnumDebugType.LOOT_EVENTS, this, "onLivingDropsEvent", "invalid mob " + e.getEntity());
        }
    }
}
