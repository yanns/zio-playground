package playground

import zio.console.Console
import zio.random.Random
import zio.{RIO, ZIO, console}


object Main extends zio.App {

  override def run(args: List[String]): ZIO[Main.Environment, Nothing, Int] =
    completeProgram.foldM[Main.Environment, Nothing, Int](
      failure = ex => console.putStrLn(s"error: ${ex.getMessage}").map(_ => 1),
      success = ZIO.succeed
    )

  type AppEnvironment = AccountGateway with InitializedHttpClient with Console with Random
  type AppTask[A] = RIO[AppEnvironment, A]

  val program: AppTask[Int] =
    ZIO.accessM { env =>
      for {
        _ <- env.accountGateway.createAccount("my@email", "my password")
        _ <- env.accountGateway.login("my@email", "my password")
      } yield 0
    }

  val completeProgram: ZIO[Console with Random, Throwable, Int] = {
    HttpClient(10).use { anHttpClient =>
      val liveEnv: AppEnvironment = newLiveEnv(anHttpClient)
      program.provide(liveEnv)
    }
  }

  private def newLiveEnv(anHttpClient: HttpClient): AppEnvironment =
    new InitializedHttpClient with AccountGateway.Live with Console.Live with Random.Live {
      override val httpClient: InitializedHttpClient.Service[Any] = InitializedHttpClient.live(anHttpClient)
    }
}
