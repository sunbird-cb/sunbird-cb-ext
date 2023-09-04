package org.sunbird.workallocation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.util.Constants;
import org.sunbird.core.exception.BadRequestException;
import org.sunbird.workallocation.model.PdfGeneratorRequest;
import org.sunbird.workallocation.util.WorkAllocationConstants;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PdfGeneratorServiceImpl implements PdfGeneratorService {

	public static final String DEPT_NAME = "deptName";
	public static final String DEPT_IMG_URL = "deptImgUrl";
	public static final String UD_HTML_FILE_PATH = "ud_htmlFilePath";
	public static final String UD_FILE_NAME = "ud_fileName";
	public static final String UD_HTML_HEADER_FILE_PATH = "ud_htmlHeaderFilePath";
	public static final String HTML = ".html";
	public static final String UD_HTML_FOOTER_FILE_PATH = "ud_htmlFooterFilePath";
	public static final String EXCEPTION_OCCURRED_WHILE_CREATING_THE_PDF = "Exception occurred while creating the pdf";
	@Value("${html.store.path}")
	public String htmlFolderPath;

	@Value("${pdf.store.path}")
	public String pdfFolderPath;

	@Value("${pdf.draft.template.name}")
	private String draftTemplateName;

	@Value("${pdf.published.template.name}")
	private String publishedTemplateName;

	@Value("${domain.host.name}")
	public String baseUrl;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private AllocationServiceV2 allocationService;

	static final String TEMPLATE_PATH = "templates/";

	private Logger log = LoggerFactory.getLogger(PdfGeneratorServiceImpl.class);
	@Autowired
	CassandraOperation cassandraOperation;

	public byte[] generatePdf(PdfGeneratorRequest request) throws Exception {
		if (StringUtils.isEmpty(request.getTemplateId())) {
			throw new BadRequestException("Template Id is mandatory!");
		}
		String footerTemplateName = "templates/pdf-draft-footer.html";
		Map<String, Object> headerDetails = new HashMap<>();
		String deptId = (String) request.getTagValuePair().get("deptId");
		headerDetails.put(DEPT_NAME, request.getTagValuePair().get(DEPT_NAME));
		headerDetails.put(DEPT_IMG_URL, request.getTagValuePair().get(DEPT_IMG_URL));
		String headerMessage = readVm("pdf-header.vm", headerDetails);
		String headerHtmlFilePath = createHTMLFile("pdf-header", headerMessage);

		String message = readVm(request.getTemplateId() + ".vm", request.getTagValuePair());
		String htmlFilePath = createHTMLFile(request.getTemplateId(), message);
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(UD_HTML_FILE_PATH, htmlFilePath);
		paramMap.put(UD_FILE_NAME, htmlFilePath.replace(HTML, ".pdf"));
		paramMap.put(UD_HTML_HEADER_FILE_PATH, headerHtmlFilePath);

		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(footerTemplateName);

		byte[] buffer = new byte[inputStream.available()];
		inputStream.read(buffer);

		File htmlFooterPath = new File("/tmp/" + deptId + "pdf-draft-footer.html");
		try(OutputStream outStream = new FileOutputStream(htmlFooterPath)){
			outStream.write(buffer);
		}
		paramMap.put(UD_HTML_FOOTER_FILE_PATH, htmlFooterPath.getAbsolutePath());

		String pdfFilePath = "";
		try {
			pdfFilePath = makePdf(paramMap);
		} catch (Exception exception) {
			log.error(EXCEPTION_OCCURRED_WHILE_CREATING_THE_PDF, exception);
		}
		File file = new File(pdfFilePath);
		byte[] bytes = new byte[(int) file.length()];
		try (FileInputStream fis = new FileInputStream(file)) {
			fis.read(bytes);
		}
		return bytes;
	}

	public byte[] generatePdf(String woId) {
		try {
			String pdfFilePath = getPDFFilePath(woId);
			if (pdfFilePath == null) return new byte[0];
			File file = new File(pdfFilePath);
			byte[] bytes = new byte[(int) file.length()];
			try (FileInputStream fis = new FileInputStream(file)) {
				fis.read(bytes);
			}
			return bytes;

		}
		catch (Exception e) {
			log.error("Failed to retrieve WorkOrder object for pdf generation.", e);
		}
		return new byte[0];
	}

	@Override
	public String getPublishedPdfLink(String woId) {
		try {
			Map<String, Object> workOrder = allocationService.getWorkOrderObject(woId);
			return (String) workOrder.get("publishedPdfLink");
		} catch (Exception e) {
			log.error("Failed to retrieve published pdf.", e);
		}
		return null;
	}

	private String getPDFFilePath(String woId) throws Exception {
		Map<String, Object> workOrder = allocationService.getWorkOrderObject(woId);
		if (workOrder == null) {
			return null;
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
		String printedTime = simpleDateFormat.format(new Date());
		simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");
		printedTime = printedTime + " " + simpleDateFormat.format(new Date());
		workOrder.put("printedTime", printedTime);
		String status = (String) workOrder.get("status");
		String deptId = "";
		if(workOrder.get("deptId") instanceof Integer){
			deptId = String.valueOf(workOrder.get("deptId"));
		}
		else if(workOrder.get("deptId") instanceof String)
		{
			deptId = (String)workOrder.get("deptId");
		}
		String templateName = null;
		String footerTemplateName = null;
		if (WorkAllocationConstants.DRAFT_STATUS.equalsIgnoreCase(status)) {
			templateName = draftTemplateName;
			footerTemplateName = "templates/pdf-draft-footer.html";
		} else if (WorkAllocationConstants.PUBLISHED_STATUS.equalsIgnoreCase(status)) {
			String qrImageUrl = baseUrl + (String)workOrder.get("id");
			File qrCodeFile = QRCode.from(qrImageUrl).to(ImageType.PNG).file();
			workOrder.put("qrcodeurl", qrCodeFile.getAbsolutePath());
			templateName = publishedTemplateName;
			footerTemplateName = "templates/pdf-published-footer.html";
		} else {
			log.error("Invalid WorkOrder object status. Failed to generate PDF file.");
			return null;
		}

		Map<String, Object> headerDetails = new HashMap<>();
		try {
			ClassPathResource classPathResource = new ClassPathResource("government-of-india.jpg");
			InputStream inputStream = classPathResource.getInputStream();
			File tempFile = File.createTempFile("government-of-india", ".jpg");
			try(OutputStream outStream = new FileOutputStream(tempFile)){
				byte[] buffer = new byte[8 * 1024];
				int bytesRead;
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outStream.write(buffer, 0, bytesRead);
				}
				IOUtils.closeQuietly(inputStream);
				IOUtils.closeQuietly(outStream);
			}
			headerDetails.put(DEPT_IMG_URL,  tempFile.getAbsolutePath());
		}catch (Exception ex){
			log.error("Exception occurred while loading the default department logo");
		}
		headerDetails.put(DEPT_NAME,  workOrder.get(DEPT_NAME));
//		headerDetails.put("deptImgUrl",  (String) workOrder.get("deptImgUrl"));
		String headerMessage = readVm("pdf-header.vm", headerDetails);
		String headerHtmlFilePath = createHTMLFile("pdf-header", headerMessage);

		String message = readVm(templateName + ".vm", workOrder);
		String htmlFilePath = createHTMLFile(templateName, message);
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(UD_HTML_FILE_PATH, htmlFilePath);
		paramMap.put(UD_FILE_NAME, htmlFilePath.replace(HTML, ".pdf"));
		paramMap.put(UD_HTML_HEADER_FILE_PATH, headerHtmlFilePath);

		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(footerTemplateName);

		byte[] buffer = new byte[inputStream.available()];
		inputStream.read(buffer);

		File htmlFooterPath = new File("/tmp/" + deptId + "pdf-draft-footer.html");
		try(OutputStream outStream = new FileOutputStream(htmlFooterPath)){
			outStream.write(buffer);
		}
		paramMap.put(UD_HTML_FOOTER_FILE_PATH, htmlFooterPath.getAbsolutePath());

		String pdfFilePath = "";
		try {
			pdfFilePath = makePdf(paramMap);
		} catch (Exception exception) {
			log.error(EXCEPTION_OCCURRED_WHILE_CREATING_THE_PDF, exception);
		}
		return pdfFilePath;
	}

	@Override
	public String generatePdfAndGetFilePath(String woId) {
		try {
			return getPDFFilePath(woId);
		} catch (Exception exception) {
			log.error(EXCEPTION_OCCURRED_WHILE_CREATING_THE_PDF, exception);
		}
		return null;
	}

	public String createHTMLFile(String fName, String htmlContent) throws IOException {
		String prefix = UUID.randomUUID().toString().toUpperCase() + "-" + System.currentTimeMillis();
		String htmlFilePath = htmlFolderPath + "/" + prefix + "_" + fName + HTML;
		File theDir = new File(htmlFolderPath);
		if (!theDir.exists()) {
			theDir.mkdirs();
		}
		if (htmlContent.contains("â€˜")) {
			htmlContent = htmlContent.replaceAll("â€˜", "'");
		}
		if (htmlContent.contains("â€™")) {
			htmlContent = htmlContent.replaceAll("â€™", "'");
		}
		BufferedWriter out = null;
		try (FileWriter fstream = new FileWriter(htmlFilePath)){
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
				if (null != paramValue)
					context.put(k, v);
			});
		}
		Properties p = new Properties();
		p.setProperty("resource.loader", "class");
		p.setProperty("class.resource.loader.class",
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
			log.error("Exception occurred while loading the template file", e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					log.error(e.toString());
				}
			}
		}
		log.info("Read the template successfully!");
		return body;
	}

	public String makePdf(Map<String, String> paramMap) throws IOException {
		if (null == paramMap.get(UD_HTML_FILE_PATH)) {
			return null;
		}
		StringBuilder commandLine = new StringBuilder();
		commandLine.append(" wkhtmltopdf --enable-local-file-access --margin-top 20.0 --margin-left 10.0 --margin-right 10.0 --footer-spacing 5 ");
		commandLine.append("--header-spacing 5  --footer-font-size 8 --orientation Portrait --page-size A4 ");
		commandLine.append("--load-media-error-handling ignore  --no-header-line --no-footer-line --enable-forms ");
		commandLine.append("--load-error-handling ignore --header-right [page]/[toPage] ");
		commandLine.append("--minimum-font-size 11 --footer-html ").append(paramMap.get(UD_HTML_FOOTER_FILE_PATH));
		commandLine.append(" --header-html ").append(paramMap.get(UD_HTML_HEADER_FILE_PATH)).append(" ");

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
		String htmlFilePath = paramMap.get(UD_HTML_FILE_PATH);
		String pdfFileName = paramMap.get(UD_FILE_NAME);
		if (!pdfFileName.endsWith(".pdf")) {
			pdfFileName = pdfFileName + ".pdf";
		}

		String pdfFilePath = pdfFileName;

		File theDir = new File(pdfFolderPath);
		if (!theDir.exists()) {
			theDir.mkdirs();
		}

		if (htmlFilePath != null) {
			commandLine.append(" " + htmlFilePath);
			commandLine.append(" " + pdfFilePath + " \n");
			String command = commandLine.toString();
			BufferedReader brCleanUp = null;
			Process process = null;
			try {
				process = Runtime.getRuntime().exec(command);
				InputStream stderr = process.getErrorStream();

				String line;
				brCleanUp = new BufferedReader(new InputStreamReader(stderr));
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
			log.info("Failed to create PDF file for filename ===> {}", htmlFilePath);
		}
		return pdfFilePath;
	}

	public  byte[] getBatchSessionQRPdf(String courseId,String batchId) throws IOException {
		if(StringUtils.isEmpty(courseId) || StringUtils.isEmpty(batchId))
		{
			throw new BadRequestException("CourseId & BatchId should be passed !");
		}
		HashMap<String,HashMap<String,String>> pdfDetails = populatePDFTemplateDetails();
		HashMap<String,HashMap> pdfParams = populatePDFParams();
		HashMap propertyMap = new HashMap();
		propertyMap.put(Constants.COURSE_ID_COLUMN,courseId);
		propertyMap.put(Constants.BATCH_ID_COLUMN,batchId);

		List<Map<String, Object>> batches = cassandraOperation.getRecordsByProperties(
				Constants.KEYSPACE_SUNBIRD_COURSES, Constants.TABLE_COURSE_BATCH, propertyMap,
				ListUtils.EMPTY_LIST);
		if(batches == null || batches.isEmpty())
		{
			throw new BadRequestException("Batch not exist for the passed CourseId : "+ courseId+ " & BatchId : "+ batchId);
		}
		ObjectMapper objectMapper = new ObjectMapper();
		ArrayList<HashMap<String,Object>> sessionDetails;
		try {
			sessionDetails = (ArrayList<HashMap<String,Object>>)objectMapper.readValue((String) batches.get(0).get(Constants.TABLE_COURSE_BATCH_ATTRIBUTES), Map.class).get(Constants.TABLE_COURSE_SESSION_DETAILS);
		} catch (Exception e) {
			throw new BadRequestException("Session Details does not exist for the passed CourseId : "+ courseId+ " & BatchId : "+ batchId);
		}
		int count = 0;
		for(HashMap<String,Object> session:sessionDetails )
		{
			pdfParams.put(Constants.SESSION+count++,populateSession(session,courseId,batchId));
		}
		return generatePdf(pdfDetails,pdfParams);
	}
	private HashMap<String,String> populateSession(HashMap<String,Object> sessionData,String courseId,String batchId)
	{
		HashMap<String,String> session = new HashMap<>();
		session.put(Constants.TITLE,(String) sessionData.get(Constants.TITLE));
		session.put(Constants.START_DATE,(String)sessionData.get(Constants.START_DATE));
		session.put(Constants.START_TIME_KEY,(String)sessionData.get(Constants.START_TIME_KEY));
		session.put(Constants.END_TIME_KEY,(String)sessionData.get(Constants.END_TIME_KEY));
		session.put(Constants.SESSION_ID,(String)sessionData.get(Constants.SESSION_ID));
		session.put(Constants.COURSE_ID,courseId);
		session.put(Constants.BATCH_ID,batchId);
		session.put(Constants.QR_CODE_URL,generateBatchSessionQRCode(courseId,batchId,(String)sessionData.get(Constants.SESSION_ID)));
		return session;
	}
	public String generateBatchSessionQRCode(String courseId,String batchId,String sessionId){
		String qrCodeBody = Constants.EMPTY;
		HashMap<String,Object> qrBody = new HashMap<>();
		 qrBody.put(Constants.COURSE_ID,courseId);
		 qrBody.put(Constants.BATCH_ID,batchId);
		 qrBody.put(Constants.SESSION_ID,sessionId);
		try {
			qrCodeBody= mapper.writeValueAsString(qrBody);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		File qrCodeFile = QRCode.from(qrCodeBody).to(ImageType.PNG).file(sessionId);
		return qrCodeFile.getAbsolutePath();
	}
	public byte[] generatePdf(HashMap<String,HashMap<String,String>> pdfDetails ,HashMap<String,HashMap> params  ) throws IOException {
		Map<String, String> pdfData = new HashMap<>();
		for (Map.Entry<String, HashMap<String,String>> pdf : pdfDetails.entrySet()) {
			String key = pdf.getKey();
			HashMap<String,String> value = pdf.getValue();
			String file ="";
			if(value.get(Constants.BUDGET_DOC_FILE_TYPE).equalsIgnoreCase(Constants.VM))
				file=	generateHTMLfrmVM(value.get(Constants.BUDGET_DOC_FILE_NAME),params.get(key));
			else
				file=	value.get(Constants.BUDGET_DOC_FILE_NAME);
			switch (key) {
				case Constants.FOOTER:
					pdfData.put(UD_HTML_FOOTER_FILE_PATH,file);
					break;
				case Constants.HEADER:
					pdfData.put(UD_HTML_HEADER_FILE_PATH,file);
					break;
				default:
					String body ="";
					for (Map.Entry<String, HashMap> entry1 : params.entrySet()) {
						String key1 = entry1.getKey();
						if (key1.startsWith(Constants.SESSION)) {
							body=	body+readVm(value.get(Constants.BUDGET_DOC_FILE_NAME)+ Constants.DOT_SEPARATOR+Constants.VM, params.get(key1)) ;
						}
					}
					body= createHTMLFile(key, body);
					pdfData.put(UD_HTML_FILE_PATH, body);
					pdfData.put(UD_FILE_NAME, body.replace(HTML, Constants.DOT_SEPARATOR+Constants.PDF));
					break;
			}
		}
		String pdfFilePath = "";
		try {
			pdfFilePath = makePdf(pdfData);
		} catch (Exception exception) {
			log.error(EXCEPTION_OCCURRED_WHILE_CREATING_THE_PDF, exception);
		}
		File file = new File(pdfFilePath);
		byte[] bytes = new byte[(int) file.length()];
		try (FileInputStream fis = new FileInputStream(file)) {
			fis.read(bytes);
		}
		return bytes;
	}
	public String generateHTMLfrmVM(String vmFName,HashMap params ) throws IOException {
		String message = readVm(vmFName+ Constants.DOT_SEPARATOR+Constants.VM, params);
		return createHTMLFile(vmFName, message);
	}
	private HashMap<String,HashMap<String,String>> populatePDFTemplateDetails(){
		HashMap<String,HashMap<String,String>> pdfDetails = new HashMap<>();
		HashMap<String,String> headerDetails = new HashMap<>();
		headerDetails.put(Constants.BUDGET_DOC_FILE_TYPE,Constants.VM);
		headerDetails.put(Constants.BUDGET_DOC_FILE_NAME,Constants.BATCH_SESSION_HEADER);
		pdfDetails.put(Constants.HEADER,headerDetails);
		HashMap<String,String> bodyDetails = new HashMap<>();
		bodyDetails.put(Constants.BUDGET_DOC_FILE_TYPE,Constants.VM);
		bodyDetails.put(Constants.BUDGET_DOC_FILE_NAME,Constants.BATCH_SESSION_BODY);
		pdfDetails.put(Constants.BODY,bodyDetails);
		HashMap<String,String> footerDetails = new HashMap<>();
		footerDetails.put(Constants.BUDGET_DOC_FILE_TYPE,Constants.VM);
		footerDetails.put(Constants.BUDGET_DOC_FILE_NAME,Constants.BATCH_SESSION_FOOTER);
		pdfDetails.put(Constants.FOOTER,footerDetails);
		return pdfDetails;
	}
	private HashMap<String,HashMap> populatePDFParams() {
		HashMap<String,HashMap> params = new HashMap<>();
		HashMap<String,String> headerParams = new HashMap<>();
		headerParams.put(Constants.PROGRAM_NAME,Constants.EMPTY);
		params.put(Constants.HEADER,headerParams);
		HashMap<String,String> footerParams = new HashMap<>();
		footerParams.put(Constants.PROGRAM_NAME,Constants.EMPTY);
		params.put(Constants.FOOTER,footerParams);
		return params;
	}

}
