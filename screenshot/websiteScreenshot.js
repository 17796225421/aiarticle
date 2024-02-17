// 引入用于爬虫的库
const puppeteer = require('puppeteer-extra')
const StealthPlugin = require('puppeteer-extra-plugin-stealth')

puppeteer.use(StealthPlugin())

const fs = require('fs');
const path = require('path');

// 定义读取cookie的函数
async function getCookies() {
    // 定义cookie文件的路径
    const cookiesPath = path.join('C:\\Users\\zhouzihong\\Desktop\\aibrower\\server\\cookies.json');

    // 读取cookie文件
    const cookiesData = fs.readFileSync(cookiesPath, 'utf8');

    // 解析cookie数据
    return JSON.parse(cookiesData);
}

// 打开网页并截图
console.log("开始打开网页并截图"); // 添加打印语句

async function run(req) {
    try {
        console.log('启动浏览器并打开新页面...'); // 日志
        // 启动浏览器，并打开一个新的页面
        const browser = await puppeteer.launch({headless: true});
        const page = await browser.newPage();

        // 读取cookie
        const cookies = await getCookies();

        // 设置cookie
        await page.setCookie(...cookies);

        await page.goto(process.argv[2]);
        await page.waitForTimeout(5000);

        // 将截图保存为给定的文件路径，这个路径由第三个命令行参数指定
        await page.screenshot({path: process.argv[3], fullPage: true})

        console.log("截图完成");

        await browser.close();
    } catch (error) {
        console.error('捕获到错误:', error); // 打印错误信息
        return {error: error.toString()};
    }
}

run();