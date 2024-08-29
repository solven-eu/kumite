# Compile

    mvn install

# Start the server

Start the server locally

    cd server
    mvn spring-boot:run -Dspring-boot.run.profiles=inject_default_games,fake_user