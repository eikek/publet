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
  $('#mine').on('click', function(e) {
    setTimeout(function() {
      $('#repoNameSearch').trigger('click');
    }, 5);
  });
  $('#closed').on('click', function(e) {
    setTimeout(function() {
      $('#repoNameSearch').trigger('click');
    }, 5);
  });
  $('#all').on('click', function(e) {
    setTimeout(function() {
      $('#repoNameSearch').trigger('click');
    }, 5);
  });

  //perform search
  $('#repoNameSearch').click(function() {
    $('#repoListing').empty();
    var opts = {};
    if ($('#mine').hasClass('active')) opts["mine"] = true;
    if ($('#closed').hasClass('active')) opts["closed"] = true;
    if ($('#all').hasClass('active')) opts["all"] = true;
    opts["name"] = $('#repoNameInput').val();
    
    $.getJSON("gitr-repolist.json", opts, function(data) {
      $.each(data, function(i, val) {
        var name = '<td class="hover" data-original-title="'+val.name+'" data-content="'+val.description+'"><a href="?r='+val.fullName+'">'+ val.name+'</a></td>';
        var icons = '<td>';
        if (val.tag == "closed") 
          icons += '<i class="icon-lock"/>';
        else
          icons += '<i class="icon-star"/>';
        
        if (val.owned) 
          icons += '<i class="icon-user"/>';
        else
          icons += '<span class="label label-info">'+ val.owner+'</span>';
        
        icons += '</td>';
        $('<tr>'+icons+name+'<td><code class="clone">'+val.giturl+'</code></td></tr>').appendTo($('#repoListing'));
      });
        $('.hover').popover();
    });
  });
  $('#repoNameSearch').trigger('click');
});