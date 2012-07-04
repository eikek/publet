#!/bin/bash
#
# Downloads the newest version of JQuery-File-Upload
# from github and replaces the files in this source
# tree.
#

# download the files into a tmp dir
rm -rf /tmp/fupupdate 2>&1 > /dev/null
mkdir /tmp/fupupdate || exit 1
cd /tmp/fupupdate

echo "Downloading plugin ..."
wget https://github.com/blueimp/jQuery-File-Upload/zipball/master -O fup.zip
unzip fup.zip

cd -

FILES_TO_UPDATE=(jquery.fileupload-ui.css \
jquery.postmessage-transport.js \
jquery.xdr-transport.js \
canvas-to-blob.min.js \
load-image.min.js \
tmpl.min.js \
jquery.fileupload.js \
jquery.fileupload-fp.js \
browser_templ.js \
main.js \
jquery.iframe-transport.js \
locale.js \
jquery.ui.widget.js \
jquery.fileupload-ui.js)

for f in "${FILES_TO_UPDATE[@]}"; do
  # find in src/main/resources
  REPL=`find ../main/resources -name "$f" -print`
  if test -z "$REPL"; then
    echo "Error: $f not found in our source tree."
  else
    # find in download
    NEW=`find /tmp/fupupdate -name "$f" -print`
    if test -z "$NEW"; then
      echo "Error: $f not included in update!"
    else
      echo "Replace $f ..."
      cp $NEW $REPL || echo "Failed to copy $f!"
    fi
  fi
done
