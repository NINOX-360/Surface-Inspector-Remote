import requests
import yaml
from PIL import Image
from io import BytesIO
from typing import Dict, Any, Optional


def _get_image_object(image_data: bytes, ) -> Image:
    """
    Processes the image bytes from the response and validates

    :param image_data: byte sequence response which is being stored
    :return: PIL Image object for further manipulation
    :raises IOError: with the contextual message and exception for improved debugging
    """

    try:
        byte_stream = BytesIO(image_data)
        image = Image.open(byte_stream)

        # Verify that the image is valid by attempting to load it. This is not loaded in memory
        image.verify()

        # Reopen the image after verification to perform further operations
        byte_stream.seek(0)
        return Image.open(byte_stream)
    except Exception as e:
        raise handle_error(e, "saveImage FAILED: - Image failure, Image is not valid")


def _setup_http_client() -> requests.Session:
    """
    Helper function to create the HTTPS session, disabling any SSL verification and muting all warnings

    :return: session object
    :raises IOError: with the contextual message and exception for improved debugging
    """
    try:
        session = requests.Session()
        session.verify = False  # Disable SSL verification for all requests
        requests.packages.urllib3.disable_warnings(requests.packages.urllib3.exceptions.InsecureRequestWarning)
        return session
    except Exception as e:
        raise handle_error(e, "Problem encountered during HTTPS session creation")


def _execute_request(packet: str, url: str):
    """
    Executes an HTTP POST request with the given packet to the specified URL.

    :param packet: Serialized YAML packet to be sent.
    :param url: URL to send the request to.
    :return: HTTP response object.
    :raises IOError: with the contextual message and exception for improved debugging
    """
    try:
        client = _setup_http_client()
        headers = {'Content-Type': 'application/x-yaml'}
        return client.post(url, data=packet, headers=headers)
    except Exception as e:
        raise handle_error(e, "Error encountered during request execution")


def _serialize_yaml_packet(data: Dict[str, Any]) -> str:
    """
    Serializes a dictionary/Map to a YAML formatted string.

    :param data: Dictionary to be serialized.
    :return: YAML formatted string.
    :raises IOError: with the contextual message and exception for improved debugging
    """
    try:
        result: str = yaml.dump(data, default_flow_style=False)
        return result
    except Exception as e:
        raise handle_error(e, "Failed to serialize yaml packet")


def _deserialize_yaml_packet(yaml_data: str) -> Dict[str, Any]:
    """
    Deserializes a YAML formatted string to a dictionary/Map.

    :param yaml_data: YAML formatted string.
    :return: Deserialized dictionary.
    :raises IOError: with the contextual message and exception for improved debugging
    """
    try:
        outer_map: Dict[str, Any] = yaml.safe_load(yaml_data)
        return outer_map.get('PacketRemoteResponse', {})
    except Exception as e:
        raise handle_error(e, "Failed to Deserialize YAML Response Packet")


def handle_error(e: Exception, msg: str) -> IOError:
    """
    General Error handling, prints a specific message with the Exception.

    :param e: Exception to display.
    :param msg: Error message to provide additional context from the function.
    :return: IOError indicating an issue, printing the function specific message and exception details.
    :raises IOError: when an error occurs during the handling of the exception.
    """
    print(f"{msg} Hit Exception: '{e}'", file=sys.stderr)
    return IOError(f"{msg}: {str(e)}")


def handle_rejected_request(response: requests.Response) -> IOError:
    """
    Handle a failed or blocked response packet when trying to execute a request raising an exception if anything goes wrong.

    :param response: object returned back from the server.
    :return IOError: indicates an issue, printing the function specific message and exception details.
    """
    response_body = response.text if response.text else "No response body"
    return IOError(
        f"Request failed: Status - '{response.status_code}', Message - '{response.reason}' , Body - '{response_body}'")


class HttpCommunicationUtil:
    """
    Handles HTTPS setup, Packet Construction, Server interaction, Serialization, Deserialization,
    Image requests, and Error Handling
    """

    def __init__(self, ip_base='https://localhost:3003'):
        self.ipAddressOut = ""
        self.ipBase = ip_base

    def construct_packet(self, server_secret: str, client_secret: str, version: int, command: str, text: str) -> str:
        """
        Constructs a YAML packet based on the provided parameters and sets the target URL.

        :param server_secret: Secret key displayed on "Remote Control" page of the SI application.
        :param client_secret: Secret key returned from a registration packet.
        :param version: Version number.
        :param command: Command to be executed, if empty it is assumed a registration packet is requested.
        :param text: Text content included for some commands
        :return: Serialized YAML packet as a string.
        :raises IOError: with the contextual message and exception for improved debugging
        """
        try:
            if not command:
                # Create a registration packet if a command is not provided
                packet = {
                    'PacketRemoteRegister': {
                        'scanner_secret': server_secret,
                        'version': version
                    }
                }
                self.ipAddressOut = self.ipBase + '/register'
            else:
                # Create a command packet if a command is provided
                packet = {
                    'PacketRemoteCommand': {
                        'secret': client_secret,
                        'value': text,
                        'command': command
                    }
                }
                self.ipAddressOut = self.ipBase + '/command'

            # Serialize the YAML packet into a string and return it
            return _serialize_yaml_packet(packet)
        except Exception as e:
            raise handle_error(e, "Failed to create outbound packet prior to serialization")

    @classmethod
    def send_packet_and_deserialize_yaml(cls, packet: str, url: str) -> Optional[Dict[str, Any]]:
        """
        Sends a YAML packet to the specified URL and deserializes the YAML response. If a command request fails the error
         will be passed through otherwise an IOError will be raised.

        :param packet: Serialized YAML packet to be sent.
        :param url: URL to send the packet to.
        :return: Deserialized response dictionary, if available.
        :raises IOError: with the contextual message and exception for improved debugging
        """
        try:
            response = _execute_request(packet, url)

            if not response.ok:
                return handle_rejected_request(response)
            return _deserialize_yaml_packet(response.text)
        except Exception as e:
            raise handle_error(e, "Failed to process command request")

    @classmethod
    def send_packet_and_deserialize_image(cls, packet: str, url: str) -> Image:
        """
        Sends a YAML packet to the specified URL and returns the response as an image. If a command request fails the error
         will be passed through otherwise an IOError will be raised.

        :param packet: Serialized YAML packet to be sent.
        :param url: URL to send the packet to.
        :return: Image object from the response content.
        :raises IOError: with the contextual message and exception for improved debugging
        """
        try:
            response = _execute_request(packet, url)

            if not response.ok:
                raise handle_rejected_request(response)
            return _get_image_object(response.content)
        except Exception as e:
            raise handle_error(e, "Failed to process image command request")
