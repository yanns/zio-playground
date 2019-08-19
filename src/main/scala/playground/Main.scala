package playground

import zio.console.Console
import zio.internal.PlatformLive
import zio.{RIO, Runtime, Task, ZIO}


object Main {

  def main(args: Array[String]): Unit = {
    val bootstrapRuntime = Runtime[Console](
      Console.Live,
      PlatformLive.Default)

    sys.exit(bootstrapRuntime.unsafeRun(completeProgram))
  }

  type AppEnvironment = AccountGateway with InitializedHttpClient with Console
  type AppTask[A] = RIO[AppEnvironment, A]

  val program: AppTask[Int] =
    ZIO.accessM { env =>
      for {
        _ <- env.accountGateway.createAccount("my@email", "my password")
        _ <- env.accountGateway.login("my@email", "my password")
      } yield 1
    }

  val completeProgram: ZIO[Console, Throwable, Int] = {
    HttpClient(10).use { anHttpClient =>
      val liveEnv: AppEnvironment = newLiveEnv(anHttpClient)
      program.provide(liveEnv)
    }
  }

  private def newLiveEnv(anHttpClient: HttpClient): AppEnvironment =
    new InitializedHttpClient with AccountGateway.Live with Console.Live {
      override val httpClient: InitializedHttpClient.Service[Any] = InitializedHttpClient.live(anHttpClient)
    }
}
