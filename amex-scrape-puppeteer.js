const puppeteer = require("puppeteer");
const path = require("path");
const fs = require("fs");

const main = async () => {
  console.log("Opening browser...");
  const browser = await puppeteer.launch();
  const page = await browser.newPage();

  console.log("Visiting amex homepage...");
  await Promise.all([
    page.waitForNavigation(),
    page.goto("https://www.americanexpress.com/uk/")
  ]);

  console.log("Logging in...");
  await page.type("input[name=UserID]", process.env.AMEX_USERNAME);
  await page.type("input[name=Password]", process.env.AMEX_PASSWORD);
  await page.click("#login-submit");
  await Promise.all([page.waitForNavigation(), page.click("#login-submit")]);

  console.log("Visiting export statement data page...");
  await Promise.all([
    page.waitForNavigation(),
    page.goto(
      "https://global.americanexpress.com/myca/intl/download/emea/download.do?request_type=&Face=en_GB&BPIndex=0&inav=gb_myca_pc_statement_export_statement_data&account_key=BD5653E3488D45B1817F84FAEC6D819D"
    )
  ]);

  console.log("Selecting all statement data...");
  await page.click("#CSV");
  await page.click("#selectCard10");
  await page.click("#radioid00");
  await page.click("#radioid01");
  await page.click("#radioid02");
  await page.click("#radioid03");
  await page._client.send("Page.setDownloadBehavior", {
    behavior: "allow",
    downloadPath: process.env.AMEX_TRANSACTIONS_PATH
  });

  console.log("Starting download...");
  await page.evaluate(() => {
    document.querySelector("#myBlueButton1").click();
  });

  console.log("Waiting for download to finish...");
  await new Promise((resolve, reject) => {
    const timer = setTimeout(() => {
      reject(new Error("Download did not finish after 30 seconds."));
    }, 30000);

    fs.watch(process.env.AMEX_TRANSACTIONS_PATH, (eventType, filename) => {
      if (
        eventType === "rename" &&
        filename === process.env.AMEX_TRANSACTIONS_FILENAME
      ) {
        clearTimeout(timer);
        resolve();
      }
    });
  });

  console.log("Closing browser...");
  await browser.close();
};

main()
  .then(() => {
    console.log("Finished!");
    process.exit(0);
  })
  .catch(err => {
    console.error(err);
    process.exit(1);
  });
