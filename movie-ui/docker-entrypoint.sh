#!/bin/sh
envsubst '\
  $REACT_APP_BASE_URL \
  $REACT_APP_KEYCLOAK_BASE_URL \
  $REACT_APP_OMDB_API_KEY' \
  < /app/public/config.template.js \
  > /app/public/config.js

exec "$@"   # starts webpack