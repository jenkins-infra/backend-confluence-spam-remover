Backend tool: Confluence Spam Remover
=====================================

From time to time we have mass spam attack on Jenkins Wiki, despite capture and use of
[Stop forum spam API](http://www.stopforumspam.com/).

This repository hosts tools we've developed to fight back.

* [Bulk removal GUI tool](BULK-DELETION.md)
* [Spambot](SPAMBOT.md)

Also see [this INFRA wiki page](https://wiki.jenkins-ci.org/display/infra/Ban+user+spamming+Wiki) for how to fight back.

TODO
====
* When deleting pages, delete all the other pages and comments created by this user and delete them.
* Similarly, delete this user from LDAP