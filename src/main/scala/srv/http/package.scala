package srv

import java.util.UUID

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.MediaTypes.{`application/json` => JSON}
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.stream.scaladsl.Source
import akka.util.ByteString
import cats.effect.IO
import io.circe.parser._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}


package object http {
  implicit def toResponseMarshaller[A: Encoder]: ToEntityMarshaller[IO[A]] = {
    Marshaller.withFixedContentType(JSON) { ioA: IO[A] =>
      HttpEntity(
        JSON,
        Source.fromFuture(ioA.map(a => ByteString(a.asJson.noSpaces)).unsafeToFuture())
      )
    }
  }

  implicit def fromRequestUnmarshaller[A: Decoder]: FromEntityUnmarshaller[IO[A]] =
    Unmarshaller.stringUnmarshaller.map(str => IO.fromEither(decode[A](str)))

  implicit val stringToUUIDUnmarshaller: Unmarshaller[String, UUID] =
    Unmarshaller.strict(UUID.fromString)
}
