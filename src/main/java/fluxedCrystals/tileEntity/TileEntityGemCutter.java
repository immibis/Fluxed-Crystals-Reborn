package fluxedCrystals.tileEntity;

import WayofTime.alchemicalWizardry.api.soulNetwork.SoulNetworkHandler;
import fluxedCrystals.init.FCItems;
import fluxedCrystals.network.PacketHandler;
import fluxedCrystals.recipe.RecipeGemCutter;
import fluxedCrystals.recipe.RecipeRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import vazkii.botania.api.mana.IManaReceiver;

import java.util.ArrayList;
import java.util.EnumSet;

/**
 * Created by Jared on 11/2/2014.
 */
public class TileEntityGemCutter extends TileEnergyBase implements IManaReceiver, ISidedInventory {

	public ItemStack[] items;

	private int cut = 0;
	private int timePerCut = 0;

	// -1 if not currently working on any valid recipe
	private int recipeIndex;

	public int mana;
	public int MAX_MANA;
	public boolean RF = true;

	public int getTimePerCut() {
		return timePerCut;
	}

	public void setTimePerCut(int timePerCut) {
		this.timePerCut = timePerCut;
	}

	public int getRecipeIndex() {
		return recipeIndex;
	}

	public void setRecipeIndex(int recipeIndex) {
		this.recipeIndex = recipeIndex;
	}

	public int getCut() {
		return cut;
	}

	public TileEntityGemCutter() {
		super(10000);

		MAX_MANA = getMaxStorage();
		mana = 0;
		items = new ItemStack[7];
	}

	public void updateEntity() {
		super.updateEntity();
		if (worldObj != null && !worldObj.isRemote) {
			if (storage.getEnergyStored() > 0) {
				if (!isUpgradeActive(FCItems.upgradeMana) && !isUpgradeActive(FCItems.upgradeLP) && !isUpgradeActive(FCItems.upgradeEssentia)) {
					if (getStackInSlot(1) != null) {
						if (worldObj.getTotalWorldTime() % getSpeed() == 0 && storage.getEnergyStored() >= getEffeciency() && getStackInSlot(1).stackSize < getStackInSlot(1).getMaxStackSize()) {
							refine();
							return;
						}
					} else {
						if (worldObj.getTotalWorldTime() % getSpeed() == 0 && storage.getEnergyStored() >= getEffeciency()) {
							refine();
							return;
						}
					}
				}
			}
		}
	}

	public boolean isUpgradeActive(Item upgradeItem) {
		for (int slot : UPGRADE_SLOTS) {
			ItemStack stack = getStackInSlot(slot);
			if (stack != null && stack.getItem() == upgradeItem) {
				return true;
			}
		}
		return false;
	}

	public int getSpeed() {
		int speed = 100;
		for (int slot : UPGRADE_SLOTS) {
			ItemStack item = getStackInSlot(slot);
			if (item != null && item.getItem() == FCItems.upgradeSpeed) {
				speed -= 20;
			}
		}
		return speed;
	}

	public int getEffeciency() {
		int eff = 250;
		for (int slot : UPGRADE_SLOTS) {
			ItemStack item = getStackInSlot(slot);
			if (item != null) {
				if (item.getItem() == FCItems.upgradeSpeed) {
					eff += 30;
				}
				if (item.getItem() == FCItems.upgradeEffeciency) {
					eff -= 25;
				}
			}
		}

		if (eff <= 0) {
			eff = 1;
		}
		return eff;
	}
	
	private final int[] UPGRADE_SLOTS = {2, 3, 4};

	@Override
	public void closeInventory() {

	}

	@Override
	public ItemStack decrStackSize(int i, int count) {
		ItemStack itemstack = getStackInSlot(i);

		if (itemstack != null) {
			if (itemstack.stackSize <= count) {
				setInventorySlotContents(i, null);
			} else {
				itemstack = itemstack.splitStack(count);

			}
		}

		return itemstack;
	}

	@Override
	public String getInventoryName() {
		return "Gem Cutter";
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public int getSizeInventory() {
		return items.length;
	}

	@Override
	public ItemStack getStackInSlot(int par1) {

		return items[par1];
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		ItemStack item = getStackInSlot(i);
		setInventorySlotContents(i, item);
		return item;
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if (stack == null) {
			return false;
		}
		switch (slot) {
		default:
			return false;

		case 0:

			for (int i : RecipeRegistry.getAllGemCutterRecipes().keySet()) {

				RecipeGemCutter recipeGemCutter = RecipeRegistry.getGemCutterRecipeByID(i);

				if (recipeGemCutter.getInput().isItemEqual(stack)) {

					return true;

				}

			}

		case 1:
			return false;

		}
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return player.getDistanceSq(xCoord + 0.5f, yCoord + 0.5f, zCoord + 0.5f) <= 64;
	}

	@Override
	public void openInventory() {

	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		
		boolean changedItem;
		if(items[i] == null || itemstack == null)
			changedItem = (items[i] == null) != (itemstack == null); // non-null to null, or vice versa
		else
			changedItem = !items[i].isItemEqual(itemstack);
		
		items[i] = itemstack;
		
		if (itemstack != null && itemstack.stackSize > getInventoryStackLimit()) {
			itemstack.stackSize = getInventoryStackLimit();
		}
		
		if(i == 0 && changedItem)
			updateCurrentRecipe();
	}

	public boolean addInventorySlotContents(int i, ItemStack itemstack) {
		if (items[i] != null) {

			if (items[i].isItemEqual(itemstack)) {
				items[i].stackSize += itemstack.stackSize;
			}
			if (items[i].stackSize > getInventoryStackLimit()) {
				items[i].stackSize = getInventoryStackLimit();
			}
		} else {
			setInventorySlotContents(i, itemstack);
		}
		return false;
	}

	/* NBT */
	@Override
	public void readFromNBT(NBTTagCompound tags) {
		super.readFromNBT(tags);
		readInventoryFromNBT(tags);
		cut = tags.getInteger("cut");
		setRecipeIndex(tags.getInteger("recipeIndex"));
		mana = tags.getInteger("mana");
		
		updateCurrentRecipe();
	}

	public void readInventoryFromNBT(NBTTagCompound tags) {
		NBTTagList nbttaglist = tags.getTagList("Items", Constants.NBT.TAG_COMPOUND);
		for (int iter = 0; iter < nbttaglist.tagCount(); iter++) {
			NBTTagCompound tagList = nbttaglist.getCompoundTagAt(iter);
			byte slotID = tagList.getByte("Slot");
			if (slotID >= 0 && slotID < items.length) {
				items[slotID] = ItemStack.loadItemStackFromNBT(tagList);
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound tags) {
		super.writeToNBT(tags);
		writeInventoryToNBT(tags);
		tags.setInteger("cut", cut);
		tags.setInteger("recipeIndex", getRecipeIndex());
		tags.setInteger("mana", mana);
	}

	public void writeInventoryToNBT(NBTTagCompound tags) {
		NBTTagList nbttaglist = new NBTTagList();
		for (int iter = 0; iter < items.length; iter++) {
			if (items[iter] != null) {
				NBTTagCompound tagList = new NBTTagCompound();
				tagList.setByte("Slot", (byte) iter);
				items[iter].writeToNBT(tagList);
				nbttaglist.appendTag(tagList);
			}
		}

		tags.setTag("Items", nbttaglist);
	}

	public boolean refine() {
		if (getRecipeIndex() != -1) {

			RecipeGemCutter recipe = RecipeRegistry.getGemCutterRecipeByID(recipeIndex);
			if (getStackInSlot(0) != null && recipe.matchesExact(getStackInSlot(0))) {
				System.out.println("hi");
				if (getStackInSlot(1) == null || getStackInSlot(1).isItemEqual(recipe.getOutput())) {
					decrStackSize(0, 1);
					cut++;
					storage.extractEnergy(250, false);

					if (cut >= recipe.getInputamount()) {
						ItemStack out = recipe.getOutput().copy();
						out.stackSize = recipe.getOutputAmount();
						addInventorySlotContents(1, out);
						cut = 0;
					}
				}
				return true;
			}
		}
		cut = 0;
		return false;
	}

	public boolean refineMana() {
		if (getRecipeIndex() != -1) {
			RecipeGemCutter recipe = RecipeRegistry.getGemCutterRecipeByID(recipeIndex);
			if (recipe.matchesExact(getStackInSlot(0))) {
				if (getStackInSlot(1) == null || getStackInSlot(1).isItemEqual(recipe.getOutput())) {
					decrStackSize(0, 1);
					cut++;
					mana -= 250;
					if (cut == recipe.getInputamount()) {
						ItemStack out = recipe.getOutput().copy();
						addInventorySlotContents(1, out);
						out.stackSize = recipe.getOutputAmount();
						mana -= 500;
						cut = 0;
						setRecipeIndex(-1);

					}
				}
				return true;
			}
		}
		cut = 0;
		return false;
	}

	public boolean refineLP() {
		if (getRecipeIndex() != -1) {
			RecipeGemCutter recipe = RecipeRegistry.getGemCutterRecipeByID(recipeIndex);
			if (recipe.matchesExact(getStackInSlot(0))) {
				if (getStackInSlot(1) == null || getStackInSlot(1).isItemEqual(recipe.getOutput())) {
					decrStackSize(0, 1);
					cut++;
					SoulNetworkHandler.syphonFromNetwork(getStackInSlot(6), 250 / 4);
					if (cut == recipe.getInputamount()) {
						ItemStack out = recipe.getOutput().copy();
						out.stackSize = recipe.getOutputAmount();
						addInventorySlotContents(1, out);
						cut = 0;
						setRecipeIndex(-1);

					}
				}
				return true;
			}
		}
		cut = 0;
		return false;
	}

	public boolean refineEssentia() {
		if (getRecipeIndex() != -1) {
			RecipeGemCutter recipe = RecipeRegistry.getGemCutterRecipeByID(recipeIndex);
			if (recipe.matchesExact(getStackInSlot(0))) {
				if (getStackInSlot(1) == null || getStackInSlot(1).isItemEqual(recipe.getOutput())) {
					decrStackSize(0, 1);
					cut++;
					if (cut == recipe.getInputamount()) {
						ItemStack out = recipe.getOutput().copy();
						addInventorySlotContents(1, out);
						out.stackSize = recipe.getOutputAmount();
						cut = 0;
						setRecipeIndex(-1);

					}
				}
				return true;
			}
		}
		cut = 0;
		return false;
	}

 	public void updateCurrentRecipe() {
  		setRecipeIndex(-1);
  		ItemStack inputStack = getStackInSlot(0);
  		if (inputStack != null && inputStack.stackSize > 0) {
  			for (int id : RecipeRegistry.getAllGemCutterRecipes().keySet()) {
  				RecipeGemCutter recipe = RecipeRegistry.getGemCutterRecipeByID(id);
 				if (recipe.matchesExact(inputStack)) {
  					setRecipeIndex(id);
  					break;
  				}
  			}
  		}
	}

	@Override
	public EnumSet<ForgeDirection> getValidOutputs() {
		return EnumSet.noneOf(ForgeDirection.class);
	}

	@Override
	public EnumSet<ForgeDirection> getValidInputs() {
		return EnumSet.allOf(ForgeDirection.class);
	}

	@Override
	public int getCurrentMana() {
		return mana;
	}

	@Override
	public boolean isFull() {
		return mana == MAX_MANA;
	}

	@Override
	public void recieveMana(int mana) {
		if (!isFull()) {
			this.mana += mana;
		}
		if (getCurrentMana() > MAX_MANA) {
			this.mana = MAX_MANA;
		}
	}

	@Override
	public boolean canRecieveManaFromBursts() {
		return true;
	}

	public double getManaColor() {
		return getCurrentMana() * 255 / MAX_MANA;
	}

	private static int[] slotsAll = { 0, 1, 2, 3, 4, 5, 6 };

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return slotsAll;
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side) {
		if (isItemValidForSlot(slot, stack)) {

			for (int i : RecipeRegistry.getAllGemCutterRecipes().keySet()) {

				RecipeGemCutter recipeGemCutter = RecipeRegistry.getGemCutterRecipeByID(i);

				if (recipeGemCutter.getInput().isItemEqual(stack)) {
					return true;
				}

			}

		}
		return false;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side) {
		return slot != 0 && slot != 2 && slot != 3 && slot != 4;
	}

}