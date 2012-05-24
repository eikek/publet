$(function() {

    /**
     * Replace all `p:ref=...` elements with the
     * corresponding content.
     * Implements client-side includes
     */
    $('[p\\:ref]').each(function(index, el) {
        var jel = $(el);
        var uri = jel.attr("p:ref");
        if (uri.indexOf("?") > 0) {
            uri = uri +"&noLayout"
        } else {
            uri = uri +"?noLayout"
        }
        $.get(uri, function(data) {
            jel.empty();
            jel.removeAttr("p:ref");
            $(data).appendTo(jel);
        });
    });
});

//found here: http://stackoverflow.com/questions/1403888/get-url-parameter-with-jquery#answer-8764051
function getURLParameter(name) {
    return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.search)||[,""])[1].replace(/\+/g, '%20'))||null;
}

//found on github: https://github.com/douglascrockford/JSON-js/blob/master/json2.js
function isJson(str) {
    return /^[\],:{}\s]*$/.test(str.replace(/\\["\\\/bfnrtu]/g, '@').
        replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g, ']').
        replace(/(?:^|:|,)(?:\s*\[)+/g, ''));
}