package ninja.shadowfox.shadowfox_botany.common.item.baubles

import baubles.api.BaubleType
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.ItemRenderer
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.IIcon
import net.minecraft.util.StatCollector
import net.minecraftforge.client.event.RenderPlayerEvent
import ninja.shadowfox.shadowfox_botany.api.item.IPriestColorOverride
import ninja.shadowfox.shadowfox_botany.common.core.ShadowFoxCreativeTab
import ninja.shadowfox.shadowfox_botany.common.item.ItemIridescent
import ninja.shadowfox.shadowfox_botany.common.utils.helper.IconHelper
import org.lwjgl.opengl.GL11
import vazkii.botania.api.item.IBaubleRender
import vazkii.botania.api.item.ICosmeticBauble
import vazkii.botania.api.recipe.IFlowerComponent
import vazkii.botania.client.core.helper.ShaderHelper
import vazkii.botania.common.item.equipment.bauble.ItemBauble

class ItemCoatOfArms() : ItemBauble("coatOfArms"), ICosmeticBauble, IPriestColorOverride, IFlowerComponent {

    val TYPES = 18
    var icons: Array<IIcon?> = arrayOfNulls(TYPES)
    val colorMap = intArrayOf(
            0x00137F, 0x0043FF, 0xFF0037, 0xFFD800,
            0x002EFF, 0x001A8E, 0x009944, 0x003BFF,
            0x00FF3B, 0xFF003B, 0x603A20, 0xFFFF00,
            0xFF0015, 0x0048FF, 0xFFD400, 0xFFFFFF,
            0xFFFFFF, 0xFF0037
    )

    init {
        setHasSubtypes(true)
        creativeTab = ShadowFoxCreativeTab
    }

    override fun canFit(stack: ItemStack, inventory: IInventory): Boolean {
        return stack.itemDamage == 6
    }

    override fun getParticleColor(stack: ItemStack): Int {
        return colorMap[stack.itemDamage]
    }

    override fun registerIcons(par1IconRegister: IIconRegister) {
        for (i in 0..TYPES - 1)
            icons[i] = IconHelper.forItem(par1IconRegister, this, i, "coatofarms")
    }

    override fun colorOverride(stack: ItemStack?): Int {
        if (stack != null) {
            if (stack.itemDamage < TYPES - 1 && stack.itemDamage >= 0 && stack.itemDamage != 16)
                return colorMap[stack.itemDamage]
            else if (stack.itemDamage == 16)
                return ItemIridescent.rainbowColor()
        }
        return -1
    }

    override fun getSubItems(item: Item, tab: CreativeTabs?, list: MutableList<Any?>) {
        for (i in 0..TYPES - 1)
            list.add(ItemStack(item, 1, i))
    }

    override fun getIconFromDamage(dmg: Int): IIcon? {
        return icons[Math.min(TYPES - 1, dmg)]
    }

    override fun onEquipped(stack: ItemStack, player: EntityLivingBase) {
        super.onEquipped(stack, player)
        if (stack.itemDamage == 1 && "paris".toRegex().find(stack.displayName.toLowerCase()) != null) {
            stack.itemDamage = 17
            stack.tagCompound.removeTag("display")
        }
    }

    override fun getUnlocalizedNameInefficiently(par1ItemStack: ItemStack): String {
        return super.getUnlocalizedNameInefficiently(par1ItemStack).replace("item\\.botania:".toRegex(), "item.shadowfox_botany:") + par1ItemStack.itemDamage
    }

    override fun addHiddenTooltip(par1ItemStack: ItemStack, par2EntityPlayer: EntityPlayer, par3List: MutableList<Any?>, par4: Boolean) {
        addStringToTooltip(StatCollector.translateToLocal("botaniamisc.cosmeticBauble"), par3List)
        super.addHiddenTooltip(par1ItemStack, par2EntityPlayer, par3List, par4)
    }

    fun addStringToTooltip(s: String, tooltip: MutableList<Any?>?) {
        tooltip!!.add(s.replace("&".toRegex(), "\u00a7"))
    }

    override fun getBaubleType(arg0: ItemStack): BaubleType {
        return BaubleType.AMULET
    }

    override fun onPlayerBaubleRender(stack: ItemStack, event: RenderPlayerEvent, type: IBaubleRender.RenderType) {
        if (type == IBaubleRender.RenderType.BODY) {
            Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationItemsTexture)
            IBaubleRender.Helper.rotateIfSneaking(event.entityPlayer)
            chestTranslate()
            scale(0.8F)
            GL11.glTranslatef(0.2F, -0.2F, -0.35F)
            GL11.glRotatef(10F, 0F, 0F, 1F)
            if (stack.itemDamage == 16) {
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                GL11.glAlphaFunc(GL11.GL_EQUAL, 1F)
                ShaderHelper.useShader(ShaderHelper.halo)
            }
            renderIcon(stack.itemDamage)
            if (stack.itemDamage == 16) {
                ShaderHelper.releaseShader()
                GL11.glAlphaFunc(GL11.GL_ALWAYS, 1F)
                GL11.glDisable(GL11.GL_BLEND)
            }
        }
    }

    fun chestTranslate() {
        GL11.glRotatef(180F, 1F, 0F, 0F)
        GL11.glTranslatef(-0.5F, -0.7F, 0.15F)
    }

    fun scale(f: Float) {
        GL11.glScalef(f, f, f)
    }

    fun renderIcon(i: Int) {
        var icon = icons[i]
        if (icon != null)
            ItemRenderer.renderItemIn2D(Tessellator.instance, icon.maxU, icon.minV, icon.minU, icon.maxV, icon.iconWidth, icon.iconHeight, 1F / 16F)
    }
}
