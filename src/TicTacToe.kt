import java.util.*

internal const val PLAYER_COUNT = 32
internal const val INITIAL_GENE_COUNT: Int = 40
internal const val GENE_MUTATIONS: Int = 1
internal const val ELITE_FILL_RATE: Double = 1.0
internal const val GENERATION_COUNT: Int = 10_000_000

internal const val BOARD_FIELD_COUNT: Int = 9

val rng = Random()

fun main(arguments: Array<String>) {
    val players = mutableListOf<Player>()
    (1 until PLAYER_COUNT).forEach {
        players.add(GeneticPlayer("Genetic_$it", "$it"))
    }

    for (generation in 0 until GENERATION_COUNT) {
        players.forEach { it.wins = 0; it.games = 0 }

        for (geneticPlayer in players) {
            for (i in 0 until 100) {
                playOneGame(geneticPlayer, RandomPlayer("Randy", "R"))
            }
        }

        val best = players.sortedByDescending { it.wins }.first { it is GeneticPlayer }

        val toBeRemoved = players.subList(0, (players.size * ELITE_FILL_RATE).toInt()).toMutableList()
        if (toBeRemoved.contains(best)) {
            toBeRemoved.remove(best)
        }

        players.removeAll(toBeRemoved)

        toBeRemoved.forEach {
            players.add((best as GeneticPlayer).mutate())
        }

        println("Generation $generation done. Best is '${best::class.simpleName}' with ${best.wins} of ${best.games}.")
    }

    analyseResults(players)
}

fun analyseResults(players: List<Player>) {
    println()
    println()
    println("Evolution done")

    val sortedByDescending = players.sortedByDescending { it.wins }
    sortedByDescending.forEach {
        val player = it as GeneticPlayer
        println("%-16s ... % 5d: %s".format(it::class.simpleName, it.wins, extractSourceCode(player.genes.last(), player.genes)
        ))
    }

    val elite = sortedByDescending.first { it is GeneticPlayer }
    println()
    println()
    println("Genes from ${elite.name}:")
    (elite as GeneticPlayer).genes.reversed().forEachIndexed { index, gene ->
        println("%-3d: %-15s( [%2d] [%2d] ) = %d".format(elite.genes.size - index, gene::class.simpleName, gene.x, gene.y, gene.eval(elite.genes)))
    }

    println()
    println()
    println("Testing against random")

    elite.games = 0
    elite.wins = 0
    elite.piece = "@"

    val pete = RandomPlayer("Pete", "P")
    for (i in 0 until 100) {
        val board = playOneGame(elite, pete)

        if (board.getWinner() == pete) {
//            board.draw()
//            println("-> ${board.getWinner()?.name ?: "no one"} won.")
        }
    }

    println()
    println("Genetic won ${elite.wins} games.")
    println("Random won ${pete.wins} games.")

    println()
    println("Winning Code:")

    print(extractSourceCode(elite.genes.last(), elite.genes))
}

fun extractSourceCode(gene: Gene, genes: List<Gene>): String =
        if (gene is Gene.Constant) {
            if (genes.indexOf(gene) < BOARD_FIELD_COUNT) {
                "Field[${gene.eval(listOf())}]"
            } else {
                "${gene.eval(listOf())}"
            }
        } else {
            "${gene::class.simpleName}(${extractSourceCode(genes[gene.x], genes)},${extractSourceCode(genes[gene.y], genes)})"
        }

private fun <T> Pair<T, T>.randomize(): Pair<T, T> =
        if (rng.nextBoolean()) {
            Pair(first, second)
        } else {
            Pair(second, first)
        }

private fun <T> List<T>.permutations(): List<Pair<T, T>> {
    val mutableInput = this.toMutableList()
    val permutations = mutableListOf<Pair<T, T>>()

    for (element: T in this) {
        mutableInput.remove(element)

        val permutation = mutableListOf<Pair<T, T>>()
        for (mutated: T in mutableInput) {
            permutation.add(Pair(element, mutated))
        }
        permutations.addAll(permutation)
    }

    return permutations
}

fun playOneGame(playerX: Player, playerO: Player): Board {
    val board = Board()
    var move = 0
    playerO.games++
    playerX.games++

    while (board.getWinner() == null && !board.isFull()) {
        if (move % 2 == 0) {
            playerX
        } else {
            playerO
        }.move(board)
        move++
    }

    val winner = board.getWinner()
    if (winner != null) {
        winner.wins++
    }
    return board
}

interface Player {
    fun move(board: Board)

    val name: String
    var piece: String

    var games: Int
    var wins: Int
}

data class RandomPlayer(override val name: String, override var piece: String, override var wins: Int = 0, override var games: Int = 0) : Player {
    override fun move(board: Board) {
        val freeFieldIndex = board.getFreeIndices().randomElement()
        board.place(this, freeFieldIndex)
    }
}


class Board(private val fields: Array<Player?> = Array(BOARD_FIELD_COUNT) { null }) {
    fun isFull() = fields.count { it != null } == BOARD_FIELD_COUNT

    fun getFreeIndices(): List<Int> = fields.mapIndexed { i, o -> if (o == null) i else null }.filterNotNull()

    fun place(player: Player, index: Int) {
        if (fields[index] == null) {
            fields[index] = player
        }
    }

    fun isEmpty(index: Int) = index >= 0 && index < fields.size && fields[index] == null

    fun getWinner(): Player? {
        listOf(
                getRow(0), getRow(1), getRow(2),
                getColumn(0), getColumn(1), getColumn(2),
                getDiagonal(0), getDiagonal(1)
        ).forEach { candidates ->
            if (candidates.count { it != null && it == candidates[0] } == 3) {
                return candidates[0]
            }
        }

        return null
    }

    fun draw() {
        fun p(index: Int) = fields[index]?.piece ?: " "

        var i = 0
        println("┌-┬-┬-┐")
        println("│${p(i++)}│${p(i++)}│${p(i++)}│")
        println("├-┼-┼-┤")
        println("│${p(i++)}│${p(i++)}│${p(i++)}│")
        println("├-┼-┼-┤")
        println("│${p(i++)}│${p(i++)}│${p(i++)}│")
        println("└-┴-┴-┘")
    }

    private fun getRow(row: Int): Array<Player?> = arrayOf(fields[0 + row * 3], fields[1 + row * 3], fields[2 + row * 3])

    private fun getColumn(column: Int): Array<Player?> = arrayOf(fields[column + 0 * 3], fields[column + 1 * 3], fields[column + 2 * 3])

    private fun getDiagonal(diagonal: Int): Array<Player?> = arrayOf(fields[(2 * (1 - diagonal)) - 0 + 0 * 3], fields[(2 * (1 - diagonal)) - 1 + 1 * 3], fields[(2 * (1 - diagonal)) - 2 + 2 * 3])

    fun mapFieldsToPlayerState(player: Player): List<Int> = fields.map {
        when (it) { player -> 1; null -> 0; else -> 2
        }
    }

}

internal fun <T> List<T>.randomElement(): T = this[rng.nextInt(size)]
