import pandas as pd
import re

RAW_PATH = r"C:\Users\yashe\.cache\kagglehub\datasets\apkaayush\india-medicines-and-drug-info-dataset\versions\1\India Medicines and Drug Info Dataset.csv"

df = pd.read_csv(RAW_PATH, engine="python", on_bad_lines="skip")

# Matches a capitalized word sitting right next to "(650mg)" style strength
pattern = re.compile(r'([A-Z][a-zA-Z]{2,25})\s*\(([\d.]+\s*(?:mg|mcg|ml|g|%)[^)]*)\)')

# Words that sometimes get mistaken for a drug name; skip these
NOISE_WORDS = {
    "Tablet", "Tablets", "Capsule", "Capsules", "Syrup", "Injection",
    "Ltd", "Pvt", "Healthcare", "Pharma", "Pharmaceuticals", "Biotech",
    "Remedies", "Life", "Sciences", "Labs", "Lab", "Formulations", "Care", "Drugs"
}

records = []
for _, row in df.iterrows():
    med_name = str(row.get("Medicine Name", "")).strip()
    # These two columns got run together with no space by the source data
    combined = str(row.get("Type of Medicine", "")) + str(row.get("Composition", ""))
    # Insert a space wherever lowercase is directly followed by uppercase
    combined_spaced = re.sub(r'(?<=[a-z])(?=[A-Z])', ' ', combined)

    matches = pattern.findall(combined_spaced)
    if not med_name or not matches:
        continue

    generic, strength = matches[0]
    if generic in NOISE_WORDS:
        continue

    records.append({
        "medicine_name": med_name,
        "generic_name": generic.strip(),
        "strength": strength.strip()
    })

clean_df = pd.DataFrame(records)
clean_df = clean_df.drop_duplicates(subset=["medicine_name"])

print("Clean rows extracted:", len(clean_df))
print(clean_df.head(10))

sample = clean_df.sample(n=min(6000, len(clean_df)), random_state=42)
sample.to_csv("ml/dataset/medicines_clean.csv", index=False)
print("Saved ml/dataset/medicines_clean.csv with", len(sample), "rows")