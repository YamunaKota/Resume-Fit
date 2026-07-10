package com.resumefit.resumefit.service;


import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import java.io.File;

@Service
public class PdfService {

    public String extractText(File file) throws Exception {
        var document = Loader.loadPDF(file);

        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);

        document.close();

        return text;
    }
}
