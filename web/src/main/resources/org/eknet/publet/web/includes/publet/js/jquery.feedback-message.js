/*
 * Copyright 2013 Eike Kettner
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

/**
 * This is a very simple jquery plugin to display feedback messages. It uses
 * by default bootstraps "alert alert-info" css class. By default the message
 * is removed after a certain delay (the html is cleared and the css classes
 * are reverted).
 *
 * To simply display a message:
 *
 *   $('.myfeedback').feedbackMessage('Your transaction has completed.');
 *
 * Give custom css classes to use:
 *
 *   $('.myfeedback').feedbackMessage({
 *     message: 'Your transaction has completed.',
 *     cssClass: 'alert alert-success'
 *   });
 *
 * Give a custom delay time in milliseconds. Use 0 or a negative value to display
 * the message permanently (it is not removed).
 *
 *   $('.myfeedback').feedbackMessage({
 *     message: 'Your transaction has completed.',
 *     cssClass: 'alert alert-success',
 *     delay: 1000
 *   });
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 11.04.13 09:56
 */
(function ($) {

  function display($this, settings) {
    if (settings.cssClass) {
      $this.addClass(settings.cssClass);
    }
    if (settings.delay && settings.delay > 0) {
      $this.html(settings.message).animate({delay: 1}, settings.delay, function () {
        $this.html("");
        if (settings.cssClass) {
          $this.removeClass(settings.cssClass);
        }
      })
    } else {
      $this.html(settings.message);
    }
  }

  var methods = {
    init: function (options) {
      return this.each(function () {
        var $this = $(this);
        var opts;
        if (typeof options === "string") {
          opts = { message: options };
        } else {
          opts = options;
        }
        var settings = $.extend({
          delay: 3500,
          cssClass: 'alert alert-info'
        }, opts);

        display($this, settings)
      });
    }
  };

  $.fn.feedbackMessage = function (method) {
    return methods.init.apply(this, arguments);
  };
})(jQuery);