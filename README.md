# SafePay
Carefree Payments , Zero Risk
# SafePay - Escrow Payment App

## Overview
SafePay is a secure escrow payment app that ensures safe transactions between buyers and sellers. It verifies delivered products using AI-powered video analysis and holds payments until product authenticity is confirmed.

## Features
- Secure escrow payments
- Order details fetching from e-commerce platforms
- CameraX-based video recording for product verification
- AI-based product authenticity check
- Automated refunds in case of non-delivery or mismatches

## Tech Stack
### Frontend (Android)
- Android Studio (Kotlin)
- CameraX for video capture
- Retrofit for API calls
- Jetpack Compose UI

### Backend
- FastAPI (Python) / Node.js (Optional)
- PostgreSQL / Firebase (Database)
- Cloud Storage for video uploads

### AI (Computer Vision)
- OpenCV for image feature matching
- TensorFlow/CLIP for advanced product verification

## Installation
### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/SafePay.git
cd SafePay
```

### 2. Android App Setup
- Open `android-app/` in Android Studio
- Sync Gradle and install dependencies
- Run the app on an emulator or a physical device

### 3. Backend Setup
- Install Python dependencies:
```bash
pip install -r backend/requirements.txt
```
- Run the FastAPI server:
```bash
uvicorn backend.api.main:app --reload
```
- Access API at `http://localhost:8000`

## API Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST   | /order   | Create an order |
| POST   | /upload_video | Upload a product verification video |
| GET    | /verify  | Verify product authenticity |
| POST   | /refund  | Initiate a refund |

## Contribution Guidelines
1. Create a new branch for features:
```bash
git checkout -b feature-name
```
2. Commit and push changes:
```bash
git add .
git commit -m "Added feature-name"
git push origin feature-name
```
3. Open a Pull Request for review.

## Deployment
- Deploy backend on AWS/GCP/Vercel
- Integrate Firebase for real-time database (optional)

## Contact
For any queries, contact `your-email@example.com`.

---
Let's build SafePay together! 🚀

