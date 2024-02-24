package com.mgs.mgsMappingFabric.api

import net.minecraft.util.math.BlockPos

data class GameBlock<T : Enum<T>>(
	val blockType: Enum<T>,
	val pos: BlockPos
)
