package com.mgs.mgsMappingFabric.api

import com.sk89q.worldedit.IncompleteRegionException
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.fabric.FabricAdapter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.NullRegion
import com.sk89q.worldedit.regions.Region
import com.sk89q.worldedit.util.formatting.text.TextComponent
import com.sk89q.worldedit.world.block.BaseBlock
import com.sk89q.worldedit.world.block.BlockType
import com.sk89q.worldedit.entity.Player
import com.sk89q.worldedit.world.World
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos

private fun getPlayerSelectionRegion(player: Player): Region? {
	val sessionManager = WorldEdit.getInstance().sessionManager
	val session = sessionManager.get(player)
	val selectionWorld = session.selectionWorld
	val region : Region
	session.clipboard
	try {
		if (selectionWorld == null) throw IncompleteRegionException()
		region = session.getSelection(selectionWorld)
	} catch (ex: IncompleteRegionException) {
		player.printError(TextComponent.of("Please make a selection first."))
		return null
	}
	return region
}


fun <T: Enum<T>> scanGameBlockRegion(player: PlayerEntity, map: (BaseBlock) -> T?): MutableList<GameBlock<T>> {
	val wePlayer = FabricAdapter.adaptPlayer(player as ServerPlayerEntity)
	val region = getPlayerSelectionRegion(wePlayer) ?: return arrayListOf()
	val world = region.world ?: return arrayListOf()

	val offset = wePlayer.blockLocation

	val outArray = arrayListOf<GameBlock<T>>()
	region.forEach {
		val blockType = scanGameBlockFromVector(it, world, map)
		if (blockType != null) {
			outArray.add(GameBlock(blockType, BlockPos(it.x - offset.blockX, it.y - offset.blockY, it.z - offset.blockZ)))
		}
	}
	return outArray
}

private fun <T: Enum<T>> scanGameBlockFromVector(vector: BlockVector3, world: World, map: (BaseBlock) -> T?) : T? {
	val pos = BlockPos(vector.x, vector.y, vector.z)
	val block = world.getFullBlock(FabricAdapter.adapt(pos))
	return map(block)
}

fun <T: Enum<T>> scanGameBlockRegion(player: PlayerEntity, map: Map<BlockType, T>): MutableList<GameBlock<T>> {
	return scanGameBlockRegion(player){baseBlock -> map[baseBlock.blockType]}
}

fun getBlockRegion(player: PlayerEntity): Region {
	val wePlayer = FabricAdapter.adaptPlayer(player as ServerPlayerEntity)
	return getPlayerSelectionRegion(wePlayer) ?: NullRegion()
}
