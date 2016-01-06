package ninja.shadowfox.shadowfox_botany.common.blocks.schema

import net.minecraft.block.material.Material
import ninja.shadowfox.shadowfox_botany.common.blocks.base.BlockMod

/**
 * Created by l0nekitsune on 1/3/16.
 */
class BlockFiller() : BlockMod(Material.wood) {

    init {
        //        val size = 0.1875f
        //        this.setBlockBounds(size, size, size, 1.0f - size, 1.0f - size, 1.0f - size)
        this.setBlockName("fillerBlock")
    }

    override fun isOpaqueCube(): Boolean {
        return false
    }
}
