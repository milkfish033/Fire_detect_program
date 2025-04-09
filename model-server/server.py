from flask import Flask, request, jsonify, Response
from PIL import Image
import base64
import io
import torch
import cv2
import numpy as np

app = Flask(__name__)

# Load YOLOv8 fire detection model
model = torch.hub.load('ultralytics/yolov5', 'custom', path='model.pt')

@app.route('/predict', methods=['POST'])
def predict():
    data = request.get_json()
    img_data = base64.b64decode(data['image'])
    img = Image.open(io.BytesIO(img_data)).convert('RGB')
    results = model(img, size=640)
    fire_detected = any(result['name'] == 'fire' for result in results.pandas().xyxy[0].to_dict('records'))
    return jsonify({'fire': fire_detected})

@app.route('/stream')
def stream():
    def generate():
        cap = cv2.VideoCapture(0)  # Replace with RTSP stream URL if needed
        while True:
            success, frame = cap.read()
            if not success:
                break
            _, buffer = cv2.imencode('.jpg', frame)
            jpg_as_text = buffer.tobytes()
            yield (b'--frame\r\n'
                   b'Content-Type: image/jpeg\r\n\r\n' + jpg_as_text + b'\r\n')
    return Response(generate(), mimetype='multipart/x-mixed-replace; boundary=frame')

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0')