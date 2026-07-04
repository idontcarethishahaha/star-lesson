package com.tianji.search.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tianji.search.domain.po.Course;
import com.tianji.search.repository.CourseRepository;
import com.tianji.common.exceptions.CommonException;
import com.tianji.common.utils.JsonUtils;
import com.tianji.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.tianji.search.constants.SearchErrorInfo.*;

@Slf4j
@Component
public class CourseRepositoryImpl implements CourseRepository {

    private final RestHighLevelClient restHighLevelClient;
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CourseRepositoryImpl(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
        this.restClient = restHighLevelClient.getLowLevelClient();
    }

    @Override
    public void save(Course course) {
        try {
            String endpoint = "/" + INDEX_NAME + "/_doc/" + course.getId();
            Request request = new Request("PUT", endpoint);
            request.setEntity(new NStringEntity(JsonUtils.toJsonStr(course), ContentType.APPLICATION_JSON));
            Response response = restClient.performRequest(request);
            int status = response.getStatusLine().getStatusCode();
            if (status >= 300) {
                String body = EntityUtils.toString(response.getEntity());
                throw new CommonException(SAVE_COURSE_ERROR, new RuntimeException("保存课程失败，ES返回：" + body));
            }
        } catch (CommonException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CommonException(SAVE_COURSE_ERROR, e);
        }
    }

    @Override
    public void deleteById(Long courseId) {
        try {
            String endpoint = "/" + INDEX_NAME + "/_doc/" + courseId;
            Request request = new Request("DELETE", endpoint);
            Response response = restClient.performRequest(request);
            int status = response.getStatusLine().getStatusCode();
            if (status >= 300) {
                String body = EntityUtils.toString(response.getEntity());
                log.warn("删除课程[{}]失败: {}", courseId, body);
            }
        } catch (Exception e) {
            throw new CommonException(SAVE_COURSE_ERROR, e);
        }
    }

    @Override
    public Optional<Course> findById(Long courseId) {
        try {
            String endpoint = "/" + INDEX_NAME + "/_doc/" + courseId;
            Request request = new Request("GET", endpoint);
            Response response = restClient.performRequest(request);
            int status = response.getStatusLine().getStatusCode();
            if (status != 200) {
                return Optional.empty();
            }
            String body = EntityUtils.toString(response.getEntity());
            // 解析 {"_source": {...}}
            if (body == null || !body.contains("_source")) {
                return Optional.empty();
            }
            // 简单解析：找到 "_source" 后面的 JSON 对象
            int sourceStart = body.indexOf("\"_source\":");
            if (sourceStart < 0) return Optional.empty();
            // 找到第一个 { 在 _source 后
            int braceStart = body.indexOf("{", sourceStart);
            if (braceStart < 0) return Optional.empty();
            // 配对找到对应的 }
            int depth = 0;
            int braceEnd = braceStart;
            boolean inString = false;
            boolean escaped = false;
            for (int i = braceStart; i < body.length(); i++) {
                char c = body.charAt(i);
                if (escaped) { escaped = false; continue; }
                if (c == '\\') { escaped = true; continue; }
                if (c == '"') { inString = !inString; continue; }
                if (!inString) {
                    if (c == '{') depth++;
                    else if (c == '}') {
                        depth--;
                        if (depth == 0) { braceEnd = i; break; }
                    }
                }
            }
            String sourceJson = body.substring(braceStart, braceEnd + 1);
            return Optional.ofNullable(JsonUtils.toBean(sourceJson, Course.class));
        } catch (Exception e) {
            log.warn("查询课程[{}]失败: {}", courseId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void updateById(Long courseId, Object... sources) {
        if (sources == null || sources.length == 0 || sources.length % 2 != 0) {
            throw new CommonException(SAVE_COURSE_ERROR, new RuntimeException("更新参数必须成对"));
        }
        try {
            Map<String, Object> doc = new HashMap<>();
            for (int i = 0; i < sources.length; i += 2) {
                doc.put(String.valueOf(sources[i]), sources[i + 1]);
            }
            Map<String, Object> body = new HashMap<>();
            body.put("doc", doc);
            String endpoint = "/" + INDEX_NAME + "/_update/" + courseId;
            Request request = new Request("POST", endpoint);
            request.setEntity(new NStringEntity(JsonUtils.toJsonStr(body), ContentType.APPLICATION_JSON));
            Response response = restClient.performRequest(request);
            int status = response.getStatusLine().getStatusCode();
            if (status >= 300) {
                String respBody = EntityUtils.toString(response.getEntity());
                log.warn("更新课程[{}]失败: {}", courseId, respBody);
            }
        } catch (Exception e) {
            throw new CommonException(UPDATE_COURSE_STATUS_ERROR, e);
        }
    }

    @Override
    public void increment(Long courseId, String field, int amount) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("count", amount);
            Map<String, Object> script = new HashMap<>();
            script.put("source", "ctx._source." + field + " += params.count");
            script.put("lang", "painless");
            script.put("params", params);
            Map<String, Object> body = new HashMap<>();
            body.put("script", script);
            String endpoint = "/" + INDEX_NAME + "/_update/" + courseId;
            Request request = new Request("POST", endpoint);
            request.setEntity(new NStringEntity(JsonUtils.toJsonStr(body), ContentType.APPLICATION_JSON));
            restClient.performRequest(request);
        } catch (Exception e) {
            throw new CommonException(UPDATE_COURSE_STATUS_ERROR, e);
        }
    }

    @Override
    public void incrementSold(List<Long> courseIds, int amount) {
        if (courseIds == null || courseIds.isEmpty()) return;
        try {
            StringBuilder sb = new StringBuilder();
            for (Long courseId : courseIds) {
                Map<String, Object> meta = new HashMap<>();
                Map<String, Object> innerMeta = new HashMap<>();
                innerMeta.put("_id", courseId);
                meta.put("update", innerMeta);
                sb.append(JsonUtils.toJsonStr(meta)).append("\n");
                Map<String, Object> params = new HashMap<>();
                params.put(INCREMENT_SOLD_SCRIPT_PARAM, amount);
                Map<String, Object> script = new HashMap<>();
                script.put("id", INCREMENT_SOLD_SCRIPT_ID);
                script.put("params", params);
                Map<String, Object> body = new HashMap<>();
                body.put("script", script);
                sb.append(JsonUtils.toJsonStr(body)).append("\n");
            }
            String endpoint = "/" + INDEX_NAME + "/_bulk";
            Request request = new Request("POST", endpoint);
            request.setEntity(new NStringEntity(sb.toString(), ContentType.create("application/x-ndjson")));
            Response response = restClient.performRequest(request);
            int status = response.getStatusLine().getStatusCode();
            if (status >= 300) {
                String body = EntityUtils.toString(response.getEntity());
                log.warn("批量增量更新失败: {}", body);
            }
        } catch (Exception e) {
            throw new CommonException(UPDATE_COURSE_STATUS_ERROR, e);
        }
    }

    @Override
    public void saveAll(List<Course> list) {
        if (list == null || list.isEmpty()) return;
        try {
            // 构建 NDJSON bulk 请求体
            StringBuilder sb = new StringBuilder();
            for (Course course : list) {
                // action 行
                Map<String, Object> action = new HashMap<>();
                Map<String, Object> indexAction = new HashMap<>();
                indexAction.put("_id", course.getId());
                action.put("index", indexAction);
                sb.append(JsonUtils.toJsonStr(action)).append("\n");
                // source 行
                sb.append(JsonUtils.toJsonStr(course)).append("\n");
            }
            String endpoint = "/" + INDEX_NAME + "/_bulk";
            Request request = new Request("POST", endpoint);
            request.setEntity(new NStringEntity(sb.toString(), ContentType.create("application/x-ndjson")));
            Response response = restClient.performRequest(request);
            int status = response.getStatusLine().getStatusCode();
            if (status >= 300) {
                String body = EntityUtils.toString(response.getEntity());
                throw new CommonException(SAVE_COURSE_ERROR, new RuntimeException("批量保存课程失败: " + body));
            }
            // 解析响应，检查是否有失败项
            String body = EntityUtils.toString(response.getEntity());
            if (body.contains("\"errors\":true")) {
                log.warn("批量保存有失败项: {}", body);
            }
        } catch (CommonException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CommonException(SAVE_COURSE_ERROR, e);
        }
    }

    @Override
    public void deleteByIds(List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) return;
        try {
            StringBuilder sb = new StringBuilder();
            for (Long courseId : courseIds) {
                Map<String, Object> action = new HashMap<>();
                Map<String, Object> deleteAction = new HashMap<>();
                deleteAction.put("_id", courseId);
                action.put("delete", deleteAction);
                sb.append(JsonUtils.toJsonStr(action)).append("\n");
            }
            String endpoint = "/" + INDEX_NAME + "/_bulk";
            Request request = new Request("POST", endpoint);
            request.setEntity(new NStringEntity(sb.toString(), ContentType.create("application/x-ndjson")));
            Response response = restClient.performRequest(request);
            int status = response.getStatusLine().getStatusCode();
            if (status >= 300) {
                String body = EntityUtils.toString(response.getEntity());
                log.warn("批量删除失败: {}", body);
            }
        } catch (Exception e) {
            throw new CommonException(SAVE_COURSE_ERROR, e);
        }
    }

    @Override
    public boolean isIndexHealthy() {
        try {
            // 1. HEAD /course 检查索引是否存在
            Request headRequest = new Request("HEAD", "/" + INDEX_NAME);
            Response response = restClient.performRequest(headRequest);
            int status = response.getStatusLine().getStatusCode();
            if (status != 200) {
                log.warn("ES索引[{}]不存在，status={}", INDEX_NAME, status);
                return false;
            }
            // 2. GET /course/_count 检查文档数量
            long docCount = getDocCount();
            if (docCount < 0) {
                log.warn("ES索引[{}]查询失败，需要重建", INDEX_NAME);
                return false;
            }
            if (docCount == 0) {
                log.warn("ES索引[{}]存在但无文档，需要重建", INDEX_NAME);
                return false;
            }
            log.info("ES索引[{}]健康，包含{}条文档", INDEX_NAME, docCount);
            return true;
        } catch (Exception e) {
            log.warn("ES索引[{}]不健康: {}", INDEX_NAME, e.getMessage());
            return false;
        }
    }

    @Override
    public long getDocCount() {
        try {
            Request request = new Request("GET", "/" + INDEX_NAME + "/_count");
            Response response = restClient.performRequest(request);
            String body = EntityUtils.toString(response.getEntity());
            // 解析 {"count": 10, "_shards": {...}}
            if (body != null && body.contains("\"count\":")) {
                String countStr = body.split("\"count\":")[1].split(",")[0].trim();
                return Long.parseLong(countStr);
            }
            return -1;
        } catch (Exception e) {
            log.warn("获取索引[{}]文档数失败: {}", INDEX_NAME, e.getMessage());
            return -1;
        }
    }

    @Override
    public void rebuildIndex() {
        try {
            // 1.如果索引存在，先删除
            try {
                Request headRequest = new Request("HEAD", "/" + INDEX_NAME);
                Response headResponse = restClient.performRequest(headRequest);
                if (headResponse.getStatusLine().getStatusCode() == 200) {
                    Request deleteRequest = new Request("DELETE", "/" + INDEX_NAME);
                    restClient.performRequest(deleteRequest);
                    log.info("已删除旧索引[{}]", INDEX_NAME);
                }
            } catch (Exception e) {
                // 索引不存在，忽略
            }
            // 2.创建新索引，指定正确的mapping
            String mapping = "{\n" +
                    "  \"mappings\": {\n" +
                    "    \"properties\": {\n" +
                    "      \"id\": {\"type\": \"long\"},\n" +
                    "      \"name\": {\"type\": \"text\", \"analyzer\": \"standard\", \"search_analyzer\": \"standard\", \"fields\": {\"keyword\": {\"type\": \"keyword\"}}},\n" +
                    "      \"categoryIdLv1\": {\"type\": \"long\"},\n" +
                    "      \"categoryIdLv2\": {\"type\": \"long\"},\n" +
                    "      \"categoryIdLv3\": {\"type\": \"long\"},\n" +
                    "      \"free\": {\"type\": \"boolean\"},\n" +
                    "      \"type\": {\"type\": \"integer\"},\n" +
                    "      \"sold\": {\"type\": \"integer\"},\n" +
                    "      \"price\": {\"type\": \"integer\"},\n" +
                    "      \"score\": {\"type\": \"integer\"},\n" +
                    "      \"teacher\": {\"type\": \"long\"},\n" +
                    "      \"sections\": {\"type\": \"integer\"},\n" +
                    "      \"coverUrl\": {\"type\": \"keyword\"},\n" +
                    "      \"publishTime\": {\"type\": \"date\", \"format\": \"yyyy-MM-dd'T'HH:mm:ss||yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis\"}\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";
            Request createRequest = new Request("PUT", "/" + INDEX_NAME);
            createRequest.setEntity(new NStringEntity(mapping, ContentType.APPLICATION_JSON));
            Response createResponse = restClient.performRequest(createRequest);
            int createStatus = createResponse.getStatusLine().getStatusCode();
            if (createStatus >= 300) {
                String body = EntityUtils.toString(createResponse.getEntity());
                throw new CommonException(SAVE_COURSE_ERROR, new RuntimeException("创建索引失败: " + body));
            }
            log.info("已创建新索引[{}]", INDEX_NAME);
        } catch (CommonException ce) {
            throw ce;
        } catch (Exception e) {
            log.error("重建索引[{}]失败: {}", INDEX_NAME, e.getMessage());
            throw new CommonException(SAVE_COURSE_ERROR, e);
        }
    }
}
