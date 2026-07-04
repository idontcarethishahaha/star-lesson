package com.tianji.search.service.impl;

import com.tianji.api.cache.CategoryCache;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.constants.ErrorInfo;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.exceptions.CommonException;
import com.tianji.common.utils.*;
import com.tianji.search.config.InterestsProperties;
import com.tianji.search.constants.SearchErrorInfo;
import com.tianji.search.domain.po.Course;
import com.tianji.search.domain.query.CoursePageQuery;
import com.tianji.search.domain.vo.CourseVO;
import com.tianji.search.repository.CourseRepository;
import com.tianji.search.service.IInterestsService;
import com.tianji.search.service.ISearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.tianji.search.repository.CourseRepository.PUBLISH_TIME;

@Slf4j
@Service
public class SearchServiceImpl implements ISearchService {

    @Autowired
    private RestHighLevelClient restClient;

    @Autowired
    private IInterestsService interestsService;

    @Autowired
    private UserClient userClient;

    @Autowired
    private CategoryCache categoryCache;

    @Autowired
    private InterestsProperties interestsProperties;

    @Override
    public List<CourseVO> queryCourseByCateId(Long cateLv2Id) {
        try {
            return queryTopNByCategoryIdLv2sAndFree(
                    CollUtils.singletonList(cateLv2Id), null, PUBLISH_TIME, false, 10);
        } catch (Exception e) {
            log.warn("queryCourseByCateId failed: {}", e.getMessage());
            return CollUtils.emptyList();
        }
    }

    @Override
    public List<CourseVO> queryBestTopN() {
        try {
            return queryTopNCourseOnMarketByFree(false, CourseRepository.SOLD);
        } catch (Exception e) {
            log.warn("queryBestTopN failed: {}", e.getMessage());
            return CollUtils.emptyList();
        }
    }

    @Override
    public List<CourseVO> queryNewTopN() {
        try {
            return queryTopNCourseOnMarketByFree(false, PUBLISH_TIME);
        } catch (Exception e) {
            log.warn("queryNewTopN failed: {}", e.getMessage());
            return CollUtils.emptyList();
        }
    }

    @Override
    public List<CourseVO> queryFreeTopN() {
        try {
            return queryTopNCourseOnMarketByFree(true, CourseRepository.SOLD);
        } catch (Exception e) {
            log.warn("queryFreeTopN failed: {}", e.getMessage());
            return CollUtils.emptyList();
        }
    }

    private List<CourseVO> queryTopNCourseOnMarketByFree(boolean isFree, String sortBy) {
        // 1.获取当前用户
        Long id = UserContext.getUser();
        // 2.查询课程
        List<CourseVO> courses = null;
        if (id == null) {
            // 3.未登录，直接查询报名人数最多的
            courses = queryTopNByCategoryIdLv2sAndFree(
                    null, isFree, sortBy, false, interestsProperties.getTopNumber());
        } else {
            // 4.已登录，根据兴趣爱好查询
            List<Long> categoryIds = interestsService.queryMyInterestsIds();
            if (CollUtils.isEmpty(categoryIds)) {
                // 4.1.没有兴趣爱好，直接查询报名人数最多的
                courses = queryTopNByCategoryIdLv2sAndFree(
                        null, isFree, sortBy, false, interestsProperties.getTopNumber());
            } else {
                // 4.2.有爱好.查询爱好课程中报名人数最多的
                courses = queryTopNByCategoryIdLv2sAndFree(
                        categoryIds, isFree, sortBy, false, interestsProperties.getTopNumber());
            }
        }
        return courses;
    }

    // 课程封面图片直接访问 MinIO
    private static final String MINIO_COVER_URL = "http://192.168.227.128:9000/tj-media/img-course-cover/";
    // 教师头像图片直接访问 MinIO
    private static final String MINIO_AVATAR_URL = "http://192.168.227.128:9000/tj-media/img-tx/";

    private List<CourseVO> queryTopNByCategoryIdLv2sAndFree(
            List<Long> categoryIds, Boolean isFree, String sortBy, boolean isASC, int n) {
        // 1.准备Request
        SearchRequest request = new SearchRequest(CourseRepository.INDEX_NAME);
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // 1.1.是否免费
        if(isFree != null) {
            queryBuilder.filter(QueryBuilders.termQuery(CourseRepository.FREE, isFree));
        }
        // 1.2.分类id
        if (categoryIds != null) {
            if (categoryIds.size() == 1) {
                queryBuilder.filter(QueryBuilders.termQuery(CourseRepository.CATEGORY_ID_LV2, categoryIds.get(0)));
            } else {
                queryBuilder.filter(QueryBuilders.termsQuery(CourseRepository.CATEGORY_ID_LV2, categoryIds));
            }
        }
        if(isFree != null || categoryIds != null) {
            request.source().query(queryBuilder);
        }
        // 1.3.TopN
        request.source().size(n).sort(sortBy, isASC ? SortOrder.ASC : SortOrder.DESC);
        // 2.发送请求
        SearchResponse response = null;
        try {
            response = restClient.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new CommonException(SearchErrorInfo.QUERY_COURSE_ERROR, e);
        }
        // 3.解析
        SearchHits searchHits = response.getHits();
        SearchHit[] hits = searchHits.getHits();
        if (hits == null || hits.length == 0) {
            return CollUtils.emptyList();
        }
        List<CourseVO> courses = new ArrayList<>(hits.length);
        Set<Long> teacherIds = new HashSet<>(hits.length);
        for (SearchHit hit : hits) {
            // 3.1.数据转换
            CourseVO vo = JsonUtils.toBean(hit.getSourceAsString(), CourseVO.class);
            // 3.2.获取教师id
            String teacherStr = vo.getTeacher();
            if (StringUtils.isNotBlank(teacherStr)) {
                try {
                    teacherIds.add(Long.valueOf(teacherStr));
                } catch (NumberFormatException e) {
                    // teacher字段不是有效数字，忽略
                }
            }
            // 3.3.保存
            courses.add(vo);
        }
        teacherIds.remove(0L);
        if (teacherIds.isEmpty()) {
            // 没有有效的教师id，直接返回
            for (CourseVO c : courses) {
                c.setTeacher("匿名");
            }
            return courses;
        }
        // 4.查询教师
        List<UserDTO> teachers = userClient.queryUserByIds(teacherIds);
        if (CollUtils.isEmpty(teachers)) {
            // 用户服务不可用或无匹配教师，使用默认值
            for (CourseVO c : courses) {
                c.setTeacher("匿名");
            }
            return courses;
        }
        Map<Long, UserDTO> tMap = teachers.stream()
                .collect(Collectors.toMap(UserDTO::getId, t -> t));
        for (CourseVO c : courses) {
            String teacherStr = c.getTeacher();
            if (StringUtils.isNotBlank(teacherStr)) {
                try {
                    Long teacherId = Long.valueOf(teacherStr);
                    UserDTO teacher = tMap.get(teacherId);
                    if (teacher != null) {
                        c.setTeacher(teacher.getName());
                        c.setIcon(teacher.getIcon());
                    } else {
                        c.setTeacher("匿名");
                    }
                } catch (NumberFormatException e) {
                    c.setTeacher("匿名");
                }
            } else {
                c.setTeacher("匿名");
            }
        }
        normalizeCoverUrls(courses);
        return courses;
    }

    /**
     * 统一封面和头像 URL，直接访问 MinIO：
     * - http://192.168.227.128:9000/tj-media/... → 原样
     * - http://旧IP/...                          → 提取文件名，拼成 MinIO URL
     * - /files/public/xxx                        → 提取文件名，拼成 MinIO URL
     * - xxx.png                                  → 拼成 MinIO URL
     */
    private void normalizeCoverUrls(List<CourseVO> courses) {
        if (CollUtils.isEmpty(courses)) {
            return;
        }
        for (CourseVO c : courses) {
            // 处理封面
            normalizeUrl(c::getCoverUrl, c::setCoverUrl, MINIO_COVER_URL);
            // 处理教师头像
            normalizeUrl(c::getIcon, c::setIcon, MINIO_AVATAR_URL);
        }
    }

    private void normalizeUrl(java.util.function.Supplier<String> getter, java.util.function.Consumer<String> setter, String baseUrl) {
        String url = getter.get();
        if (StringUtils.isBlank(url)) {
            return;
        }
        String trimmed = url.trim();
        if (trimmed.startsWith("http://192.168.227.128:9000/")) {
            return;
        }
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            int lastSlash = trimmed.lastIndexOf('/');
            if (lastSlash > 0 && lastSlash < trimmed.length() - 1) {
                String filename = trimmed.substring(lastSlash + 1);
                filename = removeDuplicateSuffix(filename);
                setter.accept(baseUrl + filename);
            }
            return;
        }
        if (trimmed.startsWith("/files/public/")) {
            String filename = trimmed.substring("/files/public/".length());
            filename = removeDuplicateSuffix(filename);
            setter.accept(baseUrl + filename);
            return;
        }
        if (trimmed.startsWith("/files/")) {
            String filename = trimmed.substring("/files/".length());
            filename = removeDuplicateSuffix(filename);
            setter.accept(baseUrl + filename);
            return;
        }
        if (trimmed.startsWith("files/")) {
            String filename = trimmed.substring("files/".length());
            filename = removeDuplicateSuffix(filename);
            setter.accept(baseUrl + filename);
            return;
        }
        if (!trimmed.startsWith("/")) {
            String filename = removeDuplicateSuffix(trimmed);
            setter.accept(baseUrl + filename);
            return;
        }
    }

    private String removeDuplicateSuffix(String filename) {
        if (filename == null) return null;
        // 处理 .png.png, .jpg.jpg 等重复后缀
        if (filename.endsWith(".png.png")) {
            return filename.substring(0, filename.length() - 4);
        }
        if (filename.endsWith(".jpg.jpg")) {
            return filename.substring(0, filename.length() - 4);
        }
        if (filename.endsWith(".jpeg.jpeg")) {
            return filename.substring(0, filename.length() - 5);
        }
        if (filename.endsWith(".gif.gif")) {
            return filename.substring(0, filename.length() - 4);
        }
        if (filename.endsWith(".webp.webp")) {
            return filename.substring(0, filename.length() - 5);
        }
        return filename;
    }

    @Override
    public PageDTO<CourseVO> queryCoursesForPortal(CoursePageQuery query) {
        try {
            // 1.搜索数据
            SearchResponse response = searchForResponse(query, CourseVO.EXCLUDE_FIELDS);
            // 2.解析响应
            PageDTO<Course> result = handleSearchResponse(response, query.getPageSize());
            // 3.处理VO
            List<Course> list = result.getList();
            if (CollUtils.isEmpty(list)) {
                return PageDTO.empty(result.getTotal(), result.getPages());
            }
            // 3.1.查询教师信息
            Set<Long> teacherIds = list.stream()
                    .map(Course::getTeacher)
                    .filter(id -> id != null && id > 0)
                    .collect(Collectors.toSet());
            Map<Long, String> teacherMap = new HashMap<>();
            if (!teacherIds.isEmpty()) {
                List<UserDTO> teachers = userClient.queryUserByIds(teacherIds);
                if (CollUtils.isNotEmpty(teachers)) {
                    teacherMap = teachers.stream()
                            .collect(Collectors.toMap(UserDTO::getId, UserDTO::getName));
                }
            }
            // 3.2.转换VO
            List<CourseVO> vos = new ArrayList<>(list.size());
            for (Course c : list) {
                CourseVO vo = BeanUtils.toBean(c, CourseVO.class);
                vo.setTeacher(teacherMap.getOrDefault(c.getTeacher(), "未知"));
                vos.add(vo);
            }
            normalizeCoverUrls(vos);
            return new PageDTO<>(result.getTotal(), result.getPages(), vos);
        } catch (Exception e) {
            log.warn("queryCoursesForPortal failed: {}", e.getMessage());
            return PageDTO.empty(0L, 0L);
        }
    }

    @Override
    public List<Long> queryCoursesIdByName(String keyword) {
        // 1.创建Request
        SearchRequest request = new SearchRequest(CourseRepository.INDEX_NAME);
        // 2.构建DSL
        request.source()
                .query(QueryBuilders.matchPhraseQuery(CourseRepository.DEFAULT_QUERY_NAME, keyword))
                .fetchSource(new String[]{"id"}, null);
        // 3.查询
        SearchResponse response;
        try {
            response = restClient.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new CommonException(SearchErrorInfo.QUERY_COURSE_ERROR, e);
        }
        // 4.解析
        SearchHits searchHits = response.getHits();
        // 4.1.获取hits
        SearchHit[] hits = searchHits.getHits();
        if (hits.length == 0) {
            return CollUtils.emptyList();
        }
        // 4.2.获取id
        return Arrays.stream(hits)
                .map(SearchHit::getId)
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }


    private SearchResponse searchForResponse(CoursePageQuery query, String[] excludeFields) {
        // 1.创建Request
        SearchRequest request = new SearchRequest(CourseRepository.INDEX_NAME);
        // 2.构建DSL
        // 2.1.构建query
        buildBasicQuery(request, query);
        // 2.2.排序
        String sortBy = query.getSortBy();
        if (StringUtils.isNotBlank(sortBy)) {
            request.source().sort(sortBy, query.getIsAsc() ? SortOrder.ASC : SortOrder.DESC);
        }
        // 2.3.分页
        request.source().from(query.from()).size(query.getPageSize());
        // 2.4.高亮
        request.source().highlighter(new HighlightBuilder().field(CourseRepository.DEFAULT_QUERY_NAME));
        // 2.5.source处理
        request.source().fetchSource(null, excludeFields);
        // 3.发送请求
        SearchResponse response = null;
        try {
            response = restClient.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new CommonException(ErrorInfo.Msg.SERVER_INTER_ERROR, e);
        }
        return response;
    }

    private void buildBasicQuery(SearchRequest request, CoursePageQuery query) {
        // 1.准备bool查询
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // 2.关键字搜索
        String keyword = query.getKeyword();
        if (StringUtils.isBlank(keyword)) {
            queryBuilder.must(QueryBuilders.matchAllQuery());
        } else {
            queryBuilder.must(QueryBuilders.matchPhraseQuery(CourseRepository.DEFAULT_QUERY_NAME, keyword));
        }
        // 3.其它条件
        if (query.getCategoryIdLv1() != null) {
            queryBuilder.filter(QueryBuilders.termQuery(CourseRepository.CATEGORY_ID_LV1, query.getCategoryIdLv1()));
        }
        if (query.getCategoryIdLv2() != null) {
            queryBuilder.filter(QueryBuilders.termQuery(CourseRepository.CATEGORY_ID_LV2, query.getCategoryIdLv2()));
        }
        if (query.getCategoryIdLv3() != null) {
            queryBuilder.filter(QueryBuilders.termQuery(CourseRepository.CATEGORY_ID_LV3, query.getCategoryIdLv3()));
        }
        if (query.getFree() != null) {
            queryBuilder.filter(QueryBuilders.termQuery(CourseRepository.FREE, query.getFree()));
        }
        if (query.getType() != null) {
            queryBuilder.filter(QueryBuilders.termQuery(CourseRepository.TYPE, query.getType()));
        }
        LocalDateTime beginTime = query.getBeginTime();
        LocalDateTime endTime = query.getEndTime();
        if(beginTime != null || endTime != null) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery(CourseRepository.UPDATE_TIME);
            if (beginTime != null) {
                rangeQuery.gte(beginTime);
            }
            if (endTime != null) {
                rangeQuery.lte(endTime);
            }
            queryBuilder.filter(rangeQuery);
        }
        // 4.写入request
        request.source().query(queryBuilder);
    }

    private PageDTO<Course> handleSearchResponse(SearchResponse response, int pageSize) {
        SearchHits searchHits = response.getHits();
        // 1.总条数
        long total = searchHits.getTotalHits().value;
        // 2.总页数
        long totalPages = (total + pageSize - 1) / pageSize;
        // 3.获取命中的数据
        SearchHit[] hits = searchHits.getHits();
        if (hits.length <= 0) {
            return new PageDTO<>(total, totalPages, CollUtils.emptyList());
        }
        // 4.遍历
        List<Course> list = new ArrayList<>(hits.length);
        for (SearchHit hit : hits) {
            // 5.获取某一条source
            String jsonSource = hit.getSourceAsString();
            // 6.反序列化
            Course course = JsonUtils.toBean(jsonSource, Course.class);
            // 7.处理高亮
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (CollUtils.isNotEmpty(highlightFields)) {
                // 7.1.获取高亮结果
                HighlightField field = highlightFields.get(CourseRepository.DEFAULT_QUERY_NAME);
                Object[] fragments = field.getFragments();
                String value = StringUtils.join(fragments);
                // 7.2.覆盖非高亮结果
                course.setName(value);
            }
            list.add(course);
        }
        return new PageDTO<>(total, totalPages, list);
    }
}
