package fluxedCrystals.client.gui.gemCutter;

import fluxedCrystals.init.FCItems;
import fluxedCrystals.reference.Reference;
import fluxedCrystals.tileEntity.TileEntityGemCutter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GUIGemCutter extends GuiContainer
{

	private TileEntityGemCutter tile;
	private int energyOffset = 0;
	private int cut = 0;
	private int sawX;
	private int sawY;
	private boolean sawRange = false;

	public GUIGemCutter(InventoryPlayer invPlayer, TileEntityGemCutter tile2) {
		super(new ContainerGemCutter(invPlayer, tile2));

		xSize = 176;
		ySize = 166;
		this.tile = tile2;

	}

	private static final ResourceLocation texture = new ResourceLocation(Reference.LOWERCASE_MOD_ID, "textures/gui/cutrefine.png");

	@SuppressWarnings("unchecked")
	public void initGui() {
		super.initGui();
		sawX = guiLeft + 90;
		sawY = guiTop + 37;
	}

	@Override
	public void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		energyOffset++;
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		if (!tile.isUpgradeActive(FCItems.upgradeMana) && !tile.isUpgradeActive(FCItems.upgradeEssentia) && !tile.isUpgradeActive(FCItems.upgradeLP)) {
			GL11.glColor4d(tile.getEnergyColor(), tile.getEnergyColor(), tile.getEnergyColor(), 1f);
			drawTexturedModalRect(guiLeft + 14, guiTop + 15, 193, 4, 14, 42);
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		}
		if (tile.isUpgradeActive(FCItems.upgradeMana)) {
			drawTexturedModalRect(guiLeft + 14, guiTop + 15, 193, 47, 14, 42);
		}
		if (tile.isUpgradeActive(FCItems.upgradeLP)) {
			drawTexturedModalRect(guiLeft + 14, guiTop + 15, 193, 90, 14, 42);
		}
		if (tile.isUpgradeActive(FCItems.upgradeEssentia)) {
			drawTexturedModalRect(guiLeft + 14, guiTop + 15, 193, 133, 14, 42);
		}
		GL11.glColor4f(0.2f, 0.2f, 0.2f, 1.0f);
		if (tile.getRecipeIndex() >= 0 && tile.getStackInSlot(0) != null) {
			if (energyOffset % 20 == 0) {

				if (sawRange) {
					sawX = sawX + 5;
					sawY = sawY - 2;
					sawRange = false;
				} else {
					sawX = sawX + 5;
					sawY = sawY + 2;
					sawRange = true;
				}
			}
			if (sawX >= guiLeft + 105) {
				sawX = guiLeft + 62;
			}
			RenderItem.getInstance().renderItemIntoGUI(fontRendererObj, Minecraft.getMinecraft().getTextureManager(), tile.getStackInSlot(0), sawX, sawY);

		}
	}
}
