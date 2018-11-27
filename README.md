To start developing, install the dependencies with `npm i`, then start a
shadow-cljs server process with `npm run shadow-start`.

The following environment variables must be set:
- STARLING_TOKEN
- AMEX_USERNAME
- AMEX_PASSWORD

First scrape the amex transactions using `npm run amex-scrape` until you see "Finished!".

Save a list of recurring transactions in the following format in `recurring.json`.
```
[
  {
    "narrative": "Eating out",
    "amount": "-80",
    "day": "7",
    "frequency": "weekly"
  },
  {
    "narrative": "Child care",
    "amount": "-38.33",
    "day": "22",
    "frequency": "monthly"
  }
]
```

Save a list of adjustment transactions in the following format in `adjustments.json`.
```
[
  {
    "narrative": "adjustment 0",
    "amount": "-100.99",
    "date": "2018-11-25"
  }
]
```

Next enter `npm run server-watch` until you see "Build completed."

Next run `npm run server-start` in a separate terminal. The server will now be listening on port 3000.

Next run `npm run app-watch` in a seperate terminal. Once you see "Build completed." you can load the
app at `http://localhost:8020`.

# TODO:
Recurring transactions starting after date X.
One off transactions.
