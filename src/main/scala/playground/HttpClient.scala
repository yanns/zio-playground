package playground

import zio.console.Console
import zio.random.Random
import zio._

case class HttpResponse(
  status: Int,
  body: String)

// pseudo http client
case class HttpClient(threadPool: Int) {
  import HttpClient.Result

  def get(path: String): Result[HttpResponse] =
    for {
      _ <- console.putStrLn(s"GET $path")
      result <- random.nextInt(5) flatMap {
        case 0 => ZIO.fail(new Exception("technical http exception"))
        case 1 => ZIO.succeed(HttpResponse(404, "Result of GET"))
        case _ => ZIO.succeed(HttpResponse(200, "Result of GET"))
      }
    } yield result

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
  type Dependencies = Console with Random
  type Result[A] = ZIO[Dependencies, Throwable, A]

  def apply(threadPool: Int): ZManaged[Dependencies, Throwable, HttpClient] = {
    val load = for {
      _ <- console.putStrLn(s"initializing http client with $threadPool threads")
      httpClient <- Task.effectTotal(new HttpClient(threadPool))
    } yield httpClient

    def close(httpClient: HttpClient): ZIO[Dependencies, Nothing, Unit] =
      for {
        _ <- console.putStrLn("closing http client")
        _ <- httpClient.close
              .catchAll(e => console.putStrLn(s"error while closing http client: ${e.getMessage}"))
      } yield ()

    ZManaged.make[Dependencies, Throwable, HttpClient](load)(close)
  }
}
