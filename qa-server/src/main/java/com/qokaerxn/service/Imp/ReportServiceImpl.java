package com.qokaerxn.service.Imp;

import com.qokaerxn.constant.StatusConstant;
import com.qokaerxn.dto.GoodsSalesDTO;
import com.qokaerxn.entity.Orders;
import com.qokaerxn.entity.User;
import com.qokaerxn.mapper.OrdersMapper;
import com.qokaerxn.mapper.UserMapper;
import com.qokaerxn.service.ReportService;
import com.qokaerxn.service.WorkspaceService;
import com.qokaerxn.vo.*;
import io.swagger.models.auth.In;
import net.bytebuddy.asm.Advice;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 营业额统计
     *
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {

        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Double> turnOverList = new ArrayList<>();

        for (LocalDate date : dateList) {

            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap<>();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            map.put("status", Orders.COMPLETED);

            Double sumOfDay = ordersMapper.getSumOfDayByMap(map);

            sumOfDay = sumOfDay == null ? 0.0 : sumOfDay;

            turnOverList.add(sumOfDay);

        }

        TurnoverReportVO turnoverReportVO = TurnoverReportVO
                .builder()
                .turnoverList(StringUtils.join(turnOverList, ","))
                .dateList(StringUtils.join(dateList, ","))
                .build();

        return turnoverReportVO;
    }


    /**
     * 统计指定时间区间内的用户数据
     *
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Integer> totalUserList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();

        for (LocalDate date : dateList) {

            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //用动态的方法获得不同条件的用户数量
            Map map = new HashMap<>();

            //获得总用户数量时只需要该天结束时间就好
            map.put("endTime", endTime);
            Integer totalUser = userMapper.countByMap(map);
            totalUserList.add(totalUser);

            //获得新用户数量时，同时需要开始和结束时间
            map.put("beginTime", beginTime);
            Integer newUser = userMapper.countByMap(map);
            newUserList.add(newUser);
        }

        UserReportVO userReportVO = UserReportVO
                .builder()
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .dateList(StringUtils.join(dateList, ","))
                .build();

        return userReportVO;
    }


    /**
     * 订单统计
     *
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Integer> orderCountList = new ArrayList<>();

        List<Integer> validOrderCountList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap<>();

            map.put("endTime", endTime);
            map.put("beginTime", beginTime);

            Integer orderCount = ordersMapper.countByMap(map);
            orderCountList.add(orderCount);

            map.put("status", Orders.COMPLETED);
            Integer validOrderCount = ordersMapper.countByMap(map);
            validOrderCountList.add(validOrderCount);
        }

        //Integer totalOrderCount = 0;
        //for (Integer integer : orderCountList) {
        //    totalOrderCount += integer;
        //}

        //获得总订单总数
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        //获得有效订单总数
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();

        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 销量排名top10
     *
     * @param begin
     * @param end
     * @return
     */
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {

        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> top10Orders = ordersMapper.getTop10Orders(beginTime, endTime);

        List<String> nameList = new ArrayList<>();
        List<Integer> numberList = new ArrayList<>();

        for (GoodsSalesDTO top10Order : top10Orders) {
            nameList.add(top10Order.getName());
            numberList.add(top10Order.getNumber());
        }

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();
    }

    /**
     * 导出运营数据报表
     *
     * @param response
     */
    public void exportBusinessData(HttpServletResponse response) {

        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);

        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));

        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {

            XSSFWorkbook excel = new XSSFWorkbook(in);
            XSSFSheet sheet = excel.getSheet("sheet1");

            //填充时间
            XSSFRow row1 = sheet.getRow(1);
            row1.getCell(1).setCellValue("时间："+ begin + "至" + end);

            //填充营业额
            sheet.getRow(3).getCell(2).setCellValue(businessData.getTurnover());
            //填充订单完成率
            sheet.getRow(3).getCell(4).setCellValue(businessData.getOrderCompletionRate());
            //填充新增用户
            sheet.getRow(3).getCell(6).setCellValue(businessData.getNewUsers());
            //填充有效订单
            sheet.getRow(4).getCell(2).setCellValue(businessData.getValidOrderCount());
            //填充客单价
            sheet.getRow(4).getCell(4).setCellValue(businessData.getUnitPrice());

            for (int i = 0; i < 30; i++) {
                LocalDate day = begin.plusDays(i);
                BusinessDataVO businessData1 = workspaceService.getBusinessData(LocalDateTime.of(day, LocalTime.MAX), LocalDateTime.of(day, LocalTime.MAX));
                sheet.getRow(i+7).getCell(1).setCellValue(day.toString());
                sheet.getRow(i+7).getCell(2).setCellValue(businessData1.getTurnover());
                sheet.getRow(i+7).getCell(3).setCellValue(businessData1.getValidOrderCount());
                sheet.getRow(i+7).getCell(4).setCellValue(businessData1.getOrderCompletionRate());
                sheet.getRow(i+7).getCell(5).setCellValue(businessData1.getUnitPrice());
                sheet.getRow(i+7).getCell(6).setCellValue(businessData1.getNewUsers());
            }

            //发送给前端
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);

            outputStream.close();
            excel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
