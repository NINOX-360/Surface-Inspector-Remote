package ninox360.util;

import okhttp3.*;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Handles HTTPS setup, Packet Construction, Server interaction, Serialization, Deserialization,
 * Image requests, and Error Handling.
 */
public class HttpCommunicationUtil {
    static String ipAddressOut;
    static String ipBase = "https://localhost:3003";

    /**
     * Constructs a Nested Map representing a YAML packet and sets the routing address. Calls the serializer.
     *
     * @param serverSecret key which is provided by the remote control application on SI
     * @param clientSecret client secret given by the server
     * @param version      packet version
     * @param command      action which we are issuing
     * @param text         additional parameters for a command
     * @return constructed packet as a serialized String
     * @throws IOException indicated an issue, printing the function specific message and exception details.
     */
    public static String constructPacket(String serverSecret, String clientSecret, int version, String command, String text) throws IOException {
        Map<String, Object> packetName = new HashMap<>();
        Map<String, Object> innerPacket = new HashMap<>();

        try {
            if (clientSecret.isEmpty()) { //assume we are registering the device
                innerPacket.put("scanner_secret", serverSecret);
                innerPacket.put("version", version);

                packetName.put("PacketRemoteRegister", innerPacket);

                ipAddressOut = ipBase + "/register";
            } else {
                innerPacket.put("secret", clientSecret);
                innerPacket.put("value", text);
                innerPacket.put("command", command);

                packetName.put("PacketRemoteCommand", innerPacket);

                ipAddressOut = ipBase + "/command";
            }

            return serializeYaml(packetName);
        } catch (Exception e) {
           throw handleError(e, "Failed to create outbound packet prior to serialization");
        }
    }

    /**
     * Serialize/Format the yaml map into a string
     *
     * @param yamlPacket Nested Map which will be serialized
     * @return Formatted String
     * @throws IOException indicated an issue, printing the function specific message and exception details.
     */
    private static String serializeYaml(Map<String, Object> yamlPacket) throws IOException {
        try {
            DumperOptions options = new DumperOptions();

            options.setIndent(2);
            options.setPrettyFlow(true);
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

            Yaml yaml = new Yaml(options);

            return yaml.dump(yamlPacket);
        } catch (Exception e) {
            throw handleError(e, "Failed to serialize yaml packet");
        }
    }

    /**
     * Helper function to simplify request creation and execution
     *
     * @param packet which is being sent out
     * @param url    endpoint which the packet is being sent to
     * @return Response from the server
     * @throws IOException indicated an issue, printing the function specific message and exception details.
     */
    private Response executeRequest(String packet, String url) throws IOException {
        OkHttpClient client = setupOkHttpClient();

        try {
            RequestBody body = RequestBody.create(packet, MediaType.parse("application/x-yaml"));

            Request request = new Request.Builder().url(url).post(body).build();

            return client.newCall(request).execute();
        } catch (Exception e) {
            throw handleError(e, "Error encountered during request execution");
        }
    }

    /**
     * Ingests a serialized YAML packet to send out to the server, manages the Yaml response
     *
     * @param packet Serialized YAML packet which is being sent out.
     * @param url,    URL endpoint.
     * @return Response Packet of the response contents for easy processing
     * @throws IOException indicated an issue, printing the function specific message and exception details.
     */
    public Map<String, Object> sendPacketAndDeserializeYaml(String packet, String url) throws IOException {
        // Execute the request and get the response
        try (Response response = executeRequest(packet, url)) {
            Objects.requireNonNull(response);
            if (!response.isSuccessful()) handleRejectedRequest(response);

            Objects.requireNonNull(response.body());
            return deserializeYamlPacket(response.body().string());
        } catch (Exception e) {
            throw handleError(e, "Failed to process command request");
        }
    }

    /**
     * Ingests a serialized YAML packet to send out to the server, manages the Image response
     *
     * @param packet, Serialized YAML packet which is being sent out.
     * @param url,    URL endpoint.
     * @return BufferedImage for further processing
     * @throws IOException indicated an issue, printing the function specific message and exception details.
     */
    public BufferedImage sendPacketAndDeserializeImage(String packet, String url) throws IOException {
        // Execute the request and get the response
        try (Response response = executeRequest(packet, url)) {
            Objects.requireNonNull(response);
            if (!response.isSuccessful()) handleRejectedRequest(response);

            Objects.requireNonNull(response.body());
            return getImageObject(response.body().bytes());
        } catch (Exception e) {
            throw handleError(e, "Failed to process image command request");
        }
    }

    /**
     * Extracts the yaml information and populates a global map for the response packet contents
     *
     * @param textIn raw string response which is being deserialized
     * @throws IOException indicated an issue, printing the function specific message and exception details.
     */
    private static Map<String, Object> deserializeYamlPacket(String textIn) throws IOException {
        Yaml yaml = new Yaml();
        try {
            // load the full packet
            Map<String, Object> yamlResult = yaml.load(textIn);
            // extract the inner map and store it globally
            return ((Map<String, Object>) yamlResult.get("PacketRemoteResponse"));
        } catch (Exception e) {
            throw handleError(e, "Failed to Deserialize YAML Response Packet");
        }
    }

    /**
     * Processes the image bytes from the response and validates
     *
     * @param imageBytes response Bytes which we are saving locally
     * @throws IOException indicated an issue, printing the function specific message and exception details.
     */
    private static BufferedImage getImageObject(byte[] imageBytes) throws IOException {
        // Convert the byte array to BufferedImage
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(imageBytes)) {
            BufferedImage image = ImageIO.read(byteStream);
            if (image == null) {
                throw new IOException("The byte array does not contain a valid image");
            }
            return image;
        } catch (IOException e) {
            throw handleError(e, "saveImage FAILED: - ImageIo.read failure, Image is not valid");
        }
    }

    /**
     * General Error handling, prints a specific message with the Exception.
     *
     * @param e   Exception to display.
     * @param msg Error message to provide additional context from the function.
     * @return IOException indicated an issue, printing the function specific message and exception details.
     */
    private static IOException handleError(Exception e, String msg) {
        System.err.println(msg + " Hit Exception: '" + e + "'");
        return new IOException(msg, e);
    }

    /**
     * Handle a failed or blocked response packet when trying to execute a request.
     *
     * @param response object returned back from the server.
     * @throws IOException indicated an issue, printing the function specific message and exception details.
     */
    private void handleRejectedRequest(Response response) throws IOException {
        String responseBody = response.body() != null ? response.body().string() : "No response body";
        throw new IOException("Request failed: Status - '" + response.code() + "', Message - '" + response.message() + "' , Body - '" + responseBody + "'");
    }

    /**
     * SSL Management
     *
     * @return the modified client instance which we will communicate via
     * @throws IOException indicated an issue, printing the function specific message and exception details.
     */
    private static OkHttpClient setupOkHttpClient() throws IOException {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[]{};
                }
            }};

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            // Create a ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);

            return builder.build();
        } catch (Exception e) {
            throw handleError(e, "Failed to setup OkHttpClient");
        }
    }
}
