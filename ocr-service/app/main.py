from pathlib import Path
from fastapi import FastAPI, File, UploadFile
from fastapi.middleware.cors import CORSMiddleware
import easyocr
import joblib
import shutil
import os
import uuid

BASE = Path(__file__).resolve().parent.parent   # ocr-service/
ML = BASE / "ml"
EASY = BASE / "easyocr_models"

app = FastAPI(title="MediNear OCR Service")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

reader = easyocr.Reader(['en'], gpu=False, model_storage_directory=str(EASY))
vectorizer = joblib.load(ML / "vectorizer.pkl")
nn = joblib.load(ML / "nn_model.pkl")
labels = joblib.load(ML / "labels.pkl")

CONFIDENCE_THRESHOLD = 0.4


def extract_medicine_text(image_path):
    results = reader.readtext(image_path)
    good_text = [text for (bbox, text, conf) in results if conf >= CONFIDENCE_THRESHOLD]
    return " ".join(good_text)


def predict_medicine(text, top_n=3):
    vec = vectorizer.transform([text])
    distances, indices = nn.kneighbors(vec, n_neighbors=15)
    seen = {}
    for dist, idx in zip(distances[0], indices[0]):
        name = labels[idx]
        similarity = 1 - dist
        if name not in seen or similarity > seen[name]:
            seen[name] = similarity
    ranked = sorted(seen.items(), key=lambda x: x[1], reverse=True)[:top_n]
    return ranked


@app.get("/")
def home():
    return {"message": "MediNear OCR service is running"}


@app.post("/api/v1/identify")
async def identify_medicine(file: UploadFile = File(...)):
    ext = os.path.splitext(file.filename or "")[1] or ".png"
    temp_filename = f"temp_{uuid.uuid4().hex}{ext}"
    with open(temp_filename, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)

    try:
        filtered_text = extract_medicine_text(temp_filename)
        if not filtered_text.strip():
            return {"ocr_text": "", "matches": []}

        matches = predict_medicine(filtered_text)
        result = [
            {"medicine_name": name, "confidence": round(float(score), 3)}
            for name, score in matches
        ]
        return {"ocr_text": filtered_text, "matches": result}
    finally:
        if os.path.exists(temp_filename):
            os.remove(temp_filename)