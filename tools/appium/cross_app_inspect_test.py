import json
import os
import re
import subprocess
import sys
import time
import urllib.error
import urllib.request


APPIUM_SERVER = "http://127.0.0.1:4723"
SAMPLE_PACKAGE = "com.apkparse.sample"


def resolve_device_serial():
    explicit_serial = os.environ.get("ANDROID_SERIAL")
    if explicit_serial:
        return explicit_serial

    completed = subprocess.run(["adb", "devices"], check=True, capture_output=True, text=False)
    output = completed.stdout.decode("utf-8", errors="replace").splitlines()
    serials = []
    for line in output[1:]:
        line = line.strip()
        if not line:
            continue
        parts = line.split()
        if len(parts) >= 2 and parts[1] == "device":
            serials.append(parts[0])

    if len(serials) != 1:
        raise RuntimeError("Set ANDROID_SERIAL or connect exactly one Android device")
    return serials[0]


def request_json(method, path, payload=None):
    data = None
    headers = {"Content-Type": "application/json"}
    if payload is not None:
        data = json.dumps(payload).encode("utf-8")
    request = urllib.request.Request(APPIUM_SERVER + path, data=data, headers=headers, method=method)
    with urllib.request.urlopen(request, timeout=60) as response:
        return json.loads(response.read().decode("utf-8"))


def run_adb(*args):
    command = ["adb", "-s", resolve_device_serial()] + list(args)
    completed = subprocess.run(command, check=True, capture_output=True, text=False)
    return completed.stdout.decode("utf-8", errors="replace").strip()


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
        "appium:forceAppLaunch": True,
        "appium:newCommandTimeout": 180
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


def delete_session(session_id):
    request_json("DELETE", f"/session/{session_id}")


def get_page_source(session_id):
    return request_json("GET", f"/session/{session_id}/source").get("value", "")


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


def find_optional_element(session_id, strategy, selector):
    try:
        return find_element(session_id, strategy, selector)
    except Exception:
        return None


def click_element(session_id, element_id):
    request_json("POST", f"/session/{session_id}/element/{element_id}/click", {})


def tap_point(session_id, x, y):
    request_json(
        "POST",
        f"/session/{session_id}/actions",
        {
            "actions": [
                {
                    "type": "pointer",
                    "id": "finger1",
                    "parameters": {"pointerType": "touch"},
                    "actions": [
                        {"type": "pointerMove", "duration": 0, "x": x, "y": y},
                        {"type": "pointerDown", "button": 0},
                        {"type": "pause", "duration": 80},
                        {"type": "pointerUp", "button": 0}
                    ]
                }
            ]
        }
    )
    request_json("DELETE", f"/session/{session_id}/actions")


def parse_display_metrics():
    size_text = run_adb("shell", "wm", "size")
    density_text = run_adb("shell", "wm", "density")
    size_match = re.search(r"(\d+)x(\d+)", size_text)
    density_match = re.search(r"(\d+)", density_text)
    if not size_match or not density_match:
        raise RuntimeError("Unable to parse device metrics")

    width = int(size_match.group(1))
    density = int(density_match.group(1))
    scale = density / 160.0
    ball_size = int(58 * scale)
    margin = int(16 * scale)
    ball_x = width - ball_size - margin
    ball_y = int(220 * scale)
    center_x = ball_x + ball_size // 2
    center_y = ball_y + ball_size // 2
    return width, center_x, center_y


def wait_for_text(session_id, text, timeout_seconds=20):
    for _ in range(timeout_seconds * 2):
        try:
            element_id = find_element(session_id, "xpath", "//*[@text='" + text + "']")
            if element_id:
                return element_id
        except Exception:
            time.sleep(0.5)
    raise RuntimeError("Timed out waiting for text: " + text)


def dismiss_known_dialogs(session_id):
    for _ in range(3):
        ok_button = find_optional_element(session_id, "xpath", "//*[@resource-id='android:id/button1']")
        if not ok_button:
            return
        click_element(session_id, ok_button)
        time.sleep(1)


def ensure_accessibility_enabled():
    dumpsys_text = run_adb("shell", "dumpsys", "accessibility")
    expected_service = SAMPLE_PACKAGE + "/com.apkparse.android.service.UiParseAccessibilityService"
    if expected_service not in dumpsys_text:
        raise RuntimeError("Enable APK UI Parse Service in system accessibility settings before running this test")


def read_logs():
    return run_adb("logcat", "-d", "-s", "FloatingDumpService:V", "UiParseA11yService:V")


def extract_captured_package(log_text):
    match = re.search(r"package=([^\s]+)", log_text)
    return match.group(1) if match else ""


def main():
    wait_for_server()
    ensure_accessibility_enabled()
    run_adb("shell", "am", "force-stop", SAMPLE_PACKAGE)
    run_adb("logcat", "-c")

    session_id = create_session()
    try:
        dismiss_known_dialogs(session_id)
        start_button = wait_for_text(session_id, "Start Floating Button")
        click_element(session_id, start_button)
        time.sleep(1)
        run_adb("shell", "input", "keyevent", "KEYCODE_HOME")

        run_adb("shell", "am", "start", "-a", "android.settings.SETTINGS")
        time.sleep(2)

        _, center_x, center_y = parse_display_metrics()
        candidate_offsets = [(0, 0), (-30, 0), (30, 0), (0, -30), (0, 30)]
        matched_log = ""
        matched_point = None
        for offset_x, offset_y in candidate_offsets:
            run_adb("logcat", "-c")
            tap_point(session_id, center_x + offset_x, center_y + offset_y)
            time.sleep(2)
            logs = read_logs()
            if "refreshInspectSnapshot success" in logs:
                matched_log = logs
                matched_point = (center_x + offset_x, center_y + offset_y)
                break

        if not matched_log:
            raise RuntimeError("Floating ball tap did not trigger inspect mode")

        captured_package = extract_captured_package(matched_log)
        if not captured_package or captured_package == SAMPLE_PACKAGE:
            raise RuntimeError("Inspect mode did not capture a foreign foreground package\n" + matched_log)

        run_adb("logcat", "-c")
        tap_point(session_id, 540, 1000)
        time.sleep(2)
        selection_logs = read_logs()
        selection_ok = "inspectAt selected node=" in selection_logs

        print(json.dumps({
            "ok": True,
            "capturedPackage": captured_package,
            "ballTapPoint": {"x": matched_point[0], "y": matched_point[1]},
            "selectionPoint": {"x": 540, "y": 1000},
            "selectionTriggered": selection_ok
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
