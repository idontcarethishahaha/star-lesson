package com.tianji.aigc.tools;

import com.tianji.api.client.course.CourseClient;
import com.tianji.api.client.learning.LearningClient;
import com.tianji.api.dto.course.CourseFullInfoDTO;
import com.tianji.api.dto.leanring.LearningLessonDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LearningTools {

    private final CourseClient courseClient;
    private final LearningClient learningClient;

    @Tool(description = "为指定课程生成结构化学习笔记，包含核心知识点、重点总结、学习建议。适用于学完课程或章节后需要复习的场景。")
    public String generateCourseNotes(
            @ToolParam(description = "课程ID") Long courseId,
            @ToolParam(description = "笔记类型，可选值：overview(总览), chapter(章节), summary(总结)") String noteType,
            @ToolParam(description = "章节名称或章节ID，章节类型时使用") String chapterName) {
        try {
            CourseFullInfoDTO course = courseClient.getCourseInfoById(courseId, true, true);
            if (course == null) {
                return "未找到课程信息";
            }

            StringBuilder prompt = new StringBuilder();
            prompt.append("请基于以下课程信息，生成一份");

            switch (noteType != null ? noteType : "overview") {
                case "chapter":
                    prompt.append("针对章节【").append(chapterName != null ? chapterName : "指定章节").append("】的详细学习笔记");
                    break;
                case "summary":
                    prompt.append("课程知识总结笔记");
                    break;
                default:
                    prompt.append("课程总览笔记");
                    break;
            }
            prompt.append("。\n\n");
            prompt.append("课程名称：").append(course.getName()).append("\n");

            if (course.getChapters() != null) {
                prompt.append("课程结构：\n");
                for (var chapter : course.getChapters()) {
                    prompt.append("- 第").append(chapter.getIndex()).append("章：").append(chapter.getName()).append("\n");
                    if (chapter.getSections() != null) {
                        for (var section : chapter.getSections()) {
                            prompt.append("  - ").append(section.getIndex()).append(". ").append(section.getName()).append("\n");
                        }
                    }
                }
                prompt.append("\n");
            }

            prompt.append("笔记结构要求：\n");
            prompt.append("## 📚 章节/知识点梳理\n");
            prompt.append("## 🎯 核心重点总结\n");
            prompt.append("## 💡 学习建议\n");
            prompt.append("## 🧠 记忆技巧\n");

            return prompt.toString();
        } catch (Exception e) {
            log.error("生成课程笔记失败", e);
            return "生成笔记时发生错误：" + e.getMessage();
        }
    }

    @Tool(description = "为指定课程生成思维导图大纲，以 Markdown 格式输出，包含层级结构和知识点。用于快速梳理课程知识体系。")
    public String generateMindMap(
            @ToolParam(description = "课程ID") Long courseId,
            @ToolParam(description = "思维导图深度，1-4级，默认2") Integer depth) {
        try {
            CourseFullInfoDTO course = courseClient.getCourseInfoById(courseId, true, false);
            if (course == null) {
                return "未找到课程信息";
            }

            StringBuilder result = new StringBuilder();
            result.append("## 课程思维导图：").append(course.getName()).append("\n\n");
            result.append("```markdown\n");
            result.append("# ").append(course.getName()).append("\n\n");

            if (course.getChapters() != null) {
                int maxDepth = depth != null ? Math.min(depth, 4) : 2;
                for (var chapter : course.getChapters()) {
                    result.append("## 第").append(chapter.getIndex()).append("章：").append(chapter.getName()).append("\n");
                    if (maxDepth >= 2 && chapter.getSections() != null) {
                        for (var section : chapter.getSections()) {
                            result.append("### ").append(section.getIndex()).append(" ").append(section.getName()).append("\n");
                        }
                    }
                    result.append("\n");
                }
            }

            result.append("```\n\n");
            result.append("你可以基于以上课程结构，帮我生成一个更详细的思维导图吗？请包含每个章节的核心知识点和学习重点。");

            return result.toString();
        } catch (Exception e) {
            log.error("生成思维导图失败", e);
            return "生成思维导图时发生错误：" + e.getMessage();
        }
    }

    @Tool(description = "根据用户当前学习进度，生成下一个学习建议，包括应该学习什么、怎么学、预计学习时间。")
    public String getNextLearningRecommendation(
            @ToolParam(description = "课程ID") Long courseId) {
        try {
            LearningLessonDTO lesson = learningClient.queryLearningRecordByCourse(courseId);
            CourseFullInfoDTO course = courseClient.getCourseInfoById(courseId, true, false);

            StringBuilder result = new StringBuilder();
            result.append("## 📖 学习建议\n\n");

            if (lesson == null || lesson.getRecords() == null || lesson.getRecords().isEmpty()) {
                result.append("你还没有开始学习这门课程！\n\n");
                if (course != null && course.getChapters() != null && !course.getChapters().isEmpty()) {
                    var firstChapter = course.getChapters().get(0);
                    result.append("### 建议从第一章开始：\n");
                    result.append("- **第").append(firstChapter.getIndex()).append("章：").append(firstChapter.getName()).append("**\n");
                    if (firstChapter.getSections() != null && !firstChapter.getSections().isEmpty()) {
                        result.append("  1. ").append(firstChapter.getSections().get(0).getName()).append("\n");
                    }
                }
                result.append("\n预计学习时间：约 1-2 小时\n\n");
                result.append("学习建议：\n");
                result.append("1. 先浏览整个课程目录，了解课程结构\n");
                result.append("2. 准备好笔记本，记录重点\n");
                result.append("3. 按照章节顺序学习，循序渐进\n");
            } else {
                int learnedCount = lesson.getRecords().size();
                result.append("你已经学习了 **").append(learnedCount).append("** 个小节！继续加油！💪\n\n");

                if (lesson.getLatestSectionId() != null) {
                    var sectionInfo = courseClient.sectionInfo(lesson.getLatestSectionId());
                    if (sectionInfo != null) {
                        result.append("### 最近学习：\n");
                        result.append("- 最近学习的小节 ID：").append(lesson.getLatestSectionId()).append("\n\n");
                    }
                }

                result.append("### 下一步学习建议：\n");
                result.append("1. 复习已学内容，巩固知识点\n");
                result.append("2. 继续学习下一个小节\n");
                result.append("3. 适当做一些练习，检验学习成果\n\n");
                result.append("你想让我帮你整理一份已学内容的复习笔记吗？");
            }

            return result.toString();
        } catch (Exception e) {
            log.error("获取学习建议失败", e);
            return "获取学习建议时发生错误：" + e.getMessage();
        }
    }
}
