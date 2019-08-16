package playground

import zio.ZIO
import zio.console.Console


case class Account(email: String, encryptedPassword: String)


trait AccountGateway extends Serializable {
  val accountGateway: AccountGateway.Service[Any]
}

object AccountGateway {
  type Result[A] = ZIO[InitializedHttpClient with Console, Throwable, A]

  trait Service[R] {
    def createAccount(email: String, password: String): Result[Account]
    def login(email: String, password: String): Result[Account]
  }

  trait Live extends AccountGateway {
    override val accountGateway: Service[Any] = new Service[Any] {
      override def createAccount(email: String, password: String): Result[Account] =
        ZIO.accessM { env =>
          for {
            httpResponse <- env.httpClient.httpClient.post("/accounts", s"email: $email, password: $password")
            result <- if (httpResponse.status == 200) ZIO(Account(email, "encrypted password"))
                      else ZIO.fail(new Exception(s"cannot create account: $httpResponse"))
          } yield result
        }

      override def login(email: String, password: String): Result[Account] =
        ZIO.accessM { env =>
          for {
            httpResponse <- env.httpClient.httpClient.get("/accounts/$email")
            result <- if (httpResponse.status == 200) ZIO(Account(email, "encrypted password"))
                      else ZIO.fail(new Exception(s"cannot create account: $httpResponse"))
          } yield result
        }
    }
  }
  object Live extends Live
}