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

/**
 * Submits the form with id `formId` via ajax post.
 *
 * if the result is json, it is checked for attributes
 * `message` and optional `level`. If they are defined,
 * a notification is triggered.
 *
 * in other cases, the html of the element with id
 * `responseId` is replaced with the result.
 *
 * @param formId
 * @param responseId
 * @return {Boolean}
 */
function formAjaxSubmit(formId, responseId) {
    if (!formId) {
        notifyError('Error! No form id specified on onclickhandler!');
        return false;
    }

    var form = $("form#"+formId);
    if (!form || form.length == 0) {
        notifyError('Error! No form with id "'+formId+'" found!');
        return false;
    }

    var url = form.attr('action');
    if (!url) {
        notifyError('Error! No form action defined in form: ' + formId);
        return false;
    }

    var values = {};
    var errors = 0;
    form.find("[name]").each(function(i, el) {
        var e = $(el);
        if (e.attr("required")) {
            if (e.val() == "" && errors == 0) {
                e.css("ym-error");
                e.focus();
                errors++;
            }
        }
        values[e.attr("name")] =  e.val();
    });
    if (errors > 0) {
        notifyError('Form not filled correctly.');
        return false;
    }

    var options = {
        success: function(data) {
            if (isJson(data)) {
                //if it is in standard message format, display the message
                var msg = data["message"];
                var lvl = data["level"];
                if (!lvl && data["success"]!=undefined) {
                    if (data["success"]) lvl = "success";
                    if (!data["success"]) lvl = "error";
                }
                if (msg) {
                    notify(msg, lvl);
                }
            }
            //otherwise replace response element
            if (responseId) {
                var responseEl = $('#'+ responseId);
                if (!responseEl) {
                    $.sticky('Response element with id "'+ responseId+'" not found.', {level:'error'});
                } else {
                    responseEl.html(data);
                }
            }
        }
    };
    form.ajaxSubmit(options);
    return false;
}

//found on github: https://github.com/douglascrockford/JSON-js/blob/master/json2.js
function isJson(str) {
    return /^[\],:{}\s]*$/.test(str.replace(/\\["\\\/bfnrtu]/g, '@').
        replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g, ']').
        replace(/(?:^|:|,)(?:\s*\[)+/g, ''));
}