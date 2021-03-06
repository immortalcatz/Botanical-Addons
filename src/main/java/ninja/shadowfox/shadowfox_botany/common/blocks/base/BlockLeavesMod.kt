package ninja.shadowfox.shadowfox_botany.common.blocks.base

import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.registry.GameRegistry
import cpw.mods.fml.relauncher.FMLLaunchHandler
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.block.Block
import net.minecraft.block.BlockLeaves
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.item.ItemStack
import net.minecraft.util.IIcon
import net.minecraft.util.MovingObjectPosition
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.common.IShearable
import net.minecraftforge.common.MinecraftForge
import ninja.shadowfox.shadowfox_botany.common.core.ShadowFoxCreativeTab
import ninja.shadowfox.shadowfox_botany.common.item.blocks.ItemSubtypedBlockMod
import ninja.shadowfox.shadowfox_botany.common.utils.helper.IconHelper
import ninja.shadowfox.shadowfox_botany.common.utils.helper.InterpolatedIconHelper
import vazkii.botania.api.lexicon.ILexiconable
import java.util.*


abstract class BlockLeavesMod() : BlockLeaves(), IShearable, ILexiconable {

    internal var decayField: IntArray? = null
    protected var icons: Array<IIcon?> = emptyArray()

    init {
        setHardness(0.2f)
        setCreativeTab(ShadowFoxCreativeTab)
        setLightOpacity(1)
        setStepSound(Block.soundTypeGrass)
        if (FMLLaunchHandler.side().isClient && this.isInterpolated())
            MinecraftForge.EVENT_BUS.register(this)
    }

    override fun setBlockName(par1Str: String): Block {
        register(par1Str)
        return super.setBlockName(par1Str)
    }

    abstract override fun quantityDropped(random: Random): Int

    open fun register(name: String) {
        GameRegistry.registerBlock(this, ItemSubtypedBlockMod::class.java, name)
    }


    @SideOnly(Side.CLIENT)
    override fun getIcon(side: Int, meta: Int): IIcon? {
        this.setGraphicsLevel(Minecraft.getMinecraft().gameSettings.fancyGraphics)
        return icons[if (this.field_150121_P) 0 else 1]
    }

    private fun removeLeaves(world: World, x: Int, y: Int, z: Int) {
        this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0)
        world.setBlockToAir(x, y, z)
    }

    override fun registerBlockIcons(iconRegister: IIconRegister) {
        if (!this.isInterpolated())
            icons = Array(2, { i -> IconHelper.forBlock(iconRegister, this, if (i == 0) "" else "_opaque") })
    }

    open fun isInterpolated(): Boolean = false

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    open fun loadTextures(event: TextureStitchEvent.Pre) {
        if (event.map.textureType == 0 && this.isInterpolated())
            icons = Array(2, { i -> InterpolatedIconHelper.forBlock(event.map, this, if (i == 0) "" else "_opaque") })
    }

    override fun isLeaves(world: IBlockAccess, x: Int, y: Int, z: Int): Boolean = true
    override fun isOpaqueCube(): Boolean = false
    override fun isShearable(item: ItemStack?, world: IBlockAccess?, x: Int, y: Int, z: Int): Boolean = true


    override fun onSheared(item: ItemStack, world: IBlockAccess, x: Int, y: Int, z: Int, fortune: Int): ArrayList<ItemStack> {
        val ret = ArrayList<ItemStack>()
        val meta = world.getBlockMetadata(x, y, z) and this.decayBit().inv()
        ret.add(ItemStack(this, 1, meta))
        return ret
    }

    override fun beginLeavesDecay(world: World?, x: Int, y: Int, z: Int) {
    }

    override fun updateTick(world: World?, x: Int, y: Int, z: Int, random: Random?) {
        if (!world!!.isRemote) {
            val meta = world.getBlockMetadata(x, y, z)
            if (!this.canDecay(meta)) return

            val b0 = 4
            val i1 = b0 + 1
            val b1 = 32
            val j1 = b1 * b1
            val k1 = b1 / 2

            if (this.decayField == null) {
                this.decayField = IntArray(b1.toInt() * b1.toInt() * b1.toInt())
            }

            var l1: Int

            if (world.checkChunksExist(x - i1, y - i1, z - i1, x + i1, y + i1, z + i1)) {
                var i2: Int
                var j2: Int

                l1 = -b0
                while (l1 <= b0) {
                    i2 = -b0
                    while (i2 <= b0) {
                        j2 = -b0
                        while (j2 <= b0) {
                            val block = world.getBlock(x + l1, y + i2, z + j2)

                            if (!block.canSustainLeaves(world, x + l1, y + i2, z + j2)) {
                                if (block.isLeaves(world, x + l1, y + i2, z + j2)) {
                                    decayField!![(l1 + k1) * j1 + (i2 + k1) * b1 + j2 + k1] = -2
                                } else {
                                    decayField!![(l1 + k1) * j1 + (i2 + k1) * b1 + j2 + k1] = -1
                                }
                            } else {
                                decayField!![(l1 + k1) * j1 + (i2 + k1) * b1 + j2 + k1] = 0
                            }
                            ++j2
                        }
                        ++i2
                    }
                    ++l1
                }

                l1 = 1
                while (l1 <= 4) {
                    i2 = -b0
                    while (i2 <= b0) {
                        j2 = -b0
                        while (j2 <= b0) {
                            for (k2 in -b0..b0) {
                                if (decayField!![(i2 + k1) * j1 + (j2 + k1) * b1 + k2 + k1] == l1 - 1) {
                                    if (decayField!![(i2 + k1 - 1) * j1 + (j2 + k1) * b1 + k2 + k1] == -2) {
                                        decayField!![(i2 + k1 - 1) * j1 + (j2 + k1) * b1 + k2 + k1] = l1
                                    }

                                    if (decayField!![(i2 + k1 + 1) * j1 + (j2 + k1) * b1 + k2 + k1] == -2) {
                                        decayField!![(i2 + k1 + 1) * j1 + (j2 + k1) * b1 + k2 + k1] = l1
                                    }

                                    if (decayField!![(i2 + k1) * j1 + (j2 + k1 - 1) * b1 + k2 + k1] == -2) {
                                        decayField!![(i2 + k1) * j1 + (j2 + k1 - 1) * b1 + k2 + k1] = l1
                                    }

                                    if (decayField!![(i2 + k1) * j1 + (j2 + k1 + 1) * b1 + k2 + k1] == -2) {
                                        decayField!![(i2 + k1) * j1 + (j2 + k1 + 1) * b1 + k2 + k1] = l1
                                    }

                                    if (decayField!![(i2 + k1) * j1 + (j2 + k1) * b1 + (k2 + k1 - 1)] == -2) {
                                        decayField!![(i2 + k1) * j1 + (j2 + k1) * b1 + (k2 + k1 - 1)] = l1
                                    }

                                    if (decayField!![(i2 + k1) * j1 + (j2 + k1) * b1 + k2 + k1 + 1] == -2) {
                                        decayField!![(i2 + k1) * j1 + (j2 + k1) * b1 + k2 + k1 + 1] = l1
                                    }
                                }
                            }
                            ++j2
                        }
                        ++i2
                    }
                    ++l1
                }
            }

            l1 = decayField!![k1 * j1 + k1 * b1 + k1]

            if (l1 >= 0) {
            } else removeLeaves(world, x, y, z)

        }
    }

    override fun getPickBlock(target: MovingObjectPosition?, world: World, x: Int, y: Int, z: Int): ItemStack {
        return ItemStack(this, 1, world.getBlockMetadata(x, y, z) and decayBit().inv())
    }

    abstract fun decayBit(): Int
    fun canDecay(meta: Int): Boolean {
        return meta and decayBit() == 0
    }

    override fun getDamageValue(world: World, x: Int, y: Int, z: Int): Int {
        return world.getBlockMetadata(x, y, z)
    }

    override fun dropBlockAsItemWithChance(world: World, x: Int, y: Int, z: Int, metadata: Int,
                                           chance: Float, fortune: Int) {
        super.dropBlockAsItemWithChance(world, x, y, z, metadata, 1.0f, fortune)
    }

    override fun getDrops(world: World, x: Int, y: Int, z: Int, metadata: Int, fortune: Int): ArrayList<ItemStack> {
        var ret: ArrayList<ItemStack> = ArrayList()

        var chance: Int = 20

        if (fortune > 0) {
            chance -= 2 shl fortune
            if (chance < 10) chance = 10
        }

        if (world.rand.nextInt(chance) == 0)
            ret.add(ItemStack(this.getItemDropped(metadata, world.rand, fortune), 1, 0))

        chance = 200
        if (fortune > 0) {
            chance -= 10 shl fortune
            if (chance < 40) chance = 40
        }

        this.captureDrops(true)
        this.func_150124_c(world, x, y, z, metadata, chance)
        ret.addAll(this.captureDrops(false))
        return ret
    }
}
