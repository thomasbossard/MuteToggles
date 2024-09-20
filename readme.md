This is a work in progress. Currently, I am experiencing an issue where. 

If you toggle to Silent (by using the OS) and then toggle through 

-> vibrate -> normal -> silent -> etc. 

it works fine. However as soon as you use the OS to toggle to Vibrate or Normal, toggling back to 
silent won't work anymore. 

Probably has to do with the OS and how notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL) is handled in specific instances. 