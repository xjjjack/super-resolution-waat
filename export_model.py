import torch
from model.esrt import ESRT  # Assuming 'esrt' is a module with the ESRT model definition
from model.bicubicpp import Bicubic_plus_plus

# Define the path where the model's checkpoint is stored
CHECKPOINT_PATH = r"G:\ECE1508\project\ESRT\checkpoint\x3_1\epoch_20.pth"
# Define the path where the scripted model will be saved
SCRIPTED_MODEL_PATH = r"G:\ECE1508\project\torch_onnx\model\1234.pt"

def load_and_script_model(checkpoint_path, scripted_model_path, model_type="esrt"):
    """
    This function loads a pre-trained ESRT model from a given checkpoint,
    scripts it using TorchScript, and saves the scripted model to a specified path.

    Parameters:
    - checkpoint_path (str): Path to the model's checkpoint file.
    - scripted_model_path (str): Path where the scripted model will be saved.

    Returns:
    None
    """
    # Initialize the model with the desired upscale factor
    if model_type == "esrt":
        model = ESRT(upscale=3)
    elif model_type == "bicubicpp":
        model = Bicubic_plus_plus(sr_rate=3)
    
    # Load the model's weights from the checkpoint
    model.load_state_dict(torch.load(checkpoint_path))
    
    # Set the model to evaluation mode
    model.eval()
    
    # Script the model using TorchScript
    scripted_model = torch.jit.script(model)
    
    # Save the scripted model to the specified path
    torch.jit.save(scripted_model, scripted_model_path)

# Call the function to load, script, and save the model
load_and_script_model(CHECKPOINT_PATH, SCRIPTED_MODEL_PATH)