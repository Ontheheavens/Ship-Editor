
   Version 0.7.4
   By Ontheheavens


 - Installation Notes:

1. Ship Editor requires Java Runtime 21 to be present. Folder with said runtime should be named "jre", and should be located in the root directory of the application. Naming mismatch will cause startup to fail.

2. Java Runtime is available at https://jdk.java.net/21/. You'll need a matching one for your operating system - rename it to "jre" and put into editor main folder.

3. Should there be any crash errors, "log" folder is bound to have a file with stack trace lines. Note: file loading failures that do not have a modal popup and appear exclusively in log lines are to be expected; it is the result of inputs non-conforming to spec JSON layouts.