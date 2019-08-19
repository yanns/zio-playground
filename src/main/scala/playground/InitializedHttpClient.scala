package playground


trait InitializedHttpClient extends Serializable {
  val httpClient: InitializedHttpClient.Service[Any]
}

object InitializedHttpClient {
  trait Service[R] {
    val httpClient: HttpClient
  }

  def live(anHttpClient: HttpClient): InitializedHttpClient.Service[Any] =
    new InitializedHttpClient.Service[Any] {
      override val httpClient: HttpClient = anHttpClient
    }
}
