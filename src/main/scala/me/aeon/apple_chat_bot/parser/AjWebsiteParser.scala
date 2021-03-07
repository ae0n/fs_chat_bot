package me.aeon.apple_chat_bot.parser

import me.aeon.apple_chat_bot.parser.AjWebsiteParser._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._

import scala.util.Try

class AjWebsiteParser {

  import java.security.SecureRandom
  import java.security.cert.X509Certificate
  import javax.net.ssl._


  private def enableSSLSocket(): Unit = {
    HttpsURLConnection.setDefaultHostnameVerifier((_: String, _: SSLSession) => true)
    val context = SSLContext.getInstance("TLS")
    context.init(null, Array(new X509TrustManager {
      def checkClientTrusted(chain: Array[X509Certificate], authType: String): Unit = {

      }

      def checkServerTrusted(chain: Array[X509Certificate], authType: String): Unit = {

      }

      def getAcceptedIssuers: Array[X509Certificate] = Array.empty
    }), new SecureRandom())
    HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory)
  }

  private val nameIdRemapper = Map[String, String](
    "Силиконовый чехол" -> "iphoneCase",
    "Кожаный чехол" -> "iphoneLeatherCase",
    "Адаптер питания USB-C" -> "usbCPower",
    "iMac Pro" -> "iMacPro",
    "iPad Pro 10,5\" 2017" -> "ipad2017_10",
    "Lightning Adapter" -> "lightning_cable",
    "EarPods with Remote and Mic" -> "earpods"
  )

  private val priceRegEx = """([\W—]?[\d\s]+(₽|руб.))$""".r
  enableSSLSocket()
  private val browser = JsoupBrowser()

  def collectPrices(): Map[String, Item] = {

    val doc = browser.get("https://aj.ru/")

    val bigItems = doc >> elementList("article:not(.right):not(.left)")
    val smallItems = doc >> elementList("article.right, article.left")


    val bigItemsParsed = bigItems.map { e =>
      val name = e >> text("h2")

      val preId = e.attrs.getOrElse("class", "").split(" ").head.trim


      val id = nameIdRemapper.getOrElse(name, preId).replaceAll("-", "_").trim


      val modifications = (e >> elementList("li")).flatMap { m =>
        val str = m >> text

        if (str.nonEmpty) {
          val modificationName = priceRegEx.replaceAllIn(str, "").trim
          val price = priceRegEx.findFirstIn(str).map(s => Price(s.replaceAll("\\D", "").toInt))

          Some(ItemModification(modificationName, price))
        } else None
      }

      Item(id.toLowerCase, name, modifications)

    }

    val smallItemsParsed = smallItems.map { e =>
      val id = e.attrs.getOrElse("class", "").replaceAll("left|right", "").trim
      val name = e >> text("h2")

      val price = Try(Price((e >> text("span")).replaceAll("\\D", "").toInt)).toOption

      Item(id.toLowerCase, name, basePrice = price)
    }

    val allItems = (bigItemsParsed ::: smallItemsParsed).foldLeft(Map.empty[String, Item]) {
      (acc, item) =>
        acc.get(item.id) match {
          case Some(stored) =>
            acc.updated(item.id, stored.copy(modifications = stored.modifications ++ item.modifications))
          case None =>
            acc.updated(item.id, item)
        }
    }

    //    println(allItems.keys.mkString("\n"))

    allItems

  }


}

object AjWebsiteParser {

  case class Price(value: Int) extends AnyVal {
    override def toString: String = {
      val discountedPrice = math.floor(value * 0.95).toInt
      s"$value ₽ (~$discountedPrice ₽*)"
    }
  }

  case class ItemModification(name: String, price: Option[Price])

  case class Item(id: String, name: String, modifications: Seq[ItemModification] = Seq.empty, basePrice: Option[Price] = None) {
    override def toString: String = {
      val modificationString = modifications.map { m =>
        m.price match {
          case Some(price) =>
            s"${m.name} - $price"
          case _ => m.name
        }
      }

      val stringedItem = if (modificationString.nonEmpty) {
        (name +: "" +: modificationString).mkString("\n")
      } else {
        val p = basePrice.map(_.toString).getOrElse("")
        Seq(name, "", p).mkString(" - ")
      }

      stringedItem + "\n* Для леперов, но это не точно. Уточняйте у @applejesus."
    }
  }

}


