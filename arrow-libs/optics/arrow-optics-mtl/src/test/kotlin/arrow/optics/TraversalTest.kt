package arrow.optics

import arrow.core.ForListK
import arrow.core.ListK
import arrow.core.extensions.listk.traverse.traverse
import arrow.core.fix
import arrow.core.test.UnitSpec
import arrow.core.test.generators.functionAToB
import arrow.core.test.generators.listK
import arrow.core.toT
import arrow.mtl.State
import arrow.mtl.map
import arrow.mtl.run
import arrow.optics.mtl.assign
import arrow.optics.mtl.assignOld
import arrow.optics.mtl.assign_
import arrow.optics.mtl.extract
import arrow.optics.mtl.extractMap
import arrow.optics.mtl.toState
import arrow.optics.mtl.update
import arrow.optics.mtl.updateOld
import arrow.optics.mtl.update_
import io.kotest.property.Arb
import io.kotest.property.checkAll

class TraversalTest : UnitSpec() {

  init {

    val listKTraverse = Traversal.fromTraversable<ForListK, Int, Int>(ListK.traverse())

    with(listKTraverse) {
      "Extract should extract the focus from the state" {
        checkAll(Gen.listK(Arb.int())) { ints ->
          extract().run(ints) ==
            State { iis: ListK<Int> ->
              iis toT getAll(iis)
            }.run(ints)
        }
      }

      "toState should be an alias to extract" {
        checkAll(Gen.listK(Arb.int())) { ints ->
          toState().run(ints) == extract().run(ints)
        }
      }

      "Extracts with f should be same as extract and map" {
        checkAll(Gen.listK(Arb.int()), Arb.functionAToB<Int, String>(Arb.string())) { ints, f ->
          extractMap(f).run(ints) == extract().map { it.map(f) }.run(ints)
        }
      }

      "update f should be same modify f within State and returning new state" {
        checkAll(Gen.listK(Arb.int()), Arb.functionAToB<Int, Int>(Arb.int())) { ints, f ->
          update(f).run(ints) ==
            State<ListK<Int>, ListK<Int>> { iis: ListK<Int> ->
              modify(iis, f)
                .let { it.fix() toT getAll(it) }
            }.run(ints)
        }
      }

      "updateOld f should be same as modify f within State and returning old state" {
        checkAll(Gen.listK(Arb.int()), Arb.functionAToB<Int, Int>(Arb.int())) { ints, f ->
          updateOld(f).run(ints) ==
            State { iis: ListK<Int> ->
              modify(iis, f).fix() toT getAll(iis)
            }.run(ints)
        }
      }

      "update_ f should be as modify f within State and returning Unit" {
        checkAll(Gen.listK(Arb.int()), Arb.functionAToB<Int, Int>(Arb.int())) { ints, f ->
          update_(f).run(ints) ==
            State { iis: ListK<Int> ->
              modify(iis, f).fix() toT Unit
            }.run(ints)
        }
      }

      "assign a should be same set a within State and returning new value" {
        checkAll(Gen.listK(Arb.int()), Arb.int()) { ints, i ->
          assign(i).run(ints) ==
            State { iis: ListK<Int> ->
              set(iis, i)
                .let { it.fix() toT getAll(it) }
            }.run(ints)
        }
      }

      "assignOld f should be same as modify f within State and returning old state" {
        checkAll(Gen.listK(Arb.int()), Arb.int()) { ints, i ->
          assignOld(i).run(ints) ==
            State { iis: ListK<Int> ->
              set(iis, i).fix() toT getAll(iis)
            }.run(ints)
        }
      }

      "assign_ f should be as modify f within State and returning Unit" {
        checkAll(Gen.listK(Arb.int()), Arb.int()) { ints, i ->
          assign_(i).run(ints) ==
            State { iis: ListK<Int> ->
              set(iis, i).fix() toT Unit
            }.run(ints)
        }
      }
    }
  }
}
