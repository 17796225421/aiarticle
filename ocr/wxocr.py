import time
import sys
from wechat_ocr.ocr_manager import OcrManager, OCR_MAX_TASK_ID

wechat_ocr_dir = "C:\\Users\\zhouzihong\\AppData\\Roaming\\Tencent\\WeChat\\XPlugin\\Plugins\\WeChatOCR\\7071\\extracted\\WeChatOCR.exe"
wechat_dir = "D:\\app\\WeChat\\[3.9.9.43]"

sys.stdout.reconfigure(encoding='utf-8')
sys.stderr.reconfigure(encoding='utf-8')

def ocr_result_callback(img_path: str, results: dict):
    print("OCR识别成功，正在聚合识别的文本部分...")  # 添加打印语句
    aggregated_text = '\n'.join([result['text'] for result in results['ocrResult']])
    print("文本聚合成功，识别结果为：")  # 添加打印语句
    with open('../ocr/wxocr.txt', 'w', encoding='utf-8') as f:  # 将识别的文本写入到文本文件中
        f.write(aggregated_text)

def main(img_path):
    print("正在初始化OCR管理器...")  # 添加打印语句
    ocr_manager = OcrManager(wechat_dir)
    print("正在设置WeChatOcr目录...")  # 添加打印语句
    ocr_manager.SetExePath(wechat_ocr_dir)
    print("正在设置微信所在路径...")  # 添加打印语句
    ocr_manager.SetUsrLibDir(wechat_dir)
    print("正在设置OCR识别结果的回调函数...")  # 添加打印语句
    ocr_manager.SetOcrResultCallback(ocr_result_callback)
    print("正在启动OCR服务...")  # 添加打印语句
    ocr_manager.StartWeChatOCR()
    print("OCR服务启动成功，正在识别图片...")  # 添加打印语句
    ocr_manager.DoOCRTask(img_path)
    print("图片识别任务提交成功，正在等待识别结果...")  # 添加打印语句
    time.sleep(1)
    while ocr_manager.m_task_id.qsize() != OCR_MAX_TASK_ID:
        pass
    print("图片识别完成，正在输出识别结果...")  # 添加打印语句
    ocr_manager.KillWeChatOCR()
    print("OCR服务关闭成功")  # 添加打印语句

if __name__ == "__main__":
    img_path = sys.argv[1]
    main(img_path)