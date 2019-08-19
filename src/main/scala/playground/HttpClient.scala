package playground

import playground.InitializedHttpClient.Service
import zio.{Task, ZIO, ZManaged, console}
import zio.console.Console

case class HttpResponse(
  status: Int,
  body: String)

// pseudo http client
case class HttpClient(threadPool: Int) {
  import HttpClient.Result

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

object HttpClient {
  type Result[A] = ZIO[Console, Throwable, A]

  def apply(threadPool: Int): ZManaged[Console, Throwable, HttpClient] = {
    val load = for {
      _ <- console.putStrLn(s"initializing http client with $threadPool threads")
      httpClient <- Task.effectTotal(new HttpClient(threadPool))
    } yield httpClient

    def close(httpClient: HttpClient): ZIO[Console, Nothing, Unit] =
      for {
        _ <- console.putStrLn("closing http client")
        _ <- httpClient.close
              .catchAll(e => console.putStrLn(s"error while closing http client: ${e.getMessage}"))
      } yield ()

    ZManaged.make[Console, Throwable, HttpClient](load)(close)
  }
}
