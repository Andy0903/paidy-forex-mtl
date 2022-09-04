package forex.services.rates.interpreters

import forex.services.rates.Algebra
import cats.Applicative
import cats.syntax.applicative._
import cats.syntax.either._
import forex.domain.{ Price, Rate, Timestamp }
import java.net.http.{ HttpClient, HttpRequest }
import java.net.URI
import java.net.http.HttpResponse
import io.circe.parser._
import forex.config.OneFrameConfig
import forex.domain.Currency
import java.time.Instant
import java.time.ZoneOffset
import java.time.Duration
import forex.services.rates.errors.Error

class OneFrameLive[F[_]: Applicative](config : OneFrameConfig) extends Algebra[F] {
  
  val request : HttpRequest = getRequest()
  var cache : Map[Rate.Pair, (Price, Timestamp)] = Map.empty
  
  private def getRequest() : HttpRequest = {
    val queryParams : Set[String] = for {
      pair <- Currency.allPossiblePairs
    } yield s"pair=${pair.from}${pair.to}"
    
    HttpRequest.newBuilder()
    .uri(new URI(s"${config.url}/rates?" +  queryParams.mkString("&")))
    .header("token", config.token)
    .GET()
    .build()
  }
  
  override def get(pair: Rate.Pair): F[Either[Error, Rate]] = 
    getPriceFromCache(pair).map(_.asRight[Error]).getOrElse(getPriceFromAPI(pair)).pure[F]
  
  private def getPriceFromCache(pair: Rate.Pair): Option[Rate] = {
    cache.get(pair) match {
      case None => Option.empty
      case Some((price, timestamp)) => {
        val timeSinceFetch = Duration.between(timestamp.value, Timestamp.now.value).toSeconds()
        if (timeSinceFetch < config.refreshrate) Option(Rate(pair, price, timestamp)) else Option.empty
      }
    }
  }
  
  private def getPriceFromAPI(pair: Rate.Pair): Either[Error, Rate] = {
    getPricesFromAPI() match {
      case Left(error) => Left(error)
      case Right(rates) => {
        cache = rates
        val (price, timestamp) = cache(pair)
        Right(Rate(pair, price, timestamp))
      }
    }
  }
  
  private def getPricesFromAPI(): Either[Error, Map[Rate.Pair, (Price, Timestamp)]] = {
    val response = HttpClient.newHttpClient.send(request, HttpResponse.BodyHandlers.ofString())
    val responses = decode[List[OneFrameRate]](response.body())
    
    responses match {
      case Left(error) => Left(Error.OneFrameLookupFailed(s"Failed to decode OneFrame rates. Error message: ${error.getMessage()}"))
      case Right(value) => {
        Right(value.map(x => {
          val pair = Rate.Pair(Currency.withName(x.from), Currency.withName(x.to))
          val price = Price(x.price)
          val timestamp = Timestamp(Instant.parse(x.timestamp).atOffset(ZoneOffset.UTC))
          (pair, (price, timestamp))
        }).toMap)
      }
    }
  }
}
