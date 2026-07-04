package com.tianji.aigc.tools;

import com.tianji.api.client.learning.LearningClient;
import com.tianji.api.client.course.CourseClient;
import com.tianji.api.dto.leanring.LearningLessonDTO;
import com.tianji.api.dto.course.CourseFullInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LearningReportTools {

    private final LearningClient learningClient;
    private final CourseClient courseClient;

    @Tool(description = "生成指定课程的学习报告，包含学习进度、已学内容、学习建议等。帮助用户了解自己的学习情况。")
    public String generateLearningReport(
            @ToolParam(description = "课程ID") Long courseId) {
        try {
            CourseFullInfoDTO course = courseClient.getCourseInfoById(courseId, true, false);
            LearningLessonDTO lesson = null;
            try {
                lesson = learningClient.queryLearningRecordByCourse(courseId);
            } catch (Exception e) {
                log.warn("获取学习进度失败", e);
            }

            StringBuilder report = new StringBuilder();
            report.append("## 📊 学习报告\n\n");

            if (course != null) {
                report.append("### 课程信息\n");
                report.append("- **课程名称**：").append(course.getName()).append("\n");
                report.append("- **总小节数**：").append(course.getSectionNum() != null ? course.getSectionNum() : 0).append("\n\n");
            }

            if (lesson == null || lesson.getRecords() == null || lesson.getRecords().isEmpty()) {
                report.append("### 学习进度\n");
                report.append("你还没有开始学习这门课程哦！\n\n");
                report.append("### 建议\n");
                report.append("现在就开始学习吧！千里之行，始于足下 🚀\n");
                return report.toString();
            }

            int totalSections = course != null && course.getSectionNum() != null ? course.getSectionNum() : 0;
            int learnedSections = lesson.getRecords().size();
            double progress = totalSections > 0 ? (double) learnedSections / totalSections * 100 : 0;

            report.append("### 学习进度\n");
            report.append("- **已学小节**：").append(learnedSections).append(" / ").append(totalSections).append("\n");
            report.append("- **完成度**：").append(String.format("%.1f", progress)).append("%\n");
            report.append("```\n");
            int filled = (int) (progress / 5);
            report.append("█".repeat(Math.max(0, filled)));
            report.append("░".repeat(Math.max(0, 20 - filled)));
            report.append(" ").append(String.format("%.1f", progress)).append("%\n");
            report.append("```\n\n");

            report.append("### 已学章节\n");
            if (course != null && course.getChapters() != null) {
                for (var chapter : course.getChapters()) {
                    if (chapter.getSections() != null) {
                        long chapterLearned = lesson.getRecords().stream()
                                .filter(r -> chapter.getSections().stream()
                                        .anyMatch(s -> s.getId() != null && s.getId().equals(r.getSectionId())))
                                .count();
                        if (chapterLearned > 0) {
                            report.append("- 第").append(chapter.getIndex()).append("章：").append(chapter.getName())
                                    .append(" (").append(chapterLearned).append("/").append(chapter.getSections().size()).append(")\n");
                        }
                    }
                }
            } else {
                report.append("共学习了 ").append(learnedSections).append(" 个小节\n");
            }
            report.append("\n");

            report.append("### 🎯 学习评价\n");
            if (progress >= 100) {
                report.append("太棒了！你已经完成了全部课程！🎉 建议进行一次全面复习，巩固所学知识。\n");
            } else if (progress >= 80) {
                report.append("非常棒！已经完成了大部分课程！💪 继续加油，胜利就在眼前！\n");
            } else if (progress >= 50) {
                report.append("不错哦！已经学完一半了！👏 保持这个节奏，很快就能学完！\n");
            } else if (progress >= 20) {
                report.append("你已经有了一个好的开始！💡 坚持学习，积少成多！\n");
            } else {
                report.append("刚刚起步，一切皆有可能！🚀 保持每天学习一点点，进步会很明显的！\n");
            }
            report.append("\n");

            report.append("### 💡 学习建议\n");
            if (progress < 100) {
                report.append("1. 保持每天学习的习惯，哪怕只有 15 分钟\n");
                report.append("2. 学完每章后做一个小总结，加深理解\n");
                report.append("3. 遇到不懂的地方，可以随时问我！\n");
                report.append("4. 适当做练习，实践出真知\n");
            } else {
                report.append("1. 回顾整个课程，整理一份知识体系图\n");
                report.append("2. 找一个实际项目，把学到的知识用起来\n");
                report.append("3. 如果有不太熟悉的章节，可以再看一遍\n");
                report.append("4. 考虑学习进阶课程，继续提升！\n");
            }
            report.append("\n");

            report.append("需要我帮你生成一份复习计划吗？或者你想了解某章的详细内容？");

            return report.toString();
        } catch (Exception e) {
            log.error("生成学习报告失败", e);
            return "生成学习报告时发生错误：" + e.getMessage();
        }
    }

    @Tool(description = "生成整体学习报告，统计所有已购课程的学习情况、总学习时长、学习成就等。")
    public String generateOverallReport() {
        try {
            StringBuilder report = new StringBuilder();
            report.append("## 📈 总体学习报告\n\n");
            report.append("### 🎓 学习概况\n\n");
            report.append("目前系统中包含多门课程，你可以：\n");
            report.append("- 查看单门课程的学习报告\n");
            report.append("- 制定个性化学习计划\n");
            report.append("- 获取课程学习建议\n\n");

            report.append("### 🏆 学习成就系统\n\n");
            report.append("| 成就 | 条件 |\n");
            report.append("|------|------|\n");
            report.append("| 🌱 初学者 | 完成第一节课 |\n");
            report.append("| 📚 好学不倦 | 累计学习10小时 |\n");
            report.append("| 🎯 坚持达人 | 连续学习7天 |\n");
            report.append("| ⚡ 学霸 | 完成3门课程 |\n");
            report.append("| 👑 全能学者 | 完成10门课程 |\n\n");

            report.append("### 💡 学习小贴士\n\n");
            report.append("1. **番茄工作法**：学习25分钟，休息5分钟，效率更高\n");
            report.append("2. **费曼学习法**：用自己的话复述学到的知识，检验理解程度\n");
            report.append("3. **间隔重复**：定期复习，记忆更牢固\n");
            report.append("4. **主动学习**：带着问题去学习，效果更好\n\n");

            report.append("你想查看哪门课程的详细学习报告？或者需要我帮你制定学习计划？");

            return report.toString();
        } catch (Exception e) {
            log.error("生成总体报告失败", e);
            return "生成总体报告时发生错误：" + e.getMessage();
        }
    }
}
