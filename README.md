To start developing, install the dependencies with `npm i`, then start a
shadow-cljs server process with `npm run shadow-start`.

The following environment variables must be set:
- PORT
- STARLING_HOST
- STARLING_TOKEN
- RECURRING_TRANSACTIONS_FILENAME
- AMEX_TRANSACTIONS_FILENAME
- AMEX_TRANSACTIONS_PATH
- AMEX_USERNAME
- AMEX_PASSWORD

First scrape the amex transactions using `npm run amex-scrape` until you see "Finished!".

Next enter `npm run server-watch` until you see "Build completed."

Next run `npm run server-start` in a separate terminal. The server will now be listening on port 3000.

Next run `npm run app-watch` in a seperate terminal. Once you see "Build completed." you can load the
app at `http://localhost:8020`.

# TODO:
Recurring transactions starting after date X.
One off transactions.
