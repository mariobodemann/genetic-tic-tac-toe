import kotlin.reflect.full.primaryConstructor

sealed class Gene {
    var x: Int = 0
    var y: Int = 0

    abstract fun eval(genes: List<Gene>): Int

    class Constant : Gene() {
        override fun eval(genes: List<Gene>): Int = x
    }

    class Add : Gene() {
        override fun eval(genes: List<Gene>): Int = genes[x].eval(genes) + genes[y].eval(genes)
    }

    class Subtract : Gene() {
        override fun eval(genes: List<Gene>): Int = genes[x].eval(genes) - genes[y].eval(genes)
    }

    class Multiply : Gene() {
        override fun eval(genes: List<Gene>): Int = genes[x].eval(genes) * genes[y].eval(genes)
    }

    class Divide : Gene() {
        override fun eval(genes: List<Gene>): Int {
            val a = genes[x].eval(genes)
            val b = genes[y].eval(genes)

            return if (b == 0) {
                0
            } else {
                a / b
            }
        }
    }

    class Modulo : Gene() {
        override fun eval(genes: List<Gene>): Int {
            val a = genes[x].eval(genes)
            val b = genes[y].eval(genes)

            return if (b == 0) {
                0
            } else {
                a % b
            }
        }
    }

    class Max : Gene() {
        override fun eval(genes: List<Gene>): Int = Integer.max(genes[x].eval(genes), genes[y].eval(genes))
    }

    class Min : Gene() {
        override fun eval(genes: List<Gene>): Int = Integer.min(genes[x].eval(genes), genes[y].eval(genes))
    }

    class Abs : Gene() {
        override fun eval(genes: List<Gene>): Int = Math.abs(genes[x].eval(genes))
    }

    class Equals : Gene() {
        override fun eval(genes: List<Gene>): Int = if (genes[x].eval(genes) == genes[y].eval(genes)) 1 else 0
    }

    class NotEquals : Gene() {
        override fun eval(genes: List<Gene>): Int = if (genes[x].eval(genes) != genes[y].eval(genes)) 1 else 0
    }

    class IsTrue : Gene() {
        override fun eval(genes: List<Gene>): Int = if (genes[x].eval(genes) == 0) 1 else 0
    }

    class IsFalse : Gene() {
        override fun eval(genes: List<Gene>): Int = if (genes[x].eval(genes) == 0) 1 else 0
    }

    class Signum : Gene() {
        override fun eval(genes: List<Gene>): Int = Math.signum(genes[x].eval(genes).toFloat()).toInt()
    }

    class IsEmpty : Gene() {
        override fun eval(genes: List<Gene>): Int = if (genes[x].eval(genes) == 0) 1 else 0
    }

    class IsYourPiece : Gene() {
        override fun eval(genes: List<Gene>): Int = if (genes[x].eval(genes) == 1) 1 else 0
    }

    class IsTheirPiece : Gene() {
        override fun eval(genes: List<Gene>): Int = if (genes[x].eval(genes) == 2) 1 else 0
    }
}

fun constructRandomGene(): Gene =
        Gene::class.nestedClasses.map { it.primaryConstructor }.randomElement()?.call() as Gene?
                ?: Gene.Constant()

