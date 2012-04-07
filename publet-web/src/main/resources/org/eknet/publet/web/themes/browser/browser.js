function renderFileBrowser() {
    var loc = window.location;
    var url = loc.protocol+ '//'+ loc.host + loc.pathname;
    $.getJSON(url, { a: "list" }, function(data) {
        var lis = [];
        $.each(data.files, function(i, file) {
            lis.push('<li><a href="'+ file.name +'?a=edit">'+ file.name+'</a></li>');
        });
        $('<ul/>', {
            html: lis.join('')
        }).appendTo("#filesTree");
    });
}