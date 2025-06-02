package ninox360.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * API to simplify interation with a Surface inspector device for Remote Control.
 */
public class RemoteAPI {
    HttpCommunicationUtil util = new HttpCommunicationUtil();
    String clientSecret;
    PacketRemoteResponse responsePacket = new PacketRemoteResponse();

    /**
     * Set the Device base IP address
     *
     * @param ip address representing the scanner server
     */
    public void setIP(String ip) {
        HttpCommunicationUtil.ipBase = ip;
    }

    /**
     * Sets the local response variables based on the deserialized response map
     *
     * @param response Map of the Response and Message from PacketRemoteResponse
     */
    private void setResponse(Map<String, Object> response) {
        responsePacket.response = (String) response.get("response");
        responsePacket.message = (String) response.get("message");
    }

    /**
     * Indicates a status after an outbound command/register packet has been sent, stored until the next packet is sent.
     * <pre>
     *     Usage Example:
     *     api.sendRegister(serverSecret);
     *
     *     String result = api.getResponseStatus();
     *     System.out.println("Response: " + result); // Prints the response status code from outgoing the request
     *
     *     // sample out:  response: SUCCESS
     * </pre>
     *
     * @return returns the corresponding network status string from a prior response.
     */
    public String getResponseStatus() {
        return responsePacket.response;
    }

    /**
     * Gets the server response message following a request, stored until the next packet is sent.
     * <pre>
     *     Usage Example:
     *     api.sendRegister(serverSecret);
     *
     *     String result = api.getResponseMsg();
     *     System.out.println("Message: " + result); // printing out the client secret returned
     *
     *     // sample out:  Message: BLZ1x
     * </pre>
     *
     * @return returns the corresponding message string, in the example: "MaDA1" would be returned
     */
    public String getResponseMsg() {
        return responsePacket.message;
    }

    /**
     * Registers the device by requesting a client secret which is later used for future commands
     *
     * @param serverSecret Generated key found in the "Remote Control" feature of the Surface Inspector device
     * @throws IOException An exception will be thrown if any process fails,
     *                     detailed information will be displayed depending on the process
     */
    public void sendRegister(String serverSecret) throws IOException {
        String registerPacket = HttpCommunicationUtil.constructPacket(serverSecret, "", 0, "", "");
        setResponse(util.sendPacketAndDeserializeYaml(registerPacket, HttpCommunicationUtil.ipAddressOut));

        // set the client secret to the response message for future commands
        this.clientSecret = getResponseMsg();
    }

    /**
     * Sends the request to Start scanning.
     *
     * @throws IOException An exception will be thrown if any process fails,
     *                     detailed information will be displayed depending on the process
     */
    public void sendStartScan() throws IOException {
        String commandPacket = HttpCommunicationUtil.constructPacket("", clientSecret, 0, "START_SCAN", "");
        setResponse(util.sendPacketAndDeserializeYaml(commandPacket, HttpCommunicationUtil.ipAddressOut));
    }

    /**
     * Sends the request to Stop scanning.
     *
     * @throws IOException An exception will be thrown if any process fails,
     *                     detailed information will be displayed depending on the process
     */
    public void sendStopScan() throws IOException {
        String commandPacket = HttpCommunicationUtil.constructPacket("", clientSecret, 0, "STOP_SCAN", "");
        setResponse(util.sendPacketAndDeserializeYaml(commandPacket, HttpCommunicationUtil.ipAddressOut));
    }

    /**
     * Requests the current scanner State.
     *
     * @throws IOException An exception will be thrown if any process fails,
     *                     detailed information will be displayed depending on the process
     */
    public void sendGetState() throws IOException {
        String commandPacket = HttpCommunicationUtil.constructPacket("", clientSecret, 0, "GET_STATE", "");
        setResponse(util.sendPacketAndDeserializeYaml(commandPacket, HttpCommunicationUtil.ipAddressOut));
    }

    /**
     * Sets the camera stillness requirement.
     *
     * @param state: Boolean indicating if the stillness is required prior to scanning, true=on false=off
     * @throws IOException An exception will be thrown if any process fails,
     *                     detailed information will be displayed depending on the process
     */
    public void sendStillnessRequired(boolean state) throws IOException {
        String commandPacket = HttpCommunicationUtil.constructPacket("", clientSecret, 0, "REQUIRE_STILL", Boolean.toString(state));
        setResponse(util.sendPacketAndDeserializeYaml(commandPacket, HttpCommunicationUtil.ipAddressOut));
    }

    /**
     * Sets the camera QR marker requirement.
     *
     * @param state: Boolean indicating if the QR marker is required prior to scanning, true=on false=off
     * @throws IOException An exception will be thrown if any process fails,
     *                     detailed information will be displayed depending on the process
     */
    public void sendMarkerRequired(boolean state) throws IOException {
        String commandPacket = HttpCommunicationUtil.constructPacket("", clientSecret, 0, "REQUIRE_MARKER", Boolean.toString(state));
        setResponse(util.sendPacketAndDeserializeYaml(commandPacket, HttpCommunicationUtil.ipAddressOut));
    }

    /**
     * Sets the camera sensor exposure time (nanoseconds) aka shutter speed.
     *
     * @param exposure_ns, exposure time aka shutter speed, represented in nanoseconds
     * @throws IOException An exception will be thrown if any process fails,
     *                     detailed information will be displayed depending on the process
     */
    public void sendSetCameraExposure(long exposure_ns) throws IOException {
        String commandPacket = HttpCommunicationUtil.constructPacket("", clientSecret, 0, "EXPOSURE", Long.toString(exposure_ns));
        setResponse(util.sendPacketAndDeserializeYaml(commandPacket, HttpCommunicationUtil.ipAddressOut));
    }

    /**
     * Sets the camera sensor sensitivity aka ISO.
     *
     * @param sensitivity_iso, exposure time aka shutter speed, represented in nanoseconds
     * @throws IOException An exception will be thrown if any process fails,
     *                     detailed information will be displayed depending on the process
     */
    public void sendSetCameraSensitivity(int sensitivity_iso) throws IOException {
        String commandPacket = HttpCommunicationUtil.constructPacket("", clientSecret, 0, "SENSITIVITY", Integer.toString(sensitivity_iso));
        setResponse(util.sendPacketAndDeserializeYaml(commandPacket, HttpCommunicationUtil.ipAddressOut));
    }

    /**
     * Sets the nickname for the most recent scan/photo. The name is set in 'value'.
     *
     * @param name a String to name the Scan file
     * @throws IOException An exception will be thrown if any process fails,
     *                     detailed information will be displayed depending on the process
     */
    public void sendSetNickname(String name) throws IOException {
        String commandPacket = HttpCommunicationUtil.constructPacket("", clientSecret, 0, "SET_NICKNAME", name);
        setResponse(util.sendPacketAndDeserializeYaml(commandPacket, HttpCommunicationUtil.ipAddressOut));
    }

    /**
     * Captures a video frame from the video stream, returning a BufferedImage for further manipulation
     *
     * @return BufferedImage for further interaction/manipulation
     * @throws IOException An exception will be thrown if any process fails,
     *                     detailed information will be displayed depending on the process
     */
    public BufferedImage sendCaptureImageFrame() throws IOException {
        String commandPacket = HttpCommunicationUtil.constructPacket("", clientSecret, 0, "CAPTURE_VIDEO_FRAME", "");
        return util.sendPacketAndDeserializeImage(commandPacket, HttpCommunicationUtil.ipAddressOut);
    }

    /**
     * Uploads the Scan or File to Ninox360 Cloud storage.
     *
     * @param fileName of the file which we are uploading
     * @throws IOException An exception will be thrown if any process fails,
     *                     detailed information will be displayed depending on the process
     */
    public void sendUploadScan(String fileName) throws IOException {
        String commandPacket = HttpCommunicationUtil.constructPacket("", clientSecret, 0, "UPLOAD_REMOTE", fileName);
        setResponse(util.sendPacketAndDeserializeYaml(commandPacket, HttpCommunicationUtil.ipAddressOut));
    }

    /**
     * Request that the scanner send over the specified file.
     *
     * @param fileName Filename which we are getting from the server.
     * @throws IOException An exception will be thrown if any process fails,
     *                     detailed information will be displayed depending on the process
     */
    public void sendGetFile(String fileName) throws IOException {
        String commandPacket = HttpCommunicationUtil.constructPacket("", clientSecret, 0, "GET_FILE", fileName);
        setResponse(util.sendPacketAndDeserializeYaml(commandPacket, HttpCommunicationUtil.ipAddressOut));
    }

    /**
     * Disconnect this client from the scanner. Forget the client specific secret.
     */
    public void disconnect() {
        clientSecret = "";
    }

    /**
     * API utility for saving images to disk as a JPG.
     *
     * @param image    BufferedImage object
     * @param filePath storage path
     * @return the boolean result from the ImageIO.write() call
     * @throws IOException An exception will be thrown if any process fails,
     *                     detailed information will be displayed depending on the process + The file data
     */
    public static boolean saveImageToDisk(BufferedImage image, String filePath) throws IOException {
        File file = new File(filePath);
        try {
            System.out.println("Saving image to disk: " + filePath + " at path: " + file.getAbsolutePath());
            return ImageIO.write(image, "jpg", file);
        } catch (Exception e) {
            throw new IOException("Failed to save the image: " + filePath + " at path: " + file.getAbsolutePath());
        }
    }
}
