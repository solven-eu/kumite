# Compile

    mvn install

# Start the server

Start the server locally

    cd server
    mvn spring-boot:run -Dspring-boot.run.profiles=inject_default_games,fake_user

## Spring Profiles

### `-Dspring-boot.run.profiles=inject_default_games`

It will add a default set of games and contests.

### `-Dspring-boot.run.profiles=fake_user`

It will shortcut through security, making all calls being done with a fake user.

# Clean your style

    mvn com.diffplug.spotless:spotless-maven-plugin:apply
    mvn com.diffplug.spotless:spotless-maven-plugin:check

# Deploy to Heroku

    heroku login
    heroku apps:create kumite-dev --region eu
    heroku labs:enable "runtime-heroku-metrics" -a kumite-dev
    
    heroku open
    
## Run Heroku locally

https://devcenter.heroku.com/articles/heroku-local

    heroku local web
    heroku local web -f Procfile.local
    