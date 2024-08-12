package com.qokaerxn.service;

import com.qokaerxn.vo.*;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

public interface ReportService {
    /**
     *营业额统计
     * @param begin
     * @param end
     * @return
     */
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);


    /**
     * 统计指定时间区间内的用户数据
     * @param begin
     * @param end
     * @return
     */
     UserReportVO getUserStatistics(LocalDate begin, LocalDate end);

    OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end);

    SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end);

    /**
     * 导出运营数据报表
     * @param response
     */
    void exportBusinessData(HttpServletResponse response);
}
