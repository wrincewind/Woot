package ipsis.woot.factory.blocks.heart;

import com.mojang.blaze3d.platform.GlStateManager;
import ipsis.woot.Woot;
import ipsis.woot.factory.FactoryUIInfo;
import ipsis.woot.factory.FactoryUpgrade;
import ipsis.woot.factory.items.UpgradeItem;
import ipsis.woot.network.HeartStaticDataRequest;
import ipsis.woot.network.Network;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * There are two types of information displayed here.
 * Static - the factory recipe and drops - custom packet
 * Dynamic - progress - vanilla progress mechanism
 */

@OnlyIn(Dist.CLIENT)
public class HeartScreen extends ContainerScreen<HeartContainer> {

    private ResourceLocation GUI = new ResourceLocation(Woot.MODID, "textures/gui/heart.png");

    public HeartScreen(HeartContainer container, PlayerInventory inv, ITextComponent name) {
        super(container, inv, name);
        xSize = 252;
        ySize = 152;
    }

    private List<StackElement> stackElements = new ArrayList<>();
    private List<StackElement> mobElements = new ArrayList<>();
    private List<StackElement> upgradeElements = new ArrayList<>();

    private int DROPS_COLS = 13;
    private int DROPS_ROWS = 2;
    private int DROPS_X = 10;
    private int DROPS_Y = 110;
    private int MOBS_X = 10;
    private int MOBS_Y = 76;
    private int UPGRADES_X = 91;
    private int UPGRADES_Y = 76;

    @Override
    protected void init() {
        super.init();

        // Mobs
        for (int i = 0; i < 4; i++)
            mobElements.add(new StackElement(MOBS_X + (i * 18), MOBS_Y, true));

        // Upgrades
        for (int i = 0; i < 4; i++)
            upgradeElements.add(new StackElement(UPGRADES_X + (i * 18), UPGRADES_Y, true));

        // Recipe

        // Drops
        for (int row = 0; row < DROPS_ROWS; row++) {
            for (int col = 0; col < DROPS_COLS; col++) {
                stackElements.add(new StackElement(DROPS_X + (col * 18), DROPS_Y + (row * 18)));
            }
        }

        // Request the static data
        Network.channel.sendToServer(new HeartStaticDataRequest(container.getPos()));
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);

        // TODO use tick to cycle the dropelements

    }

    /**
     * 0,0 is top left hand corner of the gui texture
     */
    private boolean sync = false;
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {

        FactoryUIInfo factoryUIInfo = container.getFactoryUIInfo();
        if (factoryUIInfo == null)
            return;

        if (!sync) {
            int idx = 0;
            for (ItemStack itemStack : factoryUIInfo.drops) {
                List<String> tooltip = getTooltipFromItem(itemStack);
                tooltip.add(String.format("Drop chance: %d %%", itemStack.getCount()));
                stackElements.get(idx).addDrop(itemStack, tooltip);
                idx = (idx + 1) % stackElements.size();
            }

            idx = 0;
            for (ItemStack itemStack : factoryUIInfo.mobs) {
                List<String> tooltip = getTooltipFromItem(itemStack);
                mobElements.get(idx).addDrop(itemStack, tooltip);
                idx = (idx + 1) % mobElements.size();
            }

            idx = 0;
            for (FactoryUpgrade upgrade : factoryUIInfo.upgrades) {
                ItemStack itemStack = UpgradeItem.getItemStack(upgrade);
                List<String> tooltip = getTooltipFromItem(itemStack);
                upgradeElements.get(idx).addDrop(itemStack, tooltip);
                idx = (idx + 1) % upgradeElements.size();
            }
            sync = true;
        }

        int TEXT_COLOR = 4210752;

        // TODO Can only render 20 itemstacks!???
        // I'm going to assume that I am an idiot
        font.drawString("Effort", 10, 10, TEXT_COLOR);
        font.drawString(": " + factoryUIInfo.recipeEffort + " mB", 70, 10, TEXT_COLOR);
        font.drawString("Time", 10, 20, TEXT_COLOR);
        font.drawString(": " + factoryUIInfo.recipeTicks + " ticks", 70, 20, TEXT_COLOR);
        font.drawString("Rate", 10, 30, TEXT_COLOR);
        font.drawString(": " + factoryUIInfo.recipeCostPerTick + " mb/tick ", 70, 30, TEXT_COLOR);
        font.drawString("Stored", 10, 40, TEXT_COLOR);
        font.drawString(": " + factoryUIInfo.effortStored + " mB ", 70, 40, TEXT_COLOR);
        font.drawString("Progress", 10, 50, TEXT_COLOR);
        font.drawString(": 15% ", 70, 50, TEXT_COLOR);

        font.drawString("Mobs", MOBS_X, MOBS_Y - 10, TEXT_COLOR);
        font.drawString("Upgrades", UPGRADES_X, UPGRADES_Y - 10, TEXT_COLOR);

        int RECIPE_X = 172;
        int RECIPE_Y = 76;
        ItemStack mobStack = new ItemStack(Items.DIRT);
        font.drawString("Recipe", RECIPE_X, 66.0F, TEXT_COLOR);
        for (int i = 0; i < 4; i ++)
            drawItemStack(mobStack, RECIPE_X + (i * 18), RECIPE_Y, false, "");

        font.drawString("Drops:", DROPS_X, DROPS_Y - 10, TEXT_COLOR);

        mobElements.forEach(e -> e.drawForeground(this, mouseX, mouseY));
        upgradeElements.forEach(e -> e.drawForeground(this, mouseX, mouseY));
        stackElements.forEach(e -> e.drawForeground(this, mouseX, mouseY));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.getTextureManager().bindTexture(GUI);
        int relX = (width - xSize) / 2;
        int relY = (height - ySize) / 2;
        blit(relX, relY, 0, 0, xSize, ySize);

    }

    public void drawItemStack(ItemStack stack, int x, int y, boolean drawOverlay, String overlayTxt) {
        GlStateManager.translatef(0.0F, 0.0F, 32.0F);
        blitOffset = 200;
        itemRenderer.zLevel = 200.0F;
        FontRenderer font = stack.getItem().getFontRenderer(stack);
        if (font == null)
            font = this.font;

        itemRenderer.renderItemAndEffectIntoGUI(stack, x, y);
        if (drawOverlay)
            itemRenderer.renderItemOverlayIntoGUI(font, stack, x, y - 8, overlayTxt);

        blitOffset = 0;
        itemRenderer.zLevel = 0.0F;
    }

    class StackElement {
        int x;
        int y;
        boolean isLocked = false;
        int idx = 0;
        List<ItemStack> itemStacks = new ArrayList<>();
        List<List<String>> tooltips = new ArrayList<>();
        public StackElement(int x, int y, boolean locked) {
            this.x = x;
            this.y = y;
            this.isLocked = locked;
        }

        public StackElement(int x, int y) {
            this(x, y, false);
        }

        public void addDrop(ItemStack itemStack, List<String> tooltip) {
            isLocked = false;
            itemStacks.add(itemStack);
            tooltips.add(tooltip);
        }

        public void cycle() { idx = (idx + 1) % itemStacks.size(); }

        public void drawForeground(HeartScreen screen, int mouseX, int mouseY) {

            if (itemStacks.isEmpty())
                return;

            if (isLocked) {
                // TODO draw a cross or something
                return;
            }

            ItemStack itemStack = itemStacks.get(idx);
            List<String> tooltip = tooltips.get(idx);

            itemRenderer.renderItemIntoGUI(itemStack, x, y);
            if (mouseX >= guiLeft + x && mouseX <= guiLeft + x + 20 && mouseY >= guiTop + y && mouseY <= guiTop + y + 20) {
                FontRenderer fontRenderer = itemStack.getItem().getFontRenderer(itemStack);
                if (fontRenderer == null)
                    fontRenderer = font;
                renderTooltip(tooltip, mouseX - guiLeft, mouseY - guiTop, fontRenderer);
            }
        }
    }
}
