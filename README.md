To start developing, install the dependencies with `npm i`, then start a
shadow-cljs server process with `npm run shadow-start`.

The following environment variables must be set:
- PORT
- STARLING_HOST
- STARLING_TOKEN
- RECURRING_TRANSACTIONS_FILENAME
- AMEX_TRANSACTIONS_FILENAME

To run the server open a new terminal window and enter `npm run server-watch`
until you see "Build completed." Now run `npm run server-start`. The server will
now be listening on port 3000.

To run the browser app, run `npm run app-watch`. Once you see "Build completed."
you can load the app at `http://localhost:8020`.
