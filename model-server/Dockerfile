FROM python:3.9-slim
WORKDIR /app
COPY . .
RUN pip install flask pillow opencv-python torch torchvision
CMD ["python", "server.py"]
