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

(function( $ ) {
  
  $.fn.gitrRepoBrowser = function(options) {
    
    //events: 'publetResourceClick'  on resource click
    //            arguments: e - event object
    //                       data - the complete json server response
    //                       i    - the index in the files array
    //                       el   - the `a` element
    
    var obj = this;
    var settings = $.extend( {
      'actionUrl'     : "gitrview.json",
      'barElementId'  : "bar",
      'repo'          : getURLParameter('repo') || "contentroot.git",
      'ref'           : getURLParameter('ref') || "",
      'path'          : getURLParameter('path') || ""
    }, options);
  
    var _contents = function(repo, callback) {
      $.getJSON(settings.actionUrl, { repo: repo, ref: settings.ref, path: settings.path }, function (data) {
        if (data.success) {
          var list = [];
          if (data.parent) {
            list.push('<tr><td/><td><a href="?repo='+repo+'&ref='+settings.ref+'&path='+data.parentPath+'">..</a></td><td/><td/><tr>');
          }
          
          //normal resource list, already ordered
          $.each(data.files, function(i, file) {
            var css = file.icon;
            var li = '<tr>';
            var path = data.containerPath? data.containerPath +"/"+ file.name : file.name
            li += '<td><i class="'+ css +'"></i></td><td><a href="?repo='+repo+'&ref='+settings.ref+'&path='+path+'">'+ file.name +'</a></td>';
            li += '<td>' + file.age + '</td>';
            li += '<td>' + file.message + ' <span class="label">'+file.author+'</span></td>';
            list.push(li);
          });
          
          var el = $('<table/>', {
            class: "table table-condensed table-striped",
            html: '<thead><tr><th style="width:16px;"></th><th class="span6">name</th><th>age</th><th>message</th></tr></thead><tbody>'+list.join('\n')+'</tbody>'
          });
          callback(data, el);
        } else {
          callback(data)
        }
        
      });
    };
  
    var addContents = function(repo) {
      _contents(repo, function(data, list) {
        list.appendTo(obj);
      });
    };
  
    return this.each(function() {
      addContents(settings.repo);
    });
  };
})( jQuery );