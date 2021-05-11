package me.novascomp.home.generator;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import me.novascomp.flat.config.BeansInit;
import me.novascomp.home.flat.uploader.NVHomeFlat;
import me.novascomp.home.flat.uploader.FlatUploader;
import me.novascomp.home.flat.uploader.LightweightToken;
import me.novascomp.utils.standalone.service.exceptions.BadRequestException;
import me.novascomp.utils.standalone.service.exceptions.InternalException;

public class DefaultFlatAccessDocumentPDFGenerator {

    public static String generateFlatsDefaultTokenDocument(String organizationIco, FlatUploader flats) {

        try {
            Document doc = new Document();
            String fileName = "Organizace " + organizationIco + " výchozí přístupy do jednotek.pdf";
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(fileName));
            doc.open();

            BaseFont czech = BaseFont.createFont(BaseFont.HELVETICA, "cp1250",
                    BaseFont.EMBEDDED);

            Font defaultTextFont = new Font(czech, 16, Font.NORMAL, Color.BLACK);
            Font tokenFont = new Font(czech, 16, Font.BOLD, Color.BLACK);
            Font webPageFont = new Font(czech, 16, Font.BOLDITALIC, Color.BLUE);

            int pageCount = 0;

            Paragraph beginning = new Paragraph(writeBeginning(flats).toString(), defaultTextFont);
            Paragraph webPage = new Paragraph("https://vlastnik.novascomp.synology.me/", webPageFont);

            doc.add(beginning);
            doc.add(webPage);
            doc.newPage();

            for (NVHomeFlat flatUpload : flats.getFlatsToUpload()) {
                LightweightToken lightweightToken = flats.getGeneratedTokens().get(flatUpload.getIdentifier());
                if (Optional.ofNullable(lightweightToken).isPresent()) {
                    Paragraph greeting = new Paragraph(writeGreeting(flatUpload).toString(), defaultTextFont);
                    Paragraph token = new Paragraph(lightweightToken.getKey(), tokenFont);
                    Paragraph breakA = new Paragraph(" ", defaultTextFont);
                    Paragraph breakB = new Paragraph(" ", defaultTextFont);
                    Paragraph conclusion = new Paragraph(writeConclusion().toString(), defaultTextFont);
                    doc.add(greeting);
                    doc.add(token);
                    doc.add(breakA);
                    doc.add(webPage);
                    doc.add(breakB);
                    doc.add(conclusion);
                    // doc.add(Image.getInstance("https://api.qrserver.com/v1/create-qr-code/?size=100x100&data=" + URLEncoder.encode(flats.getGeneratedTokens().get(flatUpload.getIdentifier()), StandardCharsets.UTF_8)));
                    doc.newPage();
                    pageCount++;
                }
            }

            if (pageCount == 0) {
                throw new BadRequestException("");
            }

            doc.close();
            writer.close();

            return fileName;
        } catch (DocumentException | FileNotFoundException e) {
            throw new InternalException(e.toString());
        } catch (IOException ex) {
            throw new InternalException(ex.toString());
        }
    }

    private static StringBuilder writeBeginning(FlatUploader flats) {
        StringBuilder beginning = new StringBuilder();
        beginning.append("Vážený člene výboru").append(",\n\n")
                .append("na základě úspěšného nahrání jednotek byly vygenerovány následující přístupové kódy.").append("\n\n");
        flats.getFlatsToUpload().forEach((nVHomeFlat) -> {
            beginning.append("Jednotka ID. ").append(nVHomeFlat.getIdentifier()).append(" přístupový kód: ")
                    .append(flats.getGeneratedTokens().get(nVHomeFlat.getIdentifier()).getKey()).append("\n");
        });
        beginning.append("\n\n")
                .append("Na následujících stranách naleznete dopisy určené do schránky vlastníka jednotky.").append("\n\n")
                .append("Celkově bylo vygenerováno ").append(flats.getFlatsToUpload().size()).append(" přístupových kódů.").append("\n\n")
                .append("S pozdravem").append("\n\n")
                .append("VLASTNÍK");
        return beginning;
    }

    private static StringBuilder writeGreeting(NVHomeFlat homeFlat) {
        StringBuilder greeting = new StringBuilder();
        greeting.append("Vážený vlastníku jednotky ").append(homeFlat.getIdentifier()).append(",\n\n")
                .append("byl Vám vygenerován následující přístupový kód, nikomu ho nesdělujte a uplatněte jej na níže uvedené stránce. ").append("\n\n");
        return greeting;
    }

    private static StringBuilder writeConclusion() {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat(BeansInit.DEFAULT_DATE_FORMAT);
        String strDate = dateFormat.format(date);
        StringBuilder conclusion = new StringBuilder();
        conclusion.append("Kód je určen pouze osobě, která má vlastnické právo k jednotce.").append("\n")
                .append("Každý kód lze použít právě jednou.").append("\n\n")
                .append("V případě více vlastníků jednotky nebo ztráty či zcizení kontaktujte výbor pro obdržení dalších kódů.").append("\n\n")
                .append("S pozdravem výbor SVJ.").append("\n\n")
                .append("Ze dne: ").append(strDate);
        return conclusion;

    }
}
