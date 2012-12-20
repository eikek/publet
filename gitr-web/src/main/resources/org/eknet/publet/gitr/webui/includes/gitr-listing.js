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

$(function() {
  // if the quick search buttons are clicked,
  // trigger a search. have to do it _after_
  // the boostrap plugin set the active class
  $('.repoFilter').on('click', function(e) {
    setTimeout(function() {
      $('#repoNameSearch').trigger('click');
    }, 5);
  });

  //perform search
  $('#repoNameSearch').click(function() {
    var opts = {};
    $(".repoFilter").each(function(i, el) {
      if ($(el).hasClass("active")) {
        opts["filter"] = el.id;
      }
    });
    opts["name"] = $('#repoNameInput').val();

    var contentEl = $('#repoListing');
    var _mask = function() {
      contentEl.mask({
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
      contentEl.unmask();
    };

    _mask();
    $.getJSON("gitr-repolist.json", opts, function(data) {
      contentEl.empty();
      _unmask();
      $.each(data, function(i, val) {
        var pushIcon = val.push ? ' <i class="icon-pencil"></i>' : '';
        var name = '<td><div class="hover" data-original-title="'+val.name+'" data-content="'+val.description+'">' +
            '<a href="?r='+val.fullName+'">'+ val.name+ pushIcon+ '</a></div></td>';
        var icons = '<td>';
        if (val.tag == "closed") 
          icons += '<i class="icon-lock"/>';
        else
          icons += '<i class="icon-star"/>';
        
        if (val.owned) 
          icons += '<i class="icon-user"/>';
        else if (val.owner)
          icons += '<span class="label label-info">'+ val.owner+'</span>';
        
        icons += '</td>';
        $('<tr>'+icons+name+'<td><code class="clone">'+val.giturl+'</code></td></tr>').appendTo(contentEl);
      });
      $('.hover').popover();
    });
  });
  $('#repoNameSearch').trigger('click');
});