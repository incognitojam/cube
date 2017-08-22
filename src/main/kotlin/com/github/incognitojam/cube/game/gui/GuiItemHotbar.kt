package com.github.incognitojam.cube.game.gui

import com.github.incognitojam.cube.engine.graphics.*
import com.github.incognitojam.cube.game.block.Blocks
import com.github.incognitojam.cube.game.inventory.Inventory
import com.github.incognitojam.cube.game.inventory.ItemStack
import com.github.incognitojam.cube.game.item.Items
import com.github.incognitojam.cube.game.world.Direction
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import java.awt.Font

class GuiItemHotbar(private val inventory: Inventory, private val guiTextures: TextureMap) : GuiItem() {

    private val hotbarSize = inventory.width
    private val tileSize = 64F
    private val itemSize = 48F

    private lateinit var hotbarFont: FontTexture
    private lateinit var quantityTexts: Array<TextItem>
    private var selectedMesh: Mesh? = null
    private var itemsMesh: Mesh? = null
    private var blocksMesh: Mesh? = null

    override var width = tileSize * hotbarSize
    override var height = tileSize

    private val hotbarItems = Array(inventory.hotbarSize) { ItemStack() }

    override fun initialise() {
        hotbarFont = FontTexture(Font("Arial", Font.BOLD, 16), GuiHud.CHARSET)
        quantityTexts = Array(hotbarSize) { TextItem("", hotbarFont) }

        val positionsList = ArrayList<Float>()
        val textureCoordinatesList = ArrayList<Float>()
        val indicesList = ArrayList<Int>()

        for (hotbarIndex in 0 until hotbarSize) {
            positionsList.addAll(floatArrayOf(
                    -(hotbarSize * tileSize * 0.5F) + hotbarIndex * tileSize, 0f, Z_POS,
                    -(hotbarSize * tileSize * 0.5F) + hotbarIndex * tileSize, tileSize, Z_POS,
                    -(hotbarSize * tileSize * 0.5F) + (hotbarIndex + 1) * tileSize, tileSize, Z_POS,
                    -(hotbarSize * tileSize * 0.5F) + (hotbarIndex + 1) * tileSize, 0f, Z_POS
            ).toList())
            textureCoordinatesList.addAll(guiTextures.getTextureCoordinates(1).toList())
            indicesList.addAll(ChunkMeshBuilder.INDICES_DELTA.map { it + (hotbarIndex * 4) })
        }
        mesh = Mesh(positionsList.toFloatArray(), textureCoordinatesList.toFloatArray(), indicesList.toIntArray(), guiTextures)

        val positions = floatArrayOf(
                0f, 0f, Z_POS,
                0f, tileSize, Z_POS,
                tileSize, tileSize, Z_POS,
                tileSize, 0f, Z_POS
        )
        selectedMesh = Mesh(positions, guiTextures.getTextureCoordinates(2), ChunkMeshBuilder.INDICES_DELTA, guiTextures)

        updateHotbarItems()
    }

    override fun update() {
        super.update()

        var hotbarItemsNeedUpdating = false
        for (slotIndex in 0 until inventory.hotbarSize) {
            val slot = inventory.getSlot(slotIndex)!!
            val slotItemStack = slot.itemStack
            val hotbarItemStack = hotbarItems[slotIndex]

            if (slotItemStack != hotbarItemStack) {
                hotbarItemsNeedUpdating = true
                hotbarItems[slotIndex] = ItemStack(slot.itemStack)
            }
        }

        if (hotbarItemsNeedUpdating) updateHotbarItems()
    }

    override fun render(shader: ShaderProgram, projectionMatrix: Matrix4f) {
        super.render(shader, projectionMatrix)

        selectedMesh?.let { selectedMesh ->
            val selectedRenderPosition = Vector3f(renderPosition).add(tileSize * (inventory.selectedIndex - (hotbarSize * 0.5f)), 0f, .5f)
            val projectionModelMatrix = Transformation.getOrthographicProjectionModelMatrix(selectedRenderPosition, projectionMatrix)
            shader.setUniform("projectionModelMatrix", projectionModelMatrix)
            shader.setUniform("colour", Vector4f(1f, 1f, 1f, 1f))
            shader.setUniform("hasTexture", 1)
            selectedMesh.render()
        }

        itemsMesh?.let { itemsMesh ->
            val selectedRenderPosition = Vector3f(renderPosition).add(0f, 0f, .2f)
            val projectionModelMatrix = Transformation.getOrthographicProjectionModelMatrix(selectedRenderPosition, projectionMatrix)
            shader.setUniform("projectionModelMatrix", projectionModelMatrix)
            shader.setUniform("colour", Vector4f(1f, 1f, 1f, 1f))
            shader.setUniform("hasTexture", 1)
            itemsMesh.render()
        }

        blocksMesh?.let { blocksMesh ->
            val selectedRenderPosition = Vector3f(renderPosition).add(0f, 0f, .3f)
            val projectionModelMatrix = Transformation.getOrthographicProjectionModelMatrix(selectedRenderPosition, projectionMatrix)
            shader.setUniform("projectionModelMatrix", projectionModelMatrix)
            shader.setUniform("colour", Vector4f(1f, 1f, 1f, 1f))
            shader.setUniform("hasTexture", 1)
            blocksMesh.render()
        }

        for (quantityText in quantityTexts) {
            quantityText.mesh?.let { textMesh ->
                val selectedRenderPosition = Vector3f(quantityText.renderPosition).add(renderPosition).add(0f, 0f, 1f)
                val projectionModelMatrix = Transformation.getOrthographicProjectionModelMatrix(selectedRenderPosition, projectionMatrix)
                shader.setUniform("projectionModelMatrix", projectionModelMatrix)
                shader.setUniform("colour", Vector4f(0f, 0f, 0f, 1f))
                shader.setUniform("hasTexture", 1)
                textMesh.render()
            }
        }
    }

    private fun updateHotbarItems() {
        val itemsPositionsList = ArrayList<Float>()
        val blocksPositionsList = ArrayList<Float>()
        val itemsTextureCoordinatesList = ArrayList<Float>()
        val blocksTextureCoordinatesList = ArrayList<Float>()
        val itemsIndicesList = ArrayList<Int>()
        val blocksIndicesList = ArrayList<Int>()

        var itemCount = 0
        var blockCount = 0

        for ((hotbarIndex, itemStack) in hotbarItems.withIndex()) {
            if (itemStack.quantity > 0 && itemStack.item != Items.AIR) {
                val item = itemStack.item
                val block = item.getBlock()

                if (block != null) {
                    blocksPositionsList.addAll(floatArrayOf(
                            -(hotbarSize * tileSize * .5F) + hotbarIndex * tileSize + (tileSize - itemSize), tileSize - itemSize, Z_POS,
                            -(hotbarSize * tileSize * .5F) + hotbarIndex * tileSize + (tileSize - itemSize), itemSize, Z_POS,
                            -(hotbarSize * tileSize * .5F) + (hotbarIndex + 1) * tileSize - (tileSize - itemSize), itemSize, Z_POS,
                            -(hotbarSize * tileSize * .5F) + (hotbarIndex + 1) * tileSize - (tileSize - itemSize), tileSize - itemSize, Z_POS
                    ).toList())
                    blocksTextureCoordinatesList.addAll(block.getTextureCoordinates(Direction.UP).toList())
                    blocksIndicesList.addAll(ChunkMeshBuilder.INDICES_DELTA.map { it + (blockCount * 4) })
                    blockCount++
                } else {
                    itemsPositionsList.addAll(floatArrayOf(
                            -(hotbarSize * tileSize * .5F) + hotbarIndex * tileSize + (tileSize - itemSize), tileSize - itemSize, Z_POS,
                            -(hotbarSize * tileSize * .5F) + hotbarIndex * tileSize + (tileSize - itemSize), itemSize, Z_POS,
                            -(hotbarSize * tileSize * .5F) + (hotbarIndex + 1) * tileSize - (tileSize - itemSize), itemSize, Z_POS,
                            -(hotbarSize * tileSize * .5F) + (hotbarIndex + 1) * tileSize - (tileSize - itemSize), tileSize - itemSize, Z_POS
                    ).toList())
                    blocksTextureCoordinatesList.addAll(item.getTextureCoordinates()!!.toList())
                    blocksIndicesList.addAll(ChunkMeshBuilder.INDICES_DELTA.map { it + (itemCount * 4) })
                    itemCount++
                }
            }
        }

        itemsMesh?.deleteBuffers()
        itemsMesh = Mesh(itemsPositionsList.toFloatArray(), itemsTextureCoordinatesList.toFloatArray(), itemsIndicesList.toIntArray(), Items.getTextureMap())

        blocksMesh?.deleteBuffers()
        blocksMesh = Mesh(blocksPositionsList.toFloatArray(), blocksTextureCoordinatesList.toFloatArray(), blocksIndicesList.toIntArray(), Blocks.getTextureMap())

        for ((index, quantityText, itemStack) in quantityTexts.withIndex().map { Triple(it.index, it.value, hotbarItems[it.index]) }) {
            if (itemStack.quantity == 0 || itemStack.item == Items.AIR) {
                quantityText.text = ""
            } else {
                quantityText.text = itemStack.quantity.toString()
                quantityText.setPosition(
                        -(hotbarSize * tileSize * .5F) + (index + 1) * tileSize - (tileSize - itemSize) - (quantityText.width / 2),
                        itemSize - (quantityText.height / 2)
                )
            }
        }
    }

}