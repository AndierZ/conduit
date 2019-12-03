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
23. Make sure Morphia annotation works regarding index and validation
24. Use validation framework with annotations
25. Use Morphia service in handlers
26. CRUD articles
27. add favorite/unfavorite
28. CRUD comments
29. add follow/unfollow
30. add tags
31. add query routes

add annotation for middle ware methods
base handler to populate base fields (create time/update time and etc?)

better deal with event.fail(e), instead of just showing internal server error?

add custom jwt handler to expect "token" instead of "bearer"
add unit tests

security review
Dockerize each verticle
Run each docker independently in a cluster
Add load balancer in front of the http verticles
look into using protobuf as customized codec for service proxy