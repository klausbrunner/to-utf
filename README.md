To-UTF is a tiny Java Swing application to convert all files with specific filename extensions within a certain directory structure to UTF-8 encoding. Specifically, I use it to convert the source code of entire Java projects from ISO-8859-1 or Windows-1252 to UTF-8.

This is a simple point-and-click GUI application. If you’re looking for something scriptable, more configurable, and generally much richer in functionality, recode or iconv are good choices.

Note that To-UTF does not understand or change content-specific explicit encoding information, such as the XML declaration’s encoding attribute. It does understand BOMs, though.

This project has not been updated since circa 2008. It's kept here for archival purposes.
