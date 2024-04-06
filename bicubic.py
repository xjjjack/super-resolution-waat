from PIL import Image

# Load the image
input_image_path = r'c:\Users\Simonzhang\Desktop\image\star\star.png'
img = Image.open(input_image_path)

# Calculate the new size
new_width = img.width * 3
new_height = img.height * 3

# Resize the image using bicubic interpolation
resized_img = img.resize((new_width, new_height), Image.BICUBIC)

# Save the resized image
output_image_path = r'c:\Users\Simonzhang\Desktop\image\star\bqstar.png'
resized_img.save(output_image_path)

print("Image resized successfully!")