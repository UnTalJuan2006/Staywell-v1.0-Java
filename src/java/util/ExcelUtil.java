package util;

import java.io.IOException;
import java.util.List;
import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtil {

    public static void generarExcel(String nombreArchivo,
                                    String nombreHoja,
                                    String[] headers,
                                    List<Object[]> datos) throws IOException {

        HttpServletResponse response =
                (HttpServletResponse) FacesContext.getCurrentInstance()
                        .getExternalContext().getResponse();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + nombreArchivo + ".xlsx");

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet(nombreHoja);

        // Encabezados
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // Datos
        int rowNum = 1;
        for (Object[] fila : datos) {
            Row row = sheet.createRow(rowNum++);
            for (int i = 0; i < fila.length; i++) {
                Object value = fila[i];
                row.createCell(i).setCellValue(value == null ? "" : value.toString());
            }
        }

        // Auto-size columnas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ServletOutputStream out = response.getOutputStream();
        workbook.write(out);
        workbook.close();
        out.flush();
        out.close();

        FacesContext.getCurrentInstance().responseComplete();
    }
}