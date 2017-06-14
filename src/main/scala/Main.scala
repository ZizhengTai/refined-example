import scala.language.{higherKinds, implicitConversions}

import cats.implicits._
import doobie.imports._
import fs2.Task
import fs2.interop.cats._
import eu.timepit.refined.api.{RefType, Validate}
import eu.timepit.refined.types.numeric.PosInt

object Main {
  implicit def refinedMeta[T, P, F[_,_]](
    implicit metaT:    Meta[T],
             validate: Validate[T, P],
             refType:  RefType[F],
             manifest: Manifest[F[T,P]]
  ): Meta[F[T, P]] =
    metaT.xmap[F[T,P]](
      refineType[T,P,F],
      unwrapRefinedType[T,P,F]
    )

  implicit def refinedComposite[T, P, F[_,_]](
    implicit compositeT: Composite[T],
             validate:   Validate[T, P],
             refType:    RefType[F],
             manifest:   Manifest[F[T,P]]
  ): Composite[F[T,P]] =
    compositeT.imap[F[T,P]](refineType[T,P,F])(unwrapRefinedType[T,P,F])

  private def refineType[T,P,F[_,_]](t: T)(
    implicit refType:  RefType[F],
             validate: Validate[T, P],
             manifest: Manifest[F[T,P]]
  ): F[T,P] =
    rightOrException[F[T,P]](refType.refine[P](t)(validate))

  private def unwrapRefinedType[T,P,F[_,_]](ftp: F[T,P])(
    implicit refType: RefType[F]
  ): T =
    refType.unwrap(ftp)

  private def rightOrException[T](either: Either[String, T])(
    implicit manifest: Manifest[T]
  ): T =
    either match {
      case Left(err) => throw new Exception(s"${manifest.runtimeClass.getName}: validation failed: $err")
      case Right(t) => t
    }

  def main(args: Array[String]): Unit = {
    val driver = "org.postgresql.Driver"
    val url = "jdbc:postgresql:db"
    val username = "user"
    val password = "pass"

    val xa = DriverManagerTransactor[Task](driver, url, username, password)

    val x = sql"SELECT 42".query[Option[PosInt]].unique.attempt.transact(xa).unsafeRun()
    println(x)

    val y = sql"SELECT NULL".query[Option[PosInt]].unique.attempt.transact(xa).unsafeRun()
    println(y)
  }
}
