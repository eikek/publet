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

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 24.05.12 20:09
 */
(function ($) {

  function createLocationBar($this, path) {
    var settings = $this.data('publetBrowser').settings;
    return $('<'+settings.locationBarEl+'/>', {
      id: settings.locationBarId,
      class: settings.locationBarClass,
      html: path
    })
  }

  function _contents($this, pathname, callback) {
    var settings = $this.data('publetBrowser').settings;
    $.getJSON(settings.actionUrl, { path: pathname }, function (data) {
      var list = [];
      if (data.parent) {
        var li;
        if (settings.parentLiClass) {
          li = '<li class="'+settings.parentLiClass+'"><a href="#">..</a></li>';
        } else {
          li = '<li><a href="#">..</a></li>';
        }
        list.push(li);
      }
      //normal resource list, already ordered
      $.each(data.files, function(i, file) {
        var css;
        if (file.container) {
          css = settings.folderClass;
        } else {
          css = settings.fileClass;
        }
        var li;
        if (css) {
          li = '<li class="'+ css +'"><a href="#">'+ file.name +'</a></li>';
        } else {
          li = '<li><a href="#">'+ file.name +'</a></li>';
        }
        list.push(li);
      });

      var el = $('<ul/>', {
        class: settings.resourceListClass,
        html: list.join('\n')
      });
      callback(el, data);
    });
  }

  function addContents($this, pathname) {
    _contents($this, pathname, function(list, data) {
      var bar = createLocationBar($this, data.containerPath);
      bar.appendTo($this);
      list.appendTo($this);
      $("a", list).each(function(i, el) {
        $(el).bind('click', function(e) {
          $this.trigger('publetResourceClick', [data, i, el]);
          if (e.target.text == "..") {
            $this.empty();
            addContents($this, data.parent);
          } else {
            var offset = data.parent? 1 : 0;
            var file = data.files[i - offset];
            if (file.container) {
              $this.empty();
              addContents($this, file.sourceRef);
            }
          }
        });
      })
    });
  }

  var methods = {
    init:function (options) {
      return this.each(function() {
        var $this = $(this);
        var data = $this.data('publetBrowser');

        if (!data) {
          var settings = $.extend({
            'actionUrl'     : "/publet/webeditor/scripts/toc.json",
            'resourcePath'  : getURLParameter("resource") || "/",
            'resourceListClass' : null,
            'locationBarClass' : 'locationBar',
            'locationBarEl'    : 'div',
            'locationBarId'    : null,
            'parentLiClass'    : 'backLink',
            'folderClass'      : 'folder',
            'fileClass'        : 'page'
          }, options);
          $(this).data('publetBrowser', {
            target:$this,
            settings:settings
          });
          addContents($this, settings.resourcePath);
        }
      });
    }
  };

  $.fn.publetBrowser = function (method) {
    if (methods[method]) {
      return methods[ method ].apply(this, Array.prototype.slice.call(arguments, 1));
    } else if (typeof method === 'object' || !method) {
      return methods.init.apply(this, arguments);
    } else {
      $.error('Method ' + method + ' does not exist on jQuery.publetBrowser');
    }
  };
})(jQuery);