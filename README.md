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

CRUD for user with authentication
CRUD articles
CRUD comments using RX2?
add follow/unfollow
add favorite/unfavorite
add tags
add query routes

security review

- Create docker for each separate each verticle as its own process/service
- Run each docker/service independently on a cluster
- Add load balancer in front of the http verticles
