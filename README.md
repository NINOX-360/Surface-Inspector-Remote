# Ninox-360 Remote Control API Info

### Supported Languages:

[![Supported Languages](https://img.shields.io/badge/Java%2017-blue)](https://www.oracle.com/java/technologies/downloads/) [![Supported Lang](https://img.shields.io/badge/Python%203.x-yellow)](https://www.python.org/)

### Overview

Welcome to the Wi-Fi enabled NINOX 360 Remote Control API for Surface Inspector. Remote control allows you to integrate the scanner with robots (e.g. crawlers and drones) or extend your reach to where you can't see. The API is very simple, and you can get up and running in minutes.
 
With this API you will be able to complete remote scans, name scans, upload scans, pull the current image frame on-screen and more. For a full list of commands and features check the [Remote API](#library-modules) key methods below Library Modules.

In this repository you will encounter two main types of code:
- `libs`: Home of the language specific API and Utility code.
- `apps`: Home of any demonstration code.



### How to set up/use the Remote Control API:
1. Turn on the Surface Inspector device.
2. Navigate to "Remote Control" on screen.
3. Ensure you are connected to the same network as the device. 
4. Write down the IP address and server secret or scan the QR code found under "Remote Control."
5. Set the target IP to the written down IP address.
6. Set the client secret to the written down server secret. 
7. Send a registration request with the client secret.
8. Once registration is complete, the API will store the issued client secret for future API calls.

### Demo Module
`RemoteInteractionDemo` is a basic example that demonstrates how to use the API to remotely request the scanner status and capture an image frame from the current stream. The image is then saved to disk.

For language specific instruction, please refer to the README's located within their directories.


### Library Modules
<details>
<summary><strong>1. HttpCommunicationUtil</strong></summary>

`HttpCommunicationUtil` is a utility class that handles HTTPS setup, packet construction, server interaction, serialization/deserialization, image requests, and error handling.

#### Key Methods

- **constructPacket**: Constructs a nested map representing a YAML packet and sets the routing address.
- **sendPacketAndDeserializeYaml**: Sends a serialized YAML packet to the server and manages the YAML response.
- **sendPacketAndDeserializeImage**: Sends a serialized YAML packet to the server and manages the image response.

</details>

<details>
<summary><strong>2. PacketRemoteResponse</strong></summary>

`PacketRemoteResponse` is a class for relaying responses back from the device/server.

</details>

<details>
<summary><strong>3. RemoteAPI</strong></summary>

`RemoteAPI` provides an API to simplify interaction with a Surface Inspector device for remote control.

#### Key Methods

- **setIP**: Sets the device base IP address.
- **getResponseStatus**: Gets the "response" field from PacketRemoteResponse.
- **getResponseMsg**: Gets the "message" field from PacketRemoteResponse.
- **sendRegister**: Registers the device by requesting a client secret.
- **sendStartScan**: Sends the request to start scanning.
- **sendStopScan**: Sends the request to stop scanning.
- **sendGetState**: Requests the current scanner state.
- **sendSetNickname**: Sets the nickname for a scan file.
- **sendCaptureImageFrame**: Captures a video frame from the device.
- **sendUploadScan**: Uploads the scan to Ninox360 Cloud storage. Requires an argument for the file name being requested.
- **sendGetFile**: Sends the request to get a file. Requires an argument for the file name being requested.
- **disconnect**: Clears the client secret.
- **saveImageToDisk**: Saves a BufferedImage to disk.

</details>

### High level overview of general API interaction:
1. Registration:
    - A required step: Send a registration request containing the server secret.
    - The response will contain the SI server generated client secret, completing the handshake process, allowing for further interaction.
2. Commands:
    - If registration is completed you can send various commands to remotely control your device.
    - If the registration is not complete and a command packet is requested, it will be rerouted as a registration request.
3. Server Response
    - Server Responses are issued after every registration or command request and are stored in the following structure:
        - Response: this field will handle the status of an issued request. Some examples include: `SUCCESS`, `ERROR`, `BAD_SECRET`
        - Message: this field handles passing back any information provided from the server. An example would be the generated client secret after a registration request.

### License

This project is licensed under the Apache 2.0 License. See the [LICENSE](LICENSE) file for more details.