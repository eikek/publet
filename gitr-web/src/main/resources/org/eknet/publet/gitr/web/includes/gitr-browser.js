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
 * Repository browser jQuery plugin
 */

(function ($) {

  $.fn.gitrRepoBrowser = function (options) {

    //events: 'publetResourceClick'  on resource click
    //            arguments: e - event object
    //                       data - the complete json server response
    //                       i    - the index in the files array
    //                       el   - the `a` element

    var rParam = "r";
    var hParam = "h";
    var pParam = "p";

    var obj = this;
    var settings = $.extend({
      'actionUrl':"gitrview.json",
      'barElementId':"bar",
      'repo':getURLParameter(rParam) || "contentroot.git",
      'ref':getURLParameter(hParam) || "",
      'path':getURLParameter(pParam) || "",
      'tableClass'  : 'table table-condensed table-striped'
    }, options);

    var _contents = function (repo, head, path, callback) {
      var params = {};
      params[rParam] = repo;
      params[hParam] = head;
      params[pParam] = path;

      $.getJSON(settings.actionUrl, params, function (data) {
        if (data.success) {
          var list = [];
          if (data.parent) {
            list.push('<tr><td/><td><a href="#">..</a></td><td/><td/><tr>');
          }

          //normal resource list, already ordered
          $.each(data.files, function (i, file) {
            var css = file.icon;
            var li = '<tr>';
            var path = data.containerPath + file.name;
            li += '<td><i class="' + css + '"></i></td><td><a href="#">' + file.name + '</a></td>';
            li += '<td>' + file.age + '</td>';
            li += '<td>' + file.message + ' <span class="label">' + file.author + '</span></td>';
            list.push(li);
          });

          var table = $('<table/>', {
            class: settings.tableClass,
            html:'<thead><tr><th style="width:16px;"></th>' +
              '<th>name</th><th>age</th><th>message</th></tr></thead>' +
              '<tbody>' + list.join('\n') + '</tbody>'
          });
          callback(data, table);
        } else {
          callback(data)
        }

      });
    };

    var addContents = function (repo, head, path) {
      _contents(repo, head, path, function (data, table) {
        if (data.success) {
          table.appendTo(obj);
          $('a', table).each(function(i, el) {
            $(el).bind('click', function(e) {
              obj.trigger('gitResourceClick', [data, i, el]);
              if (e.target.text == "..") {
                obj.empty();
                addContents(repo, head, data.parentPath);
              } else {
                var offset = data.parent? 1 : 0;
                var file = data.files[i - offset];
                var path = data.containerPath + file.name;
                if (file.container) {
                  obj.empty();
                  addContents(repo, head, path)
                }
              }

            });
          });
        } else {
          obj.empty();
          $('<div class="alert alert-error">'+data.message+'</div>').appendTo(obj)
        }
      });
    };

    return this.each(function () {
      addContents(settings.repo, settings.ref, settings.path);
    });
  };
})(jQuery);