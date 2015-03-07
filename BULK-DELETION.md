Backend tool: Confluence Spam Remover
=====================================

From time to time we have mass spam attack on Jenkins Wiki, despite capture and use of
[Stop forum spam API](http://www.stopforumspam.com/).

This little GUI tool helps admins fight back by bulk-delete pages. Since every attack
is somewhat different, it is almost always necessary to modify the tool to detect
the current attack wave before you use this tool.

This tool works in the following sequence:

1. Launch the tool
1. Click "Scan" button to have it scan the JENKINS space and score each page by the likeliness of spam
   (the higher the score, more like it is a spam.) Depending on the detection mode, this is a time consuming
   process. Let it run.
1. GUI will show all the scanned pages with their spam score.
   Check/uncheck boxes manually so that only the actual spam pages are checked.
1. Click "Clean" button to delete all the checked pages.
   (Pages go to the "trash" area, so an incorrectly deleted page can still be resurrected.)

Also see [this INFRA wiki page](https://wiki.jenkins-ci.org/display/infra/Ban+user+spamming+Wiki) for how to fight back.

TODO
====
* When deleting pages, delete all the other pages and comments created by this user and delete them.
* Similarly, delete this user from LDAP