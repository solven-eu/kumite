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

### Default profiles

If you sure without any profile, the server should start successfully with an unsafe configuration. It will trigger:

- `unsafe_oauth2`: setup a static but open-sourced `privateKey`. This must not be used in production.
- `fakeuser`: activated a fake `accountId` with multiple `playerIds`
- `unsafe_external_oauth2`: enables the `Kumite-DEV` Github application, enabling real login flow. The `clientSecret` is open-sourced.

### `-Dspring-boot.run.profiles=inject_default_games`

It will add a default set of games and contests.

### `-Dspring-boot.run.profiles=fakeuser`

It will shortcut through security, making all calls being done with a fake user.

# Clean your style

    mvn spotless:apply
    mvn spotless:check

# Deploy to Heroku

    heroku login
    
## Initial deployment

    heroku apps:create kumite-dev --region eu
    heroku labs:enable "runtime-heroku-metrics" -a kumite-dev
    heroku ps:scale web=1

## Continuous Deployment

Heroku could deploy automatically, following pushed on Github default branch.

Heroku may also deploy on push over Heroku own repository: `git push heroku master`

## Monitor Heroku

    heroku open
    heroku logs --tail
    heroku logs --tail --dyno web
    heroku logs --tail --dyno player

## Run Heroku locally

https://devcenter.heroku.com/articles/heroku-local

    heroku local web
    heroku local web -f Procfile.local
    heroku local web --port 8080

## Run Server on Heroku

    heroku config:set SPRING_PROFILES_ACTIVE=heroku,redis
    heroku config:set KUMITE_OAUTH2_SIGNING_KEY='XYZ' (KumiteTokenService)
    heroku config:set KUMITE_OAUTH2_ISSUER_BASE_URL=$(heroku info -s | grep web_url | cut -d= -f2)

## Run Player on Heroku

If both `contest-server` and `player` on the same Heroku App, `kumite.server.base-url` can be setup with:

    heroku config:set KUMITE_PLAYER_CONTEST_BASE_URL=$(heroku info -s | grep web_url | cut -d= -f2)
    heroku config:set KUMITE_PLAYER_REFRESH_TOKEN='XYZ' `<kumite.player.contest-base-url>/html/me`

### OAuth2 Applications

#### Github for Heroku (PRD)

https://github.com/organizations/solven-eu/settings/applications/2707435

    heroku config
    heroku config:set KUMITE_LOGIN_OAUTH2_GITHUB_CLIENTID=Ov23linFq8dxGOOIL1WA
    heroku config:set KUMITE_LOGIN_OAUTH2_GITHUB_CLIENTSECRET=XYZ

