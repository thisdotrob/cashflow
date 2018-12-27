FROM ubuntu:18.10

ARG DEBIAN_FRONTEND=noninteractive

RUN apt update
RUN apt upgrade -y

RUN apt install -y openjdk-8-jdk locales git curl gnupg2

RUN update-java-alternatives --set /usr/lib/jvm/java-1.8.0-openjdk-amd64

RUN curl -sL https://deb.nodesource.com/setup_11.x | bash -
RUN apt-get update && \
    apt-get install -y build-essential nodejs && \
    apt-get clean

RUN sed -i -e 's/# en_GB.UTF-8 UTF-8/en_GB.UTF-8 UTF-8/' /etc/locale.gen && \
    locale-gen
ENV LANG en_GB.UTF-8
ENV LANGUAGE en_GB:en
ENV LC_ALL en_GB.UTF-8

ARG UID
ARG GID

USER root

RUN if [ -z "`getent group $GID`" ]; then \
      addgroup --system --gid $GID dockergroup; \
    else \
      groupmod -n dockergroup `getent group $GID | cut -d: -f1`; \
    fi && \
    if [ -z "`getent passwd $UID`" ]; then \
      adduser --system --uid $UID --gid $GID --shell /bin/sh dockeruser; \
    else \
      usermod -l dockeruser -g $GID -d /home/dockeruser -m `getent passwd $UID | cut -d: -f1`; \
    fi

WORKDIR /home/dockeruser/app

RUN chown dockeruser:dockergroup /home/dockeruser/app

RUN mkdir /home/dockeruser/.m2 && \
    mkdir /home/dockeruser/app/public && \
    mkdir /home/dockeruser/app/user_data && \
    mkdir /home/dockeruser/app/amex_data && \
    chown dockeruser:dockergroup /home/dockeruser/.m2 && \
    chown dockeruser:dockergroup /home/dockeruser/app/public && \
    chown dockeruser:dockergroup /home/dockeruser/app/user_data && \
    chown dockeruser:dockergroup /home/dockeruser/app/amex_data

COPY --chown=dockeruser:dockergroup package.json .
COPY --chown=dockeruser:dockergroup package-lock.json .
COPY --chown=dockeruser:dockergroup shadow-cljs.edn .

USER dockeruser

RUN npm install

CMD npm run build
