package forex.services.rates.interpreters

import io.circe.Decoder
import io.circe.HCursor

final case class OneFrameRate(
    from: String,
    to: String,
    bid: BigDecimal,
    ask: BigDecimal,
    price: BigDecimal,
    timestamp: String
)


object OneFrameRate {
    implicit val decoder: Decoder[OneFrameRate] = (hCursor: HCursor) =>
    for {
      from <- hCursor.get[String]("from")
      to <- hCursor.get[String]("to")
      bid <- hCursor.get[BigDecimal]("bid")
      ask <- hCursor.get[BigDecimal]("ask")
      price <- hCursor.get[BigDecimal]("price")
      timestamp <- hCursor.get[String]("time_stamp")
    } yield OneFrameRate(from, to, bid, ask, price, timestamp)
}