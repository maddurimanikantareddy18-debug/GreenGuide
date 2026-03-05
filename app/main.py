from fastapi import FastAPI, File, UploadFile
import random

app = FastAPI()

plants = [
    "Neem",
    "Tulsi",
    "Mango",
    "Rose",
    "Aloe Vera",
    "Banana",
    "Tomato",
    "Mint"
]

@app.get("/")
def home():
    return {"message": "GreenGuide backend running"}

@app.post("/detect-plant/")
async def detect_plant(file: UploadFile = File(...)):

    plant_name = model.predict(image)

    return {
        "plant_name": plant_name,
        "confidence": "90%",
        "care_advice": "Water every 12 hours and keep in sunlight"
    }