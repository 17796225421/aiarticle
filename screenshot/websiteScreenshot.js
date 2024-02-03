const puppeteer = require('puppeteer-extra');
const StealthPlugin = require('puppeteer-extra-plugin-stealth');
const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

puppeteer.use(StealthPlugin());

// 定义cookies文件的路径
const cookiesFilePath = path.join(__dirname, 'cookies.json');

// 获取cookies文件的最后修改时间
const stats = fs.statSync(cookiesFilePath);
const lastModifiedTime = new Date(stats.mtime).getTime();
const currentTime = new Date().getTime();

// 如果cookies文件的最后修改时间超过24小时，先执行hack-browser-data命令行
if (currentTime - lastModifiedTime > 24 * 60 * 60 * 1000) {
    // 执行hack-browser-data命令行
    execSync('hack-browser-data.exe -b chrome -f json --dir ./tmp');
    // 找到带有6和cookie的文件，将其内容更新到cookies.json
    const files = fs.readdirSync('./tmp');
    const targetFile = files.find(file => file.includes('6') && file.includes('cookie'));
    const targetFilePath = path.join(__dirname, 'tmp', targetFile);
    const targetContent = fs.readFileSync(targetFilePath, 'utf-8');
    fs.writeFileSync(cookiesFilePath, targetContent);
}

// 读取cookies.json的内容并设置
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

// 打开网页并截图
puppeteer.launch({ headless: true }).then(async browser => {
    const page = await browser.newPage();
    await page.setViewport({ width: 800, height: 600 });
    await page.setCookie(...mappedCookies);
    await page.goto(process.argv[2], { waitUntil: 'networkidle2' });
    await page.waitForTimeout(5000);

    // 将截图保存为"websiteScreenshot.png"，并将其放在"screenshot"目录
    await page.screenshot({ path: path.join(__dirname, '.', 'websiteScreenshot.png') });

    await browser.close();
});