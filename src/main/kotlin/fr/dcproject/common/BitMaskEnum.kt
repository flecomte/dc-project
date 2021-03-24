package fr.dcproject.common

interface BitMaskI {
    val bit: Long

    infix operator fun contains(which: BitMaskI): Boolean = bit and which.bit == which.bit
    infix operator fun plus(mask: BitMaskI): BitMaskI = BitMask(mask.bit and this.bit)
    infix operator fun minus(mask: BitMaskI): BitMaskI = BitMask(this.bit - mask.bit)
}

class BitMask(override val bit: Long) : BitMaskI
