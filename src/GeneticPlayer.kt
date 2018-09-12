data class GeneticPlayer(override val name: String, override var piece: String, override var wins: Int = 0, override var games: Int = 0) : Player {
    val genes: MutableList<Gene> = mutableListOf()

    init {
        generateInputGenes()

        generateRandomInitialMutation()
    }

    private fun generateInputGenes() {
        (0 until BOARD_FIELD_COUNT).forEach {
            genes.add(Gene.Constant().apply { x = 0 })
        }
    }

    private fun generateRandomInitialMutation() {
        (BOARD_FIELD_COUNT until BOARD_FIELD_COUNT + INITIAL_GENE_COUNT).forEach { index ->
            val gene = constructRandomGene()

            gene.x = rng.nextInt(index)
            gene.y = rng.nextInt(index)

            genes.add(gene)
        }
    }

    override fun move(board: Board) {
        val indexToPlace = askGenesForNextMove(board)

        if (board.isEmpty(indexToPlace)) {
            board.place(this, indexToPlace)
        } else {
            // If the want to place something on the already existing one, don't intervene...
        }
    }

    private fun askGenesForNextMove(board: Board): Int {
        val playerFields = board.mapFieldsToPlayerState(this)
        (0 until BOARD_FIELD_COUNT).forEach { index ->
            genes[index] = Gene.Constant().apply { x = playerFields[index] }
        }

        return genes.last().eval(genes)
    }

    fun mutate(): GeneticPlayer =
            GeneticPlayer("Genetic_${rng.nextInt()}", piece, wins, games).apply {
                (0 until GENE_MUTATIONS).forEach {
                    val gene = genes.randomElement()
                    var index = genes.indexOf(gene)
                    if (index < BOARD_FIELD_COUNT) {
                        index += BOARD_FIELD_COUNT
                    }

                    if (rng.nextBoolean()) {
                        val newGene = constructRandomGene()
                        newGene.x = gene.x
                        newGene.y = gene.y

                        genes[index] = newGene
                    } else {
                        gene.x = rng.nextInt(index)
                        gene.y = rng.nextInt(index)
                    }
                }
            }

}