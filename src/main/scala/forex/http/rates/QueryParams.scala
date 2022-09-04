package forex.http.rates

import forex.domain.Currency
import org.http4s.QueryParamDecoder
import org.http4s.dsl.impl.ValidatingQueryParamDecoderMatcher
import org.http4s.ParseFailure

object QueryParams {

  private[http] implicit val currencyQueryParam: QueryParamDecoder[Currency] =
    QueryParamDecoder[String].emap( currencyCode => {
      try { 
        Right(Currency.withName(currencyCode))
      } catch {
        case _ : NoSuchElementException => Left(ParseFailure("invalid currency code", s"invalid currency code, $currencyCode"))
      }
  })

  object FromQueryParam extends ValidatingQueryParamDecoderMatcher[Currency]("from")
  object ToQueryParam extends ValidatingQueryParamDecoderMatcher[Currency]("to")

}
