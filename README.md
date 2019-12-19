# Getting started

- Clone the repo
- Recommend Intellij as the IDE
- Allow gradle to finish downloading all dependencies
- Gradle tasks -> application -> vertxRun to run the application

# Application structure

- Entity class: defines the data object and the translation from and to json
- Handler class: defines routes and forward arguments to the respective service for processing
- Service class: performs CRUD operations and the database service talks to the database
- Service Verticles: instantiates a service
- Http Verticle: sets up all the routes
- App Verticle: main verticle that creates all the other verticles
* We don't have to use the App Verticle as the entry point of the application. For example, each verticle can be launched in a separate VM.

# Development steps

- Setup workspace using vertx gradle plugin
- Setup first http verticle with hello world
- Create a convenient logger
- Setup config file in resources
- Setup first route with a simple handler for post request - register new user
- Setup second route with a simple handler for get request - get user
- Setup jwt auth handler for protected routes
- Setup a user service to process the requests
- Use bcrypt for password hash
- Connect the handler and the service using event bus Use service proxy instead of working with event bus directly
- Create a UserServiceVerticle to publish the service
- Move all user handlers into its own class
- Create annotation for configuring routes
- Add annotation process to register handlers for routes
- CRUD for user with authentication
- Rxify the MongoDb service and User service.
- Fix findOneAndUpdate and use that for update routes. Send JsonObject from handler to service, and object the other way around
- Fix issue where handler is not sending responses back
- Add unit test for mongodb service
- Add Morphia
- Create Morphia service
- Make sure Morphia annotation works regarding index and validation
- Use validation framework with annotations
- Use Morphia service in handlers
- CRUD articles
- add favorite/unfavorite
- CRUD comments
- add follow/unfollow
- add tags
- add query routes
- add annotation for middle ware methods
- add user unit tests
- add article unit tests
- add comment unit tests
- add query unit tests
- Put all middleware methods in a shared base class
- populate base fields
- cleanup string literals
- add custom jwt handler to expect "Token" instead of "Bearer"

# Afterthoughts

- Didn't like how ObjectId works and the fact that it can be null
- Certain join-like queries using morphia and mongodb is a little painful
- Still can't avoid relational data model
- Still have to deal with a lot of mongodb operators and queries
- rxJava2 is a little rough to debug, and is probably an overkill.
- CompletableFuture should suffice since we are only dealing with rx Singles, not streams. And we don't need to deal with threading either, a major benefit of rxJava2
- Limitations of Vert.x
   - limited types of arguments that can be passed into a service
   - lack of flexibility regarding message codec for event bus messages
   - lack of ability to customize transport layer (multicast, back pressure and etc)
   - lack of support of CompletableFuture within the service framework (Can't auto generate service proxy that uses CompletableFuture)

# Credits (Inspired by)
- greyseal's work at https://github.com/greyseal/vertx-realworld-example-app
- skanjo's work at https://github.com/skanjo/realworld-vertx