# Skin Cancer Classification - ISIC 2024

This project uses deep learning (CNN) to classify skin lesions as benign or malignant using the ISIC 2024 dataset.

## 📌 Problem

Skin cancer detection is critical for early diagnosis. This project aims to build a model that can classify skin lesions automatically.

## 📊 Dataset

- ISIC 2024 Challenge Dataset
- Over 200,000 images
- Highly imbalanced dataset (~738:1 ratio)

## ⚠️ Challenge

The dataset is extremely imbalanced, which can lead to biased models.

## 🧠 Approach

- Data preprocessing and augmentation
- Convolutional Neural Network (CNN)
- Class weighting to handle imbalance

## 🚀 Results

- Baseline model accuracy: XX%
- Focus on improving recall for malignant cases

## 🛠️ Technologies

- Python
- PyTorch
- NumPy
- Pandas

## ▶️ How to Run

1. Clone the repo:

```bash
git clone https://github.com/TU_USUARIO/skin-cancer-classification-isic2024.git
```

2. Install dependencies:

```bash
pip install -r requirements.txt
```

3. Download the dataset from ISIC 2024 (SLICE-3D Permissive)
   - TRAINING IMAGES AND INPUT ATTRIBUTES
   - TRAINING SUPPLEMENT
   - TRAINING GROUND TRUTH

4. Place all in a folder named: data/

5. Run the notebook

📌 Author

Gerardo Macías Romo
