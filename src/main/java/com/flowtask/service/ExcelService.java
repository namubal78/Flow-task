package com.flowtask.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class ExcelService {

    /**
     * 커스텀 확장자 업로드용 양식 엑셀 파일 생성
     * 구조: 1행=제목, 2행=헤더(구분|확장자|EX) zip), 3행~=데이터 입력 영역
     */
    public byte[] createTemplate() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = wb.createSheet("확장자 차단 목록");

            // ── 스타일: 제목
            XSSFCellStyle titleStyle = wb.createCellStyle();
            XSSFFont titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 13);
            titleFont.setColor(new XSSFColor(new byte[]{(byte)255, (byte)255, (byte)255}, null));
            titleStyle.setFont(titleFont);
            titleStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte)92, (byte)59, (byte)193}, null));
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            // ── 스타일: 헤더
            XSSFCellStyle headerStyle = wb.createCellStyle();
            XSSFFont headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte)230, (byte)225, (byte)245}, null));
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // ── 스타일: 데이터 셀 (가운데 정렬)
            XSSFCellStyle dataStyle = wb.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.CENTER);

            // Row 1: 제목
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(24);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("차단할 커스텀 확장자");
            titleCell.setCellStyle(titleStyle);

            // Row 2: 헤더 (단일 컬럼)
            Row headerRow = sheet.createRow(1);
            headerRow.setHeightInPoints(20);
            Cell headerCell = headerRow.createCell(0);
            headerCell.setCellValue("확장자 ex)zip");
            headerCell.setCellStyle(headerStyle);

            // 컬럼 너비 (22자 기준) + 데이터 영역 기본 스타일 가운데 정렬
            sheet.setColumnWidth(0, 22 * 256);
            sheet.setDefaultColumnStyle(0, dataStyle);

            wb.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 업로드된 엑셀에서 확장자 목록 파싱
     * 3행부터 B열(index 1) 값을 읽음
     */
    public List<String> parseUpload(MultipartFile file) throws IOException {
        List<String> extensions = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Cell cell = row.getCell(0);  // A열
                if (cell == null) continue;
                String val = getCellValue(cell).trim().toLowerCase();
                if (!val.isEmpty()) {
                    extensions.add(val);
                }
            }
        }
        return extensions;
    }

    private String getCellValue(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default      -> "";
        };
    }
}
