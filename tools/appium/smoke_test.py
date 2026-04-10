import json
import sys
import time
import urllib.error
import urllib.parse
import urllib.request


APPIUM_SERVER = "http://127.0.0.1:4723"


def request_json(method, path, payload=None):
    data = None
    headers = {"Content-Type": "application/json"}
    if payload is not None:
        data = json.dumps(payload).encode("utf-8")
    request = urllib.request.Request(APPIUM_SERVER + path, data=data, headers=headers, method=method)
    with urllib.request.urlopen(request, timeout=60) as response:
        return json.loads(response.read().decode("utf-8"))


def wait_for_server():
    for _ in range(30):
        try:
            response = request_json("GET", "/status")
            if response.get("value", {}).get("ready", False):
                return
        except Exception:
            time.sleep(1)
    raise RuntimeError("Appium server not ready")


def create_session():
    capabilities = {
        "platformName": "Android",
        "appium:automationName": "UiAutomator2",
        "appium:deviceName": "AndroidDevice",
        "appium:appPackage": "com.apkparse.sample",
        "appium:appActivity": "com.apkparse.sample.MainActivity",
        "appium:noReset": True,
        "appium:newCommandTimeout": 120
    }
    response = request_json(
        "POST",
        "/session",
        {
            "capabilities": {
                "alwaysMatch": capabilities,
                "firstMatch": [{}]
            }
        }
    )
    return response["value"]["sessionId"]


def find_element(session_id, strategy, selector):
    response = request_json(
        "POST",
        f"/session/{session_id}/element",
        {"using": strategy, "value": selector}
    )
    value = response.get("value")
    if isinstance(value, dict):
        return value.get("element-6066-11e4-a52e-4f735466cecf") or value.get("ELEMENT")
    raise RuntimeError("Element not found")


def get_element_text(session_id, element_id):
    response = request_json("GET", f"/session/{session_id}/element/{element_id}/text")
    return response.get("value")


def click_element(session_id, element_id):
    request_json("POST", f"/session/{session_id}/element/{element_id}/click", {})


def delete_session(session_id):
    request_json("DELETE", f"/session/{session_id}")


def main():
    wait_for_server()
    session_id = create_session()
    try:
        title_id = find_element(session_id, "xpath", "//*[@text='APK UI Parse Sample']")
        title_text = get_element_text(session_id, title_id)

        settings_button = find_element(session_id, "xpath", "//*[@text='Open Accessibility Settings']")
        dump_button = find_element(session_id, "xpath", "//*[@text='Dump Top Window JSON']")
        save_button = find_element(session_id, "xpath", "//*[@text='Save JSON File']")
        share_button = find_element(session_id, "xpath", "//*[@text='Share JSON File']")

        click_element(session_id, dump_button)
        time.sleep(1)

        print(json.dumps({
            "ok": True,
            "title": title_text,
            "elements": {
                "settings": bool(settings_button),
                "dump": bool(dump_button),
                "save": bool(save_button),
                "share": bool(share_button)
            }
        }, ensure_ascii=False))
    finally:
        delete_session(session_id)


if __name__ == "__main__":
    try:
        main()
    except urllib.error.HTTPError as error:
        body = error.read().decode("utf-8", errors="replace")
        print(body, file=sys.stderr)
        raise
