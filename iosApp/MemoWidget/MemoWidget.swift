import WidgetKit
import SwiftUI

// MARK: - Shared data (mirrors com.kmemo.data.Memo JSON written by the app)

private let appGroup = "group.com.kmemo.app"
private let snapshotKey = "kmemo_widget_memos"

struct SharedMemo: Codable, Identifiable {
    let id: Int64
    let title: String
    let content: String
    let color: Int64
    let pinned: Bool
    let updatedAt: Int64
}

private func loadMemos() -> [SharedMemo] {
    guard
        let defaults = UserDefaults(suiteName: appGroup),
        let raw = defaults.string(forKey: snapshotKey),
        let data = raw.data(using: .utf8),
        let memos = try? JSONDecoder().decode([SharedMemo].self, from: data)
    else { return [] }
    return memos
}

extension Color {
    init(argb: Int64) {
        let a = Double((argb >> 24) & 0xFF) / 255.0
        let r = Double((argb >> 16) & 0xFF) / 255.0
        let g = Double((argb >> 8) & 0xFF) / 255.0
        let b = Double(argb & 0xFF) / 255.0
        self.init(.sRGB, red: r, green: g, blue: b, opacity: a)
    }
}

// MARK: - Timeline

struct MemoEntry: TimelineEntry {
    let date: Date
    let memos: [SharedMemo]
}

struct Provider: TimelineProvider {
    func placeholder(in context: Context) -> MemoEntry {
        MemoEntry(date: Date(), memos: [])
    }

    func getSnapshot(in context: Context, completion: @escaping (MemoEntry) -> Void) {
        completion(MemoEntry(date: Date(), memos: loadMemos()))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<MemoEntry>) -> Void) {
        let entry = MemoEntry(date: Date(), memos: loadMemos())
        // Refresh periodically as a fallback; the app also forces reloads on change.
        let next = Calendar.current.date(byAdding: .minute, value: 30, to: Date()) ?? Date()
        completion(Timeline(entries: [entry], policy: .after(next)))
    }
}

// MARK: - Views

struct MemoWidgetEntryView: View {
    @Environment(\.widgetFamily) var family
    var entry: Provider.Entry

    private var maxItems: Int {
        switch family {
        case .systemSmall: return 2
        case .systemMedium: return 3
        default: return 6
        }
    }

    var body: some View {
        Group {
            if entry.memos.isEmpty {
                VStack {
                    Text("메모")
                        .font(.headline)
                    Text("앱에서 메모를 추가하세요")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                VStack(alignment: .leading, spacing: 6) {
                    ForEach(entry.memos.prefix(maxItems)) { memo in
                        Link(destination: memoURL(memo.id)) {
                            MemoRow(memo: memo, compact: family == .systemSmall)
                        }
                    }
                    Spacer(minLength: 0)
                }
            }
        }
        // systemSmall ignores per-row Links, so route the whole widget to the first memo.
        .widgetURL(entry.memos.first.map { memoURL($0.id) })
        .widgetBackgroundCompat()
    }
}

private func memoURL(_ id: Int64) -> URL {
    URL(string: "kmemo://memo/\(id)")!
}

private struct MemoRow: View {
    let memo: SharedMemo
    let compact: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            if !memo.title.isEmpty {
                Text(memo.title)
                    .font(.caption).bold()
                    .foregroundStyle(.black)
                    .lineLimit(1)
            }
            if !memo.content.isEmpty {
                Text(memo.content)
                    .font(.caption2)
                    .foregroundStyle(.black.opacity(0.8))
                    .lineLimit(compact ? 1 : 2)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(8)
        .background(Color(argb: memo.color))
        .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
    }
}

private extension View {
    @ViewBuilder
    func widgetBackgroundCompat() -> some View {
        if #available(iOS 17.0, *) {
            self.padding(10).containerBackground(Color(white: 0.98), for: .widget)
        } else {
            self.padding(10).background(Color(white: 0.98))
        }
    }
}

// MARK: - Widget

struct MemoWidget: Widget {
    let kind = "MemoWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: Provider()) { entry in
            MemoWidgetEntryView(entry: entry)
        }
        .configurationDisplayName("메모")
        .description("최근 메모를 홈 화면에서 바로 확인하세요.")
        .supportedFamilies([.systemSmall, .systemMedium, .systemLarge])
    }
}
