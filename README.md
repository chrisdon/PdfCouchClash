PdfCouchClash
=============

Demo of crash that occurs on a 64 bit device when project contains Couchbase Lite and AndroidPDF dependencies 

When the app is run any device other than a Nexus 9, it displays jumbled text (See https://github.com/chrisdon/CBLHtmlAttachScramble).

When the app runs on a Nexus 9 it crashes - https://gist.github.com/chrisdon/4ff156a40758e0db3da3

Comment out line 33 - compile project(':androidpdfreader') - of app/build.gradle, recompile project and the app will display text on the Nexus 9.