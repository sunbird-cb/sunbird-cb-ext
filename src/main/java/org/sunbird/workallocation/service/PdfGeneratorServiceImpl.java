package org.sunbird.workallocation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.sunbird.core.exception.BadRequestException;
import org.sunbird.workallocation.model.PdfGeneratorRequest;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Service
public class PdfGeneratorServiceImpl implements PdfGeneratorService {

    @Value("${html.store.path}")
    public String htmlFolderPath;

    @Value("${pdf.store.path}")
    public String pdfFolderPath;

    @Autowired
    private ObjectMapper mapper;

    static final String TEMPLATE_PATH = "templates/";

    private Logger log = LoggerFactory.getLogger(PdfGeneratorServiceImpl.class);


    public byte[] generatePdf(PdfGeneratorRequest request) throws IOException {
        if (StringUtils.isEmpty(request.getTemplateId())) {
            throw new BadRequestException("Template Id is mandatory!");
        }
        String message = readVm(request.getTemplateId() + ".vm", request.getTagValuePair());
        String htmlFilePath = createHTMLFile(request.getTemplateId(), message);
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("ud_htmlFilePath", htmlFilePath);
        paramMap.put("ud_fileName", htmlFilePath.replace(".html", ".pdf"));
        String pdfFilePath = "";
        try {
            pdfFilePath =  makePdf(paramMap);
        } catch (Exception exception) {
            log.error("Exception occurred while creating the pdf", exception);
        }
        File file = new File(pdfFilePath);
        byte[] bytes = new byte[(int) file.length()];
        try(FileInputStream fis = new FileInputStream(file)){
            fis.read(bytes);
        }
        return bytes;
    }

    public String createHTMLFile(String fName, String htmlContent) throws IOException {
        String prefix = UUID.randomUUID().toString().toUpperCase() + "-"
                + System.currentTimeMillis();
        String htmlFilePath = htmlFolderPath + "/" + prefix + "_" + fName + ".html";
        File theDir = new File(htmlFolderPath);
        if (!theDir.exists()){
            theDir.mkdirs();
        }
        if (htmlContent.contains("â€˜")) {
            htmlContent = htmlContent.replaceAll("â€˜", "'");
        }
        if (htmlContent.contains("â€™")) {
            htmlContent = htmlContent.replaceAll("â€™", "'");
        }
        BufferedWriter out = null;
        try {
            FileWriter fstream = new FileWriter(htmlFilePath);
            out = new BufferedWriter(fstream);
            out.write(htmlContent);
            out.close();
        } catch (Exception ex) {
            log.error("Exception occurred while saving the html file");
        } finally {
            if (out != null) {
                out.close();
            }
        }
        log.info("Html written successfully on: {}", htmlFilePath);
        return htmlFilePath;
    }

    private String readVm(String templateName, Map<String, Object> paramValue) {
        VelocityEngine engine = new VelocityEngine();
        VelocityContext context = new VelocityContext();
        if (!CollectionUtils.isEmpty(paramValue)) {
            paramValue.forEach((k, v) -> {
                if (null != paramValue) context.put(k, v);
            });
        }
        Properties p = new Properties();
        p.setProperty("resource.loader", "class");
        p.setProperty(
                "class.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        StringWriter writer = null;
        String body = null;
        try {
            engine.init(p);
            Template template = engine.getTemplate(TEMPLATE_PATH + templateName);
            writer = new StringWriter();
            template.merge(context, writer);
            body = writer.toString();
        } catch (Exception e) {
            log.error("Exception occured while loading the template file", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                }
            }
        }
        log.info("Read the template successfully!");
        return body;
    }

    public String makePdf(Map<String, String> paramMap) throws IOException {
        if (null == paramMap.get("ud_htmlFilePath")) {
            return null;
        }
        StringBuffer commandLine = new StringBuffer();
    //    commandLine.append(" wkhtmltopdf ");
        commandLine.append(" wkhtmltopdf --margin-top 30.0 --margin-left 25.0 --margin-right 25.0 --footer-spacing 5 --header-spacing 5 --footer-font-size 8 ");
        commandLine.append("--orientation Portrait --page-size A4 --load-error-handling ignore --load-media-error-handling ignore --no-header-line --no-footer-line --enable-forms ");
        commandLine.append("--minimum-font-size 11 --footer-html /home/amit/Desktop/pdf-footer.html ");
        commandLine.append("--header-right [page]/[toPage]  --header-html /home/amit/Desktop/pdf-header.html ");

        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            // ud stands for user defined. All the parameters which are not the
            // part of
            // WKHTML2PDF lib should start with "ud_"
            if (!entry.getKey().startsWith("ud_")) {
                commandLine.append(" " + entry.getKey());
                commandLine.append(" " + entry.getValue());
            }
        }
        log.info("Saving the file content as PDF");
        String htmlFilePath = paramMap.get("ud_htmlFilePath");

        String pdfFileName = paramMap.get("ud_fileName");
        if (!pdfFileName.endsWith(".pdf")) {
            pdfFileName = pdfFileName + ".pdf";
        }

        String pdfFilePath = pdfFileName;

        File theDir = new File(pdfFolderPath);
        if (!theDir.exists()){
            theDir.mkdirs();
        }

        if (htmlFilePath != null) {
            commandLine.append(" " + htmlFilePath);
            commandLine.append(" " + pdfFilePath + " \n");
            String command = commandLine.toString();
           // command = command.replace("--header-html", "");
            BufferedReader brCleanUp = null;
            Process process = null;
            try {
                process = Runtime.getRuntime().exec(
                        command);
                InputStream stderr = process.getErrorStream();

                String line;
                brCleanUp = new BufferedReader(
                        new InputStreamReader(stderr));
                while ((line = brCleanUp.readLine()) != null) {
                    log.info("Writing the pdf file {}", line);
                }
            } catch (IOException e) {
                log.error("Exception occurred while writing the pdf file {}", e);
            } finally {
                if (brCleanUp != null) {
                    brCleanUp.close();
                }
                if (process != null) {
                    process.destroy();
                }
            }
        } else {
            log.info("Failed to create PDF file for filename ===> " + htmlFilePath);
        }
        return pdfFilePath;
    }
}
