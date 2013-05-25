(function() {

  function findCode(pre) {
    for (var node = pre.firstChild; node; node = node.nextSibling) {
      if (node.nodeName == 'CODE' && node.getAttribute('class'))
        return node;
      if (!(node.nodeType == 3 && node.nodeValue.match(/\s+/)))
        break;
    }
  }

  function initHighlighting() {
    if (initHighlighting.called)
      return;
    initHighlighting.called = true;
    Array.prototype.map.call(document.getElementsByTagName('pre'), findCode).
        filter(Boolean).
        forEach(function(code){hljs.highlightBlock(code)});
  }

  window.addEventListener('DOMContentLoaded', initHighlighting, false);
  window.addEventListener('load', initHighlighting, false);
})();