# Motorola Update System Explained

This project documents and explains how Motorola's official OTA (Over-The-Air) update system works. Here you will find detailed information about how update requests are sent to the OTA server, what data is transmitted, and how you can manually check for and download OTA update packages without the official Motorola update app.

---

## How does Motorola OTA work?

The Motorola OTA system allows devices to receive software updates wirelessly.  
Normally, the device communicates with the OTA server (hosted on Google Cloud at the `appspot.com` domain) and sends a specially crafted HTTP POST request containing hardware identifiers, software version, and other details.

### Example JSON payload

```json
{
  "id": "SERIAL_NUMBER",
  "contentTimestamp": 0,
  "deviceInfo": {
    "manufacturer": "motorola",
    "brand": "motorola",
    "model": "XT20XX",
    "product": "somecodename",
    "device": "somecodename",
    "hardware": "hw",
    "osVersion": "12",
    "buildId": "S3SR32.45-34-1"
  },
  "extraInfo": {},
  "identityInfo": {
    "imei": "XXXXXXXXXXXXXXX",
    "meid": "",
    "serial": "SERIAL_NUMBER"
  },
  "triggeredBy": "polling",
  "idType": "serialNumber"
}
```

### Example full `curl` request

You can use the following bash script to check for updates manually from your device (you may need `sudo` or `adb shell`):

```sh
curl -X POST "https://moto-cds.appspot.com/cds/upgrade/1/check/ctx/ota/key/$(getprop ro.mot.build.guid)" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "'"$(getprop ro.serialno)"'",
    "contentTimestamp": 0,
    "deviceInfo": {
      "manufacturer": "'"$(getprop ro.product.manufacturer)"'",
      "brand": "'"$(getprop ro.product.brand)"'",
      "model": "'"$(getprop ro.product.model)"'",
      "product": "'"$(getprop ro.product.name)"'",
      "device": "'"$(getprop ro.product.device)"'",
      "hardware": "'"$(getprop ro.hardware)"'",
      "osVersion": "'"$(getprop ro.build.version.release)"'",
      "buildId": "'"$(getprop ro.build.id)"'"
    },
    "extraInfo": {},
    "identityInfo": {
      "imei": "'"$(getprop persist.radio.imei)"'",
      "meid": "'"$(getprop persist.radio.meid)"'",
      "serial": "'"$(getprop ro.serialno)"'"
    },
    "triggeredBy": "polling",
    "idType": "serialNumber"
  }'
```

---

> [!NOTE]
> If you have a **rooted device** and the official Motorola update app blocks you from checking or downloading updates, you can use the above manual method instead.
> By sending requests directly to the OTA server (using `curl` or a script), you can check for and download updates even if your device is rooted or has an unlocked bootloader, as long as you can spoof or hide root from the app when needed.
> The OTA backend does **not** detect root status unless it is explicitly sent in the request payload (which the official app does not do by default). This means that manual requests work on rooted devices.

---

## Server response

The server might respond with something like:

```json
{
  "proceed" : false,
  "context" : "ota",
  "contextKey" : "xxxxxxxxxxxxxxx",
  "content" : null,
  "contentTimestamp" : 0,
  "contentResources" : null,
  "trackingId" : null,
  "reportingTags" : null,
  "pollAfterSeconds" : 172800,
  "smartUpdateBitmap" : -1,
  "uploadFailureLogs" : false
}
```

- If `proceed` is `true`, the response includes update details, a link to the ZIP package, and a hash.
- If `proceed` is `false`, no update is available.

---

## Key facts

- Motorola uses Google Cloud to host its OTA infrastructure.
- The official system update app checks for root/unlocked bootloader and may block the request locally – making a manual curl request bypasses this restriction.
- The OTA backend does not know about root unless that information is sent in the payload (it is not, by default).
- You can download and install OTA packages manually, even on rooted devices.

---

> [!WARNING]
> This project is for educational purposes only.  
> All actions are performed at your own risk! Flashing unofficial packages or modifying your system may void your warranty or brick your device.
