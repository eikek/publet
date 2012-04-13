$(document).ready(renderFileBrowser());

function getContentsAsUl(pathname, f) {
    var loc = window.location;
    var url = loc.protocol+ '//'+ loc.host + pathname;
    $.getJSON(url, { a: "list" }, function(data) {
        var lis = [];
        if (data.parent) {
            var handler = "replaceContents('"+ data.parent+ "');";
            lis.push('<li class="backLink"><a href="#" onclick="'+handler+'">..</a></li>');
        }
        $.each(data.files, function(i, file) {
            if (file.container) {
                var handler = "replaceContents('"+file.href+"');";
                lis.push('<li class="folder"><a href="#" onClick="'+ handler +'">'+ file.name+'</a></li>');
            } else {
                lis.push('<li class="page"><a href="'+ file.href +'?a=edit">'+ file.name+'</a></li>');
            }
        });
        $("#containerPath").html(data.containerPath)
        var el = $('<ul/>', {
            html: lis.join('\n')
        });
        f(el);
    });
}

function replaceContents(pathname) {
    getContentsAsUl(pathname, function(el) {
        var ndiv = $('<div/>', {
            id: "filesTree",
            html: el
        });
        $("#filesTree").replaceWith(ndiv);
    });
}

function renderFileBrowser() {
    getContentsAsUl(window.location.pathname, function(el) {
        el.appendTo("#filesTree");
    });
}
