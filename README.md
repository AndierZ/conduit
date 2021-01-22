# Getting started

- Clone the repo
- Make sure you have Java1.8+ JDK installed
- Open the project in your IDE and allow gradle to finish downloading all dependencies
- A trick for Intellij, If you don't see your gradle menue refresh with new tasks and dependencies after it's done processing, try restarting your IDE
- Install Mongodb community version and launch it in the background
- Install Robo 3T if you want to manually examine the values in the database
- Compile and try running all the unit tests under `/vertx-examples/conduit/src/test/java/`
- Run mongodb in terminal `mongod --config /usr/local/etc/mongod.conf --fork`
- `Gradle tasks -> application -> vertxRun` to run the application
- Run the test script from here `https://github.com/gothinkster/realworld/tree/master/api`

# Application structure

- Entity class: defines the data object and the translation from and to json
- Handler class: defines routes and invoke the appropriate service(s) for processing
- Service class: performs entity specific CRUD operations by invoking the database service
- Database Service class: performs generic database operations using Json as the media
- Service Verticles: instantiates a service
- Http Verticle: sets up all the routes
- App Verticle: main verticle that creates all the other verticles and entry point of the application

# Gradle tasks
- `application >- vertxRun` runs the application. It supports a feature similar to nodmon in Node.js, meaning you can make code changes and it'll recompile and relaunch the app on the fly
- `application -> vertxDebug` launches the application in remote debuging mode. You can then start up a remote debugging session on port 5005 to tap into the application
- `build -> generate` creates all the auto-generated classes. This is run as part of the build step and you shoulnd't have to run it manually
- `shadow -> shadowJar` creates a fat jar file
- more info can be found here https://github.com/jponge/vertx-gradle-plugin

# Development steps

- Setup workspace using vertx gradle plugin
- Setup first http verticle with hello world
- Setup config file in resources
- Setup first route with a simple handler for post request - register new user
- Setup second route with a simple handler for get request - get user
- Setup jwt auth handler for protected routes
- Setup a user service to process the requests
- Use bcrypt for password hash
- Connect the handler and the service using event bus
- Use service proxy instead of working with event bus directly
- Create a UserServiceVerticle to publish the service
- Move all user handlers into its own class
- Create annotation for configuring routes
- CRUD for user with authentication
- Rxify the MongoDb service and User service.
- Add unit test for mongodb service
- Create Morphia service to replace MorgonDb service
- Make sure Morphia annotation works regarding index and validation
- Use Morphia service in handlers
- CRUD articles
- add favorite/unfavorite
- CRUD comments
- add follow/unfollow
- add tags
- add query routes
- add annotation for middleware methods for the sake of clarity
- add user unit tests
- add article unit tests
- add comment unit tests
- add query unit tests
- Put all middleware methods in a shared base class
- populate base fields
- cleanup string literals
- add custom jwt handler to expect "Token" instead of "Bearer"
- Plow through the Postman test script from the realworld project page and make everything pass.

# Afterthoughts

- Wasn't a fan of how ObjectId works and the fact that it can be null
- Certain join-like queries using morphia and mongodb is quite painful
- Still can't avoid relational data model
- rxJava2 is a little rough to debug, and is probably an overkill.
- CompletableFuture should suffice since we are only dealing with rx Singles, not streams. And we don't need to deal with threading because of Vert.x, throwing away a major benefit of rxJava2
- Limitations of Vert.x
   - lack of flexibility regarding message codec for event bus messages (and hence service proxy)
   - lack of ability to customize transport layer (multicast, retry, back pressure and etc)
   - lack of support of CompletableFuture within the service proxy framework (Can't auto generate service proxy that uses CompletableFuture)
p.s. Any insight/comment/correction is welcome

# Credits (Inspired by)
- greyseal's work at https://github.com/greyseal/vertx-realworld-example-app
- skanjo's work at https://github.com/skanjo/realworld-vertx
