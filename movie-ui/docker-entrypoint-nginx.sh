#!/bin/sh
envsubst '\
  $REACT_APP_BASE_URL \
  $REACT_APP_KEYCLOAK_BASE_URL \
  $REACT_APP_OMDB_API_KEY' \
  < /usr/share/nginx/html/config.template.js \
  > /usr/share/nginx/html/config.js

exec "$@"   # starts nginx
