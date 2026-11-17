from microbit import *
import random

# UART configuration for the LoRaWAN Grove WIO-E5 module
TX_PIN = pin14
RX_PIN = pin0
BAUD_RATE = 9600

# --- The AppKey must be identical in ChirpStack ---
APP_EUI = "2CF7F1207380D71A"                  # 16 hex characters (Join EUI)
APP_KEY = "2b7e151628aed2a6abf7158809cf4f3c"  # 32 hex characters
REGION = "EU868"
RETRY_DELAY = 10000       # pause between two join attempts (ms)
SEND_INTERVAL = 60000     # pause between two sends (ms)
MAX_SEND_FAILS = 3        # send failures before attempting a new join


def sendAtCommand(command, waitFor="OK", timeout=8000):
    """
    Sends an AT command, waits for waitFor in the response (or timeout),
    then reads the end of the response so it is not cut off.

    Returns (success, raw response).
    """
    uart.init(baudrate=BAUD_RATE, tx=TX_PIN, rx=RX_PIN)
    sleep(50)
    uart.read()  # clear the buffer

    uart.write(command + '\r\n')

    startTime = running_time()
    response = b''
    while running_time() - startTime < timeout:
        chunk = uart.read()
        if chunk:
            response += chunk
        if waitFor.encode() in response:
            sleep(150)  # allow the end of the line to arrive
            chunk = uart.read()
            if chunk:
                response += chunk
            break

    # Return UART to USB so that print() shows up in the console
    uart.init(baudrate=115200)
    sleep(100)

    return (waitFor.encode() in response), response


def extractValue(response, name):
    """Extracts the value of a '+ID: <name>, XX:XX:...' line without the ':'."""
    try:
        for line in response.decode().split('\n'):
            if name in line:
                return line.split(',')[1].strip().replace(':', '')
    except Exception:
        pass
    return None


def configureModule():
    """Configures the module in OTAA (the config is saved in memory)."""
    commands = [
        ('AT', "OK"),
        ('AT+MODE=LWOTAA', "+MODE"),
        ('AT+DR=' + REGION, "+DR"),
        ('AT+CLASS=A', "+CLASS"),
        ('AT+ID=AppEui,"{}"'.format(APP_EUI), "+ID"),
        ('AT+KEY=APPKEY,"{}"'.format(APP_KEY), "+KEY"),
    ]
    for cmd, expected in commands:
        ok, resp = sendAtCommand(cmd, waitFor=expected, timeout=3000)
        print(cmd, "->", "OK" if ok else "FAILED", resp)


# Allow time to open the serial console
sleep(3000)

# --- 0. CONFIGURATION (only once) ---
display.show(Image.SQUARE_SMALL)
configureModule()

idSuccess, idResponse = sendAtCommand('AT+ID', waitFor="AppEui", timeout=3000)
devEui = extractValue(idResponse, "DevEui")
print("=================================")
print("DevEUI :", devEui)
print("AppKey :", APP_KEY)
print("=================================")

# --- 1. INFINITE LOOP: join if needed, then send ---
joined = False
sendFails = 0
attempt = 0

while True:
    # --- JOIN (retried indefinitely as long as it fails) ---
    if not joined:
        attempt += 1
        print("Join attempt", attempt)
        display.show(Image.CLOCK12)
        done, joinResponse = sendAtCommand('AT+JOIN=FORCE', waitFor="Done", timeout=20000)
        joined = b"joined" in joinResponse
        print("Join ->", "OK" if joined else "FAILED", joinResponse)

        if not joined:
            display.show(Image.NO)
            sleep(RETRY_DELAY)
            continue

        display.show(Image.YES)
        sendFails = 0
        attempt = 0
        sleep(2000)

    # --- SEND ---
    batteryLevel = random.randint(0, 100)   # Simulated battery level (%)
    storageUsed = random.randint(0, 100)    # Simulated SD card usage (%)

    # Hex payload: 1 byte battery + 2 bytes storage
    payloadHex = "{:02X}{:04X}".format(batteryLevel, storageUsed)
    sendCommand = 'AT+MSGHEX="{}"'.format(payloadHex)

    print("Battery:", batteryLevel, "| Storage:", storageUsed)
    print("Send ->", sendCommand)

    sendSuccess, sendResponse = sendAtCommand(sendCommand, waitFor="Done", timeout=15000)
    print("Response ->", "OK" if sendSuccess else "FAILED", sendResponse)
    display.show(Image.YES if sendSuccess else Image.NO)

    if sendSuccess:
        sendFails = 0
    else:
        sendFails += 1
        if sendFails >= MAX_SEND_FAILS:
            print("Too many send failures, new join")
            joined = False

    sleep(SEND_INTERVAL)