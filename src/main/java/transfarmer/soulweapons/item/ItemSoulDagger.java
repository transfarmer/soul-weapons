package transfarmer.soulweapons.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import transfarmer.soulweapons.entity.EntitySoulDagger;

import static transfarmer.soulweapons.capability.SoulWeaponProvider.CAPABILITY;
import static transfarmer.soulweapons.data.SoulWeaponDatum.SKILLS;
import static transfarmer.soulweapons.data.SoulWeaponType.DAGGER;

public class ItemSoulDagger extends ItemSoulWeapon {
    public ItemSoulDagger() {
        super(1, -2, 3);
    }

    @Override
    public int getMaxItemUseDuration(final ItemStack itemStack) {
        return 1200;
    }

    @Override
    public EnumAction getItemUseAction(final ItemStack itemStack) {
        return EnumAction.BOW;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        if (!world.isRemote && player.getCapability(CAPABILITY, null).getDatum(SKILLS, DAGGER) >= 1) {
            player.setActiveHand(hand);

            return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
        }

        return new ActionResult<>(EnumActionResult.FAIL, player.getHeldItem(hand));
    }

    @Override
    public void onPlayerStoppedUsing(final ItemStack itemStack, final World world, final EntityLivingBase entity, final int timeLeft) {
        if (!world.isRemote) {
            final EntitySoulDagger dagger = new EntitySoulDagger(world, entity, itemStack);
            final EntityPlayer player = (EntityPlayer) entity;
            final float attackSpeed = 4 + player.getCapability(CAPABILITY, null).getAttackSpeed(DAGGER);
            final float velocity = Math.min(attackSpeed * (attackSpeed / 2) * (this.getMaxItemUseDuration(itemStack) - timeLeft) / 20, attackSpeed);

            if (!player.isCreative()) {
                player.inventory.deleteStack(itemStack);
            }

            dagger.shoot(entity, entity.rotationPitch, entity.rotationYaw, velocity, attackSpeed, 1);
            world.spawnEntity(dagger);
        }
    }
}
