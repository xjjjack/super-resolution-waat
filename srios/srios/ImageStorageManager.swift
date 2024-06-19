import UIKit
import CoreData
import os.log

class ImageStorageManager {
    static let shared = ImageStorageManager()
    private let log = OSLog(subsystem: "com.yourCompany.ImageStorageManager", category: "Storage")

    private var context: NSManagedObjectContext {
        return PersistenceController.shared.container.viewContext
    }

    // Save image to a specific directory with a given filename
    func saveImage(_ image: UIImage, to directory: URL, withName name: String) -> Bool {
        let fileURL = directory.appendingPathComponent("\(name).png")
        guard let data = image.pngData() else {
            os_log("Failed to convert image to PNG data.", log: self.log, type: .error)
            return false
        }
        do {
            try data.write(to: fileURL)
            os_log("Image saved successfully to %{PUBLIC}@", log: self.log, type: .info, fileURL.path)
            saveDirectoryToCoreData(directory)
            return true
        } catch {
            os_log("Unable to save image to disk: %{PUBLIC}@, Error: %{PUBLIC}@", log: self.log, type: .error, fileURL.path, error.localizedDescription)
            return false
        }
    }

    // Load image from a specific directory with a given filename
    func loadImage(from directory: URL, withName name: String) -> UIImage? {
        let fileURL = directory.appendingPathComponent("\(name).png")
        if let imageData = try? Data(contentsOf: fileURL) {
            return UIImage(data: imageData)
        }
        os_log("Failed to load image from %{PUBLIC}@", log: self.log, type: .error, fileURL.path)
        return nil
    }

    // Delete all images in a specific directory
    func deleteImages(from directory: URL) {
        do {
            let files = try FileManager.default.contentsOfDirectory(at: directory, includingPropertiesForKeys: nil)
            for file in files {
                try FileManager.default.removeItem(at: file)
                os_log("Deleted image at %{PUBLIC}@", log: self.log, type: .info, file.path)
            }
            deleteDirectoryFromCoreData(directory)
        } catch {
            os_log("Error deleting images from directory %{PUBLIC}@: %{PUBLIC}@", log: self.log, type: .error, directory.path, error.localizedDescription)
        }
    }

    // Create and return a URL to a new empty folder
    func createNewFolder() -> URL? {
        let fileManager = FileManager.default
        if let documentsDirectory = try? fileManager.url(for: .documentDirectory, in: .userDomainMask, appropriateFor: nil, create: false) {
            let newFolderName = UUID().uuidString
            let newFolderURL = documentsDirectory.appendingPathComponent(newFolderName)

            if !fileManager.fileExists(atPath: newFolderURL.path) {
                do {
                    try fileManager.createDirectory(at: newFolderURL, withIntermediateDirectories: true, attributes: nil)
                    os_log("Created a new folder at %{PUBLIC}@", log: self.log, type: .info, newFolderURL.path)
                    return newFolderURL
                } catch {
                    os_log("Unable to create new folder at %{PUBLIC}@: %{PUBLIC}@", log: self.log, type: .error, newFolderURL.path, error.localizedDescription)
                }
            }
        }
        return nil
    }

    // Core Data methods
    private func saveDirectoryToCoreData(_ directory: URL) {
        let item = Item(context: context)
        item.urlString = directory.absoluteString
        item.timestamp = Date()
        do {
            try context.save()
            os_log("Directory URL saved to Core Data: %{PUBLIC}@", log: self.log, type: .info, directory.absoluteString)
        } catch {
            os_log("Failed to save directory URL to Core Data: %{PUBLIC}@", log: self.log, type: .error, error.localizedDescription)
        }
    }

    private func deleteDirectoryFromCoreData(_ directory: URL) {
        let fetchRequest: NSFetchRequest<Item> = Item.fetchRequest()
        fetchRequest.predicate = NSPredicate(format: "urlString == %@", directory.absoluteString)
        do {
            let results = try context.fetch(fetchRequest)
            for object in results {
                context.delete(object)
            }
            try context.save()
            os_log("Directory URL deleted from Core Data: %{PUBLIC}@", log: self.log, type: .info, directory.absoluteString)
        } catch {
            os_log("Failed to delete directory URL from Core Data: %{PUBLIC}@", log: self.log, type: .error, error.localizedDescription)
        }
    }

    func loadAllDirectoriesFromCoreData() -> [URL] {
        let fetchRequest: NSFetchRequest<Item> = Item.fetchRequest()
        let sortDescriptor = NSSortDescriptor(key: "timestamp", ascending: false)
        fetchRequest.sortDescriptors = [sortDescriptor]
        do {
            let results = try context.fetch(fetchRequest)
            return results.compactMap { URL(string: $0.urlString ?? "") }
        } catch {
            os_log("Failed to load directory URLs from Core Data: %{PUBLIC}@", log: self.log, type: .error, error.localizedDescription)
            return []
        }
    }

    // Helper function to load images from URLs
    func loadImages(from directories: [URL]) -> [UIImage] {
        return directories.compactMap { loadImage(from: $0, withName: "original") }
    }
}
