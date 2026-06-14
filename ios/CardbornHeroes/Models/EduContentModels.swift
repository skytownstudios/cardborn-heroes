import Foundation

struct LessonsFile: Codable {
    let appTitle: String
    let theme: LessonTheme
    let lessons: [LessonItem]
}

struct LessonTheme: Codable {
    let walkerSymbol: String
    let backgroundColors: [String]
}

struct LessonItem: Codable, Identifiable, Hashable {
    let id: String
    let title: String
    let fact: String
    let symbol: String
    let soundFile: String
    let category: String
}

enum EduContentLoader {
    static func loadLessons() -> LessonsFile? {
        guard let url = Bundle.main.url(forResource: "lessons", withExtension: "json"),
              let data = try? Data(contentsOf: url) else { return nil }
        return try? JSONDecoder().decode(LessonsFile.self, from: data)
    }
}
