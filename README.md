## Super Resolution on Edge device

Models we compared:

    Bicubic++
    ESRT
    FASRNet(ours)
    
# ESRT
Efficient Transformer for Single Image
Super-Resolution

## Model
<p align="center">
    <img src="figs/esrt.png" width="960"> <br />
    <em> The overall architecture of the proposed Efficient SR Transformer (ESRT). </em>
</p>
<p align="center">
    <img src="figs/EMHA.png" width="960"> <br />
    <em> Efficient Transformer and Efficient Multi-Head Attention. </em>
</p>

# Trian
    train.py --batch_size=32 --patch_size=96
    train.py --batch_size=32 --patch_size=144
    train.py --batch_size=32 --patch_size=196

# Deployment
* Language: Kotlin, JAVA

* Platform: Android

* Description: an Android application to deploy the model by using the .pt file.

### Example:

<img width="370" alt="image" src="https://github.com/xjjjack/super-resolution-waat/assets/44899736/4af6aee8-83b6-42f6-bdd4-b117ceeb0ecd">

## Comparison

<img width="960" alt="image" src="figs/compare.png">
<img width="960" alt="image" src="figs/datacompare.png">
