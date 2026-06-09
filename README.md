# Binary Skin Cancer Classification with Fine-Tuning

**Machine Learning 2 --- Final Project | Universidad Panamericana**

A comparative study of three deep learning architectures for binary classification of dermoscopic skin lesion images (malignant vs. benign), using the ISIC 2024 SLICE-3D Permissive dataset.

---

## Problem

Skin cancer is one of the most prevalent cancers worldwide. Early detection through automated dermoscopic image analysis can significantly improve patient outcomes. The main challenge is the **extreme class imbalance**: only ~0.13% of images are malignant.

## Models Compared

| Model            | Type               | Parameters | Test AUC-ROC |
| ---------------- | ------------------ | ---------- | ------------ |
| ResNet-50        | Residual CNN       | 25.6 M     | ~0.76        |
| EfficientNetV2-S | NAS-designed CNN   | 21.5 M     | ~0.87        |
| ViT-B/16         | Vision Transformer | 86.6 M     | ~0.84        |

All models are **fine-tuned** from ImageNet-pretrained weights with differential learning rates.

## Dataset

- **Source:** [ISIC 2024 --- SLICE-3D Permissive](https://challenge.isic-archive.com/) (CC-BY 4.0)
- **Total images:** ~217,000 dermoscopic skin lesion crops
- **Class distribution:** 99.87% benign / 0.13% malignant (294 malignant samples)

## Class Imbalance Strategy

1. **Full dataset training** --- Use all 217K images
2. **WeightedRandomSampler** --- Balanced mini-batches (~50/50)
3. **Cost-sensitive loss** --- `BCEWithLogitsLoss(pos_weight=~49)`

## Key Technical Details

- **Mixed precision training (AMP)** --- FP16 for memory efficiency on 8 GB VRAM
- **Gradient clipping** (`max_norm=1.0`) --- Prevents NaN from high pos_weight + FP16
- **NaN protection** --- `torch.nan_to_num()` on logits during evaluation
- **Differential learning rates** --- Backbone: 1e-4, Head: 1e-3

## Repository Structure

```
.
├── 03_final_complete.ipynb   # Complete project notebook (EDA + Training + Evaluation)
├── requirements.txt          # Python dependencies
├── README.md

```

## How to Run

### 1. Clone the repository

```bash
git clone https://github.com/GerardoMaciasRomo/skin-cancer-classification-isic2024.git
cd skin-cancer-classification-isic2024
```

### 2. Create a conda environment

```bash
conda create -n melanoma_env python=3.10 -y
conda activate melanoma_env
pip install -r requirements.txt
```

> **Note:** For GPU support, install PyTorch with CUDA from [pytorch.org](https://pytorch.org/get-started/locally/)

### 3. Download the dataset

Download from the [ISIC 2024 Challenge](https://challenge.isic-archive.com/):

- Training Images (SLICE-3D Permissive)
- Training Ground Truth

Place files in `data/`:

```
data/
├── ISIC_2024_Permissive_Training_GroundTruth.csv
└── ISIC_2024_Permissive_Training_Input/
    └── ISIC_2024_Permissive_Training_Input/
        ├── ISIC_0000000.jpg
        ├── ISIC_0000001.jpg
        └── ...
```

### 4. Run the notebook

```bash
jupyter notebook 03_final_complete.ipynb
```

Select the `melanoma_env` kernel and run all cells. Training takes ~28 hours on an RTX 3070.

## Environment

| Component | Version                            |
| --------- | ---------------------------------- |
| Python    | 3.10                               |
| PyTorch   | 2.7.1 + CUDA 11.8                  |
| GPU       | NVIDIA RTX 3070 Laptop (8 GB VRAM) |

## Author

**Gerardo Macias Romo**
Universidad Panamericana --- Machine Learning 2, 8th Semester
