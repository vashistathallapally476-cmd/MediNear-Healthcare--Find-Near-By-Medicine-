import easyocr
import joblib
import pandas as pd

reader = easyocr.Reader(['en'])
vectorizer = joblib.load("ml/vectorizer.pkl")
nn = joblib.load("ml/nn_model.pkl")
labels = joblib.load("ml/labels.pkl")

df_main = pd.read_csv("ml/dataset/medicines_clean.csv")
df_common = pd.read_csv("ml/dataset/common_medicines.csv")
df = pd.concat([df_main, df_common], ignore_index=True).drop_duplicates(subset=["medicine_name"])

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

image_path = "ml/test_images/medicine1.png"

filtered_text = extract_medicine_text(image_path)
print("Filtered OCR text:", filtered_text)

print("\nTop medicine matches:")
for name, score in predict_medicine(filtered_text):
    row = df[df["medicine_name"] == name].iloc[0]
    print(f"  {name} | {row['generic_name']} {row['strength']} | score: {score:.3f}")