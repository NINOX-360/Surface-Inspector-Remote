import os
import sys

# Get the current script directory
current_dir = os.path.dirname(os.path.abspath(__file__))

# Insert libs path
sys.path.insert(0, os.path.join(current_dir, '..', 'libs'))

# Insert the apps path
sys.path.insert(0, current_dir)

from SIRemote import RemoteAPI

api = RemoteAPI()
server_secret: str = 'k28b1'
url_base: str = 'https://localhost:3003'


def check_for_failure():
    """ Error handling for demo """
    if not api.get_response_status().lower() == "success":
        raise IOError(f"Remote interaction failed: {api.get_response_msg()}")


def print_formated_debug() -> None:
    """Prints the Response packet from the global `api` instance."""

    print("DEBUG | PRINTING FORMATED RESPONSE INFO")
    print(f'RESPONSE MSG: {api.get_response_msg()}')
    print(f'RESPONSE STATUS: {api.get_response_status()}')


def main() -> None:
    """ Demonstration of how to use the RemoteAPI for Python.
    Here we will set our scanner URL, the scanner/server_secret and then call a few command functions.

    :return: None
    """

    try:
        # set the target URL
        api.set_ip(url_base)

        # register the device
        api.send_register(server_secret)
        check_for_failure()

        # request an image
        image = api.send_capture_image_frame()
        image.show()

        # save that image to disk
        api.save_image_to_disk(image)

        # toggle the QR and Stillness requirement
        api.send_stillness_required(False)
        check_for_failure()

        api.send_marker_required(True)
        check_for_failure()

        # set the camera settings
        api.send_set_camera_exposure(10000000)
        check_for_failure()

        api.send_set_camera_sensitivity(800)
        check_for_failure()

        # get the scanner state and
        api.send_get_state()
        check_for_failure()
    except Exception as e:
        print(f"Exception caught during demo: {e}")


if __name__ == "__main__":
    main()
