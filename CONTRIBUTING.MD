# Compile

    mvn install

# Start the server

Fast-track (skipping the need to login with an external identity referential (like Github):

    cd server
    mvn spring-boot:run

## Start the server with external identity referential (like Github)

    cd server
    mvn spring-boot:run -Dspring-boot.run.profiles=unsafe
    
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
    heroku ps:scale web=1
    
    heroku open
    
## Monitor Heroku

    heroku logs --tail

## Run Heroku locally

https://devcenter.heroku.com/articles/heroku-local

    heroku local web
    heroku local web -f Procfile.local
    heroku local web --port 8080

## Run Server on Heroku

    heroku config:set SPRING_PROFILES_ACTIVE=heroku
    heroku config:set KUMITE_LOGIN_SIGNING_KEY='XYZ' (KumiteTokenService)

### OAuth2 Applications

#### Github for Heroku (PRD)

https://github.com/organizations/solven-eu/settings/applications/2707435

    heroku config
    heroku config:set KUMITE_LOGIN_OAUTH2_GITHUB_CLIENTID=Ov23linFq8dxGOOIL1WA
    heroku config:set KUMITE_LOGIN_OAUTH2_GITHUB_CLIENTSECRET=XYZ

## Run Player on Heroku

    heroku info -s | grep web_url | cut -d= -f2
    heroku config:set KUMITE_SERVER_BASE_URL=$(heroku info -s | grep web_url | cut -d= -f2)
