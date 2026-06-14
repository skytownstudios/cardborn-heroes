import Foundation

@MainActor
final class EduContentStore: ObservableObject {
    @Published private(set) var file: LessonsFile?
    @Published private(set) var lessons: [LessonItem] = []

    init() {
        reload()
    }

    func reload() {
        file = EduContentLoader.loadLessons()
        lessons = file?.lessons ?? []
    }

    func lesson(id: String) -> LessonItem? {
        lessons.first { $0.id == id }
    }

    func lessons(in category: String) -> [LessonItem] {
        lessons.filter { $0.category == category }
    }
}
