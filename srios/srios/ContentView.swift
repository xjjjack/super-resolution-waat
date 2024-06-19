import SwiftUI
import PhotosUI

struct ContentView: View {
    @Environment(\.managedObjectContext) private var viewContext

    @State private var items: [UIImage] = []
    @State private var imageUrls: [URL] = []
    @State private var showingImagePicker = false
    @State private var isEditing = false
    @State private var navigateToSuperResolutionView = false
    @State private var selectedImage: UIImage?
    @State private var selectedDirectory: URL?

    let gridItemWidth: CGFloat = 150

    var body: some View {
        NavigationView {
            GeometryReader { geometry in
                ScrollView {
                    let columns = [GridItem](repeating: .init(.fixed(gridItemWidth), spacing: 20), count: self.numberOfColumns(for: geometry.size.width))
                    
                    LazyVGrid(columns: columns, spacing: 20) {
                        Button(action: {
                            showingImagePicker = true
                        }) {
                            VStack {
                                Image(systemName: "plus")
                                    .resizable()
                                    .scaledToFit()
                                    .padding(30)
                                    .foregroundColor(.gray)
                                Text("Add Photo")
                                    .foregroundColor(.gray)
                            }
                            .frame(width: gridItemWidth, height: gridItemWidth)
                            .background(Color.white)
                            .cornerRadius(10)
                        }

                        ForEach(items.indices, id: \.self) { index in
                            if isEditing {
                                Button(action: {
                                    let urlToDelete = imageUrls[index]
                                    items.remove(at: index)
                                    imageUrls.remove(at: index)
                                    ImageStorageManager.shared.deleteImages(from: urlToDelete)
                                }) {
                                    Image(uiImage: items[index])
                                        .resizable()
                                        .scaledToFill()
                                        .frame(width: gridItemWidth, height: gridItemWidth)
                                        .clipped()
                                        .cornerRadius(10)
                                        .overlay(
                                            Image(systemName: "minus.circle.fill")
                                                .foregroundColor(.red)
                                                .padding(5),
                                            alignment: .topTrailing
                                        )
                                }
                            } else {
                                NavigationLink(
                                    destination: SuperResolutionView(image: items[index], directory: imageUrls[index])
                                ) {
                                    Image(uiImage: items[index])
                                        .resizable()
                                        .scaledToFill()
                                        .frame(width: gridItemWidth, height: gridItemWidth)
                                        .clipped()
                                        .cornerRadius(10)
                                }
                            }
                        }
                    }
                    .padding()
                }
            }
            .navigationTitle("Photos")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(isEditing ? "Done" : "Edit") {
                        isEditing.toggle()
                    }
                }
            }
            .sheet(isPresented: $showingImagePicker) {
                ImagePicker(images: $items, imageUrls: $imageUrls, selectedImage: $selectedImage, selectedDirectory: $selectedDirectory, navigateToSuperResolutionView: $navigateToSuperResolutionView)
            }
            .background(
                NavigationLink(
                    destination: SuperResolutionView(image: selectedImage ?? UIImage(), directory: selectedDirectory ?? URL(fileURLWithPath: "")),
                    isActive: $navigateToSuperResolutionView
                ) {
                    EmptyView()
                }
            )
        }
        .navigationViewStyle(StackNavigationViewStyle())
        .onAppear {
            loadInitialImages()
        }
    }

    private func numberOfColumns(for width: CGFloat) -> Int {
        return max(Int(width / (gridItemWidth + 20)), 1) // Ensure at least one column
    }

    private func loadInitialImages() {
        let initialDirectories = ImageStorageManager.shared.loadAllDirectoriesFromCoreData()
        let originalImages = ImageStorageManager.shared.loadImages(from: initialDirectories)
        self.items = originalImages
        self.imageUrls = initialDirectories
    }
}

struct ImagePicker: UIViewControllerRepresentable {
    @Environment(\.dismiss) var dismiss
    @Binding var images: [UIImage]
    @Binding var imageUrls: [URL]
    @Binding var selectedImage: UIImage?
    @Binding var selectedDirectory: URL?
    @Binding var navigateToSuperResolutionView: Bool  // Binding to control navigation

    func makeUIViewController(context: Context) -> UIImagePickerController {
        let picker = UIImagePickerController()
        picker.delegate = context.coordinator
        return picker
    }

    func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {}

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    class Coordinator: NSObject, UINavigationControllerDelegate, UIImagePickerControllerDelegate {
        var parent: ImagePicker

        init(_ parent: ImagePicker) {
            self.parent = parent
        }

        func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
            if let image = info[.originalImage] as? UIImage {
                if let newFolderURL = ImageStorageManager.shared.createNewFolder() {
                    if ImageStorageManager.shared.saveImage(image, to: newFolderURL, withName: "original") {
                        DispatchQueue.main.async {
                            self.parent.images.insert(image, at: 0)
                            self.parent.imageUrls.insert(newFolderURL, at: 0)
                            self.parent.selectedImage = image
                            self.parent.selectedDirectory = newFolderURL
                            self.parent.navigateToSuperResolutionView = true  // Trigger navigation
                        }
                    }
                }
            }
            parent.dismiss()
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
