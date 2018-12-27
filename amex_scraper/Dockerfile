FROM node:8-slim

# See https://crbug.com/795759
RUN apt-get update && apt-get install -yq libgconf-2-4

# Install latest chrome dev package and fonts to support major charsets (Chinese, Japanese, Arabic, Hebrew, Thai and a few others)
# Note: this installs the necessary libs to make the bundled version of Chromium that Puppeteer
# installs, work.
RUN apt-get update && apt-get install -y wget --no-install-recommends \
    && wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - \
    && sh -c 'echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google.list' \
    && apt-get update \
    && apt-get install -y google-chrome-unstable fonts-ipafont-gothic fonts-wqy-zenhei fonts-thai-tlwg fonts-kacst ttf-freefont \
      --no-install-recommends \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get purge --auto-remove -y curl \
    && rm -rf /src/*.deb

# It's a good idea to use dumb-init to help prevent zombie chrome processes.
ADD https://github.com/Yelp/dumb-init/releases/download/v1.2.0/dumb-init_1.2.0_amd64 /usr/local/bin/dumb-init
RUN chmod +x /usr/local/bin/dumb-init

# Uncomment to skip the chromium download when installing puppeteer. If you do,
# you'll need to launch puppeteer with:
#     browser.launch({executablePath: 'google-chrome-unstable'})
# ENV PUPPETEER_SKIP_CHROMIUM_DOWNLOAD true

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

RUN mkdir /home/dockeruser/app/amex_data && \
    chown dockeruser:dockergroup /home/dockeruser/app && \
    chown dockeruser:dockergroup /home/dockeruser/app/amex_data

COPY --chown=dockeruser:dockergroup package.json .
COPY --chown=dockeruser:dockergroup package-lock.json .

RUN npm install

COPY --chown=dockeruser:dockergroup main.js .

USER dockeruser

ENTRYPOINT ["dumb-init", "--"]
CMD node main.js
