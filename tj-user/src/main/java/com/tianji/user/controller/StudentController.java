package com.tianji.user.controller;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.utils.UserContext;
import com.tianji.user.domain.dto.StudentFormDTO;
import com.tianji.user.domain.po.UserDetail;
import com.tianji.user.domain.query.UserPageQuery;
import com.tianji.user.domain.vo.StudentPageVo;
import com.tianji.user.service.IStudentService;
import com.tianji.user.service.IUserDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 学员详情表 前端控制器
 */

@RestController
@RequestMapping("/students")
@Tag(name = "用户管理接口")
public class StudentController {

    @Autowired
    private IStudentService studentService;
    @Autowired
    private IUserDetailService detailService;

    @Operation(summary = "分页查询学生信息")
    @GetMapping("/page")
    public PageDTO<StudentPageVo> queryStudentPage(UserPageQuery pageQuery){
        return studentService.queryStudentPage(pageQuery);
    }

    @Operation(summary = "学员注册")
    @PostMapping("/register")
    public void registerStudent(@RequestBody StudentFormDTO studentFormDTO) {
        studentService.saveStudent(studentFormDTO);
    }

    @Operation(summary = "修改学员密码")
    @PutMapping("/password")
    public void updateMyPassword(@RequestBody StudentFormDTO studentFormDTO) {
        studentService.updateMyPassword(studentFormDTO);
    }

    @Operation(summary = "修改当前登录学员信息")
    @PutMapping
    public void updateStudentInfo(@RequestBody Map<String, Object> body) {
        Long userId = UserContext.getUser();
        UserDetail detail = new UserDetail();
        detail.setId(userId);
        Object name = body.get("name");
        if (name instanceof String) {
            detail.setName((String) name);
        }
        Object gender = body.get("gender");
        if (gender instanceof Integer) {
            detail.setGender((Integer) gender);
        } else if (gender instanceof String) {
            try {
                detail.setGender(Integer.parseInt((String) gender));
            } catch (NumberFormatException ignored) {
            }
        }
        Object qq = body.get("qq");
        if (qq instanceof String) {
            detail.setQq((String) qq);
        }
        Object email = body.get("email");
        if (email instanceof String) {
            detail.setEmail((String) email);
        }
        Object intro = body.get("intro");
        if (intro instanceof String) {
            detail.setIntro((String) intro);
        }
        Object icon = body.get("icon");
        if (icon instanceof String) {
            detail.setIcon((String) icon);
        }
        Object provinceName = body.get("provinceName");
        if (provinceName instanceof String) {
            detail.setProvince((String) provinceName);
        }
        Object cityName = body.get("cityName");
        if (cityName instanceof String) {
            detail.setCity((String) cityName);
        }
        Object districtName = body.get("districtName");
        if (districtName instanceof String) {
            detail.setDistrict((String) districtName);
        }
        detailService.updateById(detail);
    }
}
