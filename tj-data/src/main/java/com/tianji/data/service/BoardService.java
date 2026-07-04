package com.tianji.data.service;


import com.tianji.data.model.dto.BoardDataSetDTO;
import com.tianji.data.model.vo.CourseBoardVO;
import com.tianji.data.model.vo.EchartsVO;
import com.tianji.data.model.vo.OrderBoardVO;

import java.util.List;

/**
 * @ClassName BoardService
 * @Author wuwenjin
 * @Date 2022/10/10 16:30
 * @Version
 **/
public interface BoardService {

    /**
     * 看板数据获取
     *
     * @param types 数据类型
     * @return
     */
    EchartsVO boardData(List<Integer> types);

    /**
     * 设置看板数据
     *
     * @param boardDataSetDTO
     */
    void setBoardData(BoardDataSetDTO boardDataSetDTO);

    /**
     * 订单看板数据获取
     *
     * @return
     */
    OrderBoardVO getOrderBoard();

    /**
     * 课程看板数据获取
     *
     * @return
     */
    CourseBoardVO getCourseBoard();
}