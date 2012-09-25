#!/bin/bash
#
# Downloads the newest version of spin.js and jquery.loadmask.plugin
# from github and replaces the files in this source tree.
#

# download the files into a tmp dir
rm -rf /tmp/webupdate 2>&1 > /dev/null
mkdir /tmp/webupdate || exit 1
cd /tmp/webupdate

echo "Downloading plugin ..."
#wget https://github.com/blueimp/jQuery-File-Upload/zipball/master -O fup.zip
#unzip fup.zip
wget https://raw.github.com/fgnass/spin.js/gh-pages/dist/spin.min.js
wget https://raw.github.com/iloveitaly/jquery.loadmask.spin/master/jquery.loadmask.spin.css
wget https://raw.github.com/iloveitaly/jquery.loadmask.spin/master/jquery.loadmask.spin.js
cd -

FILES_TO_UPDATE=(spin.min.js \
jquery.loadmask.spin.css \
jquery.loadmask.spin.js)

for f in "${FILES_TO_UPDATE[@]}"; do
  # find in src/main/resources
  REPL=`find ../main/resources -name "$f" -print`
  if test -z "$REPL"; then
    echo "Error: $f not found in our source tree."
  else
    # find in download
    NEW=`find /tmp/webupdate -name "$f" -print`
    if test -z "$NEW"; then
      echo "Error: $f not included in update!"
    else
      echo "Replace $f ..."
      cp $NEW $REPL || echo "Failed to copy $f!"
    fi
  fi
done
