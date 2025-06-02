package ninox360.demo;

import ninox360.util.RemoteAPI;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Java Demonstration Code for the Remote Control API.
 * Sets an IP, completes registration, and sends several Commands to the scanner printing the response from the requests.
 */
public class RemoteInteractionDemo {
    static RemoteAPI remote = new RemoteAPI();
    static String serverSecret = "9n0SQ";
    static String outboundIP = "https://localhost:3003";

    /**
     * Optional Debug output to read the Response out.
     */
    public static void printResponse() {
        System.out.println("Server Response:" + "\n  RESPONSE: " + remote.getResponseStatus() + "\n  MESSAGE: " + remote.getResponseMsg() + "\n");
    }

    /**
     * Error handling block the demonstration code.
     *
     * @throws IOException if the response status is not "success" following a request command.
     */
    public static void checkForFailure() throws IOException {
        //printResponse();
        if (!remote.getResponseStatus().equalsIgnoreCase("success")) {
            throw new IOException("Remote interaction failed: " + remote.getResponseMsg());
        }
    }

    public static void main(String[] args) {
        try {
            // set your server IP, a default of 'https://localhost:3003' is prepopulated.
            remote.setIP(outboundIP); // replace this with the IP displayed on your device.

            // Registration is required prior to any server interaction
            remote.sendRegister(serverSecret);
            checkForFailure();

            BufferedImage image = remote.sendCaptureImageFrame();

            // API utility function for saving a Buffered Image to Disk
            if (!remote.saveImageToDisk(image, "saved_frame.jpg")) {
                throw new IOException("Failed to save Image to disk");
            }

            remote.sendGetState();
            checkForFailure();

            remote.sendStillnessRequired(false);
            remote.sendMarkerRequired(true);

            remote.sendSetCameraExposure(10_000_000);
            checkForFailure();

            remote.sendSetCameraSensitivity(400);
            checkForFailure();

        } catch (IOException e) {
            System.err.println("Error during remote interaction: " + e.getMessage());
        }
    }
}