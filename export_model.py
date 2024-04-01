import torch
from model import esrt

model = esrt.ESRT(upscale = 3)
model.load_state_dict(torch.load(r"G:\ECE1508\project\ESRT\checkpoint\x3_1\epoch_20.pth"))
model.eval()
#model.to("cuda")

scripted_model = torch.jit.script(model)
# Save the scripted model
#scripted_model.save(r"G:\ECE1508\project\torch_onnx\model\123.pt")
torch.jit.save(scripted_model, r"G:\ECE1508\project\torch_onnx\model\1234.pt")
