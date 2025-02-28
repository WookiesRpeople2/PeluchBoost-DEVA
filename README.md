Peluch Boost

tech stack:
backend:
Spring
ORM(made by me)
JSONParser(made by me)
Threading lib(made by me)

# RUN
in order to run this app follow these steps:

## IMPORTANT
make sure to run spring_api before spring_frontend


please  make sure that you have added the .env file see below
the backend has a test on the controller


backend:
```
cd spring_api
docker_compose --build
```

fronend:
```
.\mvnw.cmd clean package javafx:run -DskipTests
```


# .env
Please make sure to copy the values form the .env.example and paste them in a new file .env

here is the .env.example:
DATABASE_URL=jdbc:mysql://mysql:3306/peluchBoost?allowPublicKeyRetrieval=true
DATABASE_USER=root
DATABASE_PASSWORD=password





