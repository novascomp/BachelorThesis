package ares.vr;

import ares.vr.json.Adresa;
import ares.vr.json.AresResponseChecker;
import ares.vr.json.Fosoba;
import ares.vr.json.VypisVR;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import me.novascomp.home.config.BeansInit;
import me.novascomp.utils.standalone.service.exceptions.InternalException;

public class AresRecordGenerator {

    public static String generateAresVrBasicDocument(String organizationIco, VypisVR vypisVR, int count) {
        try {
            Document doc = new Document();
            String fileName = "Organizace " + organizationIco + " základní výpis z veřejného rejstříku záznam " + count + ".pdf";
            String aresHumanReadable = "https://wwwinfo.mfcr.cz/cgi-bin/ares/darv_vr.cgi?ico=" + organizationIco + "&xml=1";
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(fileName));

            BaseFont czech = BaseFont.createFont(BaseFont.HELVETICA, "cp1250",
                    BaseFont.EMBEDDED);
            Font headlineFont = new Font(czech, 16, Font.NORMAL, Color.BLACK);
            Font font = new Font(czech, 12, Font.NORMAL, Color.BLACK);
            Font webPageFont = new Font(czech, 10, Font.UNDERLINE, Color.BLUE);
            doc.open();

            Paragraph headline = new Paragraph(writeHeadlineText(vypisVR).toString(), headlineFont);
            Paragraph mainText = new Paragraph(writeMainText(vypisVR).toString(), font);
            Paragraph aresOnline = new Paragraph(aresHumanReadable, webPageFont);

            doc.add(headline);
            doc.add(mainText);
            doc.add(Image.getInstance("https://api.qrserver.com/v1/create-qr-code/?size=100x100&data=" + URLEncoder.encode(aresHumanReadable, StandardCharsets.UTF_8)));
            doc.add(aresOnline);
            doc.close();
            writer.close();

            return fileName;
        } catch (DocumentException | FileNotFoundException e) {
            throw new InternalException(e.getMessage());
        } catch (IOException ex) {
            throw new InternalException(ex.getMessage());
        }
    }

    private static StringBuilder writeHeadlineText(VypisVR vypisVR) {
        StringBuilder headlineText = new StringBuilder();
        headlineText.append(writeObchodniFirma(vypisVR).toString()).append("\n\n")
                .append("IČO: ").append(writeICO(vypisVR).toString()).append("\n\n");
        return headlineText;
    }

    private static StringBuilder writeMainText(VypisVR vypisVR) {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat(BeansInit.DEFAULT_DATE_FORMAT);
        String strDate = dateFormat.format(date);

        StringBuilder mainText = new StringBuilder();
        mainText.append(writeAdresa(vypisVR).toString()).append("\n\n")
                .append("Statutární orgán: ").append("\n\n")
                .append(writeClenove(vypisVR).toString()).append("\n\n")
                .append("Způsob jednání: ").append("\n\n")
                .append(writeZpusobJednani(vypisVR).toString()).append("\n\n")
                .append("Zdroj ARES ke dni: ").append(strDate);
        return mainText;
    }

    private static StringBuilder writeObchodniFirma(VypisVR vypisVR) {
        StringBuilder obchodniFirma = new StringBuilder();
        if (AresResponseChecker.checkZakladniUdajeObchodniFirma(vypisVR)) {
            return obchodniFirma.append(vypisVR.zakladniUdaje.obchodniFirma.value);
        }
        return obchodniFirma;
    }

    private static StringBuilder writeICO(VypisVR vypisVR) {
        StringBuilder ico = new StringBuilder();
        if (AresResponseChecker.checkZakladniUdaje(vypisVR)) {
            return ico.append(vypisVR.zakladniUdaje.ico.value);
        }
        return ico;
    }

    private static StringBuilder writeAdresa(VypisVR vypisVR) {
        StringBuilder plnaAdresa = new StringBuilder();
        if (AresResponseChecker.checkZakladniUdajeSidloAdresa(vypisVR)) {
            Adresa adresa = vypisVR.zakladniUdaje.sidlo.adresa;
            plnaAdresa.append(checkS(adresa.nazevStatu) ? adresa.nazevStatu + "\n" : "")
                    .append(checkS(adresa.nazevOkresu) ? adresa.nazevOkresu + "\n" : "")
                    .append(checkS(adresa.nazevObce) ? adresa.nazevObce + " " : "").append(checkS(adresa.nazevCastob) ? adresa.nazevCastob : "").append("\n")
                    .append(checkS(adresa.nazevUvp) ? adresa.nazevUvp + " " : "").append(checkS(adresa.cisloDomu) ? adresa.cisloDomu : "").append(" ").append(checkS(adresa.cisloOr) ? "/ " + adresa.cisloOr : "").append("\n")
                    .append(checkS(adresa.psc) ? adresa.psc + "\n" : "");
        }
        return plnaAdresa;
    }

    private static StringBuilder writeClenove(VypisVR vypisVR) {
        StringBuilder clenove = new StringBuilder();
        if (AresResponseChecker.checkStatutarniOrganClen(vypisVR)) {
            vypisVR.statutarniOrgan.clen.forEach((clen) -> {
                if (AresResponseChecker.checkStatutarniOrganClen(clen)) {
                    if (AresResponseChecker.checkStatutarniOrganClenFunkceNazev(clen)) {
                        clenove.append(clen.funkce.nazev).append("\n");
                    }
                    if (AresResponseChecker.checkStatutarniOrganClenFosoba(clen)) {
                        Fosoba fosoba = clen.fosoba;
                        clenove.append(checkS(fosoba.titulPred) ? fosoba.titulPred + " " : "").append(checkS(fosoba.jmeno) ? fosoba.jmeno : "").append(" ").append(checkS(fosoba.prijmeni) ? fosoba.prijmeni : "").append("\n\n");
                    }
                }
            });
        }
        return clenove;
    }

    private static StringBuilder writeZpusobJednani(VypisVR vypisVR) {
        StringBuilder obchodniFirma = new StringBuilder();
        if (AresResponseChecker.checkStatutarniZpusobJednani(vypisVR)) {
            return obchodniFirma.append(vypisVR.statutarniOrgan.zpusobJednani.text);
        }
        return obchodniFirma;
    }

    private static boolean checkS(String string) {
        return Optional.ofNullable(string).isPresent();
    }
}
