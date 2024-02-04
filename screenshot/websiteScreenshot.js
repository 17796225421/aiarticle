const puppeteer = require('puppeteer-extra');
const StealthPlugin = require('puppeteer-extra-plugin-stealth');
const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

puppeteer.use(StealthPlugin());

// 定义cookies文件的路径
const cookiesFilePath = path.join(__dirname, 'cookies.json');

// 检查cookies.json是否存在，如果不存在则创建并执行hack
if (!fs.existsSync(cookiesFilePath)) {
    console.log("cookies.json文件不存在，开始执行hackAndUpdateCookies函数"); // 添加打印语句
    hackAndUpdateCookies();
} else {
    console.log("cookies.json文件存在，开始检查最后修改时间"); // 添加打印语句
    // 获取cookies文件的最后修改时间
    const stats = fs.statSync(cookiesFilePath);
    const lastModifiedTime = new Date(stats.mtime).getTime();
    const currentTime = new Date().getTime();

    // 如果cookies文件的最后修改时间超过24小时，执行hack
    if (currentTime - lastModifiedTime > 24 * 60 * 60 * 1000) {
        console.log("cookies.json文件最后修改时间超过24小时，开始执行hackAndUpdateCookies函数"); // 添加打印语句
        hackAndUpdateCookies();
    }
}

// 读取cookies.json的内容并设置
console.log("开始读取cookies.json的内容并设置"); // 添加打印语句
const cookies = require(cookiesFilePath);
const mappedCookies = cookies.map(cookie => ({
    name: cookie.KeyName,
    value: cookie.Value,
    domain: cookie.Host,
    path: cookie.Path,
    expires: new Date(cookie.ExpireDate).getTime() / 1000,
    httpOnly: cookie.IsHTTPOnly,
    secure: cookie.IsSecure,
    sameSite: 'unspecified'
}));
console.log("完成读取cookies.json的内容并设置"); // 添加打印语句

// 打开网页并截图
console.log("开始打开网页并截图"); // 添加打印语句
puppeteer.launch({ headless: true }).then(async browser => {
    const page = await browser.newPage();
    await page.setViewport({ width: 800, height: 600 });
    await page.setCookie(...mappedCookies);

    await page.goto(process.argv[2]);
    await page.waitForTimeout(5000);

    // 将截图保存为"websiteScreenshot.png"，并将其放在"screenshot"目录
    await page.screenshot({ path: '../screenshot/websiteScreenshot.png', fullPage: true })

    console.log("截图完成");

    await browser.close();
});

// hack和更新cookies的函数
function hackAndUpdateCookies() {
    console.log("开始执行hack-browser-data命令行"); // 添加打印语句
    // 执行hack-browser-data命令行
    execSync('hack-browser-data.exe -b chrome -f json --dir ./tmp');
    console.log("hack-browser-data命令行执行完成"); // 添加打印语句

    console.log("开始查找带有6和cookie的文件"); // 添加打印语句
    // 找到带有6和cookie的文件，将其内容更新到cookies.json
    const files = fs.readdirSync('./tmp');
    const targetFile = files.find(file => file.includes('6') && file.includes('cookie'));
    console.log("找到的文件名为：" + targetFile); // 添加打印语句

    console.log("开始读取文件内容并写入到cookies.json"); // 添加打印语句
    const targetFilePath = path.join(__dirname, 'tmp', targetFile);
    const targetContent = fs.readFileSync(targetFilePath, 'utf-8');
    fs.writeFileSync(cookiesFilePath, targetContent);
    console.log("文件内容更新完成"); // 添加打印语句
}