import torch
from model.esrt import ESRT

model = ESRT(upscale=3)
model.load_state_dict(torch.load(r"G:\ECE1508\project\ESRT\checkpoint\3x\epoch_1000.pth"))
model.eval()

dummy_input = torch.randn(1, 3, 96, 96)
torch.onnx.export(
    model,
    dummy_input,
    r"G:\ECE1508\project\torch_onnx\model\model.onnx",
    verbose=True,
    opset_version = 18,
    input_names=['input'],   # the model's input names
    output_names=['output'],  # the model's output names
    dynamic_axes={'input' : {0 : 'batch_size'},  
                  'output' : {0 : 'batch_size'}}
    )
