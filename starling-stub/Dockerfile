FROM node:10.7.0-alpine
COPY ./package.json .
COPY ./package-lock.json .
RUN npm install
COPY ./db.json .
