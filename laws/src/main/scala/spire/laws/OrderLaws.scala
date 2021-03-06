package spire
package laws

import spire.algebra._
import spire.implicits._

import org.typelevel.discipline.Laws

import org.scalacheck.{Arbitrary, Prop}
import org.scalacheck.Prop._

object OrderLaws {
  def apply[A : Eq : Arbitrary] = new OrderLaws[A] {
    def Equ = Eq[A]
    def Arb = implicitly[Arbitrary[A]]
  }
}

trait OrderLaws[A] extends Laws {

  implicit def Equ: Eq[A]
  implicit def Arb: Arbitrary[A]

  def partialOrder(implicit A: PartialOrder[A]) = new OrderProperties(
    name = "partialOrder",
    parent = None,
    "reflexitivity" → forAll((x: A) =>
      checkTrue(x <= x)
    ),
    "antisymmetry" → forAll((x: A, y: A) =>
      checkTrue((x <= y && y <= x) imp (x === y))
    ),
    "transitivity" → forAll((x: A, y: A, z: A) =>
      checkTrue((x <= y && y <= z) imp (x <= z))
    ),
    "gteqv" → forAll((x: A, y: A) =>
      (x <= y) <=> (y >= x)
    ),
    "lt" → forAll((x: A, y: A) =>
      (x < y) <=> (x <= y && x =!= y)
    ),
    "gt" → forAll((x: A, y: A) =>
      (x < y) <=> (y > x)
    )
  )

  def order(implicit A: Order[A]) = new OrderProperties(
    name = "order",
    parent = Some(partialOrder),
    "totality" → forAll((x: A, y: A) =>
      checkTrue(x <= y || y <= x)
    )
  )

  def signed(implicit A: Signed[A]) = new OrderProperties(
    name = "signed",
    parent = Some(order),
    "abs non-negative" → forAll((x: A) =>
      checkTrue(x.abs.sign != Sign.Negative)
    ),
    "signum returns -1/0/1" → forAll((x: A) =>
      checkTrue(x.signum.abs <= 1)
    ),
    "signum is sign.toInt" → forAll((x: A) =>
      checkTrue(x.signum == x.sign.toInt)
    )
  )

  def truncatedDivision(implicit cRingA: CRing[A], truncatedDivisionA: TruncatedDivision[A]) = new DefaultRuleSet(
    name = "truncatedDivision",
    parent = Some(signed),
    "division rule (tquotmod)" → forAll { (x: A, y: A) =>
      checkTrue(y.isZero || {
        val (q, r) = x tquotmod y
        x === y * q + r
      })
    },
    "division rule (fquotmod)" → forAll { (x: A, y: A) =>
      checkTrue(y.isZero || {
        val (q, r) = x fquotmod y
        x == y * q + r
      })
    },
    "quotient is integer (tquot)" → forAll { (x: A, y: A) =>
      checkTrue(y.isZero || (x tquot y).toBigIntOpt.nonEmpty)
    },
    "quotient is integer (fquot)" → forAll { (x: A, y: A) =>
      checkTrue(y.isZero || (x fquot y).toBigIntOpt.nonEmpty)
    },
    "|r| < |y| (tmod)" → forAll { (x: A, y: A) =>
      checkTrue(y.isZero || {
        val r = x tmod y
        r.abs < y.abs
      })
    },
    "|r| < |y| (fmod)" → forAll { (x: A, y: A) =>
      checkTrue(y.isZero || {
        val r = x fmod y
        r.abs < y.abs
      })
    },
    "r = 0 or sign(r) = sign(x) (tmod)" → forAll { (x: A, y: A) =>
      checkTrue(y.isZero || {
        val r = x tmod y
        r.isZero || (r.sign === x.sign)
      })
    },
    "r = 0 or sign(r) = sign(y) (fmod)" → forAll { (x: A, y: A) =>
      checkTrue(y.isZero || {
        val r = x fmod y
        r.isZero || (r.sign === y.sign)
      })
    },
    "tquot" → forAll { (x: A, y: A) =>
      checkTrue(y.isZero || {
        (x tquotmod y)._1 === (x tquot y)
      })
    },
    "tmod" → forAll { (x: A, y: A) =>
      checkTrue(y.isZero || {
        (x tquotmod y)._2 === (x tmod y)
      })
    },
    "fquot" → forAll { (x: A, y: A) =>
      checkTrue(y.isZero || {
        (x fquotmod y)._1 === (x fquot y)
      })
    },
    "fmod" → forAll { (x: A, y: A) =>
      checkTrue(y.isZero || {
        (x fquotmod y)._2 === (x fmod y)
      })
    }
    
  )

  class OrderProperties(
    name: String,
    parent: Option[OrderProperties],
    props: (String, Prop)*
  ) extends DefaultRuleSet(name, parent, props: _*)

}

// vim: expandtab:ts=2:sw=2
