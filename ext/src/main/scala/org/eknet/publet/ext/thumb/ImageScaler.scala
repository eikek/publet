/*
 * Copyright 2012 Eike Kettner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eknet.publet.ext.thumb

import java.awt.image.BufferedImage
import java.awt.{Transparency, RenderingHints}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 24.05.12 12:24
 */
object ImageScaler {


  /**
   * Scales the image using {@link #getScaledInstance(BufferedImage, int, int, Object, boolean)} if the size of
   * the image exceeds the specified boundaries. The image is scaled proportional. If the image is within the
   * specified boundaries it is returned.
   *
   * @param image
   * @param maxWidth
   * @param maxHeight
   * @return
   */
  def scaleIfNecessary(image: BufferedImage, maxWidth: Int, maxHeight: Int): BufferedImage = {
    val h = image.getHeight
    val w = image.getWidth
    if (h > maxHeight || w > maxWidth) {
      val dw = math.abs(maxWidth - w)
      val dh = math.abs(maxHeight - h)
      if (dw > dh) {
        val factor = (maxWidth * 1.0f) / w
        val nh = math.floor(h * factor).toInt
        return getScaledInstance(image, maxWidth, nh, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true)
      } else {
        val factor = (maxHeight * 1.0f) / h
        val nw = math.floor(w * factor).toInt
        return getScaledInstance(image, nw, maxHeight, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true)
      }
    }
    image
  }

  /**
   * Note, found this code on <a href="http://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html">this post</a>
   * by <a href="http://today.java.net/pub/au/60">Chris Campbell</a>.
   * <p/>
   * <p/>
   * Convenience method that returns a scaled instance of the
   * provided {@code BufferedImage}.
   *
   * @param img           the original image to be scaled
   * @param targetWidth   the desired width of the scaled instance,
   *                      in pixels
   * @param targetHeight  the desired height of the scaled instance,
   *                      in pixels
   * @param hint          one of the rendering hints that corresponds to
   *                      `RenderingHints.KEY_INTERPOLATION` (e.g.
   *                      `RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR`,
   *                      `RenderingHints.VALUE_INTERPOLATION_BILINEAR`,
   *                      `RenderingHints.VALUE_INTERPOLATION_BICUBIC`)
   * @param higherQuality if true, this method will use a multi-step
   *                      scaling technique that provides higher quality than the usual
   *                      one-step technique (only useful in downscaling cases, where
   *                      `targetWidth` or `targetHeight` is
   *                      smaller than the original dimensions, and generally only when
   *                      the `BILINEAR` hint is specified)
   * @return a scaled version of the original [[java.awt.image.BufferedImage]]
   */
  def getScaledInstance(img: BufferedImage, targetWidth: Int, targetHeight: Int, hint: Object, higherQuality: Boolean) : BufferedImage = {

    val itype = if (img.getTransparency == Transparency.OPAQUE)
      BufferedImage.TYPE_INT_RGB else BufferedImage.TYPE_INT_ARGB

    var ret = img.asInstanceOf[BufferedImage]
    var w:Int = 0
    var h:Int = 0
    if (higherQuality) {
      // Use multi-step technique: start with original size, then
      // scale down in multiple passes with drawImage()
      // until the target size is reached
      w = img.getWidth
      h = img.getHeight
    } else {
      // Use one-step technique: scale directly from original
      // size to target size with a single drawImage() call
      w = targetWidth
      h = targetHeight
    }

    do {
      if (higherQuality && w > targetWidth) {
        w /= 2
        if (w < targetWidth) {
          w = targetWidth
        }
      }

      if (higherQuality && h > targetHeight) {
        h /= 2
        if (h < targetHeight) {
          h = targetHeight
        }
      }

      val tmp = new BufferedImage(w, h, itype)
      val g2 = tmp.createGraphics()
      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint)
      g2.drawImage(ret, 0, 0, w, h, null)
      g2.dispose()

      ret = tmp
    } while (w != targetWidth || h != targetHeight)

    ret
  }

}
