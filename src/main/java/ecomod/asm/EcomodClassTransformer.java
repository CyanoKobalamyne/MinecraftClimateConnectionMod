package ecomod.asm;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import ecomod.core.EMConsts;
import ecomod.core.EcologyMod;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.classloading.FMLForgePlugin;
import net.minecraftforge.common.MinecraftForge;

public class EcomodClassTransformer implements IClassTransformer
{
	boolean b = false;
	static Logger log = LogManager.getLogger("EcomodASM");
	
	private static final boolean DEBUG = false;
	
	public static List<String> failed_transformers = new ArrayList<String>();
	
	public EcomodClassTransformer()
	{
		EMConsts.asm_transformer_inited = true;
		log.info("Initializing EcomodClassTransformer.");
	}
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		if(DEBUG)
		if(strictCompareByEnvironment(transformedName, "net.minecraft.client.renderer.texture.TextureManager", "net.minecraft.client.renderer.texture.TextureManager"))
			test_handleTextureManager(name, transformedName, basicClass);
		
		name = transformedName;
		
		basicClass = handleGrowable(name, basicClass);
		
		if(strictCompareByEnvironment(name, "net.minecraft.block.BlockGrass", "net.minecraft.block.BlockGrass"))
			return handleBlockGrass(name, basicClass);
		
		if(strictCompareByEnvironment(name, "net.minecraft.block.BlockFire", "net.minecraft.block.BlockFire"))
			return handleBlockFire(name, basicClass);
		
		if(strictCompareByEnvironment(name, "net.minecraft.block.BlockLeaves", "net.minecraft.block.BlockLeaves"))
			return handleBlockLeaves(name, basicClass);
		
		if(strictCompareByEnvironment(name, "net.minecraft.block.BlockFarmland", "net.minecraft.block.BlockFarmland"))
			return handleBlockFarmland(name, basicClass);
		
		if(strictCompareByEnvironment(name, "net.minecraft.client.renderer.EntityRenderer", "net.minecraft.client.renderer.EntityRenderer"))
			return handleEntityRenderer(name, basicClass);
		
		if(strictCompareByEnvironment(name, "net.minecraft.item.Item", "net.minecraft.item.Item"))
			return handleItem(name, basicClass);
			
		if(strictCompareByEnvironment(name, "net.minecraft.item.ItemFood", "net.minecraft.item.ItemFood"))
			return handleItemFood(name, basicClass);
		
		if(strictCompareByEnvironment(name, "net.minecraft.tileentity.TileEntityFurnace", "net.minecraft.tileentity.TileEntityFurnace"))
			return handleTileFurnace(name, basicClass);
		
		if(strictCompareByEnvironment(name, "net.minecraft.entity.item.EntityItem", "net.minecraft.entity.item.EntityItem"))
			return handleEntityItem(name, basicClass);
		
		if(strictCompareByEnvironment(name, "net.minecraft.entity.projectile.EntityFishHook", "net.minecraft.entity.projectile.EntityFishHook"))
			return handleFishHook(name, basicClass);
		
		if(strictCompareByEnvironment(name, "net.minecraft.tileentity.TileEntityBrewingStand", "net.minecraft.tileentity.TileEntityBrewingStand"))
			return handleBrewingStand(name, basicClass);
			
		if(strictCompareByEnvironment(name, "org.blockartistry.DynSurround.client.weather.WeatherProperties", "org.blockartistry.DynSurround.client.weather.WeatherProperties"))
			return handle_DynamicSurroundings_getRainTexture(name, basicClass);
		
		return basicClass;
	}

	
	public static String chooseByEnvironment(String deobf, String obf)
	{
		return FMLForgePlugin.RUNTIME_DEOBF ? obf : deobf;
	}
	
	private static final String onUpdate_desc = "(Lahb;IIILjava/util/Random;)V";
	
	private void patchFailed(String name, Exception e) {
		log.error("Unable to patch "+name+ '!');
		log.error(e.toString());
		e.printStackTrace();

		failed_transformers.add(name);
	}
	
	private byte[] handle_DynamicSurroundings_getRainTexture(String name, byte[] bytecode)
	{
			log.info("Transforming "+name);
			log.info("Initial size: "+bytecode.length+" bytes");
	
			byte[] bytes = bytecode.clone();
	
			try
			{
				ClassNode classNode = new ClassNode();
				ClassReader classReader = new ClassReader(bytes);
				classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
				ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
	
				if(DEBUG)
					printClassInfo(name, classNode);
	
				MethodNode mn = getMethod(classNode, "getRainTexture", "getRainTexture", "()Lnet/minecraft/util/ResourceLocation;", "()Lnet/minecraft/util/ResourceLocation;");
	
				InsnList lst = new InsnList();
	
				lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "ecomod/asm/EcomodASMHooks", "getRainTexture", "(Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/util/ResourceLocation;", false));
	
				mn.instructions.insert(mn.instructions.getLast().getPrevious().getPrevious(), lst);
				
				classNode.accept(cw);
				bytes = cw.toByteArray();

				log.info("Transformed "+name);
				log.info("Final size: "+bytes.length+" bytes");

				return bytes;
			}
			catch(Exception e)
			{
				patchFailed(name, e);
				return bytecode;
			}
	}
	
	private byte[] handleBlockGrass(String name, byte[] bytecode)
	{
		log.info("Transforming "+name);
		log.info("Initial size: "+bytecode.length+" bytes");

		byte[] bytes = bytecode.clone();
		
		try
		{
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(bytes);
			classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			
			if(DEBUG)
				printClassInfo(name, classNode);
			
			MethodNode mn = getMethod(classNode, "updateTick", "updateTick!&!a", "(Lnet/minecraft/world/World;IIILjava/util/Random;)V", "(Lnet/minecraft/world/World;IIILjava/util/Random;)V!&!"+onUpdate_desc);
			
			InsnList lst = new InsnList();
			
			lst.add(new VarInsnNode(Opcodes.ALOAD, 1));
			lst.add(new VarInsnNode(Opcodes.ILOAD, 2));
			lst.add(new VarInsnNode(Opcodes.ILOAD, 3));
			lst.add(new VarInsnNode(Opcodes.ILOAD, 4));
			lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "ecomod/asm/EcomodASMHooks", "updateTickAddition", "(Lnet/minecraft/world/World;III)V", false));
			lst.add(new LabelNode());
			
			mn.instructions.insert(mn.instructions.getLast().getPrevious().getPrevious(), lst);
			
			classNode.accept(cw);
			bytes = cw.toByteArray();
			
			log.info("Transformed "+name);
			log.info("Final size: "+bytes.length+" bytes");
			
			return bytes;
		}
		catch(Exception e)
		{
			patchFailed(name, e);
			return bytecode;
		}
	}
	
	private byte[] handleEntityRenderer(String name, byte[] bytecode)
	{
		log.info("Transforming "+name);
		log.info("Initial size: "+bytecode.length+" bytes");
		
		byte[] bytes = bytecode.clone();
		
		try
		{
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(bytes);
			classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			
			if(DEBUG)
				printClassInfo(name, classNode);
			
			MethodNode mn = getMethod(classNode, "renderRainSnow", "func_78473_a!&!e", "(F)V", "(F)V");
			
			InsnList lst = new InsnList();
			
			lst.add(new LabelNode());
			lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "ecomod/asm/EcomodASMHooks", "renderRainAddition", "()V", false));
			
			//INVOKEVIRTUAL net/minecraft/client/renderer/texture/TextureManager.bindTexture (Lnet/minecraft/util/ResourceLocation;)V
			
			AbstractInsnNode[] ain = mn.instructions.toArray();
			int insertion_index = -1;
			
			//log.info("Environment required name is "+chooseByEnvironment("bindTexture", "func_110577_a"));
			
			for(int i = 0; i < ain.length; i++)
			{
				AbstractInsnNode insn = ain[i];
				
				if(insn instanceof MethodInsnNode)
				{
					MethodInsnNode min = (MethodInsnNode)insn;
					/*
					log.info("Method : ");
					log.info(min.getOpcode());
					log.info(min.owner);
					log.info(min.name);
					log.info(min.desc);
					log.info(min.itf);
					*/
					if(min.getOpcode() == Opcodes.INVOKEVIRTUAL && equalOneOfNames(min.owner, "net/minecraft/client/renderer/texture/TextureManager", "net/minecraft/client/renderer/texture/TextureManager!&!bqf") && equalOneOfNames(min.name, "bindTexture", "func_110577_a!&!a") && equalOneOfNames(min.desc, "(Lnet/minecraft/util/ResourceLocation;)V", "(Lnet/minecraft/util/ResourceLocation;)V!&!(Lbqx;)V") && (min.itf == false))
					{
						log.info("FOUND: INVOKEVIRTUAL net/minecraft/client/renderer/texture/TextureManager(bqf) bindTexture(a) (Lnet/minecraft/util/ResourceLocation;)V!&!(Lbqx;)V  !!!!!");
						insertion_index = i;
						break;
					}
				}
			}
			
			if(insertion_index != -1)
			{
				mn.instructions.insert(mn.instructions.get(insertion_index), lst);
			}
			else
			{
				failed_transformers.add(name);
				log.error("Not found: INVOKEVIRTUAL net/minecraft/client/renderer/texture/TextureManager.bindTexture (Lnet/minecraft/util/ResourceLocation;)V");
				return bytecode;
			}
			
			classNode.accept(cw);
			bytes = cw.toByteArray();
			
			log.info("Transformed "+name);
			log.info("Final size: "+bytes.length+" bytes");
			
			return bytes;
		}
		catch(Exception e)
		{
			patchFailed(name, e);
			return bytecode;
		}
	}
	
	private byte[] handleBlockFire(String name, byte[] bytecode)
	{
		log.info("Transforming "+name);
		log.info("Initial size: "+bytecode.length+" bytes");
		
		byte[] bytes = bytecode.clone();
		
		try
		{
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(bytes);
			classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			
			if(DEBUG)
				printClassInfo(name, classNode);
			
			MethodNode mn = getMethod(classNode, "updateTick", "updateTick!&!a", "(Lnet/minecraft/world/World;IIILjava/util/Random;)V", "(Lnet/minecraft/world/World;IIILjava/util/Random;)V!&!"+onUpdate_desc);
			
			InsnList lst = new InsnList();
			
			lst.add(new VarInsnNode(Opcodes.ALOAD, 1));
			lst.add(new VarInsnNode(Opcodes.ILOAD, 2));
			lst.add(new VarInsnNode(Opcodes.ILOAD, 3));
			lst.add(new VarInsnNode(Opcodes.ILOAD, 4));
			lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "ecomod/asm/EcomodASMHooks", "fireTickAddition", "(Lnet/minecraft/world/World;III)V", false));
			lst.add(new LabelNode());
			
			mn.instructions.insert(mn.instructions.get(1), lst);
			
			classNode.accept(cw);
			bytes = cw.toByteArray();
			
			log.info("Transformed "+name);
			log.info("Final size: "+bytes.length+" bytes");
		
			return bytes;
		}
		catch(Exception e)
		{
			patchFailed(name, e);
			return bytecode;
		}
	}
	
	private byte[] handleBlockLeaves(String name, byte[] bytecode)
	{
		log.info("Transforming "+name);
		log.info("Initial size: "+bytecode.length+" bytes");
		
		byte[] bytes = bytecode.clone();
		
		try
		{
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(bytes);
			classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			
			if(DEBUG)
				printClassInfo(name, classNode);
			
			MethodNode mn = getMethod(classNode, "updateTick", "updateTick!&!a", "(Lnet/minecraft/world/World;IIILjava/util/Random;)V", "(Lnet/minecraft/world/World;IIILjava/util/Random;)V!&!"+onUpdate_desc);
			
			InsnList lst = new InsnList();
			
			lst.add(new VarInsnNode(Opcodes.ALOAD, 1));
			lst.add(new VarInsnNode(Opcodes.ILOAD, 2));
			lst.add(new VarInsnNode(Opcodes.ILOAD, 3));
			lst.add(new VarInsnNode(Opcodes.ILOAD, 4));
			lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "ecomod/asm/EcomodASMHooks", "leavesTickAddition", "(Lnet/minecraft/world/World;III)V", false));
			lst.add(new LabelNode());
			
			//log.info(mn.instructions.getLast().getPrevious().getPrevious().toString());
			mn.instructions.insert(mn.instructions.getLast().getPrevious().getPrevious(), lst);
			
			classNode.accept(cw);
			bytes = cw.toByteArray();
			
			log.info("Transformed "+name);
			log.info("Final size: "+bytes.length+" bytes");
		
			return bytes;
		}
		catch(Exception e)
		{
			patchFailed(name, e);
			return bytecode;
		}
	}
	
	private byte[] handleBlockFarmland(String name, byte[] bytecode)
	{
		log.info("Transforming "+name);
		log.info("Initial size: "+bytecode.length+" bytes");
		
		byte[] bytes = bytecode.clone();
		
		try
		{
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(bytes);
			classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			
			if(DEBUG)
				printClassInfo(name, classNode);
			
			MethodNode mn = getMethod(classNode, "updateTick", "updateTick!&!a", "(Lnet/minecraft/world/World;IIILjava/util/Random;)V", "(Lnet/minecraft/world/World;IIILjava/util/Random;)V!&!"+onUpdate_desc);
			
			InsnList lst = new InsnList();
			
			lst.add(new VarInsnNode(Opcodes.ALOAD, 1));
			lst.add(new VarInsnNode(Opcodes.ILOAD, 2));
			lst.add(new VarInsnNode(Opcodes.ILOAD, 3));
			lst.add(new VarInsnNode(Opcodes.ILOAD, 4));
			lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "ecomod/asm/EcomodASMHooks", "farmlandTickAddition", "(Lnet/minecraft/world/World;III)V", false));
			lst.add(new LabelNode());
			
			//log.info(mn.instructions.getLast().getPrevious().getPrevious().toString());
			mn.instructions.insert(mn.instructions.getLast().getPrevious().getPrevious(), lst);
			
			classNode.accept(cw);
			bytes = cw.toByteArray();
			
			log.info("Transformed "+name);
			log.info("Final size: "+bytes.length+" bytes");
		
			return bytes;
		}
		catch(Exception e)
		{
			patchFailed(name, e);
			return bytecode;
		}
	}
		
	private byte[] handleItem(String name, byte[] bytecode)
	{
		log.info("Transforming "+name);
		log.info("Initial size: "+bytecode.length+" bytes");
		
		byte[] bytes = bytecode.clone();
		
		try
		{
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(bytes);
			classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			
			if(DEBUG)
				printClassInfo(name, classNode);
			
			MethodNode mn = getMethod(classNode, "onEntityItemUpdate", "onEntityItemUpdate", "(Lnet/minecraft/entity/item/EntityItem;)Z", "(Lnet/minecraft/entity/item/EntityItem;)Z!&!(Lxk;)Z");
			
			InsnList lst = new InsnList();
			
			lst.add(new VarInsnNode(Opcodes.ALOAD, 1));
			lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "ecomod/asm/EcomodASMHooks", "itemEntityUpdateAddition", "(Lnet/minecraft/entity/item/EntityItem;)V", false));
			lst.add(new LabelNode());
			
			mn.instructions.insert(mn.instructions.get(1), lst);
			
			classNode.accept(cw);
			bytes = cw.toByteArray();
			
			log.info("Transformed "+name);
			log.info("Final size: "+bytes.length+" bytes");
		
			return bytes;
		}
		catch(Exception e)
		{
			patchFailed(name, e);
			return bytecode;
		}
	}
	
	private byte[] handleItemFood(String name, byte[] bytecode)
	{
		log.info("Transforming "+name);
		log.info("Initial size: "+bytecode.length+" bytes");
		
		byte[] bytes = bytecode.clone();
		
		try
		{
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(bytes);
			classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			
			if(DEBUG)
				printClassInfo(name, classNode);
			
			MethodNode mn = getMethod(classNode, "onFoodEaten", "func_77849_c!&!c", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;)V", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;)V!&!(Ladd;Lahb;Lyz;)V");
			
			InsnList lst = new InsnList();
			
			lst.add(new VarInsnNode(Opcodes.ALOAD, 1));
			lst.add(new VarInsnNode(Opcodes.ALOAD, 2));
			lst.add(new VarInsnNode(Opcodes.ALOAD, 3));
			lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "ecomod/asm/EcomodASMHooks", "itemFoodOnEatenAddition", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;)V", false));
			lst.add(new LabelNode());
			
			mn.instructions.insert(mn.instructions.get(1), lst);
			
			classNode.accept(cw);
			bytes = cw.toByteArray();
			
			log.info("Transformed "+name);
			log.info("Final size: "+bytes.length+" bytes");
		
			return bytes;
		}
		catch(Exception e)
		{
			patchFailed(name, e);
			return bytecode;
		}
	}
	
	private byte[] handleTileFurnace(String name, byte[] bytecode)
	{
		log.info("Transforming "+name);
		log.info("Initial size: "+bytecode.length+" bytes");
		
		byte[] bytes = bytecode.clone();
		
		try
		{
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(bytes);
			classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			
			if(DEBUG)
				printClassInfo(name, classNode);
			
			MethodNode mn = getMethod(classNode, "smeltItem", "func_145949_j!&!j", "()V", "()V");
			
			InsnList lst = new InsnList();
			
			lst.add(new LabelNode());
			lst.add(new VarInsnNode(Opcodes.ALOAD, 0));
			lst.add(new VarInsnNode(Opcodes.ALOAD, 1));
			lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "ecomod/asm/EcomodASMHooks", "smeltItemFurnaceAddition", "(Lnet/minecraft/tileentity/TileEntityFurnace;Lnet/minecraft/item/ItemStack;)V", false));
			
			AbstractInsnNode[] ain = mn.instructions.toArray();
			int insertion_index = -1;
			
			for(int i = 0; i < ain.length; i++)
			{
				AbstractInsnNode insn = ain[i];
				
				if(insn instanceof VarInsnNode)
				{
					VarInsnNode min = (VarInsnNode)insn;

					if(min.getOpcode() == Opcodes.ASTORE && min.var == 1)
					{
						log.info("FOUND: ASTORE 1");
						insertion_index = i;
						break;
					}
				}
			}
			
			if(insertion_index != -1)
			{
				mn.instructions.insert(mn.instructions.get(insertion_index), lst);
			}
			else
			{
				log.error("Not found: ASTORE 1");
				failed_transformers.add(name);
				return bytecode;
			}
			
			classNode.accept(cw);
			bytes = cw.toByteArray();
			
			log.info("Transformed "+name);
			log.info("Final size: "+bytes.length+" bytes");
			
			return bytes;
		}
		catch(Exception e)
		{
			patchFailed(name, e);
			return bytecode;
		}
	}
	
	private byte[] test_handleTextureManager(String n, String name, byte[] bytecode)
	{
		log.info("Name "+n);
		log.info("Transforming "+name);
		log.info("Initial size: "+bytecode.length+" bytes");
		
		byte[] bytes = bytecode.clone();
		
		try
		{
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(bytes);
			classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			
			if(DEBUG)
				printClassInfo(name, classNode);
			
			MethodNode mn = getMethod(classNode, "onFoodEaten", "func_77849_c", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;)V", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;)V!&!(Lafj;Lajs;Laay;)V");
			
			classNode.accept(cw);
			bytes = cw.toByteArray();
			
			log.info("Transformed "+name);
			log.info("Final size: "+bytes.length+" bytes");
		
			return bytes;
		}
		catch(Exception e)
		{
			patchFailed(name, e);
			return bytecode;
		}
	}
	
	private byte[] handleEntityItem(String name, byte[] bytecode)
	{
		log.info("Transforming "+name);
		log.info("Initial size: "+bytecode.length+" bytes");
		
		byte[] bytes = bytecode.clone();
		
		try
		{
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(bytes);
			classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			
			if(DEBUG)
				printClassInfo(name, classNode);
			
			MethodNode mn = getMethod(classNode, "attackEntityFrom", "func_70097_a!&!a", "(Lnet/minecraft/util/DamageSource;F)Z", "(Lnet/minecraft/util/DamageSource;F)Z!&!(Lro;F)Z");
			
			InsnList lst = new InsnList();
			
			lst.add(new LabelNode());
			lst.add(new VarInsnNode(Opcodes.ALOAD, 0));
			lst.add(new VarInsnNode(Opcodes.ALOAD, 1));
			lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "ecomod/asm/EcomodASMHooks", "entityItemAttackedAddition", "(Lnet/minecraft/entity/item/EntityItem;Lnet/minecraft/util/DamageSource;)V", false));
			
			AbstractInsnNode[] ain = mn.instructions.toArray();
			int insertion_index = -1;
			
			for(int i = 0; i < ain.length; i++)
			{
				AbstractInsnNode insn = ain[i];
				
				if(insn instanceof MethodInsnNode)
				{
					MethodInsnNode min = (MethodInsnNode)insn;
					if(DEBUG)
					{
					log.info("Method : ");
					log.info(min.getOpcode());
					log.info(min.owner);
					log.info(min.name);
					log.info(min.desc);
					log.info(min.itf);
					}
					if(min.getOpcode() == Opcodes.INVOKEVIRTUAL && equalOneOfNames(min.owner, "net/minecraft/entity/item/EntityItem", "net/minecraft/entity/item/EntityItem!&!"+classNode.name) && equalOneOfNames(min.name, "setDead", "func_110577_a!&!B") && equalOneOfNames(min.desc, "()V", "()V!&!()V") && (min.itf == false))
					{
						insertion_index = i;
						break;
					}
				}
			}
			
			if(insertion_index != -1)
			{
				mn.instructions.insert(mn.instructions.get(insertion_index), lst);
			}
			else
			{
				failed_transformers.add(name);
				log.error("Not found: INVOKEVIRTUAL net/minecraft/entity/item/EntityItem.setDead");
				return bytecode;
			}
			
			classNode.accept(cw);
			bytes = cw.toByteArray();
			
			log.info("Transformed "+name);
			log.info("Final size: "+bytes.length+" bytes");
		
			return bytes;
		}
		catch(Exception e)
		{
			patchFailed(name, e);
			return bytecode;
		}
	}
	
	

	private byte[] handleFishHook(String name, byte[] bytecode)
	{
		log.info("Transforming "+name);
		log.info("Initial size: "+bytecode.length+" bytes");
		
		byte[] bytes = bytecode.clone();
		
		try
		{
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(bytes);
			classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			
			if(DEBUG)
				printClassInfo(name, classNode);
			
			MethodNode mn = getMethod(classNode, "func_146034_e", "func_146034_e!&!e", "()I", "()I");
			
			InsnList lst = new InsnList();
			
			int insertion_var = mn.name.equals("e") ? 2 : 12;
			
			lst.add(new LabelNode());
			lst.add(new VarInsnNode(Opcodes.ALOAD, 0));
			lst.add(new VarInsnNode(Opcodes.ALOAD, insertion_var));
			lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "ecomod/asm/EcomodASMHooks", "handleFishing", "(Lnet/minecraft/entity/projectile/EntityFishHook;Lnet/minecraft/entity/item/EntityItem;)Lnet/minecraft/entity/item/EntityItem;", false));
			lst.add(new VarInsnNode(Opcodes.ASTORE, insertion_var));
			
			AbstractInsnNode[] ain = mn.instructions.toArray();
			int insertion_index = -1;
			
			for(int i = 0; i < ain.length; i++)
			{
				AbstractInsnNode insn = ain[i];
				
				if(insn instanceof VarInsnNode)
				{
					VarInsnNode min = (VarInsnNode)insn;
					/*
					if(DEBUG)
					{
					log.info("Var : ");
					log.info(min.getOpcode());
					log.info(min.var);
					}
					 */
					if(min.getOpcode() == 58 && min.var == insertion_var)
					{
						log.info("FOUND: ASTORE "+insertion_var);
						insertion_index = i;
						break;
					}
				}
			}
			
			if(insertion_index != -1)
			{
				mn.instructions.insert(mn.instructions.get(insertion_index), lst);
			}
			else
			{
				log.error("Not found: ASTORE "+insertion_var);
				failed_transformers.add(name);
				return bytecode;
			}
			
			classNode.accept(cw);
			bytes = cw.toByteArray();
			
			log.info("Transformed "+name);
			log.info("Final size: "+bytes.length+" bytes");
			
			return bytes;
		}
		catch(Exception e)
		{
			patchFailed(name, e);
			return bytecode;
		}
	}
	
	private byte[] handleBrewingStand(String name, byte[] bytecode)
	{
		log.info("Transforming "+name);
		log.info("Initial size: "+bytecode.length+" bytes");
		
		byte[] bytes = bytecode.clone();
		
		try
		{
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(bytes);
			classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			
			if(DEBUG)
				printClassInfo(name, classNode);
			
			MethodNode mn = getMethod(classNode, "brewPotions", "brewPotions!&!l", "()V", "()V");
			
			InsnList lst = new InsnList();
			
			lst.add(new LabelNode());
			lst.add(new VarInsnNode(Opcodes.ALOAD, 0));
			lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "ecomod/asm/EcomodASMHooks", "potionBrewedAddition", "(Lnet/minecraft/tileentity/TileEntityBrewingStand;)V", false));
			
			AbstractInsnNode[] ain = mn.instructions.toArray();
			int insertion_index = -1;
			
			for(int i = 0; i < ain.length; i++)
			{
				AbstractInsnNode insn = ain[i];
				
				if(insn instanceof MethodInsnNode)
				{
					MethodInsnNode min = (MethodInsnNode)insn;
					if(DEBUG)
					{
					log.info("Method : ");
					log.info(min.getOpcode());
					log.info(min.owner);
					log.info(min.name);
					log.info(min.desc);
					log.info(min.itf);
					}
					if(min.getOpcode() == Opcodes.INVOKESTATIC && min.owner.equals("net/minecraftforge/event/ForgeEventFactory") && min.name.equals("onPotionBrewed") && equalOneOfNames(min.desc, "([Lnet/minecraft/item/ItemStack;)V", "([Lnet/minecraft/item/ItemStack;)V!&!([Ladd;)V") && (min.itf == false))
					{
						insertion_index = i;
						break;
					}
				}
			}
			
			if(insertion_index != -1)
			{
				mn.instructions.insert(mn.instructions.get(insertion_index), lst);
			}
			else
			{
				log.error("Not found: INVOKESTATIC net/minecraftforge/event/ForgeEventFactory.onPotionBrewed ([Lnet/minecraft/item/ItemStack;)V");
				failed_transformers.add(name);
				return bytecode;
			}
			
			classNode.accept(cw);
			bytes = cw.toByteArray();
			
			log.info("Transformed "+name);
			log.info("Final size: "+bytes.length+" bytes");
			
			return bytes;
		}
		catch(Exception e)
		{
			patchFailed(name, e);
			return bytecode;
		}
	}
	
	public static String[] parseDeobfName(String name)
	{
		if(name != null && name.contains(REGEX_NOTCH_FROM_MCP))
		{
			String[] sstr = name.split(REGEX_NOTCH_FROM_MCP);
			if(sstr.length == 2)
			return sstr;
		}
		
		return new String[]{name, null};
	}
	
	public static boolean equalOneOfNames(String str, String deobf, String obf)
	{
		String name = null;
		if(!checkSameAndNullStrings(deobf,obf))
			name = chooseByEnvironment(deobf,obf);
		
		
		String def_obf = "";
		String notch_obf = "";
		if(name != null && name.contains(REGEX_NOTCH_FROM_MCP))
		{
			String[] sstr = name.split(REGEX_NOTCH_FROM_MCP);
			def_obf = sstr[0];
			notch_obf = sstr[1];
		}
		
		return str.equals(deobf) || str.equals(def_obf) || str.equals(notch_obf);
	}
	
	//Part borrowed from DummyCore(https://github.com/Modbder/DummyCore)
	public static final String REGEX_NOTCH_FROM_MCP = "!&!";
	
	public static MethodNode getMethod(ClassNode cn, String deobfName, String obfName, String deobfDesc, String obfDesc)
	{
		String methodName = null;
		String methodDesc = null;
		if(!checkSameAndNullStrings(deobfName,obfName))
			methodName = chooseByEnvironment(deobfName,obfName);
		if(!checkSameAndNullStrings(deobfDesc,obfDesc))
			methodDesc = chooseByEnvironment(deobfDesc,obfDesc);
		
		String additionalMN = "";
		if(methodName != null && methodName.contains(REGEX_NOTCH_FROM_MCP))
		{
			String[] sstr = methodName.split(REGEX_NOTCH_FROM_MCP);
			methodName = sstr[0];
			additionalMN = sstr[1];
		}
		
		String additionalMD = "";
		if(methodDesc != null && methodDesc.contains(REGEX_NOTCH_FROM_MCP))
		{
			String[] sstr = methodDesc.split(REGEX_NOTCH_FROM_MCP);
			methodDesc = sstr[0];
			additionalMD = sstr[1];
		}
		
		if(checkSameAndNullStrings(methodName, methodDesc))
			return null;
		for(MethodNode mn : cn.methods)
			if((methodName == null || methodName.equals(mn.name) || additionalMN.equals(mn.name)) && (methodDesc == null || methodDesc.equals(mn.desc) || additionalMD.equals(mn.desc)))
				return mn;
		
		return null;
	}
	
	public static boolean checkSameAndNullStrings(String par1, String par2)
	{
		if(par1 == par2)
		{
			if(par1 == null && par2 == null)
				return true;
			else
				if(par1 != null && par2 != null)
					if(par1.isEmpty() && par2.isEmpty())
						return true;
		}
		
		return false;
	}
	
	public static boolean strictCompareByEnvironment(String name, String deobf, String obf)
	{
		String comparedTo = chooseByEnvironment(deobf.replace('/', '.'),obf.replace('/', '.'));
		return comparedTo.equalsIgnoreCase(name.replace('/', '.'));
	}
	
	public static void printClassInfo(String transformedName, ClassNode clazz)
	{
		log.info("----------------------------------------------------------------------------");
		log.info("Transformed class name "+transformedName);
		log.info("Class name "+clazz.name);
		log.info("-----------------------");
		log.info("Fields:");
		for(FieldNode field : clazz.fields)
		{
			log.info(field.name + "   of type   " + field.desc + " of access "+field.access);
		}
		log.info("-----------------------");
		log.info("Methods:");
		for(MethodNode mn : clazz.methods)
		{
			log.info(mn.name + " with desc "+mn.desc);
			if(mn.visibleAnnotations != null && mn.visibleAnnotations.size() > 0)
			{
				log.info("With annotations:");
				for(AnnotationNode an : mn.visibleAnnotations)
				{
					log.info(an.desc);
					log.info(an.values);
				}
			}
		}
		log.info("----------------------------------------------------------------------------");
		
	}
	
	private byte[] handleGrowable(String name, byte[] bytecode)
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytecode);
		classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
		
		if(name.equals("net.minecraft.block.IGrowable"))
			if(DEBUG)
				printClassInfo(name, classNode);
		
		try
		{
			if(classNode.interfaces.contains("net/minecraft/block/IGrowable") || classNode.interfaces.contains("ajo"))
			{
				log.info("Transforming IGrowable: "+name);
				log.info("Initial size: "+bytecode.length+" bytes");
				
				if(DEBUG)
					printClassInfo(name, classNode);
			
				ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
				
				MethodNode mn = getMethod(classNode, "func_149851_a", "func_149851_a!&!a", "(Lnet/minecraft/world/World;IIIZ)Z", "(Lnet/minecraft/world/World;IIIZ)Z!&!(Lahb;IIIZ)Z");
			
				InsnList lst = new InsnList();
			
				lst.add(new VarInsnNode(Opcodes.ALOAD, 1));
				lst.add(new VarInsnNode(Opcodes.ILOAD, 2));
				lst.add(new VarInsnNode(Opcodes.ILOAD, 3));
				lst.add(new VarInsnNode(Opcodes.ILOAD, 4));
				lst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "ecomod/asm/EcomodASMHooks", "canCropGrow", "(ZLnet/minecraft/world/World;III)Z", false));
			
				mn.instructions.insert(mn.instructions.getLast().getPrevious().getPrevious(), lst);
			
				classNode.accept(cw);
				
				byte[] bytes = cw.toByteArray();
				
				log.info("Transformed "+name);
				log.info("Final size: "+bytes.length+" bytes");
			
				return bytes;
			}
		}
		catch(Exception e)
		{
			log.error("Unable to patch "+name+"!");
			log.error(e.toString());
			e.printStackTrace();
			
			return bytecode;
		}

		return bytecode;
	}
}
