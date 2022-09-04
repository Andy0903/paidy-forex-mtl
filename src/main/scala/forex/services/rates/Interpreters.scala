package forex.services.rates

import cats.Applicative
import interpreters._
import forex.config.OneFrameConfig

object Interpreters {
  def live[F[_]: Applicative](config : OneFrameConfig): Algebra[F] = new OneFrameLive[F](config)
}
