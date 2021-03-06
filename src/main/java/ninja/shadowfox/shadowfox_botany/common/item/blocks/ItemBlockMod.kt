package ninja.shadowfox.shadowfox_botany.common.item.blocks

import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import ninja.shadowfox.shadowfox_botany.common.blocks.base.BlockLeavesMod

open class ItemBlockMod(block: Block) : ItemBlock(block) {
    override fun setUnlocalizedName(p_77655_1_: String?): ItemBlock? {
        (this as Item).unlocalizedName = p_77655_1_
        return this
    }

    override fun getMetadata(meta: Int): Int {
        if (field_150939_a is BlockLeavesMod) return meta or field_150939_a.decayBit()
        return meta
    }

    override fun getUnlocalizedNameInefficiently(par1ItemStack: ItemStack): String {
        return getUnlocalizedNameInefficiently_(par1ItemStack).replace("tile.", "tile.shadowfox_botany:")
    }

    fun getUnlocalizedNameInefficiently_(stack: ItemStack): String {
        return super.getUnlocalizedNameInefficiently(stack)
    }
}
