import cv2
import numpy as np
import os
import torch
import torchvision.transforms as transforms
from ultralytics import YOLO
from torchvision import models
from scipy.spatial.distance import euclidean
from skimage.metrics import structural_similarity as ssim
from lpips import LPIPS  # Learned Perceptual Image Patch Similarity

# Load LPIPS model for perceptual similarity
lpips_model = LPIPS(net='vgg')

# Load YOLO (Medium Model)
model_yolo = YOLO("yolov8m.pt")

# Load EfficientNetV2 for Feature Extraction
model_efficientnet = models.efficientnet_v2_s(pretrained=True)
model_efficientnet = torch.nn.Sequential(*list(model_efficientnet.children())[:-1])
model_efficientnet.eval()

transform = transforms.Compose([
    transforms.ToPILImage(),
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225]),
])

def detect_objects(image, model):
    """Detects only shoes/slides in the image using YOLO and returns the cropped region."""
    results = model(image)
    for result in results:
        for box in result.boxes:
            if result.names[int(box.cls[0])] in ['sneaker', 'slipper', 'sandal']:
                x1, y1, x2, y2 = map(int, box.xyxy[0])
                cropped = image[y1:y2, x1:x2]
                return cropped, (x1, y1, x2, y2)
    return None, None

def extract_features(image):
    """Extracts feature embeddings from an image using EfficientNetV2."""
    image_tensor = transform(image).unsqueeze(0)
    with torch.no_grad():
        features = model_efficientnet(image_tensor)
    return features.squeeze().numpy().flatten()

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
best_score = -1  # Higher is better for similarity scoring
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
        # Euclidean Distance
        feature_distance = euclidean(video_features, image_features)
        
        # SSIM Calculation
        detected_gray = cv2.cvtColor(detected, cv2.COLOR_BGR2GRAY)
        image_gray = cv2.cvtColor(image[bbox_img[1]:bbox_img[3], bbox_img[0]:bbox_img[2]], cv2.COLOR_BGR2GRAY)
        ssim_score = ssim(detected_gray, image_gray)
        
        # LPIPS Calculation
        detected_tensor = transform(detected).unsqueeze(0)
        image_tensor = transform(image[bbox_img[1]:bbox_img[3], bbox_img[0]:bbox_img[2]]).unsqueeze(0)
        lpips_score = lpips_model(detected_tensor, image_tensor).item()
        
        # Final Score Calculation
        final_score = (1 - feature_distance / 100) * 50 + ssim_score * 30 + (1 - lpips_score) * 20
        
        if final_score > best_score:
            best_score = final_score
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
    print(f"Best Match Found with {best_match} | Similarity Score: {best_score:.2f}%")
    cv2.imshow("Best Matching Image", best_image)
    cv2.waitKey(0)
    cv2.destroyAllWindows()
else:
    print("No good match found.")
    