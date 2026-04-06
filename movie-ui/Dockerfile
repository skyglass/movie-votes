FROM node:19.6.0-alpine
WORKDIR /app
ENV PATH /app/node_modules/.bin:$PATH

# install envsubst
RUN apk add --no-cache gettext

COPY package.json .
COPY package-lock.json .
RUN npm ci

# copy your app, including public/config.template.js
COPY . .

# entrypoint will generate public/config.js before "npm start"
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

EXPOSE 3000
ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["npm", "start"]
