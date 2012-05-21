$(document).ready(function() {
    addRegisterEngineHandler();
    renderFileBrowser();
});

function getContentsAsUl(pathname, f) {
    $.getJSON("/publet/webeditor/scripts/toc.json", { path: pathname }, function(data) {
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
                lis.push('<li class="page"><a href="/publet/webeditor/scripts/edit.html?resource='+ file.href+'">'+ file.name+'</a></li>');
            }
        });
        $("#containerPath").html(data.containerPath);
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
    var resourcePath = getURLParameter("resource");
    getContentsAsUl(resourcePath, function(el) {
        el.appendTo("#filesTree");
    });
}

function addRegisterEngineHandler() {
    $("select.publetRegisterEngine").unbind("change")
    $("select.publetRegisterEngine").change(function(eo) {
        var engine = $("select.publetRegisterEngine").val();
        $.getJSON("/publet/webeditor/scripts/setengine.json", {
            path: window.location.pathname,
            publetEngine: engine
        });
    });
    return false;
}
