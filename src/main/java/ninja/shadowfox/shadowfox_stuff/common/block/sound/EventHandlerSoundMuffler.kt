package ninja.shadowfox.shadowfox_stuff.common.block.sound

import net.minecraft.client.Minecraft
import net.minecraft.client.audio.ISound
import net.minecraft.client.audio.ITickableSound
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.sound.PlaySoundEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * @author WireSegal
 * Created at 8:49 AM on 1/27/16.
 */
class EventHandlerSoundMuffler {

    companion object {
        val instance = EventHandlerSoundMuffler()

        fun register() {
            MinecraftForge.EVENT_BUS.register(instance)
        }
    }

    val MAXRANGE = 16

    @SubscribeEvent
    fun onSound(event: PlaySoundEvent) {
        if (Minecraft.getMinecraft().theWorld != null && event.result != null) {
            val world = Minecraft.getMinecraft().theWorld
            if (event.result !is ITickableSound) {

                val x = event.result.xPosF
                val y = event.result.yPosF
                val z = event.result.zPosF

                var volumeMultiplier = 1f

                for (dx in (x - MAXRANGE).toInt()..(x + MAXRANGE).toInt()) {
                    for (dy in (y - MAXRANGE).toInt()..(y + MAXRANGE).toInt()) {
                        for (dz in (z - MAXRANGE).toInt()..(z + MAXRANGE).toInt()) {
                        val block = world.getBlockState(BlockPos(dx, dy, dz)).block
                        if (block is ISoundSilencer) {
                            var distance = dist(dx, dy, dz, x, y, z)
                            if (distance <= MAXRANGE && block.canSilence(world, dx, dy, dz, distance, event)) {
                                volumeMultiplier *= block.getVolumeMultiplier(world, dx, dy, dz, distance, event)
                            }
                        }
                        }
                    }
                }
                if (volumeMultiplier != 1f) {
                    event.result = VolumeModSound(event.result, volumeMultiplier)
                }
            }
        }

    }

    inner class VolumeModSound(val sound: ISound, val volumeMult: Float) : ISound {
        override fun getSoundLocation(): ResourceLocation? = this.sound.soundLocation
        override fun canRepeat(): Boolean = this.sound.canRepeat()
        override fun getRepeatDelay(): Int = this.sound.repeatDelay
        override fun getVolume(): Float = this.sound.volume * volumeMult
        override fun getPitch(): Float = this.sound.pitch
        override fun getXPosF(): Float = this.sound.xPosF
        override fun getYPosF(): Float = this.sound.yPosF
        override fun getZPosF(): Float = this.sound.zPosF
        override fun getAttenuationType(): ISound.AttenuationType = this.sound.attenuationType
    }

    private fun dist(dx: Int, dy: Int, dz: Int, x: Float, y: Float, z: Float): Double {
        val xd = Math.pow(dx + 0.5 - x, 2.0)
        val yd = Math.pow(dy + 0.5 - y, 2.0)
        val zd = Math.pow(dz + 0.5 - z, 2.0)
        return Math.sqrt(xd + yd + zd)
    }
}
