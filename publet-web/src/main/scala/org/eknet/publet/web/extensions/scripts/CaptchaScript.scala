package org.eknet.publet.web.extensions.scripts

import org.eknet.publet.engine.scalascript.ScalaScript
import java.awt.image.BufferedImage
import java.awt.{Color, GradientPaint, RenderingHints, Font}
import util.Random
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import ScalaScript._
import org.eknet.publet.web.{Session, Key, WebContext}

/** Script generates a png image containing a random string. The
 * same string is set into the session.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 15.04.12 15:09
 */
object CaptchaScript extends ScalaScript {

  def captchaStrings(min: Int, max: Int, count: Int): Array[Array[Char]] = {
    val validchars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')
    val random = new Random()
    (for (c <- min to max) yield {
      val len = random.nextInt(max-min)+min
      (for (i <- 0 to len) yield validchars(random.nextInt(validchars.length))).toArray
    }).toArray
  }
  
  def createCaptcha(): (String, Array[Byte]) = {
    val width = 140;
    val height = 50;

    val data = captchaStrings(4, 6, 5)

    val bufferedImage = new BufferedImage(width, height,
      BufferedImage.TYPE_INT_RGB);

    val g2d = bufferedImage.createGraphics();

    val font = new Font("Georgia", Font.BOLD, 18);
    g2d.setFont(font);

    val rh = new RenderingHints(
      RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON);

    rh.put(RenderingHints.KEY_RENDERING,
      RenderingHints.VALUE_RENDER_QUALITY);

    g2d.setRenderingHints(rh);

    val gcol1 = "#"+ WebContext().parameter("col1").getOrElse("ffffff")
    val gcol2 = "#"+ WebContext().parameter("col2").getOrElse("0000ff")
    val gp = new GradientPaint(0, 0,
      Color.decode(gcol1), 0, height/2, Color.decode(gcol2), true);

    g2d.setPaint(gp);
    g2d.fillRect(0, 0, width, height);

    val frameCol = "#" + WebContext().parameter("frcol").getOrElse("000000")
    g2d.setColor(Color.decode(frameCol))
    g2d.drawRect(0, 0, width-1, height-1)

    val fgcol = "#"+ WebContext().parameter("fgcol").getOrElse("ff0f00")
    g2d.setColor(Color.decode(fgcol));

    val r = new Random();
    val index = scala.math.abs(r.nextInt()) % data.length;

    val captcha = String.copyValueOf(data(index));

    var x = 0;
    var y = 0;

    for (i <- 0 to data(index).length-1) {
      x += 10 + (scala.math.abs(r.nextInt()) % 15);
      y = 20 + scala.math.abs(r.nextInt()) % 20;
      g2d.drawChars(data(index), i, 1, x, y);
    }

    g2d.dispose();

    val baos = new ByteArrayOutputStream()
    ImageIO.write(bufferedImage, "png", baos);

    (captcha, baos.toByteArray)
  }

  def serve() = {
    val ctx = WebContext()
    import ctx._
    val captchaData = createCaptcha()

    val captchaKey = Key(parameter("captchaParam").getOrElse("captchaString"), {
      case Session => captchaData._1
    })
    ctx(captchaKey);

    makePng(captchaData._2)
  }
}
