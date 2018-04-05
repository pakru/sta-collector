import asyncio
import random
import websockets
import json
import time
import config as cfg

url = "ws://" + cfg.API_ADDRESS + ":" + cfg.API_PORT + "/add_mac"

cnt = 1000                 # кол-во выполняемых запросов add_mac
total = cnt
max_sim_request = 500       # кол-во одновременных запросов
ok_responses = 0


def gen_hex(length):         # this helps generate valid HEX addresses.
    return ''.join(random.choice('0123456789ABCDEF') for _ in range(length))


def gen_mac():       # this generates a 00:XX:XX:XX:XX:XX mac address
    generated = "00:" + gen_hex(2) + ":" + gen_hex(2) + ":" + gen_hex(2) + ":" + gen_hex(2) + ":" + gen_hex(2)
    return generated


def gen_ap_mac_limited():
    generated = "A8:B7:11:00:00:" + gen_hex(2)
    return generated


def gen_sta_mac_limited():
    generated = "00:01:11:00:00:" + gen_hex(2)
    return generated


def inc_ok():
    global ok_responses
    ok_responses += 1


def is_5g(mac):
    last_char = mac[-1:]
    value = int(last_char, 16)
    if value % 2 == 1:          # Odd - 5G; Even - 2.4G
        return True
    else:
        return False


async def do_send_ws():
    async with websockets.connect(url) as websocket:
        apMac = gen_ap_mac_limited()
        staMac = gen_sta_mac_limited()
        rssi = random.randrange(-90, -1)
        # band = random.choice("25")
        if is_5g(staMac):
            band = "5"
        else:
            band = "2"

        jsonToSend = json.dumps({"apDomain": "test_domain", "apMac": str(apMac),
                                 "staMac": str(staMac), "rssi": str(rssi), "band": str(band)})
        await websocket.send(jsonToSend)
        # print("> {}".format("Sending data to: " + url))

        response = await websocket.recv()
        # print("Resp: '{}'".format(response))
        if str(response) == "OK":
            inc_ok()
        # print("< {}".format(response))


async def async_tasks():
    global cnt
    global max_sim_request
    global total
    print("Starting...")
    start = time.time()

    while cnt > 0:
        if cnt <= max_sim_request:
            portion = cnt
        else:
            portion = max_sim_request
        cnt -= portion

        tasks = [asyncio.ensure_future(do_send_ws()) for _ in range(portion)]
        await asyncio.wait(tasks)

    t_delta = time.time() - start
    rate = total / t_delta
    print("Finish! It took: {:.2f} seconds".format(t_delta))
    print("OK responses: " + str(ok_responses), end="")
    print(";   Total: " + str(total))
    print("Rate: {:.6f};".format(rate))


# asyncio.get_event_loop().run_until_complete(do_refactor())
ioloop = asyncio.get_event_loop()
ioloop.run_until_complete(async_tasks())
ioloop.close()
