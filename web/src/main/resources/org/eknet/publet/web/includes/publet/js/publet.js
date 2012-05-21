$(document).ready(function() {

    /**
     * Replace all `p:ref=...` elements with the
     * corresponding content.
     */
    $('div[p\\:ref]').each(function(index, el) {
        var jel = $(el);
        $.get(jel.attr("p:ref"), function(data) {
          jel.replaceWith("<div>"+ data +"</div>");
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