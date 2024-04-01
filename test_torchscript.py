import torch
import cv2
import math
import numpy as np
from skimage.metrics import peak_signal_noise_ratio as psnr
from skimage.metrics import structural_similarity as ssim

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

def preprocess_image_cv(image_path):
    # Read the image using OpenCV
    image = cv2.imdecode(np.fromfile(image_path, dtype=np.uint8), cv2.IMREAD_UNCHANGED)
    image_size = image.shape
    height, width = image.shape[:2]
    slices = []

    # Calculate the number of slices in each dimension.
    x_slices = math.ceil(width / 96)
    y_slices = math.ceil(height / 96)

    for i in range(y_slices):
        for j in range(x_slices):
            left = j * 96
            top = i * 96
            right = min((j + 1) * 96, width)
            bottom = min((i + 1) * 96, height)
            slice = image[top:bottom, left:right]
            slices.append(torch.from_numpy(np.transpose(slice, (2, 0, 1))).float() / 255.0)

    return slices, image_size, image

def postprocess_images_cv(slices, original_size, upscale_factor):
    upscaled_width = int(original_size[0] * upscale_factor)
    upscaled_height = int(original_size[1] * upscale_factor)
    
    # Determine number of channels
    channels = slices[0].shape[2] if len(slices[0].shape) > 2 else 1
    if channels > 1:
        upscaled_image = np.zeros((upscaled_height, upscaled_width, channels), dtype=np.uint8)
    else:
        upscaled_image = np.zeros((upscaled_height, upscaled_width), dtype=np.uint8)
    
    x_slices = math.ceil(upscaled_width / (96 * upscale_factor))
    y_slices = math.ceil(upscaled_height / (96 * upscale_factor))
    
    current_slice = 0
    for i in range(y_slices):
        for j in range(x_slices):
            if current_slice < len(slices):
                top = i * 96 * upscale_factor
                left = j * 96 * upscale_factor
                slice = slices[current_slice]
                bottom = top + slice.shape[0]
                right = left + slice.shape[1]
                
                if channels > 1:
                    upscaled_image[top:bottom, left:right, :] = slice
                else:
                    upscaled_image[top:bottom, left:right] = slice
                
                current_slice += 1

    return upscaled_image

def compute_psnr(im1, im2):
    p = psnr(im1, im2)
    return p

def compute_ssim(im1, im2):
    s = ssim(im1, im2, channel_axis=2)
    return s

# Path to your scripted model file
model_path = r'g:\ECE1508\project\torch_onnx\model\1234.pt'

# Load the scripted model
model = torch.jit.load(model_path)

# Make sure to set the model to evaluation mode
model.to(device)
model.eval()

with torch.no_grad():
    torch.cuda.empty_cache()
    list_save = list()
    #lr_image = cv2.imdecode(np.fromfile(r"c:\Users\Simonzhang\Desktop\Set5\image_SRF_2\img_001_SRF_2_LR.png",dtype=np.uint8), cv2.IMREAD_UNCHANGED)
    #lr_image_i = torch.from_numpy(np.transpose(lr_image, (2, 0, 1))).float() / 255.0
    hr_image = cv2.imdecode(np.fromfile(r"c:\Users\Simonzhang\Desktop\Set5\image_SRF_3\img_003_SRF_3_HR.png",dtype=np.uint8), cv2.IMREAD_UNCHANGED)
    sub_image_list, original_size, lr_image = preprocess_image_cv(r"c:\Users\Simonzhang\Desktop\Set5\image_SRF_3\img_003_SRF_3_LR.png")
    for lr_image_i in sub_image_list:
        lr_image_i = lr_image_i.unsqueeze(0).to(device)
        list_save.append((np.transpose(model(lr_image_i).squeeze(0).cpu().numpy(), (1, 2, 0)) * 255.0).round().astype(np.uint8))

    sr_image = postprocess_images_cv(list_save, original_size, 3)

    #sr_image = (np.transpose(sr_image.squeeze(0).cpu().numpy(), (1, 2, 0)) * 255.0).round().astype(np.uint8)

    print(compute_psnr(sr_image, hr_image))
    print(compute_ssim(sr_image, hr_image))

    cv2.imshow("sr_image", sr_image)
    cv2.imshow("lr_image", lr_image)
    cv2.waitKey(0)

    #model = Bicubic_plus_plus(sr_rate=args.scale).to(device)