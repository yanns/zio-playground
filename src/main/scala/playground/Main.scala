package playground

import zio.console.Console
import zio.internal.PlatformLive
import zio.{RIO, Runtime, ZIO}


object Main {

  type AppEnvironment = AccountGateway with InitializedHttpClient with Console
  type AppTask[A] = RIO[AppEnvironment, A]

  def main(args: Array[String]): Unit = {
    val myRuntime = Runtime[AppEnvironment](
      new AccountGateway.Live
      with Console.Live,
      PlatformLive.Default)

    myRuntime.unsafeRun(program)
  }

  val program: AppTask[Int] =
    ZIO.accessM { env =>
      for {
        _ <- env.accountGateway.createAccount("my@email", "my password")
        _ <- env.accountGateway.login("my@email", "my password")
      } yield 1
    }

}
