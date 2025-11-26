package util;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;

import java.io.IOException;
import java.util.List;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

public class PdfUtil {

    public static void generarPdf(String nombreArchivo,
                                  String[] headers,
                                  List<Object[]> datos)
            throws IOException, DocumentException {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        HttpServletResponse response =
                (HttpServletResponse) externalContext.getResponse();

        externalContext.responseReset();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + nombreArchivo + ".pdf");

        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        // Fuente para los encabezados
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);

        // Fuente para los datos
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        // Tabla con columnas dinámicas
        PdfPTable table = new PdfPTable(headers.length);
        table.setWidthPercentage(100);

        // Encabezados
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Paragraph(h, headerFont));
            table.addCell(cell);
        }

        // Datos
        for (Object[] fila : datos) {
            for (Object value : fila) {
                PdfPCell cell = new PdfPCell(
                        new Paragraph(value == null ? "" : value.toString(), dataFont)
                );
                table.addCell(cell);
            }
        }

        document.add(table);
        document.close();

        facesContext.responseComplete();
    }

    public static void generarPdfDetalle(String nombreArchivo,
                                          String titulo,
                                          String[][] datos)
            throws IOException, DocumentException {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        HttpServletResponse response =
                (HttpServletResponse) externalContext.getResponse();

        externalContext.responseReset();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + nombreArchivo + ".pdf");

        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        document.add(new Paragraph(titulo, titleFont));
        document.add(new Paragraph("\n"));

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        for (String[] fila : datos) {
            if (fila.length < 2) {
                continue;
            }
            table.addCell(new PdfPCell(new Paragraph(fila[0], labelFont)));
            table.addCell(new PdfPCell(new Paragraph(fila[1] != null ? fila[1] : "", dataFont)));
        }

        document.add(table);
        document.close();
        facesContext.responseComplete();
    }
}
