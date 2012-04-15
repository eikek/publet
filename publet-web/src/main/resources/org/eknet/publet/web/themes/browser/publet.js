$(document).ready(function() {

    function formSubmitHandler(eo) {
        var url = $(eo.target).parents("form").attr("action");
        if (!url) return false;

        var values = {};
        var errors = 0;
        $(eo.target).parents("form").find("[name]").each(function(i, el) {
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
        if (errors > 0) return false;

        $.post(url, values, function(data) {
            var target = $(eo.target).parents("form").find(".formSubmitResponse");
            if (target.length == 0) {
                target = $(eo.target).parents("form").parent().find(".formSubmitResponse");
                if (target.length == 0) {
                    target = $(eo.target).parents("form").parent().parent().find(".formSubmitResponse");
                }
            }
            target.html(data);
        });
        return false;
    }
    function addSubmitHandler() {
        $(".publetAjaxSubmit").click(formSubmitHandler);
    }

    $('div[p\\:ref]').each(function(index, el) {
        var jel = $(el);
        $.get(jel.attr("p:ref"), function(data) {
          jel.replaceWith("<div>"+ data +"</div>");
          addSubmitHandler();
        });
    });
    addSubmitHandler();

});

