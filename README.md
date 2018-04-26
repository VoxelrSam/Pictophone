# [Pictophone](http://pictophone-env.rrwwgr9pf7.us-east-2.elasticbeanstalk.com/)
This is a web app made for CS252. The project is a fun game involving both drawing and guessing.

[http://pictophone-env.rrwwgr9pf7.us-east-2.elasticbeanstalk.com/](http://pictophone-env.rrwwgr9pf7.us-east-2.elasticbeanstalk.com/)

### How to Play
This game is much like "Telephone" where you whisper a message along a group of friends and see how the message has transformed in the end. The catch with this, though, is that instead of whispering, users must draw the message to the best of their abilities. 

In essence, there are 3 main stages to the game:

1. The first user writes a prompt that will be the initial "message."
2. The next user then tries to draw the message given to them to the best of their abilities.
3. The user after that then attempts to guess what the message was based off of the drawing the last user came up with.

The cycle then repeats through stages 2 and 3 until all of the users have contributed something to the game. In the end, all of the users are then shown the timeline of how the events unfolded. Most of the time, the message will transform into something quite different than what it started out as!

### Technology
For the front end, the usual culprits of HTML, CSS, and JavaScript are utilized alongside [JQuery](https://jquery.com/). [Bootstrap](https://getbootstrap.com/) handles much of the base layer styling, [Animate.css](https://daneden.github.io/animate.css/) is used for all of the animation work, [jscolor](http://jscolor.com/) is used for color picking, and [Font Awesome](https://fontawesome.com/) is used for all the various icons.

This project makes use of [Apache Tomcat](https://tomcat.apache.org/) for the backend, so the server is written completely in Java. As far as communication goes between the client and server, JSON is sent via WebSockets that is then parsed and handled appropriately. Jars included are [org.json](https://github.com/stleary/JSON-java) and the [MySQL Connector/J driver](https://dev.mysql.com/downloads/connector/j/5.1.html).

As for hosting, the project currently uses [AWS Elastic Beanstalk](https://aws.amazon.com/elasticbeanstalk/), and the MySQL server is an [AWS RDS](https://aws.amazon.com/rds/) instance.

*If you would like a full list of sources, they should all be listed out in [sources.txt](sources.txt)*

---

*\~\~Made with love and frustration by [Samuel Ingram](samingram.me)\~\~*
