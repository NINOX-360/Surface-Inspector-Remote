from typing import Dict, Any

from HttpCommunicationUtil import HttpCommunicationUtil
from PIL import Image

util = HttpCommunicationUtil()


class RemoteAPI:
    """
    API to simplify interation with a Surface inspector device for Remote Control
    """

    def __init__(self):
        self.util = HttpCommunicationUtil()
        self.client_secret = ""
        self.response = {}

    def set_ip(self, ip: str):
        """
        Set the Device base IP address
        :param ip: represents the scanner server
        """

        self.util.ipBase = ip

    def send_register(self, server_secret: str):
        """
         Registers the device by requesting a client secret which is later used for future commands

        :param server_secret: generated key found in the "Remote Control" feature of the Surface Inspector device
        """

        register_packet: str = self.util.construct_packet(server_secret, '', 0, '', '')
        print(self.util.ipAddressOut)
        self._set_response(util.send_packet_and_deserialize_yaml(register_packet, self.util.ipAddressOut))
        self.client_secret = self.get_response_msg()

    def send_start_scan(self):
        """ Sends the request to Start scanning. """

        command_packet = self.util.construct_packet('', self.client_secret, 0, 'START_SCAN', '')
        self._set_response(util.send_packet_and_deserialize_yaml(command_packet, self.util.ipAddressOut))

    def send_stop_scan(self):
        """ Sends the request to Stop scanning. """

        command_packet = self.util.construct_packet('', self.client_secret, 0, 'STOP_SCAN', '')
        self._set_response(util.send_packet_and_deserialize_yaml(command_packet, self.util.ipAddressOut))

    def send_get_state(self):
        """ Requests the current scanner State. """

        command_packet = self.util.construct_packet('', self.client_secret, 0, 'GET_STATE', '')
        self._set_response(util.send_packet_and_deserialize_yaml(command_packet, self.util.ipAddressOut))

    def send_set_nickname(self, name: str):
        """
        Sets the nickname for the most recent scan/photo. The name is set in 'value'.

        :param name: a String to name the Scan file
        """

        command_packet = self.util.construct_packet('', self.client_secret, 0, 'SET_NICKNAME', name)
        self._set_response(util.send_packet_and_deserialize_yaml(command_packet, self.util.ipAddressOut))

    def send_capture_image_frame(self):
        """
        Captures a video frame from the video stream, returning a BufferedImage for further manipulation

        :return: PIL Image object for further interaction/manipulation
        """

        command_packet = self.util.construct_packet('', self.client_secret, 0, 'CAPTURE_VIDEO_FRAME', '')
        return util.send_packet_and_deserialize_image(command_packet, self.util.ipAddressOut)

    def send_upload_scan(self, file_name: str):
        """
        Uploads the Scan to Ninox360 Cloud storage. The file is set in 'value'.

        :param file_name: which we are uploading
        """

        command_packet = self.util.construct_packet('', self.client_secret, 0, 'UPLOAD_REMOTE', file_name)
        self._set_response(util.send_packet_and_deserialize_yaml(command_packet, self.util.ipAddressOut))

    def send_get_file(self, file_name: str):
        """
        Request that the scanner send over the specified file.

        :param file_name: which we are requesting
        """
        command_packet = self.util.construct_packet('', self.client_secret, 0, 'GET_FILE', file_name)
        self._set_response(util.send_packet_and_deserialize_yaml(command_packet, self.util.ipAddressOut))

    def _set_response(self, response: Dict[str, Any]):
        """ sets the server response as a map """
        self.response = response

    def get_response_msg(self) -> str:
        """ :return the server response message from the previous command issued """
        return self.response.get('message')

    def get_response_status(self) -> str:
        """ :return the server response status from the previous commmand issued """
        return self.response.get('response')

    @staticmethod
    def save_image_to_disk(image: Image, file_name: str = 'image.jpg'):
        """
        Utility to save a file locally to disk

        :param image: PIL Image data which we are saving
        :param file_name: for the file which we are saving
        """

        with open(file_name, 'wb') as image_file:
            image.save(file_name)

    def disconnect(self):
        """ Resets the client_secret to empty requiring another registration for future commands """
        self.client_secret = ''
