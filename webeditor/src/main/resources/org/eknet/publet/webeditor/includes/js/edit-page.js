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

// javascript for the edit page.
// activates the codemirror editor and registers
// click handler for the few buttons

$(function() {

  //delete confirmation
  $('#confirmModal').modal({
    keyboad: false,
    show: false
  });
  $('#deleteFileButton').click(function() {
    $('#confirmModal').modal('show');
    return false;
  });
  $('#confirmModal .cancel').click(function() {
    $('#confirmModal').modal('hide');
  });
  $('#confirmModal .delete').click(function(event) {
    $(event.target).attr("disabled", "disabled").mask();
    window.location = $('#deleteFileButton').attr('href');
  });

  //activate tooltips and help popover
  var quickHelp =
      "<strong>F11</strong> toggles fullscreen mode;<br/>" +
      "<strong>CTRL+F</strong> starts search<br/>" +
      "<strong>CTRL+g</strong> next match<br/>" +
      "<strong>CTRL+Z</strong> undo<br/>" +
      "<strong>CTRL+D</strong> delete line.<br/>" +
      "<strong>CTRL+[, CTRL+]</strong> ident more/less";
  $('#editorHelpButton').attr("data-content", quickHelp);
  $('[rel="popover"]').popover({ placement: 'bottom' });
  $('[rel="tooltip"]').tooltip({ placement: 'top' });

  //convenience function to mask an element
  function _mask(element) {
    element.mask({
      spinner: {
        lines:15,
        length: 28,
        width: 3,
        radius: 18
      },
      delay: 1000
    });
  }

  function _unmask(element) {
    element.unmask();
  }

  //gets the mode for codemirror from the file extension
  function getEditorMode() {
    var filetype = $('#extensionInput').val();
    var mode = null;
    if (filetype === "scala") mode = "clike";
    if (filetype === "html" || filetype === "ssp" || filetype === "htm") mode = "htmlmixed";
    if (filetype === "css") mode = "css";
    if (filetype === "js") mode = "javascript";
    if (filetype === "markdown" || filetype === "md") mode = "markdown";
    if (filetype === "xml") mode = "xml";
    return mode;
  }

  //initializes the ajax form
  var options = {
    beforeSubmit: function(arr, form, options) {
      _mask(form);
      return true;
    },
    success: function(data, status, xhr, form) {
      _unmask(form);
      var closeIcon = ' <a class="close" data-dismiss="alert" href="#">×</a>';
      if (data.success) {
        $('#response').html('<div class="alert alert-success">'+data.message + closeIcon + '</div>');
        if (data.lastMod) {
          $('#lastHead').attr('value', data.lastMod);
        }
      } else {
        $('#response').html('<div class="alert alert-error">' +data.message + closeIcon + '</div>');
      }
    },
    dataType: 'json'
  };

  //must copy the editor contents _before_ the form plugin does its work
  //the `beforeSubmit` callback was still too late.
  $('#pageSaveButton').click(function() {
    $('#editPage').codemirror('save');
  });
  $('#editPageForm').ajaxForm(options);


  //toggle codemirror or plain textarea
  $('#toggleEditorButton').click(function() {
    var mode = getEditorMode();
    var extraKeys = {
      "F11": function(cm) {
        $('#editPage').codemirror('fullscreen');
      },
      "Esc": function(cm) {
        $('#editPage').codemirror('fullscreen', false);
      }
    };
    if (mode === "xml" || mode === "html") {
      extraKeys["'>'"] = function(cm) { cm.closeTag(cm, '>'); };
      extraKeys["'/'"] = function(cm) { cm.closeTag(cm, '/'); };
    }
    $('#editPage').codemirror('toggleEditor', {
      mode: getEditorMode(),
      extraKeys: extraKeys
    });
    if ($('#editPage').codemirror('active')) {
      $('#editorButtonBar').css('display', '');
    } else {
      $('#editorButtonBar').css('display', 'none');
    }
  });
  //use codemirror by default, of course ...
  $('#toggleEditorButton').click();

  //listen for changes of the file type extension selection and change mode
  $('#extensionInput').change(function() {
    $('#editPage').codemirror('updateMode', getEditorMode());
  });

  //register the click handlers for the few buttons
  $('#searchBarButton').click(function() {
    $('#editPage').codemirror('editor').execCommand('find');
    return false;
  });
  $('#autoformatBarButton').click(function() {
    $('#editPage').codemirror('autoFormatSelection');
    return false;
  });
  $('#commentBarButton').click(function() {
    $('#editPage').codemirror('commentSelection');
    return false;
  });
  $('#uncommentBarButton').click(function() {
    $('#editPage').codemirror('commentSelection', false);
    return false;
  });
  $('#fullscreenBarButton').click(function() {
    $('#editPage').codemirror('fullscreen');
    return false;
  })
});

