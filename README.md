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
32. add annotation for middle ware methods
33. Done - User unit tests
34. Done - Article unit tests
35. Comment unit tests
36. Query unit tests
37. Put all middleware in a shared base class

cleanup string constants
add custom jwt handler to expect "token" instead of "bearer"
populate base fields (in the GUI or at the very end, i.e. db side?)

What I don't like about Vert.x + rxJava2 + Morphia

next steps:
    New project using kotlin
    Use long for entity id
    Use postgres service to replace mongodb
    security review
    Dockerize each verticle and deploy to AWS
    look into using protobuf as customized codec for service proxy