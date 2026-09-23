from microbit import *
import random

# UART pin configuration for the Grove WIO-E5 LoRaWAN module
TX_PIN = pin14
RX_PIN = pin0
BAUD_RATE = 9600


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
    uart.read()

    uart.write(command + '\r\n')

    startTime = running_time()
    response = b''
    while running_time() - startTime < timeout:
        chunk = uart.read()
        if chunk:
            response += chunk
        if waitFor.encode() in response:
            break

    uart.init(baudrate=115200)

    return (waitFor.encode() in response), response


print("Attempting to join the network...")

# Wait for the real join confirmation, not just the command acknowledgement
joinSuccess, joinResponse = sendAtCommand('AT+JOIN', waitFor="Network joined", timeout=30000)

print("JOIN ->", "OK" if joinSuccess else "FAILED", joinResponse)
display.show(Image.YES if joinSuccess else Image.NO)
sleep(2000)

# Runs forever: builds a fresh payload and sends it immediately,
# then waits 60 seconds (60000 ms) before repeating.
while True:
    batteryLevel = random.randint(0, 100)    
    storageUsed = random.randint(0, 100)      

    # Encode payload as hex: 1 byte for battery, 2 bytes for storage
    payloadHex = "{:02X}{:02X}".format(batteryLevel, storageUsed)
    sendCommand = 'AT+MSGHEX="{}"'.format(payloadHex)
    print("battery : ", batteryLevel)
    print("stockage : ", storageUsed)
    print("hexa : ", payloadHex)
    

    print("Sending payload ->", sendCommand)
    sendSuccess, sendResponse = sendAtCommand(sendCommand, waitFor="Done", timeout=15000)

    print("Module response ->", "OK" if sendSuccess else "FAILED", sendResponse)
    display.show(Image.YES if sendSuccess else Image.NO)

    sleep(60000)