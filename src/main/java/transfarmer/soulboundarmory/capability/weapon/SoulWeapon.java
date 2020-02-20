package transfarmer.soulboundarmory.capability.weapon;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.SideOnly;
import transfarmer.soulboundarmory.Configuration;
import transfarmer.soulboundarmory.capability.SoulItemHelper;
import transfarmer.soulboundarmory.client.i18n.Mappings;
import transfarmer.soulboundarmory.statistics.IType;
import transfarmer.soulboundarmory.statistics.SoulAttribute;
import transfarmer.soulboundarmory.statistics.SoulDatum;
import transfarmer.soulboundarmory.statistics.SoulEnchantment;
import transfarmer.soulboundarmory.statistics.weapon.SoulWeaponAttribute;
import transfarmer.soulboundarmory.statistics.weapon.SoulWeaponDatum;
import transfarmer.soulboundarmory.statistics.weapon.SoulWeaponEnchantment;
import transfarmer.soulboundarmory.statistics.weapon.SoulWeaponType;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.inventory.EntityEquipmentSlot.MAINHAND;
import static net.minecraftforge.common.util.Constants.AttributeModifierOperation.ADD;
import static net.minecraftforge.fml.relauncher.Side.CLIENT;
import static transfarmer.soulboundarmory.statistics.SoulEnchantment.SOUL_SHARPNESS;
import static transfarmer.soulboundarmory.statistics.weapon.SoulWeaponAttribute.*;
import static transfarmer.soulboundarmory.statistics.weapon.SoulWeaponDatum.*;

public class SoulWeapon implements ISoulWeapon {
    private IType currentType;
    private int currentTab = 1;
    private int attackCooldown = 0;
    private int lightningCooldown = 60;
    private int boundSlot = -1;
    private int[][] data = new int[this.getItemAmount()][this.getDatumAmount()];
    private float[][] attributes = new float[this.getItemAmount()][this.getAttributeAmount()];
    private int[][] enchantments = new int[this.getItemAmount()][this.getEnchantmentAmount()];

    @Override
    public void setStatistics(final int[][] data, final float[][] attributes, final int[][] enchantments) {
        this.data = data;
        this.attributes = attributes;
        this.enchantments = enchantments;
    }

    @Override
    public void setData(final int[][] data) {
        this.data = data;
    }

    @Override
    public void setAttributes(final float[][] attributes) {
        this.attributes = attributes;
    }

    @Override
    public void setAttributes(final float[] attributes, final IType type) {
        this.attributes[type.getIndex()] = attributes;
    }

    @Override
    public void setEnchantments(final int[][] enchantments) {
        this.enchantments = enchantments;
    }

    @Override
    public IType getType(final ItemStack itemStack) {
        return SoulWeaponType.getType(itemStack);
    }

    @Override
    public void setEnchantments(final int[] enchantments, final IType type) {
        this.enchantments[type.getIndex()] = enchantments;
    }

    @Override
    public int[][] getData() {
        return this.data;
    }

    @Override
    public float[][] getAttributes() {
        return this.attributes;
    }

    @Override
    public int[][] getEnchantments() {
        return this.enchantments;
    }

    @Override
    public IType getCurrentType() {
        return this.currentType;
    }

    @Override
    public float getAttribute(final SoulAttribute attribute, final IType type) {
        return this.attributes[type.getIndex()][attribute.getIndex()];
    }

    @Override
    public void setAttribute(final float value, final SoulAttribute attribute, final IType type) {
        this.attributes[type.getIndex()][attribute.getIndex()] = value;
    }

    @Override
    public void addAttribute(final int amount, final SoulAttribute attribute, final IType type) {
        final int sign = (int) Math.signum(amount);

        for (int i = 0; i < Math.abs(amount); i++) {
            this.addDatum(-sign, ATTRIBUTE_POINTS, type);
            this.addDatum(sign, SPENT_ATTRIBUTE_POINTS, type);

            if ((attribute.equals(CRITICAL) && this.getAttribute(CRITICAL, type) + sign * CRITICAL.getIncrease(type) >= 100)) {
                this.setAttribute(100, attribute, type);
                return;
            } else if (this.attributes[type.getIndex()][attribute.getIndex()] + sign * attribute.getIncrease(type) > 0.0001) {
                this.attributes[type.getIndex()][attribute.getIndex()] += sign * attribute.getIncrease(type);
            } else {
                this.attributes[type.getIndex()][attribute.getIndex()] = 0;
                return;
            }
        }
    }

    @Override
    public void setData(final int[] data, final IType type) {
        this.data[type.getIndex()] = data;
    }

    @Override
    public float getAttackSpeed(final IType type) {
        return this.attributes[type.getIndex()][ATTACK_SPEED.getIndex()] + type.getSoulItem().getAttackSpeed();
    }

    @Override
    public float getEffectiveAttackSpeed(final IType type) {
        return this.getAttackSpeed(type) + 4;
    }

    @Override
    public float getAttackDamage(final IType type) {
        return this.attributes[type.getIndex()][ATTACK_DAMAGE.getIndex()] + type.getSoulItem().getDamage();
    }

    @Override
    public float getEffectiveAttackDamage(final IType type) {
        float attackDamage = this.getAttackDamage(type);

        if (this.getEnchantment(SOUL_SHARPNESS, type) > 0) {
            attackDamage += 1 + (this.getEnchantment(SOUL_SHARPNESS, type) - 1) / 2F;
        }

        return attackDamage;
    }

    @Override
    public ItemStack getItemStack(final ItemStack itemStack) {
        return getItemStack(SoulWeaponType.getType(itemStack));
    }

    @Override
    public ItemStack getItemStack(final IType type) {
        final ItemStack itemStack = new ItemStack(type.getItem());
        final AttributeModifier[] attributeModifiers = getAttributeModifiers(type);
        final Map<SoulEnchantment, Integer> enchantments = this.getEnchantments(type);

        itemStack.addAttributeModifier(SharedMonsterAttributes.ATTACK_SPEED.getName(), attributeModifiers[0], MAINHAND);
        itemStack.addAttributeModifier(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), attributeModifiers[1], MAINHAND);
        itemStack.addAttributeModifier(EntityPlayer.REACH_DISTANCE.getName(), attributeModifiers[2], MAINHAND);

        enchantments.forEach((final SoulEnchantment enchantment, final Integer level) -> itemStack.addEnchantment(enchantment.getEnchantment(), level));

        return itemStack;
    }

    @Override
    public AttributeModifier[] getAttributeModifiers(final IType type) {
        return new AttributeModifier[]{
                new AttributeModifier(SoulItemHelper.ATTACK_SPEED_UUID, "generic.attackSpeed", this.getAttackSpeed(type), ADD),
                new AttributeModifier(SoulItemHelper.ATTACK_DAMAGE_UUID, "generic.attackDamage", this.getAttackDamage(type), ADD),
                new AttributeModifier(SoulItemHelper.REACH_DISTANCE_UUID, "generic.reachDistance", this.currentType.getSoulItem().getReachDistance(), ADD)
        };
    }

    @Override
    public Map<SoulEnchantment, Integer> getEnchantments(final IType type) {
        final Map<SoulEnchantment, Integer> enchantments = new LinkedHashMap<>();

        for (final SoulEnchantment enchantment : SoulWeaponEnchantment.get()) {
            final int level = this.getEnchantment(enchantment, type);

            if (level > 0) {
                enchantments.put(enchantment, level);
            }
        }

        return enchantments;
    }

    @SideOnly(CLIENT)
    @Override
    public List<String> getTooltip(final IType type) {
        final NumberFormat FORMAT = DecimalFormat.getInstance();
        final List<String> tooltip = new ArrayList<>(7);
        final Map<SoulEnchantment, Integer> enchantments = this.getEnchantments(type);

        float attackDamage = this.getAttackDamage(type) + 1;

        if (enchantments.containsKey(SOUL_SHARPNESS)) {
            attackDamage += 1 + (enchantments.get(SOUL_SHARPNESS) - 1) / 2F;
        }

        tooltip.add(String.format(" %s%s %s", Mappings.ATTACK_SPEED_FORMAT, FORMAT.format(this.getAttackSpeed(type) + 4), Mappings.ATTACK_SPEED_NAME));
        tooltip.add(String.format(" %s%s %s", Mappings.ATTACK_DAMAGE_FORMAT, FORMAT.format(attackDamage), Mappings.ATTACK_DAMAGE_NAME));

        tooltip.add("");
        tooltip.add("");

        if (this.getAttribute(CRITICAL, type) > 0) {
            tooltip.add(String.format(" %s%s%% %s", Mappings.CRITICAL_FORMAT, FORMAT.format(this.getAttribute(CRITICAL, type)), Mappings.CRITICAL_NAME));
        }
        if (this.getAttribute(KNOCKBACK_ATTRIBUTE, type) > 0) {
            tooltip.add(String.format(" %s%s %s", Mappings.KNOCKBACK_ATTRIBUTE_FORMAT, FORMAT.format(this.getAttribute(KNOCKBACK_ATTRIBUTE, type)), Mappings.KNOCKBACK_ATTRIBUTE_NAME));
        }
        if (this.getAttribute(EFFICIENCY_ATTRIBUTE, type) > 0) {
            tooltip.add(String.format(" %s%s %s", Mappings.WEAPON_EFFICIENCY_FORMAT, FORMAT.format(this.getAttribute(EFFICIENCY_ATTRIBUTE, type)), Mappings.EFFICIENCY_NAME));
        }

        return tooltip;
    }

    @Override
    public int getNextLevelXP(final IType type) {
        return this.getDatum(LEVEL, type) >= Configuration.maxLevel ?
                1 : Configuration.initialWeaponXP + 4 * (int) Math.round(Math.pow(this.getDatum(LEVEL, type), 1.5));
    }

    @Override
    public int getDatum(final SoulDatum datum, final IType type) {
        return this.data[type.getIndex()][datum.getIndex()];
    }

    @Override
    public void setDatum(final int value, final SoulDatum datum, final IType type) {
        this.data[type.getIndex()][datum.getIndex()] = value;
    }

    @Override
    public boolean addDatum(final int amount, final SoulDatum datum, final IType type) {
        if (XP.equals(datum)) {
            this.data[type.getIndex()][XP.getIndex()] += amount;

            if (this.getDatum(XP, type) >= this.getNextLevelXP(type) && this.getDatum(LEVEL, type) < Configuration.maxLevel) {
                final int nextLevelXP = this.getNextLevelXP(type);
                this.addDatum(1, LEVEL, type);
                this.addDatum(-nextLevelXP, XP, type);
                return true;
            }
        } else if (LEVEL.equals(datum)) {
            final int level = ++this.data[type.getIndex()][LEVEL.getIndex()];
            if (level % (Configuration.levelsPerEnchantment) == 0) {
                this.addDatum(1, ENCHANTMENT_POINTS, type);
            }

            if (level % (Configuration.levelsPerSkill) == 0 && this.getDatum(SKILLS, type) < type.getSkills().length) {
                this.addDatum(1, SKILLS, type);
            }

            this.addDatum(1, ATTRIBUTE_POINTS, type);
        } else {
            this.data[type.getIndex()][datum.getIndex()] += amount;
        }

        return false;
    }

    @Override
    public int getEnchantment(final SoulEnchantment enchantment, final IType type) {
        return this.enchantments[type.getIndex()][enchantment.getIndex()];
    }

    @Override
    public void addEnchantment(final int amount, final SoulEnchantment enchantment, final IType type) {
        final int sign = (int) Math.signum(amount);

        for (int i = 0; i < Math.abs(amount); i++) {
            if (this.getEnchantment(enchantment, type) + sign >= 0) {
                this.addDatum(-sign, ENCHANTMENT_POINTS, type);
                this.addDatum(sign, SPENT_ENCHANTMENT_POINTS, type);

                this.enchantments[type.getIndex()][enchantment.getIndex()] += sign;
            } else {
                return;
            }
        }
    }

    @Override
    public void setCurrentTab(final int tab) {
        this.currentTab = tab;
    }

    @Override
    public int getCurrentTab() {
        return this.currentTab;
    }

    @Override
    public void setCurrentType(final IType type) {
        this.currentType = type;
    }

    @Override
    public void setCurrentType(final int index) {
        this.currentType = SoulWeaponType.getType(index);
    }

    @Override
    public void setAttackCooldown(final int ticks) {
        this.attackCooldown = ticks;
    }

    @Override
    public void resetCooldown(final IType type) {
        this.attackCooldown = this.getCooldown(type);
    }

    @Override
    public void addCooldown(final int ticks) {
        this.attackCooldown += ticks;
    }

    @Override
    public int getAttackCooldown() {
        return this.attackCooldown;
    }

    @Override
    public int getCooldown(final IType type) {
        return Math.round(20 / (4 + this.getAttackSpeed(type)));
    }

    @Override
    public float getAttackRatio(final IType type) {
        return 1 - (float) this.getAttackCooldown() / this.getCooldown(type);
    }

    @Override
    public int getBoundSlot() {
        return this.boundSlot;
    }

    @Override
    public int getItemAmount() {
        return SoulWeaponType.getAmount();
    }

    @Override
    public int getDatumAmount() {
        return SoulWeaponDatum.getAmount();
    }

    @Override
    public int getAttributeAmount() {
        return SoulWeaponAttribute.getAmount();
    }

    @Override
    public int getEnchantmentAmount() {
        return SoulWeaponEnchantment.getAmount();
    }

    @Override
    public void bindSlot(final int boundSlot) {
        this.boundSlot = boundSlot;
    }

    @Override
    public void unbindSlot() {
        this.boundSlot = -1;
    }

    @Override
    public int getLightningCooldown() {
        return lightningCooldown;
    }

    @Override
    public void resetLightningCooldown() {
        this.lightningCooldown = Math.round(96 / this.getEffectiveAttackSpeed(this.currentType));
    }

    @Override
    public void decrementLightningCooldown() {
        this.lightningCooldown--;
    }
}