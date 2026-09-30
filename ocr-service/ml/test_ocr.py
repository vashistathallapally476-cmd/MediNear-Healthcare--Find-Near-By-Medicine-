import easyocr

# Initialize once (downloads model files the first time you run this)
reader = easyocr.Reader(['en'])

image_path = "ml/test_images/medicine1.png"  # change to your actual filename

results = reader.readtext(image_path)

print("\n--- Raw OCR Output ---")
all_text = []
for (bbox, text, confidence) in results:
    print(f"Text: {text}  | Confidence: {confidence:.2f}")
    all_text.append(text)

print("\n--- Combined Text ---")
print(" ".join(all_text))