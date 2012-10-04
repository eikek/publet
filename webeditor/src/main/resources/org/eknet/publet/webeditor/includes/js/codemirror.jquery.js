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
 * Integrates <a href="http://codemirror.net/">CodeMirror</a> with JQuery.
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 04.10.12 14:06
 */
(function ($) {

  function isInitialized(obj) {
    var data = $(obj).data('codemirror');
    return  data !== null && data !== undefined;
  }

  function isFullScreen(cm) {
    return /\bCodeMirror-fullscreen\b/.test(cm.getWrapperElement().className);
  }

  function winHeight() {
    return window.innerHeight || (document.documentElement || document.body).clientHeight;
  }

  function setFullScreen(cm, full) {
    var wrap = cm.getWrapperElement(), scroll = cm.getScrollerElement();
    if (full) {
      wrap.className += " CodeMirror-fullscreen";
      scroll.style.height = winHeight() + "px";
      document.documentElement.style.overflow = "hidden";
    } else {
      wrap.className = wrap.className.replace(" CodeMirror-fullscreen", "");
      scroll.style.height = "";
      document.documentElement.style.overflow = "";
    }
    cm.refresh();
  }

  var methods = {

    init:function (options) {
      return this.each(function() {
        var $this = $(this), data = $this.data('codemirror');

        if (!data) {
          var settings = $.extend({
            lineNumbers: true,
            tabSize: 2,
            matchBrackets: true,
            onCursorActivity: function(editor) {
              editor.matchHighlight("CodeMirror-matchhighlight");
              var data = $(editor.getTextArea()).data('codemirror');
              editor.setLineClass(data.oldHlLine, null, null);
              data.oldHlLine = editor.setLineClass(editor.getCursor().line, null, "activeline");
            }
          }, options);
          $(this).data('codemirror', {
            target: $this,
            oldHlLine: 0,
            displayStyle: $this.css("display") || "inherit",
            settings: settings,
            editor: CodeMirror.fromTextArea($this[0], settings)
          });
        }
      });
    },

    updateMode: function(mode) {
      if (isInitialized(this)) {
        $(this).data('codemirror').editor.setOption("mode", mode);
      }
      return this;
    },

    clear: function() {
      if (isInitialized(this)) {
        var data = $(this).data('codemirror');
        $(this).data('codemirror').editor.toTextArea();
        $(this).data('codemirror', null);
      }
      return this
    },

    active: function() {
      return isInitialized(this);
    },

    editor: function() {
      if (isInitialized(this)) {
        return $(this).data('codemirror').editor;
      } else {
        return null;
      }
    },

    save: function() {
      if (isInitialized(this)) {
        $(this).data('codemirror').editor.save();
      }
      return this;
    },

    toggleEditor: function(options) {
      if (isInitialized(this)) {
        return this.codemirror('clear');
      } else {
        return this.codemirror(options);
      }
    },

    fullscreen: function(flag) {
      var cm = $(this).data('codemirror').editor;
      if (flag == null) flag = !isFullScreen(cm);

      setFullScreen(cm, flag);
      return this;
    }
  };

  CodeMirror.connect(window, "resize", function() {
    var showing = document.body.getElementsByClassName("CodeMirror-fullscreen")[0];
    if (!showing) return;
    showing.CodeMirror.getScrollerElement().style.height = winHeight() + "px";
  });

  $.fn.codemirror = function (method) {
    if (methods[method]) {
      return methods[ method ].apply(this, Array.prototype.slice.call(arguments, 1));
    } else if (typeof method === 'object' || !method) {
      return methods.init.apply(this, arguments);
    } else {
      $.error('Method ' + method + ' does not exist on jQuery.gitrRepoBrowser');
    }
  };
})(jQuery);
