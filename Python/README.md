
# Surface Inspector Remote Python Library + Demo

This library provides utilities for interacting with the Surface Inspector device through HTTP communication, handling tasks such as packet construction, server interaction, serialization and deserialization, image requests, and error handling.

## Table of Contents

- [Requirements](#requirements)
- [Installation](#installation)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)

## Requirements

- `Python 3.x`
- `virtualenv`

## Installation

### 1. Clone the Repository

```sh
git clone https://github.com/NINOX-360/Surface-Inspector-Remote
cd Surface-Inspector-Remote/Python
```

### 2. Set Up a Virtual Environment

It is recommended to use a virtual environment to manage dependencies. This ensures that the dependencies for this project do not interfere with other projects on your machine.

```sh
python3 -m venv venv
```

### 3. Activate the Virtual Environment

On macOS and Linux:

```sh
source venv/bin/activate
```

On Windows:

```sh
venv\Scripts\activate
```

### 4. Install Dependencies

Once the virtual environment is activated, install the required dependencies:

```sh
pip install -r requirements.txt
```

### 5. Run the project
```sh
pip install .
```
Then run:
```sh
run_python_demo
```
**Make note**: Prior to execution the server secret and server IP **MUST** be set manually by editing the demo code file.

## Usage

### Example Code

Here is an example of how to use the library:

```python
import time
from libs.RemoteApi import RemoteAPI


def main():
   api = RemoteAPI()
   server_secret = 'your_server_secret'
   url_base = 'https://localhost:3003'

   # Set the target URL
   api.set_ip(url_base)

   # Register the device
   api.send_register(server_secret)

   # Print the response
   print(f'Response Message: {api.request_response_msg()}')
   print(f'Response Status: {api.request_response_status()}')

   # start a scan
   api.send_start_scan()

   # sleep
   time.sleep(10)
   
   # stop the scan
   api.send_stop_scan()


if __name__ == "__main__":
   main()
```

### Incorporate into Your Own Project

To use this library in your own project, you can copy the `libs` directory into your project and adjust your import statements accordingly.

Alternatively, you can package this library and install it using `pip`:

1. **Package the Library**: Create a `setup.py` file if it doesn't exist and include the necessary configurations.

2. **Install the Library**: Navigate to the root directory of the library and run:

    ```sh
    pip install .
    ```

3. **Import the Library**: Import the necessary modules in your project files:

    ```python
    from surface_inspector_remote.RemoteApi import RemoteAPI
    ```

## Contributing

Contributions are welcome! Please fork this repository, make your changes, and submit a pull request.

## License

This project is licensed under the Apache 2.0 License. See the [LICENSE](../LICENSE) file for more details.
