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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudyPlanTools {

    private final CourseClient courseClient;
    private final LearningClient learningClient;

    @Tool(description = "根据课程内容和用户的学习时间，生成个性化学习计划。包含每日学习目标、预计完成时间、学习建议。")
    public String generateStudyPlan(
            @ToolParam(description = "课程ID") Long courseId,
            @ToolParam(description = "每天学习时长，单位：小时") Double dailyStudyHours,
            @ToolParam(description = "计划开始日期，格式：YYYY-MM-DD，默认今天") String startDate,
            @ToolParam(description = "学习目标，例如：通过考试、掌握技能、快速入门") String learningGoal) {
        try {
            CourseFullInfoDTO course = courseClient.getCourseInfoById(courseId, true, false);
            if (course == null) {
                return "未找到课程信息，请确认课程ID是否正确";
            }

            LearningLessonDTO lesson = null;
            try {
                lesson = learningClient.queryLearningRecordByCourse(courseId);
            } catch (Exception ignored) {}

            int totalSections = course.getSectionNum() != null ? course.getSectionNum() : 0;
            int learnedSections = 0;
            if (lesson != null && lesson.getRecords() != null) {
                learnedSections = lesson.getRecords().size();
            }
            int remainingSections = totalSections - learnedSections;
            if (remainingSections < 0) remainingSections = 0;

            double hours = dailyStudyHours != null ? dailyStudyHours : 2.0;
            double avgMinutesPerSection = 20.0;
            double totalMinutesNeeded = remainingSections * avgMinutesPerSection;
            int estimatedDays = (int) Math.ceil(totalMinutesNeeded / (hours * 60));
            if (estimatedDays < 1) estimatedDays = 1;

            LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now();
            LocalDate endDate = start.plusDays(estimatedDays - 1);

            StringBuilder plan = new StringBuilder();
            plan.append("## 📅 个人学习计划\n\n");
            plan.append("### 课程信息\n");
            plan.append("- **课程名称**：").append(course.getName()).append("\n");
            plan.append("- **总小节数**：").append(totalSections).append("\n");
            plan.append("- **已学习**：").append(learnedSections).append(" 小节\n");
            plan.append("- **剩余**：").append(remainingSections).append(" 小节\n\n");

            plan.append("### 计划参数\n");
            plan.append("- **每日学习时长**：").append(hours).append(" 小时\n");
            plan.append("- **学习目标**：").append(learningGoal != null ? learningGoal : "掌握课程内容").append("\n");
            plan.append("- **开始日期**：").append(start).append("\n");
            plan.append("- **预计完成日期**：").append(endDate).append("\n");
            plan.append("- **预计学习天数**：").append(estimatedDays).append(" 天\n\n");

            plan.append("### 学习进度安排\n\n");

            List<String> allSections = new ArrayList<>();
            if (course.getChapters() != null) {
                for (var chapter : course.getChapters()) {
                    if (chapter.getSections() != null) {
                        for (var section : chapter.getSections()) {
                            allSections.add("第" + chapter.getIndex() + "章 - " + section.getName());
                        }
                    }
                }
            }

            int sectionsPerDay = (int) Math.ceil((double) remainingSections / estimatedDays);
            if (sectionsPerDay < 1) sectionsPerDay = 1;

            int dayIndex = 1;
            int sectionIndex = learnedSections;
            List<String> sectionList = allSections.subList(
                    Math.min(learnedSections, allSections.size()),
                    allSections.size()
            );

            for (int i = 0; i < sectionList.size(); i += sectionsPerDay) {
                LocalDate dayDate = start.plusDays(dayIndex - 1);
                int endIdx = Math.min(i + sectionsPerDay, sectionList.size());
                List<String> daySections = sectionList.subList(i, endIdx);

                plan.append("#### Day ").append(dayIndex).append(" (").append(dayDate).append(")\n");
                for (int j = 0; j < daySections.size(); j++) {
                    plan.append(j + 1).append(". ").append(daySections.get(j)).append("\n");
                }
                plan.append("**学习目标**：完成 ").append(daySections.size()).append(" 个小节的学习\n");
                plan.append("**预计用时**：").append(String.format("%.1f", daySections.size() * avgMinutesPerSection / 60)).append(" 小时\n\n");

                dayIndex++;
                if (dayIndex > 30) {
                    plan.append("...（共 ").append(estimatedDays).append(" 天，此处省略中间部分）\n\n");
                    break;
                }
            }

            plan.append("### 💡 学习建议\n\n");
            plan.append("1. **保持规律**：尽量每天固定时间学习，形成习惯\n");
            plan.append("2. **及时复习**：每学完一章，花 10-15 分钟回顾知识点\n");
            plan.append("3. **做好笔记**：记录重点和难点，方便后续复习\n");
            plan.append("4. **适当练习**：通过实践巩固所学知识\n");
            plan.append("5. **劳逸结合**：每学习 45 分钟休息 5-10 分钟\n");

            if (learningGoal != null && learningGoal.contains("考试")) {
                plan.append("6. **考前冲刺**：考试前一周安排一次全面复习\n");
            }

            plan.append("\n### 🎯 学习目标确认\n");
            plan.append("你希望达成的目标是：**").append(learningGoal != null ? learningGoal : "掌握课程内容").append("**\n\n");
            plan.append("这个学习计划是根据课程内容和你的学习时间生成的，你可以根据实际情况调整。\n");
            plan.append("需要我帮你调整计划吗？比如调整每日学习时长、增加复习时间等。");

            return plan.toString();
        } catch (Exception e) {
            log.error("生成学习计划失败", e);
            return "生成学习计划时发生错误：" + e.getMessage();
        }
    }

    @Tool(description = "生成多课程学习路线图，根据课程依赖关系和学习顺序，规划系统的学习路径。")
    public String generateLearningPath(
            @ToolParam(description = "课程ID列表，按学习顺序排列") List<Long> courseIds,
            @ToolParam(description = "每天学习时长，单位：小时") Double dailyStudyHours,
            @ToolParam(description = "学习目标") String learningGoal) {
        if (courseIds == null || courseIds.isEmpty()) {
            return "请提供至少一个课程ID";
        }

        try {
            StringBuilder path = new StringBuilder();
            path.append("## 🗺️ 学习路线图\n\n");
            path.append("### 学习目标\n");
            path.append(learningGoal != null ? learningGoal : "系统学习系列课程").append("\n\n");

            path.append("### 课程安排\n\n");

            int totalDays = 0;
            double totalHours = 0;
            double hours = dailyStudyHours != null ? dailyStudyHours : 2.0;

            for (int i = 0; i < courseIds.size(); i++) {
                Long courseId = courseIds.get(i);
                try {
                    CourseFullInfoDTO course = courseClient.getCourseInfoById(courseId, false, false);
                    if (course != null) {
                        int sections = course.getSectionNum() != null ? course.getSectionNum() : 10;
                        double courseHours = sections * 20.0 / 60.0;
                        int courseDays = (int) Math.ceil(courseHours / hours);
                        if (courseDays < 1) courseDays = 1;

                        path.append("#### 第 ").append(i + 1).append(" 阶段：").append(course.getName()).append("\n");
                        path.append("- **预计学习时长**：").append(String.format("%.1f", courseHours)).append(" 小时\n");
                        path.append("- **预计学习天数**：").append(courseDays).append(" 天\n");
                        path.append("- **学习难度**：⭐⭐").append(i > 1 ? "⭐" : "").append("\n\n");

                        totalDays += courseDays;
                        totalHours += courseHours;
                    }
                } catch (Exception ignored) {}
            }

            path.append("### 📊 总体安排\n\n");
            path.append("- **总课程数**：").append(courseIds.size()).append(" 门\n");
            path.append("- **总学习时长**：约 ").append(String.format("%.1f", totalHours)).append(" 小时\n");
            path.append("- **预计总天数**：").append(totalDays).append(" 天\n");
            path.append("- **每日学习**：").append(hours).append(" 小时\n\n");

            path.append("### 💡 学习建议\n\n");
            path.append("1. **循序渐进**：按照顺序学习，打好基础再进阶\n");
            path.append("2. **阶段总结**：每完成一门课程，做一次总结复盘\n");
            path.append("3. **项目实践**：学完一个阶段后，找一个实际项目练手\n");
            path.append("4. **保持节奏**：不要急于求成，稳扎稳打更重要\n\n");

            path.append("需要我为你生成详细的每日学习计划吗？");

            return path.toString();
        } catch (Exception e) {
            log.error("生成学习路线图失败", e);
            return "生成学习路线图时发生错误：" + e.getMessage();
        }
    }
}
