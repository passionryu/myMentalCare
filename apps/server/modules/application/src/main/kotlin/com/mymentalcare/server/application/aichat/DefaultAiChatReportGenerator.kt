package com.mymentalcare.server.application.aichat

import com.mymentalcare.server.domain.aichat.AiChatReportSong
import com.mymentalcare.server.domain.aichat.AiChatReportType
import com.mymentalcare.server.domain.aichat.ChatMessage
import com.mymentalcare.server.domain.aichat.ChatMessageSenderType
import org.springframework.stereotype.Component
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Component
internal class DefaultAiChatReportGenerator : AiChatReportGenerator {
    // 대화가 충분하면 감정과 원인을 조심스럽게 정리하고, 부족하면 판단 유보 리포트를 만든다.
    override fun generateReport(reportType: AiChatReportType, messages: List<ChatMessage>): AiChatReportDraft {
        val userTexts = messages
            .filter { it.senderType == ChatMessageSenderType.USER }
            .map { it.content.trim() }
            .filter { it.isNotBlank() }

        if (reportType == AiChatReportType.SHORT) {
            return buildShortReport()
        }

        val primaryEmotion = detectPrimaryEmotion(userTexts)
        val mainCause = detectMainCause(userTexts)
        val recentTopics = userTexts.takeLast(3).joinToString(" / ") { it.take(70) }

        return AiChatReportDraft(
            summary = "오늘은 ${mainCause}와 관련된 이야기가 주로 오갔습니다. 최근 대화에서는 $recentTopics 같은 흐름이 확인되었습니다.",
            primaryEmotion = primaryEmotion.label,
            emotionIntensity = primaryEmotion.intensity,
            mainCause = mainCause,
            emotionalFlow = "대화 초반의 표현과 최근 메시지를 함께 보면, 마음을 바로 결론내리기보다 오늘의 상태를 정리하려는 흐름이 나타났습니다.",
            todaySentence = "오늘의 마음은 단정하기보다, 말로 꺼낸 만큼만 천천히 정리해도 괜찮습니다.",
            songs = songsFor(primaryEmotion.label),
        )
    }

    private fun buildShortReport(): AiChatReportDraft {
        return AiChatReportDraft(
            summary = "오늘은 짧게 대화를 시작했지만, 마음 상태나 상황을 파악할 만큼의 내용은 충분히 쌓이지 않았습니다.",
            primaryEmotion = "아직 판단하기 어려움",
            emotionIntensity = null,
            mainCause = "확인되지 않음",
            emotionalFlow = "대화량이 적어 마음의 변화 흐름을 정리하기 어렵습니다.",
            todaySentence = "아직 더 말해도 괜찮습니다.",
            songs = songsFor("짧은 대화"),
        )
    }

    private fun detectPrimaryEmotion(userTexts: List<String>): EmotionGuess {
        val joinedText = userTexts.joinToString(" ")
        return when {
            listOf("불안", "걱정", "초조").any { joinedText.contains(it) } -> EmotionGuess("불안", 4)
            listOf("지침", "피곤", "기운", "번아웃").any { joinedText.contains(it) } -> EmotionGuess("지침", 4)
            listOf("우울", "슬퍼", "외로").any { joinedText.contains(it) } -> EmotionGuess("가라앉음", 4)
            listOf("화", "짜증", "분노", "답답").any { joinedText.contains(it) } -> EmotionGuess("답답함", 4)
            listOf("괜찮", "좋아", "편안", "안도").any { joinedText.contains(it) } -> EmotionGuess("안도", 3)
            else -> EmotionGuess("정리 중인 마음", 3)
        }
    }

    private fun detectMainCause(userTexts: List<String>): String {
        val joinedText = userTexts.joinToString(" ")
        return when {
            listOf("회사", "업무", "일", "프로젝트").any { joinedText.contains(it) } -> "업무 부담"
            listOf("공부", "학교", "시험", "과제").any { joinedText.contains(it) } -> "학업 부담"
            listOf("사람", "친구", "관계", "동료").any { joinedText.contains(it) } -> "인간관계"
            listOf("가족", "부모", "형제").any { joinedText.contains(it) } -> "가족"
            listOf("건강", "아파", "몸").any { joinedText.contains(it) } -> "건강"
            listOf("돈", "월급", "비용").any { joinedText.contains(it) } -> "돈"
            listOf("잠", "수면", "피곤").any { joinedText.contains(it) } -> "회복 부족"
            else -> "대화에서 드러난 일상 흐름"
        }
    }

    private fun songsFor(emotion: String): List<AiChatReportSong> {
        val songs = when (emotion) {
            "불안" -> listOf(
                SongRecommendation("밤편지", "아이유", "조용히 마음을 가라앉히며 하루를 정리하기 좋습니다."),
                SongRecommendation("위로", "권진아", "불안을 급하게 밀어내기보다 곁에 두고 쉬어가게 합니다."),
                SongRecommendation("Better Together", "Jack Johnson", "긴장을 낮추는 부드러운 분위기가 있습니다."),
            )
            "지침" -> listOf(
                SongRecommendation("휴식", "옥상달빛", "지친 마음을 과하게 끌어올리지 않고 편하게 둡니다."),
                SongRecommendation("에필로그", "아이유", "하루를 무리 없이 마무리하는 느낌을 줍니다."),
                SongRecommendation("Banana Pancakes", "Jack Johnson", "느슨하게 쉬어가고 싶은 상태에 어울립니다."),
            )
            "가라앉음" -> listOf(
                SongRecommendation("한숨", "이하이", "무거운 마음을 혼자만의 것으로 두지 않게 합니다."),
                SongRecommendation("그대라는 시", "태연", "조용히 감정을 지나가게 하는 분위기가 있습니다."),
                SongRecommendation("Fix You", "Coldplay", "가라앉은 마음을 천천히 붙잡아주는 곡입니다."),
            )
            "답답함" -> listOf(
                SongRecommendation("가끔 미치도록 네가 안고 싶어질 때가 있어", "가을방학", "답답한 마음의 결을 차분하게 풀어냅니다."),
                SongRecommendation("Square", "백예린", "막힌 느낌을 조금 부드럽게 환기해줍니다."),
                SongRecommendation("Lost Stars", "Adam Levine", "복잡한 생각을 정리하는 밤 분위기에 어울립니다."),
            )
            else -> listOf(
                SongRecommendation("주저하는 연인들을 위해", "잔나비", "오늘의 여운을 천천히 정리하기 좋습니다."),
                SongRecommendation("밤편지", "아이유", "조용한 마무리와 잘 어울립니다."),
                SongRecommendation("좋은 밤 좋은 꿈", "너드커넥션", "하루를 부드럽게 닫는 분위기가 있습니다."),
            )
        }

        return songs.mapIndexed { index, song ->
            AiChatReportSong(
                songOrder = index + 1,
                title = song.title,
                artist = song.artist,
                reason = song.reason,
                youtubeUrl = youtubeSearchUrl(song),
            )
        }
    }

    private fun youtubeSearchUrl(song: SongRecommendation): String {
        val query = URLEncoder.encode("${song.artist} ${song.title}", StandardCharsets.UTF_8)
        return "https://www.youtube.com/results?search_query=$query"
    }
}

private data class EmotionGuess(
    val label: String,
    val intensity: Int,
)

private data class SongRecommendation(
    val title: String,
    val artist: String,
    val reason: String,
)
