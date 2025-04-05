import os
import cv2
import numpy as np
import torch
from torchvision import transforms
from transformers import SwinModel, SwinConfig, AutoImageProcessor
from ultralytics import YOLO
from skimage.metrics import structural_similarity as ssim
from lpips import LPIPS
from scipy.spatial.distance import cosine

# --- Model Setup ---
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

# Load YOLOv8m
model_yolo = YOLO("yolov8m.pt")

# Load Swin Transformer
processor = AutoImageProcessor.from_pretrained("microsoft/swin-tiny-patch4-window7-224")
model_swin = SwinModel.from_pretrained("microsoft/swin-tiny-patch4-window7-224").to(device)
model_swin.eval()

# Load LPIPS
lpips_model = LPIPS(net='alex').to(device)

# --- Function Definitions ---
def detect_product(image):
    """Detects object using YOLO and returns the cropped product image and bbox."""
    results = model_yolo(image)
    for r in results:
        for box in r.boxes:
            x1, y1, x2, y2 = map(int, box.xyxy[0])
            cropped = image[y1:y2, x1:x2]
            return cropped, (x1, y1, x2, y2)
    return None, None

def extract_swin_features(image):
    inputs = processor(images=image, return_tensors="pt").to(device)
    with torch.no_grad():
        outputs = model_swin(**inputs)
    return outputs.last_hidden_state.mean(dim=1).squeeze().cpu().numpy()

def compute_lpips(im1, im2):
    t1 = transforms.ToTensor()(cv2.resize(im1, (224, 224))).unsqueeze(0).to(device)
    t2 = transforms.ToTensor()(cv2.resize(im2, (224, 224))).unsqueeze(0).to(device)
    return lpips_model(t1, t2).item()

def compute_ssim(im1, im2):
    im1_gray = cv2.cvtColor(im1, cv2.COLOR_BGR2GRAY)
    im2_gray = cv2.cvtColor(im2, cv2.COLOR_BGR2GRAY)
    im1_gray = cv2.resize(im1_gray, (224, 224))
    im2_gray = cv2.resize(im2_gray, (224, 224))
    return ssim(im1_gray, im2_gray)

# --- Load Reference Images ---
image_folder = "images"
reference_data = {}

for filename in os.listdir(image_folder):
    if filename.endswith(".jpg") or filename.endswith(".png"):
        path = os.path.join(image_folder, filename)
        image = cv2.imread(path)
        detected, bbox = detect_product(image)
        if detected is not None:
            features = extract_swin_features(detected)
            reference_data[filename] = (features, detected, bbox, image)
        else:
            print(f"⚠️ No object detected in {filename}")

# --- Process Video Frames ---
video_path = "video.mp4"
cap = cv2.VideoCapture(video_path)

best_match = None
best_score = -1
best_image = None

while cap.isOpened():
    ret, frame = cap.read()
    if not ret:
        break

    detected, bbox = detect_product(frame)
    if detected is None:
        continue

    query_features = extract_swin_features(detected)

    for filename, (ref_features, ref_crop, ref_bbox, ref_full) in reference_data.items():
        cos_sim = 1 - cosine(query_features, ref_features)
        ssim_val = compute_ssim(detected, ref_crop)
        lpips_val = 1 - compute_lpips(detected, ref_crop)  # 1 - distance = similarity

        combined_score = 0.4 * cos_sim + 0.3 * ssim_val + 0.3 * lpips_val

        if combined_score > best_score:
            best_score = combined_score
            best_match = filename
            best_image = ref_full.copy()
            cv2.rectangle(frame, (bbox[0], bbox[1]), (bbox[2], bbox[3]), (0, 255, 0), 2)
            cv2.rectangle(best_image, (ref_bbox[0], ref_bbox[1]), (ref_bbox[2], ref_bbox[3]), (0, 0, 255), 2)

    cv2.imshow("Video Frame", frame)
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

cap.release()
cv2.destroyAllWindows()

if best_match:
    print(f"✅ Best Match Found: {best_match} | Similarity Score: {best_score * 100:.2f}%")
    cv2.imshow("Best Matching Image", best_image)
    cv2.waitKey(0)
    cv2.destroyAllWindows()
else:
    print("❌ No good match found.")
