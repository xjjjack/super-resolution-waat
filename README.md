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

# Deployment
* Language: Kotlin, JAVA

* Platform: Android

* Description: an Android application to deploy the model by using the .pt file.
*     For the ESRT model it could smoothly handle the image that size < 200*200 and compare the two images in the interface.
*     The bicubic++ and the FASRNet could run all the images have lower resolution than 720p on the edge devices 

### Example:


<img width="370" alt="image" src="https://github.com/xjjjack/super-resolution-waat/assets/44899736/4af6aee8-83b6-42f6-bdd4-b117ceeb0ecd">

