from microbit import *
import random

# UART pin configuration for the Grove WIO-E5 (RAK) LoRaWAN module
TX_PIN = pin14
RX_PIN = pin0
BAUD_RATE = 9600  # Actual baud rate expected by the RAK/Grove WIO-E5 module


def sendAtCommand(command, waitFor="OK", timeout=8000):
    """
    Send an AT command to the LoRaWAN module and wait for a specific
    response substring, or until the timeout is reached.

    Args:
        command: the AT command string to send (without \r\n)
        waitFor: substring expected in the module's response
        timeout: max time to wait for the response, in ms

    Returns:
        (success, rawResponse): success is True if waitFor was found,
        rawResponse is the raw bytes received from the module.
    """
    uart.init(baudrate=BAUD_RATE, tx=TX_PIN, rx=RX_PIN)
    sleep(50)
    uart.read()  # Flush any stale data sitting in the buffer

    uart.write(command + '\r\n')

    startTime = running_time()
    response = b''
    while running_time() - startTime < timeout:
        chunk = uart.read()
        if chunk:
            response += chunk
        if waitFor.encode() in response:
            break

    return (waitFor.encode() in response), response


# --- 1. JOIN THE LORAWAN NETWORK ---
print("Attempting to join the network...")

# Depending on the RAK firmware version, the command is AT+JOIN
# or AT+JOIN=1:0:10:8. We only check for "OK" here (command accepted),
# not the actual join confirmation event (+EVT:JOINED / "Joined").
joinSuccess, joinResponse = sendAtCommand('AT+JOIN', waitFor="OK", timeout=10000)

# Give the module time to complete the join handshake with the gateway
sleep(5000)

# --- 2. PERIODIC UPLINK LOOP ---
# Runs forever: builds a fresh payload and sends it immediately,
# then waits 60 seconds (60000 ms) before repeating.
while True:
    batteryLevel = random.randint(0, 100)     # Simulated battery level (%)
    storageUsed = random.randint(0, 100)      # Simulated SD card usage (%)

    # Encode payload as hex: 1 byte for battery, 2 bytes for storage
    payloadHex = "{:02X}{:04X}".format(batteryLevel, storageUsed)
    sendCommand = 'AT+MSG="{}"'.format(payloadHex)

    print("Sending payload ->", sendCommand)
    sendSuccess, sendResponse = sendAtCommand(sendCommand, waitFor="Done", timeout=15000)

    print("RAK response ->", "OK" if sendSuccess else "FAILED", sendResponse)
    display.show(Image.YES if sendSuccess else Image.NO)

    sleep(60000)