/**
 * Resource browser jQuery plugin
 */

(function( $ ) {

    $.fn.publetBrowser = function(options) {

        //events: 'publetResourceClick'  on resource click
        //            arguments: e - event object
        //                       data - the complete json server response
        //                       i    - the index in the files array
        //                       el   - the `a` element

        var obj = this;
        var settings = $.extend( {
            'actionUrl'     : "/publet/webeditor/scripts/toc.json",
            'resourcePath'  : getURLParameter("resource") || "/",
            'resourceListClass' : null,
            'locationBarClass' : 'locationBar',
            'locationBarEl'    : 'div',
            'locationBarId'    : null,
            'parentLiClass'    : 'backLink',
            'folderClass'      : 'folder',
            'fileClass'        : 'page'
        }, options);

        var createHandler = function(pathname, file) {
            return function() {
                alert('click!!!');
            }
        };

        var createLocationBar = function(path) {
            return $('<'+settings.locationBarEl+'/>', {
                id: settings.locationBarId,
                class: settings.locationBarClass,
                html: path
            })
        };

        var _contents = function(pathname, callback) {
            $.getJSON(settings.actionUrl, { path: pathname }, function (data) {
                var list = [];
                if (data.parent) {
                    var li;
                    if (settings.parentLiClass) {
                        li = '<li class="'+settings.parentLiClass+'"><a href="#">..</a></li>';
                    } else {
                        li = '<li><a href="#">..</a></li>';
                    }
                    list.push(li);
                }
                //normal resource list, already ordered
                $.each(data.files, function(i, file) {
                    var css;
                    if (file.container) {
                        css = settings.folderClass;
                    } else {
                        css = settings.fileClass;
                    }
                    var li;
                    if (css) {
                        li = '<li class="'+ css +'"><a href="#">'+ file.name +'</a></li>';
                    } else {
                        li = '<li><a href="#">'+ file.name +'</a></li>';
                    }
                    list.push(li);
                });

                var el = $('<ul/>', {
                    class: settings.resourceListClass,
                    html: list.join('\n')
                });
                callback(el, data);
            });
        };

        var addContents = function(pathname) {
            _contents(pathname, function(list, data) {
                var bar = createLocationBar(data.containerPath);
                bar.appendTo(obj);
                list.appendTo(obj);
                $("a", list).each(function(i, el) {
                    $(el).bind('click', function(e) {
                        obj.trigger('publetResourceClick', [data, i, el]);
                        if (e.target.text == "..") {
                            obj.empty();
                            addContents(data.parent);
                        } else {
                            var offset = data.parent? 1 : 0;
                            var file = data.files[i - offset];
                            if (file.container) {
                                obj.empty();
                                addContents(file.sourceRef);
                            }
                        }
                    });
                })
            });
        };

        return this.each(function() {
            addContents(settings.resourcePath);
        });
    };
})( jQuery );