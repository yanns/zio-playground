package playground

import zio.{ZIO, console}
import zio.console.Console

case class HttpResponse(
  status: Int,
  body: String)

// pseudo http client
case class HttpClient(threadPool: Int) {
  type Result[A] = ZIO[Console, Throwable, A]
  def get(path: String): Result[HttpResponse] =
    for {
      _ <- console.putStrLn(s"GET $path")
    } yield HttpResponse(200, "Result of GET")

  def post(path: String, payload: String): Result[HttpResponse] =
    for {
      _ <- console.putStrLn(s"POST $path with $payload")
    } yield HttpResponse(200, "Result of POST")

  val close: Result[Unit] =
    for {
      _ <- console.putStrLn("http client closed")
    } yield ()
}

