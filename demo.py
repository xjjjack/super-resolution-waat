import torch
import cv2
import numpy as np
import os

def load_image(image_path):
    """
    Load an image from disk and convert to a format suitable for the model.
    This function ensures the image has three channels and is in the correct format.
    """
    # Attempt to read the image from file
    image_data = np.fromfile(image_path, dtype=np.uint8)
    image = cv2.imdecode(image_data, cv2.IMREAD_UNCHANGED)
    
    if image is None:
        raise IOError(f"Failed to read image from {image_path}")

    # Convert grayscale images to RGB
    if len(image.shape) == 2:
        image = cv2.cvtColor(image, cv2.COLOR_GRAY2RGB)
    # Ensure the image has three channels (discard alpha channel if present)
    elif image.shape[2] == 4:
        image = image[:, :, :3]

    return image

def super_resolve_image(model, lr_image, device):
    """
    Perform super-resolution on a low-resolution input image.
    """
    # Convert image to tensor, normalize, and move to the correct device
    lr_image_tensor = torch.from_numpy(np.transpose(lr_image, (2, 0, 1))).float().unsqueeze(0).to(device) / 255.0
    
    # Perform inference
    with torch.no_grad():
        sr_image_tensor = model(lr_image_tensor)
    
    # Convert back to a numpy array
    sr_image = (np.transpose(sr_image_tensor.squeeze(0).cpu().numpy(), (1, 2, 0)) * 255.0).astype(np.uint8)
    
    return sr_image

def main():
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

    model_path = r'g:\ECE1508\project\torch_onnx\model\bicubicpp.pt'
    image_path = r"c:\Users\Simonzhang\Desktop\image\star\star.png"
    
    # Ensure the model file exists
    if not os.path.exists(model_path):
        raise FileNotFoundError(f"Model file not found: {model_path}")
    
    # Load the model
    model = torch.jit.load(model_path).to(device).eval()
    
    # Load and prepare the image
    lr_image = load_image(image_path)
    
    # Perform super-resolution
    sr_image = super_resolve_image(model, lr_image, device)
    
    # Save and display the results
    cv2.imshow("Low Resolution Image", lr_image)
    cv2.imshow("Super Resolved Image", sr_image)
    cv2.waitKey(0)
    cv2.destroyAllWindows()

if __name__ == "__main__":
    main()
