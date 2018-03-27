package ecomod.core.stuff;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.registry.GameRegistry;
import ecomod.api.EcomodItems;
import ecomod.common.items.ItemCore;
import ecomod.common.items.ItemCraftIngredient;
import ecomod.common.items.ItemPollutionEffectsBook;
import ecomod.common.items.ItemRespirator;
import ecomod.common.utils.EMUtils;
import ecomod.core.EMConsts;
import ecomod.core.EcologyMod;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;


public class EMItems 
{
	public static ArmorMaterial RESPIRATOR_MATERIAL = null;
	
	public static void doPreInit()
	{
			EcomodItems.inited = true;
			
			EcologyMod.log.info("Registering items");
			
			EcomodItems.CORE = new ItemCore().setUnlocalizedName(EMConsts.modid+".core");
			
			EcomodItems.CRAFT_INGREDIENT = new ItemCraftIngredient().setUnlocalizedName(EMConsts.modid+".craft_ingredient");
			
			RESPIRATOR_MATERIAL = EnumHelper.addArmorMaterial(EMConsts.modid+":respirator", 13, new int[]{2, 2, 3, 1}, 7);
			
			RESPIRATOR_MATERIAL.customCraftingMaterial = Items.leather;
			
			if(RESPIRATOR_MATERIAL == null)
			{
				EcologyMod.log.error("Unable to register Respirator Armor Material!!!");
			}
			
			EcomodItems.RESPIRATOR = new ItemRespirator().setUnlocalizedName(EMConsts.modid+".respirator");
			
			EcomodItems.POLLUTION_EFFECTS_BOOK = new ItemPollutionEffectsBook().setUnlocalizedName(EMConsts.modid+".effects_book");
			
			GameRegistry.registerItem(EcomodItems.CORE, "core");
			GameRegistry.registerItem(EcomodItems.CRAFT_INGREDIENT, "craft_ingredient");
			GameRegistry.registerItem(EcomodItems.RESPIRATOR, "respirator");
			GameRegistry.registerItem(EcomodItems.POLLUTION_EFFECTS_BOOK, "pollution_effects_book");
	}
}
