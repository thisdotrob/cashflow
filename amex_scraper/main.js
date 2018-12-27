const puppeteer = require("puppeteer");
const path = require("path");
const fs = require("fs");

const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms));

const main = async () => {
  let screenshotCounter = 0;
  console.log("Opening browser...");
  const browser = await puppeteer.launch();
  const page = await browser.newPage();


  const screenshot = async tag => {
    await page.screenshot({path: 'screenshot-' + tag + '.png'});
  };

  console.log("Visiting amex homepage...");
  await Promise.all([
    page.waitForNavigation(),
    page.goto("https://www.americanexpress.com/uk/")
  ]);
  await screenshot("00");

  console.log("Clicking away euc mask...");
  await sleep(3000);
  await page.click("#euc_mask")
  await screenshot("01");

  console.log("Logging in...");
  await page.type("input[name=UserID]", process.env.AMEX_USERNAME);
  await page.type("input[name=Password]", process.env.AMEX_PASSWORD);
  await Promise.all([page.waitForNavigation(), page.click("#login-submit")]);
  await screenshot("02");

  console.log("Visiting export statement data page...");
  await Promise.all([
    page.waitForNavigation(),
    page.goto(
      "https://global.americanexpress.com/myca/intl/download/emea/download.do?request_type=&Face=en_GB&BPIndex=0&inav=gb_myca_pc_statement_export_statement_data&account_key=BD5653E3488D45B1817F84FAEC6D819D"
    )
  ]);
  await screenshot("03");

  console.log("Selecting CSV");
  await page.click("#CSV");
  await screenshot("04");

  console.log("Selecting BA card");
  await page.click("#selectCard11");
  await screenshot("05");

  console.log("Selecting present");
  try {
  await page.click("#radioid10");
  } catch (err) {
    console.log("Couldn't select present");
  }
  await screenshot("06");

  console.log("Selecting last month");
  try {
    await page.click("#radioid11");
  } catch (err) {
    console.log("Couldn't select last month");
  }
  await screenshot("07");

  console.log("Selecting 2 months ago");
  try {
    await page.click("#radioid12");
  } catch (err) {
    console.log("Couldn't select 2 months ago");
  }
  await screenshot("08");

  console.log("Selecting 3 months ago");
  try {
    await page.click("#radioid13");
  } catch (err) {
    console.log("Couldn't select 3 months ago");
  }
  await screenshot("09");

  await page._client.send("Page.setDownloadBehavior", {
    behavior: "allow",
    downloadPath: "/tmp/"
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

    fs.watch("/tmp/", (eventType, filename) => {
      if (
        eventType === "rename" &&
        filename === "ofx.csv"
      ) {
        console.log("File detected...");
        clearTimeout(timer);
        resolve();
      }
    });
  });
  await screenshot("10");

  console.log("Closing browser...");
  await browser.close();

  console.log("Moving file...");
  fs.copyFileSync("/tmp/ofx.csv", "./amex_data/amex.csv");

  console.log("Removing temporary file...");
  fs.unlinkSync("/tmp/ofx.csv");
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
