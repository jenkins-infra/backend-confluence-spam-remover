For deleting spam pages on on-going basis, Jenkins project has a mail based workflow.

* People interested in participating to this effort joins [spambot ML](https://groups.google.com/forum/#!forum/jenkinsci-spambot),
  which receives email notifications from Confluence.

* Spambot is monitoring email to this list, and if it sees page additions that are spams, it'll delete the page
  automatically. (TODO: currently the bot is on trial and falls short of actually deleting the page.)

* Human can reply to the notification and have the magic word "KILL SPAM" in the body, all on one line.
  Spambot will respond to this and deletes the page.