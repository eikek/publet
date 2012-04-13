$(document).ready(function() {
    $('div[p\\:ref]').each(function(index, el) {
        var jel = $(el);
        $.get(jel.attr("p:ref"), function(data) {
          jel.replaceWith("<div>"+ data +"</div>");
        });
    });
});