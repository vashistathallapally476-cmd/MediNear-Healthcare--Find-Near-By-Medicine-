import random
import pandas as pd
import joblib
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.neighbors import NearestNeighbors

df_main = pd.read_csv("ml/dataset/medicines_clean.csv")
df_common = pd.read_csv("ml/dataset/common_medicines.csv")
df = pd.concat([df_main, df_common], ignore_index=True).drop_duplicates(subset=["medicine_name"])

SWAPS = {"o": "0", "0": "o", "l": "1", "1": "l", "i": "1", "s": "5"}

def add_noise(text):
    chars = list(text)
    for i, c in enumerate(chars):
        if random.random() < 0.15 and c.lower() in SWAPS:
            chars[i] = SWAPS[c.lower()]
    noisy = "".join(chars)
    r = random.random()
    if r < 0.15:
        noisy = noisy.replace(" ", "")
    elif r < 0.3:
        noisy = noisy.replace(" ", "-")
    elif r < 0.4:
        noisy = noisy.upper()
    elif r < 0.5:
        noisy = noisy.lower()
    if random.random() < 0.25:
        cut = random.randint(max(4, len(noisy) - 8), len(noisy))
        noisy = noisy[:cut]
    return noisy

# Build a small set of noisy variants per medicine (for the vectorizer's vocabulary)
texts, labels = [], []
for name in df["medicine_name"]:
    texts.append(name)
    labels.append(name)
    for _ in range(5):
        texts.append(add_noise(name))
        labels.append(name)

vectorizer = TfidfVectorizer(analyzer="char_wb", ngram_range=(2, 4), lowercase=True, max_features=20000)
X = vectorizer.fit_transform(texts)

nn = NearestNeighbors(n_neighbors=5, metric="cosine")
nn.fit(X)

joblib.dump(vectorizer, "ml/vectorizer.pkl")
joblib.dump(nn, "ml/nn_model.pkl")
joblib.dump(labels, "ml/labels.pkl")

print(f"Trained on {len(df)} medicines, {len(texts)} total examples.")
print("Model saved.")