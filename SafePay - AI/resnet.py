import cv2
import numpy as np
import os
import torch
import torchvision.transforms as transforms
from ultralytics import YOLO
from torchvision import models
from scipy.spatial.distance import cosine

def detect_objects(image, model):
    """Detects the shoe/slides in the image using YOLO and returns the cropped region."""
    results = model(image)
    for result in results:
        for box in result.boxes:
            x1, y1, x2, y2 = map(int, box.xyxy[0])
            cropped = image[y1:y2, x1:x2]
            return cropped, (x1, y1, x2, y2)  # Return cropped object and bounding box
    return None, None

# Load Pretrained Deep Learning Model for Feature Extraction
model_resnet = models.resnet50(pretrained=True)
model_resnet = torch.nn.Sequential(*list(model_resnet.children())[:-1])  # Remove classification layer
model_resnet.eval()

transform = transforms.Compose([
    transforms.ToPILImage(),
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225]),
])

def extract_features(image):
    """Extracts feature embeddings from an image using ResNet-50."""
    image_tensor = transform(image).unsqueeze(0)  # Add batch dimension
    with torch.no_grad():
        features = model_resnet(image_tensor)
    return features.squeeze().numpy().flatten()

# Load YOLO model
model_yolo = YOLO("yolov8n.pt")

# Process reference images
image_folder = "images"
image_files = [f for f in os.listdir(image_folder) if f.endswith(".jpg") or f.endswith(".png")]
reference_images = {}

for image_file in image_files:
    image_path = os.path.join(image_folder, image_file)
    image = cv2.imread(image_path)
    detected, bbox = detect_objects(image, model_yolo)
    if detected is not None:
        features = extract_features(detected)
        reference_images[image_file] = (features, bbox, image)
    else:
        print(f"No product detected in {image_file}")

# Process video
video_path = "video.mp4"
cap = cv2.VideoCapture(video_path)

best_match = None
best_score = float('inf')  # Lower is better for cosine similarity
best_image = None

while cap.isOpened():
    ret, frame = cap.read()
    if not ret:
        break
    
    detected, bbox = detect_objects(frame, model_yolo)
    if detected is None:
        continue
    
    video_features = extract_features(detected)
    
    for image_file, (image_features, bbox_img, image) in reference_images.items():
        similarity = cosine(video_features, image_features)  # Lower means more similar
        
        if similarity < best_score:
            best_score = similarity
            best_match = image_file
            best_image = image.copy()
            
            # Draw bounding boxes
            cv2.rectangle(frame, (bbox[0], bbox[1]), (bbox[2], bbox[3]), (0, 255, 0), 2)
            cv2.rectangle(best_image, (bbox_img[0], bbox_img[1]), (bbox_img[2], bbox_img[3]), (0, 0, 255), 2)

    # Display the current frame with bounding box
    cv2.imshow("Video Frame", frame)
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

cap.release()
cv2.destroyAllWindows()

if best_match:
    similarity_percentage = (1 - best_score) * 100  # Convert to percentage
    print(f"Best Match Found with {best_match} | Similarity Score: {similarity_percentage:.2f}%")
    cv2.imshow("Best Matching Image", best_image)
    cv2.waitKey(0)
    cv2.destroyAllWindows()
else:
    print("No good match found.")
