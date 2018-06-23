# Twitter Clone
A lightweight, full-stack clone of Twitter with a Java (Jersey) REST API, Angular v4 front-end and SQLite3 database.
[Visit the Twitter Clone front-end](https://github.com/gerryfletch/twitter-clone-front) to see some screenshots and
more detail on the architecture.

## Motivation
I built this clone to get familiar with the tech stack used on the team I joined at [Fidesa](https://www.fidessa.com/)
for an internship. You can contact me on [LinkedIn](http://linkedin.com/in/gerryfletcher) or contact me on Discord: Gerreh#0954.

## Authentication
I stored user passwords with [Spring BCrypt](https://docs.spring.io/spring-security/site/docs/4.2.5.RELEASE/apidocs/org/springframework/security/crypto/bcrypt/BCrypt.html), and used [Json Web Tokens](https://jwt.io/) to represent claims between the client and server.
To learn more about implementing the refresh token OAuth specification, visit [my Angular-Jersey auth repository](https://github.com/gerryfletch/angular-jersey-auth).

## Application Architecture
I modeled my application according to the <b>Repository Pattern</b>. Simply, the end points hold no business logic
and talk to services to get the data they need. The services then use Data Access Objects (DAOs) to communicate with
the SQLite database. As this was my first project, you may find that while my end points hold true to this
architecture, they are certainly messy. Take a look at my [authentication REST api](https://github.com/gerryfletch/angular-jersey-auth/tree/master/rest-api)
for a better example.

## Exception Handling
Database or other data modelling exceptions are handled in the services layer, and re-thrown as user friendly
exceptions. For example, an SQLException may be caught in the <i>Relationship Service</i>, and a UserNotFound exception
will be thrown as a result. Something I didn't get around to applying was exception mapping, which is
[pretty cool.](https://github.com/gerryfletch/angular-jersey-auth/blob/master/rest-api/src/main/java/me/gerryfletcher/restapi/mappers/InvalidLoginExceptionMapper.java)

## Modelling Data
I used the [GSON library](https://github.com/google/gson) to convert my json requests to Java Objects and back. This
allowed me to create models for my data (for example, a user) and <i>magically</i> create the object.

## Help
I can't recommend building a project like this for yourself enough. If you're interested in recreating Twitter,
the [Twitter developer site](https://developer.twitter.com) has lots of resources on how their responses are modelled.
I found this quite late, and it certainly would have helped me to make my application cleaner had I found it earlier on.

You can also get in contact with me via my contact details at the top.