package com.tianji.aigc.tools;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.tianji.aigc.config.ToolResultHolder;
import com.tianji.aigc.constants.Constant;
import com.tianji.aigc.tools.result.CourseInfo;
import com.tianji.api.client.course.CategoryClient;
import com.tianji.api.client.course.CourseClient;
import com.tianji.api.client.learning.LearningClient;
import com.tianji.api.dto.course.CourseFullInfoDTO;
import com.tianji.api.dto.course.CourseSimpleInfoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CourseTools {

    private final CourseClient courseClient;
    private final CategoryClient categoryClient;
    private final LearningClient learningClient;
    private static final String FIELD_NAME_FORMAT = "{}_{}";

    @Tool(description = Constant.Tools.QUERY_COURSE_BY_ID)
    public CourseInfo queryCourseById(@ToolParam(description = Constant.ToolParams.COURSE_ID) Long courseId, ToolContext toolContext) {
        return Optional.ofNullable(courseId)
                .map(id -> this.courseClient.baseInfo(id, true))
                .map(CourseInfo::of)
                .map(courseInfo -> {
                    var requestId = MapUtil.get(toolContext.getContext(), Constant.REQUEST_ID, String.class);
                    var field = StrUtil.format(FIELD_NAME_FORMAT, StrUtil.lowerFirst(CourseInfo.class.getSimpleName()), courseId);
                    ToolResultHolder.put(requestId, field, courseInfo);
                    return courseInfo;
                })
                .orElse(null);
    }

    @Tool(description = "查询课程完整信息，包括章节目录、教师信息等")
    public CourseFullInfoDTO getCourseFullInfo(
            @ToolParam(description = "课程id") Long courseId,
            @ToolParam(description = "是否包含目录") Boolean withCatalogue,
            @ToolParam(description = "是否包含教师") Boolean withTeachers) {
        try {
            return courseClient.getCourseInfoById(courseId,
                    withCatalogue != null ? withCatalogue : true,
                    withTeachers != null ? withTeachers : true);
        } catch (Exception e) {
            return null;
        }
    }

    @Tool(description = "根据课程id列表批量查询课程简单信息，用于课程对比")
    public List<CourseInfo> queryCoursesByIds(
            @ToolParam(description = "课程id列表") List<Long> courseIds,
            ToolContext toolContext) {
        if (courseIds == null || courseIds.isEmpty()) {
            return List.of();
        }
        try {
            List<CourseSimpleInfoDTO> simpleInfoList = courseClient.getSimpleInfoList(courseIds);
            return simpleInfoList.stream()
                    .map(dto -> {
                        CourseInfo info = new CourseInfo();
                        info.setId(dto.getId());
                        info.setName(dto.getName());
                        info.setPrice(dto.getPrice() != null ? dto.getPrice() / 100.0 : 0.0);
                        info.setValidDuration(dto.getValidDuration());
                        return info;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    @Tool(description = "获取在线课程id列表")
    public List<Long> getOnlineCourseIds() {
        try {
            return courseClient.queryOnlineCourseIds();
        } catch (Exception e) {
            return List.of();
        }
    }

    @Tool(description = "查询课程学习人数")
    public Integer getCourseLearnerCount(@ToolParam(description = "课程id") Long courseId) {
        try {
            return learningClient.countLearningLessonByCourse(courseId);
        } catch (Exception e) {
            return 0;
        }
    }

    @Tool(description = "查询当前用户指定课程的学习进度")
    public com.tianji.api.dto.leanring.LearningLessonDTO getLearningProgress(
            @ToolParam(description = "课程id") Long courseId) {
        try {
            return learningClient.queryLearningRecordByCourse(courseId);
        } catch (Exception e) {
            return null;
        }
    }

    @Tool(description = "校验当前用户是否可以学习该课程")
    public Boolean isLessonValid(@ToolParam(description = "课程id") Long courseId) {
        try {
            Long valid = learningClient.isLessonValid(courseId);
            return valid != null && valid > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Tool(description = "根据分类id获取分类名称")
    public String getCategoryName(@ToolParam(description = "分类id") Long categoryId) {
        try {
            List<com.tianji.api.dto.course.CategoryBasicDTO> categories = categoryClient.getAllOfOneLevel();
            if (categories != null) {
                for (var cat : categories) {
                    if (cat.getId() != null && cat.getId().equals(categoryId)) {
                        return cat.getName();
                    }
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Tool(description = "获取所有一级分类，用于课程推荐和分类导航")
    public List<com.tianji.api.dto.course.CategoryBasicDTO> getAllCategories() {
        try {
            return categoryClient.getAllOfOneLevel();
        } catch (Exception e) {
            return List.of();
        }
    }
}
