package org.sunbird.assessment.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.sunbird.assessment.dto.AssessmentSubmissionDTO;
import org.sunbird.assessment.repo.AssessmentRepository;
import org.sunbird.common.service.ContentService;
import org.sunbird.core.exception.InvalidDataInputException;

public class AssessmentServiceImpl implements AssessmentService {

	@Autowired
	AssessmentRepository repository;

	@Autowired
	ContentService contentService;

	@Override
	public Map<String, Object> submitAssessment(String rootOrg, AssessmentSubmissionDTO data, String userEmail)
			throws Exception {
		Map<String, Object> ret = new HashMap<String, Object>();

		// TODO - Need to get the Assessment ContentMeta Data
		List<Map<String, Object>> sourceList = contentService.getMetaByIDListandSource(
				new ArrayList<String>(Arrays.asList(new String[] { data.getIdentifier() })),
				new String[] { "artifactUrl", "collections", "contentType" }, "Live");

		if (sourceList.size() == 0) {
			throw new InvalidDataInputException("invalid.resource");
		}

		Map<String, Object> source = sourceList.get(0);
		// assessment answers
		Map<String, Object> answers = new HashMap<String, Object>();
		if (data.isAssessment())
			answers = repository
					.getAssessmentAnswerKey(source.get("artifactUrl").toString().replaceAll(".json", "-key.json"));

		else
			answers = repository.getQuizAnswerKey(data);

		Map<String, Object> resultMap = assessUtilServ.validateAssessment(data.getQuestions(), answers);
		Double result = (Double) resultMap.get("result");
		Integer correct = (Integer) resultMap.get("correct");
		Integer blank = (Integer) resultMap.get("blank");
		Integer inCorrect = (Integer) resultMap.get("incorrect");

		Map<String, Object> persist = new HashMap<String, Object>();

		// Fetch parent of an assessment with status live
		List<Map<String, Object>> parentList = (List<Map<String, Object>>) source.get("collections");
		List<String> parentIds = new ArrayList<>();
		parentList.forEach(item -> parentIds.add(item.get("identifier").toString()));
		String parentId = "";
		if (parentList != null && parentList.size() > 0) {
			List<Map<String, Object>> parentMeta = contentService.getMetaByIDListandSource(parentIds,
					new String[] { "status", "identifier" }, null);
			for (Map<String, Object> parentData : parentMeta) {
				if (Arrays.asList(ContentMetaConstants.LIVE_CONTENT_STATUS).contains(parentData.get("status"))) {
					parentId = parentData.get("identifier").toString();
					break;
				}
			}
		}
		persist.put("parent", parentId);

		persist.put("result", result);
		persist.put("sourceId", data.getIdentifier());
		persist.put("title", data.getTitle());
		persist.put("rootOrg", rootOrg);
		persist.put("userId", userId);
		persist.put("correct", correct);
		persist.put("blank", blank);
		persist.put("incorrect", inCorrect);

		if (data.isAssessment()) {
			Map<String, Object> tempSource = null;
			// get parent data for assessment
			if (((List<Object>) source.get("collections")).size() > 0) {

				List<Map<String, Object>> sources = contentService.getMetaByIDListandSource(
						new ArrayList<String>(Arrays.asList(parentId)), new String[] { "contentType", "collections" },
						null);

				if (sources.size() == 0) {
					throw new InvalidDataInputException("invalid.resource");
				}
				tempSource = sources.get(0);

				persist.put("parentContentType", tempSource.get("contentType"));

			} else {
				persist.put("parentContentType", "");
			}
			// insert into assessment table
			repository.insertQuizOrAssessment(persist, true);

			if (tempSource != null && result >= 60) {
				if (tempSource.get("contentType").equals("Course")) {
					// insert certificate and medals
					String courseId = parentId;

					// Fetch live programs of the course
					List<Map<String, Object>> programsOfCourse = (List<Map<String, Object>>) tempSource
							.get("collections");
					List<String> allPrograms = new ArrayList<>();
					programsOfCourse.forEach(program -> allPrograms.add(program.get("identifier").toString()));
					boolean parent = programsOfCourse.size() > 0 ? true : false;
					List<String> programId = new ArrayList<String>();

					if (allPrograms != null && allPrograms.size() > 0) {
						List<Map<String, Object>> programMeta = contentService.getMetaByIDListandSource(allPrograms,
								new String[] { "identifier", "status" }, null);
						for (Map<String, Object> programData : programMeta) {
							if (Arrays.asList(ContentMetaConstants.LIVE_CONTENT_STATUS)
									.contains(programData.get("status"))) {
								programId.add(programData.get("identifier").toString());
							}
						}
					}

					bRepository.insertInBadges(rootOrg, courseId, programId, userId, parent);
					bRepository.insertCourseAndQuizBadge(rootOrg, userId, "Course", data.getIdentifier());
				}
			}
			contentProgressService.callProgress(rootOrg, userId, data.getIdentifier(), submissionMimeType,
					Float.parseFloat(result.toString()));
		} else {
			// insert into quiz table
			persist.remove("parent");
			repository.insertQuizOrAssessment(persist, false);
			bRepository.insertCourseAndQuizBadge(rootOrg, userId, "Quiz", data.getIdentifier());
			contentProgressService.callProgress(rootOrg, userId, data.getIdentifier(), submissionMimeType, 100f);
		}

		ret.put("result", result);
		ret.put("correct", correct);
		ret.put("inCorrect", inCorrect);
		ret.put("blank", blank);
		ret.put("total", blank + inCorrect + correct);
		ret.put("passPercent", 60);

		return ret;
	}

	@Override
	public Map<String, Object> getAssessmentByContentUser(String rootOrg, String courseId, String userId)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> submitAssessmentByIframe(String rootOrg, Map<String, Object> request) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
