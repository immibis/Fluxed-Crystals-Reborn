package fluxedCrystals;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import fluxedCrystals.client.gui.GUIHandler;
import fluxedCrystals.command.CommandFC;
import fluxedCrystals.handler.ConfigurationHandler;
import fluxedCrystals.handler.RecipeHandler;
import fluxedCrystals.init.FCBlocks;
import fluxedCrystals.init.FCItems;
import fluxedCrystals.nei.FluxedCrystalsNEIConfig;
import fluxedCrystals.network.PacketHandler;
import fluxedCrystals.proxy.IProxy;
import fluxedCrystals.recipe.RecipeGemCutter;
import fluxedCrystals.recipe.RecipeGemRefiner;
import fluxedCrystals.recipe.RecipeRegistry;
import fluxedCrystals.recipe.RecipeSeedInfuser;
import fluxedCrystals.reference.Reference;
import fluxedCrystals.registry.Seed;
import fluxedCrystals.registry.SeedRegistry;
import fluxedCrystals.util.LogHelper;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import tterrag.core.common.Lang;

import java.io.File;

@Mod(modid = Reference.MOD_ID, version = Reference.VERSION, dependencies = Reference.DEPENDENCIES, name = Reference.MOD_NAME, guiFactory = Reference.GUI_FACTORY_CLASS)
public class FluxedCrystals {

	public static final CreativeTabFluxedCrystals tab = new CreativeTabFluxedCrystals();
	public static File configDir = null;
	public static int crystalRenderID;
	public static final Lang lang = new Lang(Reference.MOD_ID);

	@Mod.Instance("fluxedCrystals")
	public static FluxedCrystals instance;

	public static int seedInfuserRenderID;
	public static int glassRenderID;
	public static int chunkRenderID;

	@SidedProxy(clientSide = Reference.CLIENT_PROXY, serverSide = Reference.SERVER_PROXY)
	public static IProxy proxy;

	@Mod.EventHandler
	public void onServerStarting(FMLServerStartingEvent event) {

		event.registerServerCommand(new CommandFC());

	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {

		configDir = new File(event.getSuggestedConfigurationFile().getParentFile().getAbsolutePath() + File.separator + Reference.MOD_ID);
		ConfigurationHandler.init(new File(configDir.getAbsolutePath() + File.separator + Reference.MOD_ID + ".cfg"));
		FMLCommonHandler.instance().bus().register(new ConfigurationHandler());

		PacketHandler.init();

		SeedRegistry.getInstance();

		SeedRegistry.getInstance().Load();

		FCItems.preInit();
		FCBlocks.preInit();

		proxy.preInit();

		if (Loader.isModLoaded("NotEnoughItems") && event.getSide() == Side.CLIENT)
		{
			new FluxedCrystalsNEIConfig().loadConfig();
		}

		FMLInterModComms.sendMessage("Waila", "register", "fluxedCrystals.compat.waila.WailaCompat.load");

		LogHelper.info("Pre Initialization Complete!");

	}

	@Mod.EventHandler
	public void initialize(FMLInitializationEvent event)
	{

		FCItems.initialize();
		FCBlocks.initialize();

		proxy.initialize();
		proxy.registerRenderers();

		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GUIHandler());

		LogHelper.info("Initialization Complete!");

	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {

		FCItems.postInit();
		FCBlocks.postInit();
		RecipeHandler.postInit();

		proxy.postInit();

		LogHelper.info("Post Initialization Complete!");

	}

	@Mod.EventHandler
	public void onServerStopping(FMLServerStoppingEvent event)
	{

		SeedRegistry.getInstance().Save();

	}

	@Mod.EventHandler
	public void remap(FMLModIdMappingEvent event)
	{

		for (int i : SeedRegistry.getInstance().keySet())
		{

			Seed seed = SeedRegistry.getInstance().getSeedByID(i);

			if (seed.modRequired.equals("") || (!seed.modRequired.equals("") && Loader.isModLoaded(seed.modRequired)))
			{

				RecipeRegistry.registerSeedInfuserRecipe(seed.seedID, new RecipeSeedInfuser(new ItemStack(FCItems.universalSeed),
						seed.getIngredient(), new ItemStack(FCItems.seed, 1, seed.seedID), seed.ingredientAmount, seed.seedID));

				RecipeRegistry.registerGemCutterRecipe(seed.seedID, new RecipeGemCutter(new ItemStack(FCItems.shardRough, 1, seed.seedID), new ItemStack(FCItems.shardSmooth, 1, seed.seedID), 1, 1));

				if (seed.weightedDrop != null && !seed.weightedDrop.equals(""))
				{

					if (!(Block.getBlockFromName("minecraft:portal") == Block.getBlockFromItem(seed.getWeightedDrop().getItem())))
					{

						RecipeRegistry.registerGemRefinerRecipe(seed.seedID, new RecipeGemRefiner(new ItemStack(FCItems.shardSmooth, 1, i), seed.getWeightedDrop(), seed.refinerAmount, seed.getDropAmount()));

					}
					else
					{

						RecipeRegistry.registerGemRefinerRecipe(seed.seedID, new RecipeGemRefiner(new ItemStack(FCItems.shardSmooth, 1, i), seed.getIngredient(), seed.refinerAmount, seed.getDropAmount()));

					}

				}
				else
				{

					RecipeRegistry.registerGemRefinerRecipe(seed.seedID, new RecipeGemRefiner(new ItemStack(FCItems.shardSmooth, 1, i), seed.getIngredient(), seed.refinerAmount, seed.getDropAmount()));

				}

			}

		}

		LogHelper.info("Remap Complete!");

	}

	public SeedRegistry getSeedRegistry()
	{

		return SeedRegistry.getInstance();

	}

}
