package com.tianji.search.repository;

import com.tianji.search.domain.po.Course;

import java.util.List;
import java.util.Optional;

public interface CourseRepository{
    String INDEX_NAME = "course";
    String DEFAULT_QUERY_NAME = "name";
    String CATEGORY_ID_LV1 = "categoryIdLv1";
    String CATEGORY_ID_LV2 = "categoryIdLv2";
    String CATEGORY_ID_LV3 = "categoryIdLv3";
    String PUBLISH_TIME = "publishTime";
    String FREE = "free";
    String STATUS = "status";
    String TYPE = "type";
    String UPDATE_TIME = "updateTime";
    String SOLD = "sold";

    /**
     * <h1>更新sold的脚本</h1>
     * <pre>
     * {
     *   "script": {
     *     "lang": "painless",
     *     "source": "ctx._source.sold += params.count"
     *   }
     * }
     * </pre>
     */
    String INCREMENT_SOLD_SCRIPT_ID = "increment_sold";
    String INCREMENT_SOLD_SCRIPT_PARAM = "count";

    void save(Course course);

    void deleteById(Long courseId);

    Optional<Course> findById(Long courseId);

    void updateById(Long courseId, Object ... docs);

    void increment(Long courseId, String field, int amount);

    void incrementSold(List<Long> courseIds, int amount);

    void saveAll(List<Course> list);

    void deleteByIds(List<Long> courseIds);

    /**
     * 检查索引是否存在且健康
     * @return true 表示索引可用
     */
    boolean isIndexHealthy();

    /**
     * 删除并重建索引
     */
    void rebuildIndex();

    /**
     * 获取索引中的文档数量
     * @return 文档数，失败返回 -1
     */
    long getDocCount();
}
