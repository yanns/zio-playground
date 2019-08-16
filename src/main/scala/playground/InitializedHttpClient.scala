package playground

import zio.{IO, Task, ZIO, ZManaged, console}
import zio.console.Console


trait InitializedHttpClient extends Serializable {
  val httpClient: InitializedHttpClient.Service[Any]
}

object InitializedHttpClient {
  trait Service[R] {
    val httpClient: HttpClient
  }

  def apply(threadPool: Int): ZManaged[Console, Throwable, InitializedHttpClient] = {
    val load = Task.effectTotal(
      new InitializedHttpClient {
        override val httpClient: Service[Any] = new Service[Any] {
          override val httpClient: HttpClient = new HttpClient(threadPool)
        }
      }
    )
    def close(initializedHttpClient: InitializedHttpClient): ZIO[Console, Nothing, Unit] =
      for {
        _ <- console.putStrLn("closing http client")
        _ <- initializedHttpClient.httpClient.httpClient.close
              .catchAll(e => console.putStrLn(s"error while closing http client: ${e.getMessage}"))
      } yield ()

    ZManaged.make[Console, Throwable, InitializedHttpClient](load)(close)
  }
}
