package org.sunbird.assessment.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.sunbird.cache.RedisCacheMgr;
import org.sunbird.common.util.Constants;
import org.sunbird.core.exception.ApplicationLogicError;
import org.sunbird.core.logger.CbExtLogger;

@Service
public class AssessmentUtilServiceV2Impl implements AssessmentUtilServiceV2 {

	@Autowired
	RedisCacheMgr redisCacheMgr;

	private CbExtLogger logger = new CbExtLogger(getClass().getName());

	public static final String QUESTION_TYPE = "qType";
	public static final String OPTIONS = "options";
	public static final String IS_CORRECT = "isCorrect";
	public static final String OPTION_ID = "optionId";
	public static final String MCQ_SCA = "mcq-sca";
	public static final String MCQ_MCA = "mcq-mca";
	public static final String FTB = "ftb";
	public static final String MTF = "mtf";
	public static final String QUESTION_ID = "questionId";
	public static final String RESPONSE = "response";
	public static final String ANSWER = "answer";
	public static final String VALUE = "value";
	public static final String EDITOR_STATE = "editorState";
	public static final String BODY = "body";
	public static final String SELECTED_ANSWER = "selectedAnswer";
	public static final String INDEX = "index";

	public Map<String, Object> validateQumlAssessment(List<String> originalQuestionList,
			List<Map<String, Object>> userQuestionList) {
		try {
			Integer correct = 0;
			Integer blank = 0;
			Integer inCorrect = 0;
			Double result;
			Integer total = 0;
			Map<String, Object> resultMap = new HashMap<>();
			Map<String, Object> answers = getQumlAnswers(originalQuestionList);
			for (Map<String, Object> question : userQuestionList) {
				List<String> marked = new ArrayList<>();
				if (question.containsKey(QUESTION_TYPE)) {
					String questionType = ((String) question.get(QUESTION_TYPE)).toLowerCase();
					Map<String, Object> editorStateObj = (Map<String, Object>) question.get(EDITOR_STATE);
					List<Map<String, Object>> options = (List<Map<String, Object>>) editorStateObj.get(OPTIONS);
					switch (questionType) {
					case MTF:
						for (Map<String, Object> option : options) {
							marked.add(option.get(INDEX).toString() + "-"
									+ option.get(SELECTED_ANSWER).toString().toLowerCase());
						}
						break;
					case FTB:
						for (Map<String, Object> option : options) {
							marked.add((String) option.get(SELECTED_ANSWER));
						}
						break;
					case MCQ_SCA:
					case MCQ_MCA:
						for (Map<String, Object> option : options) {
							if ((boolean) option.get(SELECTED_ANSWER)) {
								marked.add((String) option.get(INDEX));
							}
						}
						break;
					default:
						break;
					}
				} else {
					// TODO - how to handle this case??
					// Currently throw error
				}

				if (CollectionUtils.isEmpty(marked))
					blank++;
				else {
					List<String> answer = (List<String>) answers.get(question.get(Constants.IDENTIFIER));
					if (answer.size() > 1)
						Collections.sort(answer);
					if (marked.size() > 1)
						Collections.sort(marked);
					if (answer.equals(marked))
						correct++;
					else
						inCorrect++;
				}
				total++;
			}
			// Increment the blank counter for skipped question objects
			if (answers.size() > userQuestionList.size()) {
				blank += answers.size() - userQuestionList.size();
			}
			result = ((correct * 100d) / (correct + blank + inCorrect));
			resultMap.put("result", result);
			resultMap.put("incorrect", inCorrect);
			resultMap.put("blank", blank);
			resultMap.put("correct", correct);
			resultMap.put("total", total);
			return resultMap;

		} catch (Exception ex) {
			logger.error(ex);
			throw new ApplicationLogicError("Error when verifying assessment. Error : " + ex.getMessage(), ex);
		}
	}

	private Map<String, Object> getQumlAnswers(List<String> questions) throws Exception {
		Map<String, Object> ret = new HashMap<>();
		for (String questionId : questions) {
			List<String> correctOption = new ArrayList<>();

			Map<String, Object> question = (Map<String, Object>) redisCacheMgr
					.getCache(Constants.QUESTION_ID + questionId);
			if (ObjectUtils.isEmpty(question)) {
				logger.error(new Exception("Failed to get the answer for question: " + questionId));
				// TODO - Need to handle this scenario.
			}
			if (question.containsKey(QUESTION_TYPE)) {
				String questionType = ((String) question.get(QUESTION_TYPE)).toLowerCase();
				Map<String, Object> editorStateObj = (Map<String, Object>) question.get(EDITOR_STATE);
				List<Map<String, Object>> options = (List<Map<String, Object>>) editorStateObj.get(OPTIONS);
				switch (questionType) {
				case MTF:
					for (Map<String, Object> option : options) {
						Map<String, Object> valueObj = (Map<String, Object>) option.get(VALUE);
						correctOption.add(
								valueObj.get(VALUE).toString() + "-" + option.get(ANSWER).toString().toLowerCase());
					}
					break;
				case FTB:
					for (Map<String, Object> option : options) {
						correctOption.add((String) option.get(SELECTED_ANSWER));
					}
					break;
				case MCQ_SCA:
				case MCQ_MCA:
					for (Map<String, Object> option : options) {
						if ((boolean) option.get(ANSWER)) {
							Map<String, Object> valueObj = (Map<String, Object>) option.get(VALUE);
							correctOption.add(valueObj.get(VALUE).toString());
						}
					}
					break;
				default:
					break;
				}
			} else {
				for (Map<String, Object> options : (List<Map<String, Object>>) question.get(OPTIONS)) {
					if ((boolean) options.get(IS_CORRECT))
						correctOption.add(options.get(OPTION_ID).toString());
				}
			}
			ret.put(question.get(Constants.IDENTIFIER).toString(), correctOption);
		}

		return ret;
	}
}
