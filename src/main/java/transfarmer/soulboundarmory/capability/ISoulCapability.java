package transfarmer.soulboundarmory.capability;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;
import transfarmer.soulboundarmory.statistics.*;

import java.util.List;
import java.util.Map;

public interface ISoulCapability {
    void setStatistics(int[][] data, float[][] attributes, int[][] enchantments);

    int[][] getData();

    float[][] getAttributes();

    int[][] getEnchantments();

    void setData(int[][] data);

    void setAttributes(float[][] attributes);

    void setEnchantments(int[][] enchantments);

    IType getType(ItemStack itemStack);

    IType getCurrentType();

    void setCurrentType(IType type);

    void setCurrentType(int index);

    int getItemAmount();

    int getDatumAmount();

    int getAttributeAmount();

    int getEnchantmentAmount();

    int getDatum(SoulDatum datum, IType type);

    void setDatum(int amount, SoulDatum datum, IType type);

    boolean addDatum(int amount, SoulDatum datum, IType type);

    float getAttribute(SoulAttribute attribute, IType type);

    void setAttribute(float value, SoulAttribute attribute, IType type);

    void addAttribute(int amount, SoulAttribute attribute, IType type);

    void setData(int[] data, IType type);

    void setAttributes(float[] attributes, IType type);

    int getEnchantment(SoulEnchantment enchantment, IType type);

    void setEnchantments(int[] enchantments, IType type);

    void addEnchantment(int amount, SoulEnchantment enchantment, IType type);

    int getNextLevelXP(IType type);

    int getCurrentTab();

    void setCurrentTab(int currentTab);

    int getBoundSlot();

    void bindSlot(int boundSlot);

    void unbindSlot();

    Map<SoulEnchantment, Integer> getEnchantments(IType type);

    AttributeModifier[] getAttributeModifiers(IType type);

    ItemStack getItemStack(IType type);

    ItemStack getItemStack(ItemStack itemStack);

    List<String> getTooltip(IType type);
}