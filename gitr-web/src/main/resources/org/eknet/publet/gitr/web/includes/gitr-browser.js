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

  var rParam = "r";
  var hParam = "h";
  var pParam = "p";
  var doParam = "do";

  var obj = this;

  var _mask = function() {
    $('#'+ obj.attr("id")).mask({
      spinner: {
        lines:15,
        length: 28,
        width: 3,
        radius: 18
      },
      delay: 1000
    });
  };
  var _unmask = function() {
    $('#'+ obj.attr("id")).unmask();
  };

  var _getBreadcrumbsHtml = function(repo, path) {
    var crumbs = [];
    crumbs.push('<li><a href="#">'+repo+'</a> <span class="divider">/</span></li>')
    var len = path.split('/').length;
    $.each(path.split('/'), function(i, el) {
      if (i>0 && el) {
        crumbs.push('<li><a href="#">'+el+'</a> <span class="divider">/</span></li>')
      }
    });
    return $('<ul/>', {
      class:"breadcrumb",
      html:crumbs.join('\n')
    });
  };

  var _getLastCommitHtml = function(data) {
    var lastCommit = "";
    if (data.lastCommit) {
      var c = data.lastCommit;
      var lastCommitUrl;
      if (settings.lastCommitUrlFunction) {
          lastCommitUrl = settings.lastCommitUrlFunction(c, settings);
      } else {
          lastCommitUrl = '?r='+settings.repo+'&do=commit&h='+ c.fullId;
      }
      lastCommit = $('<ol/>', {
        class: "commit-group",
        html: '<li class="commit-group-item">' +
          '<img class="gravatar" src="'+c.gravatar+'?s=35&d='+settings.gravatarTheme+'"/> ' +
          '<p class="commit-title">' +
          '<span title="'+ c.authorEmail +'">'+c.author +', '+ c.age +'</span> ' +
          '<small>-- '+ c.commitDate +' --</small>' +
          '<span class="pull-right"><a href="'+ lastCommitUrl +'">['+ c.id+']</a></span> ' +
          '<br>' +
          '<span class="shortMessage">'+ c.message +'</span> '+
          '</p></li>'
      });
    }
    return lastCommit;
  };

  var _addFileContents = function(repo, head, path, el) {
    var params = {};
    params[rParam] = repo;
    params[hParam] = head;
    params[pParam] = path;
    params[doParam] = "blob";

    _mask();
    $.getJSON(settings.actionUrl, params, function(data) {
      //obj.removeAttr("class");
      _unmask();
      if (data.success) {
        $('table', obj).prev().remove();
        $('table', obj).remove();
        $('.readme-head').remove();
        $('.readme').remove();
        _getLastCommitHtml(data).appendTo(obj);
        $('<h4/>', {
          class: "readme-head",
          html: data.fileName
        }).appendTo(obj);
        if (data.contents) {
          if (data.processed) {
            var readme = $('<div/>', {
              class: "readme",
              html: data.contents
            });
            readme.appendTo(obj);
            if (hljs) {
              $('code', readme).each(function(i, e) { hljs.highlightBlock(e); });
            }
          } else {
            var cont = $('<div class="readme"><pre class="plainPre"><code>'+data.contents+'</code></pre></div>');
            cont.appendTo(obj);
            if (hljs) {
              $('code', cont).each(function(i, e) { hljs.highlightBlock(e); });
            }
          }
        } else {
          if (data.mimeType && data.mimeType.indexOf("image") == 0) {
            var img = $('<img/>', {
              class: 'blobImage',
              src: data.url,
              alt: path
            });
            $('<div/>', {
              class: "readme",
              html: img
            }).appendTo(obj);
          } else {
            var u = '<div class="readme"><a href="'+data.url+'">Download</a></div>';
            $(u).appendTo(obj);
          }
        }
      }
    });
  };

  var _directoryContents = function (repo, head, path, callback) {
    var params = {};
    params[rParam] = repo;
    params[hParam] = head;
    params[pParam] = path;

    _mask();
    $.getJSON(settings.actionUrl, params, function (data) {
      _unmask();
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

        var breadcrumbs = _getBreadcrumbsHtml(repo, path);
        var lastCommit = _getLastCommitHtml(data);
        var table = $('<table/>', {
          class: settings.tableClass,
          html:'<thead><tr><th style="width:16px;"></th>' +
            '<th>name</th><th>age</th><th>message</th></tr></thead>' +
            '<tbody>' + list.join('\n') + '</tbody>'
        });
        callback(data, [breadcrumbs, lastCommit, table]);
      } else {
        callback(data)
      }

    });
  };

  var _addDirectoryContents = function (repo, head, path) {
    _directoryContents(repo, head, path, function (data, table) {
      if (data.success) {
        obj.empty();
        table[0].appendTo(obj); //breadcrumbs
        if (table[1]) { //lastCommit
          $('<h5 class="commit-group-head">Last commit</h5>').appendTo(obj);
          table[1].appendTo(obj);
        }
        table[2].appendTo(obj); // tree
        if (data.readme) { // readme file
          $('<h4 class="readme-head">'+data.readmeFile+'</h4>\n').appendTo(obj);
          var readme = $('<div/>', {
            class: "readme",
            html: data.readme
          });
          readme.appendTo(obj);
          if (hljs) {
            $('code', readme).each(function(i, e) { hljs.highlightBlock(e); })
          }
        }
        //register click listener for tree entry
        $('a', table[2]).each(function(i, el) {
          $(el).bind('click', function(e) {
            obj.trigger('gitResourceClick', [data, i, el]);
            if (e.target.text == "..") {
              _addDirectoryContents(repo, head, data.parentPath);
            } else {
              var offset = data.parent? 1 : 0;
              var file = data.files[i - offset];
              var path = data.containerPath + file.name;
              if (file.container) {
                _addDirectoryContents(repo, head, path);
              } else {
                _addFileContents(repo, head, path, el);
              }
            }
            return false;
          });
        });
        $('a', table[0]).each(function(i, el) {
          $(el).bind('click', function(e) {
            var path = "/";
            $('a', table[0]).each(function(l, el) {
              if (l > 0 && l <= i) {
                var name = $(el).text();
                if (name) path += name +"/";
              }
            });
            settings.path = path;
            _addDirectoryContents(settings.repo, settings.ref, path);
          });
        });
      } else {
        obj.empty();
        $('<div class="alert alert-error">'+data.message+'</div>').appendTo(obj)
      }
    });
  };

  var settings = {};
  var methods = {
    init: function(options) {
      //events: 'publetResourceClick'  on resource click
      //            arguments: e - event object
      //                       data - the complete json server response
      //                       i    - the index in the files array
      //                       el   - the `a` element

      settings = $.extend({
        'actionUrl':"gitrview.json",
        'barElementId':"bar",
        'repo':getURLParameter(rParam) || "contentroot.git",
        'ref':getURLParameter(hParam) || "",
        'path':getURLParameter(pParam) || "",
        'tableClass'  : 'table table-condensed table-striped',
        'gravatarTheme' : 'identicon',
        'lastCommitUrlFunction': null
      }, options);

      _addDirectoryContents(settings.repo, settings.ref, settings.path);
    },

    load: function(path, ref) {
      if (path) {
        settings.path = path;
      }
      if (ref) {
        settings.ref = ref;
      }
      _addDirectoryContents(settings.repo, settings.ref, settings.path);
    }

  };
  $.fn.gitrRepoBrowser = function (method) {
    // From the "create your first jquery plugin" tutorial:
    // Method calling logic
    obj = this;
    if ( methods[method] ) {
      return methods[ method ].apply( this, Array.prototype.slice.call( arguments, 1 ));
    } else if ( typeof method === 'object' || ! method ) {
      return methods.init.apply( this, arguments );
    } else {
      $.error( 'Method ' +  method + ' does not exist on jQuery.gitrRepoBrowser' );
    }

  };
})(jQuery);