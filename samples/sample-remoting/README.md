# sample-remoting

## No remoting

Open terminal in the root directory and run:

    ./gradlew :samples:sample-remoting:bootRun

### Own service
    
Run in another terminal:
    
    curl http://localhost:8081/echo/abc  

See `Echo: abc` output from `*.EchoServiceImpl` logger in the terminal.

### DataManager

Run in another terminal:
    
    curl -X POST http://localhost:8080/foo/create
    
    curl http://localhost:8080/foo/all  

See output from `OrmDataStore` and EclipseLink in the terminal.

## Client-server

Open first terminal in the root directory and run:

    ./gradlew :samples:sample-remoting:bootRun --args='--spring.profiles.active=remoting,server'     

Open second terminal in the root directory and run:

    ./gradlew :samples:sample-remoting:bootRun --args='--spring.profiles.active=remoting,client'     

### Own service executing on server

Run in another terminal (notice client's port 8081): 

    curl http://localhost:8081/echo/abc 

See `Echo: abc` output from `*.EchoServiceImpl` logger in the **first** terminal (server profile) only.

### DataManager executing on server

Run in another terminal (notice client's port 8081):
    
    curl -X POST http://localhost:8081/foo/create
    
    curl http://localhost:8081/foo/all  

See output from `OrmDataStore` and EclipseLink in the **first** terminal (server profile) only.
