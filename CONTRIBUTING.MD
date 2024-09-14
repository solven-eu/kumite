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
    heroku ps:scale web=1
    
    heroku open
    
## Monitor Heroku

    heroku logs --tail

## Run Heroku locally

https://devcenter.heroku.com/articles/heroku-local

    heroku local web
    heroku local web -f Procfile.local
    heroku local web --port 8080

## Run Heroku

    heroku config:set SPRING_PROFILES_ACTIVE=heroku
    heroku config:set KUMITE_LOGIN_SIGNING_KEY='{"kty":"oct","kid":"9a115fc0-135d-47b3-b156-1fc1f83f25bd","k":"5xQN4l91m5QLQWKaXY0cuA21OBHy4LgCD7OhOotHEXs","alg":"HS256"}' (KumiteTokenService)

## OAuth2 Applications

### Github for Heroku (PRD)

https://github.com/organizations/solven-eu/settings/applications/2707435

    heroku config
    heroku config:set KUMITE_LOGIN_OAUTH2_GITHUB_CLIENTID=Ov23linFq8dxGOOIL1WA
    heroku config:set KUMITE_LOGIN_OAUTH2_GITHUB_CLIENTSECRET=XYZ


