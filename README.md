# conduit

# steps towards a fully production ready distributed system

1. Setup workspace using vertx gradle plugin
2. Setup first http verticle with hello world
3. Build a user friendly logger
4. Setup config file in resources
5. Setup first route with a simple handler for post request - register new user
6. Setup second route with a simple handler for get request - get user
7. Setup jwt auth handler for protected routes
8. Setup a user service to process the requests
9. Setup bcrypt using the spring library for password hash
10. Connect the handler and the service using event bus
11. Setup service proxy for the user service instead of using event bus directly
12. Create a UserServiceVerticle to publish the service
13. Move all user handlers into its own class
14. Create annotation for configuring routes
15. Add annotation process to reigster handlers to routes
16. CRUD for user with authentication
17. Rxify the MongoDb service and User service.
18. Fix findOneAndUpdate and use that for update routes. Send JsonObject from handler to service, and object the other way around
19. Fix issue where handler is not sending responses back 
20. Add unit test for mongodb service
21. Use Morphia in mongodb service
22. Create Morphia service

Make sure Morphia annotation works regarding index and validation
Use Morphia service in handlers

CRUD articles
CRUD comments
add follow/unfollow
add favorite/unfavorite
add tags
add query routes
better deal with event.fail(e), instead of just showing internal server error?

security review
Create docker for each separate each verticle as its own process/service
Run each docker/service independently on a cluster
Add load balancer in front of the http verticles
look into using protobuf as customized codec for service proxy