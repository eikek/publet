$(function () {
  //initialize file browser
  $('#filesTree').publetBrowser({
    locationBarEl: 'pre',
    actionUrl: "/publet/webeditor/scripts/toc.json",
    locationBarId: 'containerPath'
  });

  // Initialize the jQuery File Upload widget:
  // if this is the upload page
  var fupload = $('#fileupload');
  if (fupload.length > 0) {
    fupload.fileupload();

    //copy the value of the location bar of the browser into the form
    //so the server knows where to put the files
    fupload.bind('fileuploadsubmit', function (e) {
      $('#containerPathInput').val($('#containerPath').text())
    });

    //load the file that is specified with `resource` param
    var res = getURLParameter("resource");
    if (res) {
      fupload.each(function () {
        var that = this;
        $.getJSON(this.action + "?resource=" + res, function (result) {
          if (result && result.name) {
            $(that).fileupload('option', 'done')
                .call(that, null, {result: { files: [result]} });
          }
        });
      });
    }
  }

  //resource click handler
  $('#filesTree').bind('publetResourceClick', function (e, data, i, el) {
    var offset = data.parent ? 1 : 0;
    var file = data.files[i - offset];
    if (file && !file.container) {
      if ((file.mimeBase && file.mimeBase == "text") || fupload.length == 0) {
        window.location = "/publet/webeditor/scripts/edit.html?resource=" + file.sourceRef;
      } else {
        if (fupload.length > 0) {
          fupload.fileupload('option', 'done').call(fupload, null, {
            result: { files: [{
              "name": file.name,
              "size": file.size,
              "url": file.href,
              "thumbnail_url": file.thumbnail,
              "delete_url": file.delete_url,
              "delete_type": "DELETE"
            }]}
          });
        }
      }
    }
  });
});
