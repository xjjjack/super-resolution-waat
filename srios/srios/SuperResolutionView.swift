import SwiftUI
import UIKit

struct SuperResolutionView: View {
    var image: UIImage
    var directory: URL

    @State private var dragOffset: CGFloat = UIScreen.main.bounds.width / 2 // Initial position of the drag bar in the middle
    @State private var selectedFilter: String = "None" // Track the selected filter

    let filters = [
        ("None", "Original"),
        ("CIPhotoEffectMono", "Mono"),
        ("CIPhotoEffectNoir", "Noir"),
        ("CIPhotoEffectFade", "Fade"),
        ("CIPhotoEffectChrome", "Chrome"),
        ("CIPhotoEffectInstant", "Instant"),
        ("CIPhotoEffectTransfer", "Transfer"),
        ("CIPhotoEffectProcess", "Process")
    ]

    var filteredImage: UIImage {
        if selectedFilter == "None" {
            return image
        }
        return applyFilter(to: image, filterName: selectedFilter)
    }

    var body: some View {
        GeometryReader { geometry in
            let imageSize = self.calculateImageSize(for: image, in: geometry.size)
            let imageFrame = CGRect(x: (geometry.size.width - imageSize.width) / 2,
                                    y: (geometry.size.height - imageSize.height) / 2,
                                    width: imageSize.width,
                                    height: imageSize.height)
            
            ZStack {
                VStack {
                    ZStack {

                        // Original image
                        Image(uiImage: image)
                            .resizable()
                            .scaledToFit()
                            .frame(width: imageSize.width, height: imageSize.height)
                            .mask(
                                HStack {
                                    Rectangle()
                                        .frame(width: dragOffset - imageFrame.minX)
                                    Spacer()
                                }
                            )
                            .position(x: geometry.size.width / 2, y: geometry.size.height / 2)

                        // Filtered image
                        Image(uiImage: filteredImage)
                            .resizable()
                            .scaledToFit()
                            .frame(width: imageSize.width, height: imageSize.height)
                            .mask(
                                HStack {
                                    Spacer()
                                    Rectangle()
                                        .frame(width: imageFrame.maxX - dragOffset)
                                }
                            )
                            .position(x: geometry.size.width / 2, y: geometry.size.height / 2)

                        // Draggable bar
                        RoundedRectangle(cornerRadius: 10)
                            .fill(LinearGradient(
                                gradient: Gradient(colors: [Color.white.opacity(0.9), Color.gray.opacity(0.9)]),
                                startPoint: .top,
                                endPoint: .bottom
                            ))
                            .frame(width: 4, height: imageSize.height+2)
                            .shadow(radius: 1)
                            .position(x: dragOffset, y: geometry.size.height / 2)
                            .gesture(
                                DragGesture()
                                    .onChanged { value in
                                        self.dragOffset = max(imageFrame.minX, min(value.location.x, imageFrame.maxX))
                                    }
                            )
                    }
                    .frame(height: geometry.size.height - 150) // Leave space for the scroll bar
                    .clipped()

                    // Filter selection
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack {
                            ForEach(filters, id: \.0) { filter in
                                FilterView(image: image, filterName: filter.0, displayName: filter.1)
                                    .onTapGesture {
                                        selectedFilter = filter.0
                                    }
                                    .padding(.horizontal, 5)
                            }
                        }
                        .padding(.horizontal)
                    }
                    .background(Color.black)
                    .frame(height: 150) // Adjust the height as needed
                }
                
                VStack {
                    Spacer()
                    HStack {
                        Spacer()
                            .padding(.trailing)
                            .background(Color.black.opacity(0.5))
                            .foregroundColor(.white)
                    }
                }
            }
            .frame(width: geometry.size.width, height: geometry.size.height) // Ensure the ZStack takes full height
        }
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                // Button("Process") {
                //     // Future functionality to process the image
                // }
            }
        }
        .ignoresSafeArea() // Ignore safe areas to make the images and draggable bar cover the entire screen
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

    private func calculateImageSize(for image: UIImage, in size: CGSize) -> CGSize {
        let aspectRatio = image.size.width / image.size.height
        if size.width / size.height > aspectRatio {
            return CGSize(width: size.height * aspectRatio, height: size.height)
        } else {
            return CGSize(width: size.width, height: size.width / aspectRatio)
        }
    }
}

struct SuperResolutionView_Previews: PreviewProvider {
    static var previews: some View {
        SuperResolutionView(image: UIImage(systemName: "photo")!, directory: URL(fileURLWithPath: ""))
    }
}
