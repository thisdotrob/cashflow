To start developing, the following environment variables must be set:
- STARLING_TOKEN
- AMEX_USERNAME
- AMEX_PASSWORD

Save a list of recurring transactions in the following format in `user_data/recurring.json`.
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

Save a list of adjustment transactions in the following format in `user_data/adjustments.json`.
```
[
  {
    "narrative": "adjustment 0",
    "amount": "-100.99",
    "date": "2018-11-25"
  }
]
```

Save a list of one-off future transactions in the following format in
`user_data/one_off.json`.
```
[
  {
    "narrative": "one off transacftion 0",
    "amount": "2000.12",
    "date": "2019-02-13"
  }
]
```

Run `npm run dc-up`.

When you see `[:server] Build completed.` and `[:client] Build completed.`, run `npm start`.
The app should then be available at `http://localhost:8020`.

# TODO:
- Unify format of recurring transactions around Starling schema.
- Move user_data to a db or something
- Recurring transactions starting after date X.
- Migrate to v2 of Starling API.
- Stop timeouts from starling crashing the server
- Add route info logs to server
