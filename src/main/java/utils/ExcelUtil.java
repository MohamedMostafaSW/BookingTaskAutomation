package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ExcelUtil {

    private static final Logger logger = LogManager.getLogger(ExcelUtil.class);
    private static final String TEST_DATA_PATH = System.getProperty("user.dir") + "/src/test/resources/testdata/";

    /**
     * Read all data from Excel sheet
     *
     * @param fileName  Excel file name
     * @param sheetName Sheet name
     * @return 2D Object array containing test data
     */
    public static Object[][] getTestData(String fileName, String sheetName) {
        Object[][] data = null;

        try {
            String filePath = TEST_DATA_PATH + fileName;
            FileInputStream fis = new FileInputStream(filePath);
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheet(sheetName);

            if (sheet == null) {
                logger.error("Sheet not found: " + sheetName);
                return new Object[0][0];
            }

            int rowCount = sheet.getLastRowNum();
            int colCount = sheet.getRow(0).getLastCellNum();

            logger.info("Reading data from Excel - Rows: " + rowCount + ", Columns: " + colCount);

            data = new Object[rowCount][colCount];

            // Skip header row, start from row 1
            for (int i = 1; i <= rowCount; i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    for (int j = 0; j < colCount; j++) {
                        Cell cell = row.getCell(j);
                        data[i - 1][j] = getCellValue(cell);
                    }
                }
            }

            workbook.close();
            fis.close();

            logger.info("Test data loaded successfully from: " + fileName);

        } catch (IOException e) {
            logger.error("Failed to read Excel file: " + fileName, e);
        }

        return data;
    }

    /**
     * Get cell value as String regardless of cell type
     *
     * @param cell Excel cell
     * @return Cell value as String
     */
    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    public static void updateDates(String fileName, String sheetName, int checkInColIndex, int checkOutColIndex) {
        String filePath = TEST_DATA_PATH + fileName;
        File file = new File(filePath);
        if (!file.exists()) {
            logger.error("❌ File not found: " + filePath);
            return;
        }
        if (file.length() == 0) {
            logger.error("❌ File is empty: " + filePath);
            return;
        }

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                logger.error("❌ Sheet not found: " + sheetName);
                return;
            }

            LocalDate today = LocalDate.now();
            LocalDate checkOutDate = today.plusDays(60);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell checkInCell = row.getCell(checkInColIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                Cell checkOutCell = row.getCell(checkOutColIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

                checkInCell.setCellValue(today.format(formatter));
                checkOutCell.setCellValue(checkOutDate.format(formatter));
            }
            fis.close();
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
                logger.info("✅ Updated check-in and check-out dates successfully.");
            }

        } catch (IOException e) {
            logger.error("❌ Failed to update dates in Excel file: " + fileName, e);
        }
    }
}
