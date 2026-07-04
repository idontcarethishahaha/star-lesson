package com.tianji.search.init;

import com.tianji.api.client.course.CourseClient;
import com.tianji.api.dto.course.CourseSearchDTO;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.search.domain.po.Course;
import com.tianji.search.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseIndexInitRunner implements ApplicationRunner {

    private final CourseClient courseClient;
    private final CourseRepository courseRepository;

    @Override
    public void run(ApplicationArguments args) {
        log.info("========== [课程索引初始化] 开始检查 ==========");
        try {
            // 1.检查索引是否健康
            long docCount = courseRepository.getDocCount();
            log.info("[课程索引初始化] 当前索引文档数: {}", docCount);

            if (docCount > 0) {
                log.info("[课程索引初始化] 索引健康且已有数据，跳过全量同步");
                return;
            }

            log.warn("[课程索引初始化] 索引不存在/无文档/损坏，开始全量同步...");
            doFullSync();
        } catch (Exception e) {
            log.error("[课程索引初始化] 发生异常: {}", e.getMessage(), e);
        }
        log.info("========== [课程索引初始化] 检查结束 ==========");
    }

    /**
     * 全量同步课程数据到ES（可被 controller 手动调用）
     */
    public void doFullSync() {
        try {
            // 1.获取所有已上架课程ID
            log.info("[课程全量同步] 1. 从 course-service 获取已上架课程ID...");
            List<Long> onlineCourseIds = courseClient.queryOnlineCourseIds();
            if (CollUtils.isEmpty(onlineCourseIds)) {
                log.warn("[课程全量同步] 没有发现已上架课程");
                return;
            }
            log.info("[课程全量同步] 发现{}个已上架课程", onlineCourseIds.size());

            // 2.重建索引（删除旧索引，创建新索引）
            log.info("[课程全量同步] 2. 重建 ES 索引...");
            courseRepository.rebuildIndex();

            // 3.批量拉取课程数据并写入ES
            log.info("[课程全量同步] 3. 同步课程数据到索引...");
            List<Course> batch = new ArrayList<>(50);
            int successCount = 0;
            int failCount = 0;
            for (int i = 0; i < onlineCourseIds.size(); i++) {
                Long courseId = onlineCourseIds.get(i);
                try {
                    CourseSearchDTO dto = courseClient.getSearchInfo(courseId);
                    if (dto == null) {
                        failCount++;
                        continue;
                    }
                    Course course = BeanUtils.toBean(dto, Course.class);
                    course.setType(dto.getCourseType());
                    batch.add(course);
                    // 每50条批量写入一次
                    if (batch.size() >= 50) {
                        courseRepository.saveAll(new ArrayList<>(batch));
                        successCount += batch.size();
                        batch.clear();
                        log.info("[课程全量同步] 进度: {}/{}", successCount, onlineCourseIds.size());
                    }
                } catch (Exception e) {
                    failCount++;
                    log.warn("[课程全量同步] 同步课程{}失败: {}", courseId, e.getMessage());
                }
            }
            // 写入剩余数据
            if (!batch.isEmpty()) {
                courseRepository.saveAll(batch);
                successCount += batch.size();
            }
            log.info("[课程全量同步] 完成！成功{}个，失败{}个", successCount, failCount);
        } catch (Exception e) {
            log.error("[课程全量同步] 失败: {}", e.getMessage(), e);
        }
    }
}
