import os
import json
import time
import sys
from wechat_ocr.ocr_manager import OcrManager, OCR_MAX_TASK_ID

wechat_ocr_dir = "C:\\Users\\zhouzihong\\AppData\\Roaming\\Tencent\\WeChat\\XPlugin\\Plugins\\WeChatOCR\\7071\\extracted\\WeChatOCR.exe"
wechat_dir = "D:\\app\\WeChat\\[3.9.9.35]"


def ocr_result_callback(img_path: str, results: dict):
    # 识别成功后删除图片
    os.remove(img_path)
    # 聚合识别的文本部分
    aggregated_text = '\n'.join([result['text'] for result in results['ocrResult']])
    print(aggregated_text)


def main(img_path):
    ocr_manager = OcrManager(wechat_dir)
    # 设置WeChatOcr目录
    ocr_manager.SetExePath(wechat_ocr_dir)
    # 设置微信所在路径
    ocr_manager.SetUsrLibDir(wechat_dir)
    # 设置ocr识别结果的回调函数
    ocr_manager.SetOcrResultCallback(ocr_result_callback)
    # 启动ocr服务
    ocr_manager.StartWeChatOCR()
    # 开始识别图片
    ocr_manager.DoOCRTask(img_path)
    time.sleep(1)
    while ocr_manager.m_task_id.qsize() != OCR_MAX_TASK_ID:
        pass
    # 识别输出结果
    ocr_manager.KillWeChatOCR()


if __name__ == "__main__":
    # python wxocr.py "C:\\Users\\zhouzihong\\Desktop\\333.png"
    # 从命令行参数获取图片路径
    img_path = sys.argv[1]
    main(img_path)