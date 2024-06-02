
# Surface Inspector Remote Java Library + Demo

This library provides utilities for interacting with the Surface Inspector device through HTTP communication, handling tasks such as packet construction, server interaction, serialization and deserialization, image requests, and error handling.

## Table of Contents

- [Requirements](#requirements)
- [Installation](#installation)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)

## Requirements

- Java Development Kit (JDK) 17 or higher

## Installation

### 1. Clone the Repository

```sh
git clone https://github.com/NINOX-360/Surface-Inspector-Remote.git
cd Surface-Inspector-Remote/Java
```

### 2. Build the Project

Use Gradle to build the project. This will compile the source code and download any necessary dependencies.

```sh
./gradlew build
```

### 3. Run the demo

Once installed, run the provided demonstration code.
**Make note**: Prior to execution the server secret and server IP **MUST** be set manually by editing the demo code file.
```sh
./gradlew :apps:run
```


## Usage

### Example Code

Here is an example of how to use the library:

```java
import ninox360.util.RemoteAPI;

public class Example {
    public static void main(String[] args) {
        RemoteAPI api = new RemoteAPI();
        api.setIP("https://your-device-ip");

        try {
            // Registration is always required prior to interaction
            api.sendRegister("your-server-secret");
            api.sendStartScan(); //start the scan

            api.sendStopScan(); //stop the scan

            // Additional API calls as needed
        } catch (IOException e) {
            System.err.println("Exception: " + e);
            System.exit();
        }
    }
}
```

### Incorporate into Your Own Project via Maven

To use this library in your own project, you can publish it to your local Maven repo. Then add it as a dependency in your `build.gradle` file:

1. Publish the library locally using Maven:
```sh
./gradlew publishToMavenLocal
```

2. Include the locally published library in another project:
```gradle
dependencies {
    implementation 'ninox360.remote:Surface-Inspector-Remote:1.0-SNAPSHOT'
}
```

## Contributing

Contributions are welcome! Please fork this repository, make your changes, and submit a pull request.

## License

This project is licensed under the Apache 2.0 License. See the [LICENSE](../LICENSE) file for more details.
