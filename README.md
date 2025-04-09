# FireWatch-Java

FireWatch-Java is a cross-language real-time fire detection system combining a Java-based desktop application with a PyTorch-based backend service.

## Architecture
- `java-client/`: A Java Swing GUI that streams video, performs detection via REST API, and sends alerts
- `model-server/`: A Python Flask service that loads a YOLOv8 model to detect fire in images

## Features
- Real-time fire detection using PyTorch
- Java GUI video streaming with email alerts
- Dockerized backend with REST API

## Setup Instructions
1. Train or download a YOLOv8 fire detection model (`model.pt`)
2. Run the model server:
   ```bash
   cd model-server
   docker build -t firewatch-server .
   docker run -p 5000:5000 firewatch-server
   ```
3. Run the Java client:
   ```bash
   cd java-client/src
   javac FireWatchClient.java
   java FireWatchClient
   ```

## License
MIT