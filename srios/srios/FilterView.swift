import SwiftUI

struct FilterView: View {
    var image: UIImage
    var filterName: String
    var displayName: String
    
    var body: some View {
        VStack {
            Image(uiImage: applyFilter(to: image, filterName: filterName))
                .resizable()
                .scaledToFill()
                .frame(width: 100, height: 100)
                .clipShape(RoundedRectangle(cornerRadius: 10))
            Text(displayName)
                .font(.caption)
                .foregroundColor(.white)
        }
        .padding(5)
        .background(Color.black.opacity(0.7))
        .clipShape(RoundedRectangle(cornerRadius: 15))
    }

    private func applyFilter(to inputImage: UIImage, filterName: String) -> UIImage {
        guard let ciImage = CIImage(image: inputImage) else { return inputImage }
        
        let filter = CIFilter(name: filterName)
        filter?.setValue(ciImage, forKey: kCIInputImageKey)
        
        guard let outputCIImage = filter?.outputImage else { return inputImage }
        
        let context = CIContext()
        guard let cgImage = context.createCGImage(outputCIImage, from: outputCIImage.extent) else { return inputImage }
        
        return UIImage(cgImage: cgImage, scale: inputImage.scale, orientation: inputImage.imageOrientation)
    }
}

struct FilterView_Previews: PreviewProvider {
    static var previews: some View {
        FilterView(image: UIImage(systemName: "photo")!, filterName: "CIPhotoEffectMono", displayName: "Mono")
    }
}
