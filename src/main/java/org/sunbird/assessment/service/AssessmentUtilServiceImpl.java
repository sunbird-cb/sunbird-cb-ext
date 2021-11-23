package org.sunbird.assessment.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.htrace.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.sunbird.assessment.model.QuestionSet;
import org.sunbird.assessment.model.Questions;
import org.sunbird.core.exception.ApplicationLogicError;

@Service
public class AssessmentUtilServiceImpl implements AssessmentUtilService {

	public static final String QUESTION_TYPE = "questionType";
	public static final String OPTIONS = "options";
	public static final String IS_CORRECT = "isCorrect";
	public static final String OPTION_ID = "optionId";
	public static final String MCQ_SCA = "mcq-sca";
	public static final String MCQ_MCA = "mcq-mca";
	public static final String FITB = "fitb";
	public static final String MTF = "mtf";
	public static final String QUESTION_ID = "questionId";
	public static final String RESPONSE = "response";
	public static final String USER_SELECTED = "userSelected";

	private Map<String, Object> getAnswers(List<Map<String, Object>> questions) {
		Map<String, Object> ret = new HashMap<>();

		for (Map<String, Object> question : questions) {
			List<String> correctOption = new ArrayList<>();

			if (question.containsKey(QUESTION_TYPE)) {
				String questionType = (String) question.get(QUESTION_TYPE);
				switch (questionType) {
				case "mtf":
					for (Map<String, Object> options : (List<Map<String, Object>>) question.get(OPTIONS)) {
						if ((boolean) options.get(IS_CORRECT))
							correctOption.add(options.get(OPTION_ID).toString() + "-"
									+ options.get("text").toString().toLowerCase() + "-"
									+ options.get("match").toString().toLowerCase());
					}
					break;
				case "fitb":
					for (Map<String, Object> options : (List<Map<String, Object>>) question.get(OPTIONS)) {
						if ((boolean) options.get(IS_CORRECT))
							correctOption.add(options.get(OPTION_ID).toString() + "-"
									+ options.get("text").toString().toLowerCase());
					}
					break;
				case MCQ_SCA:
				case MCQ_MCA:
					for (Map<String, Object> options : (List<Map<String, Object>>) question.get(OPTIONS)) {
						if ((boolean) options.get(IS_CORRECT))
							correctOption.add(options.get(OPTION_ID).toString());
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
			ret.put(question.get(QUESTION_ID).toString(), correctOption);
		}

		return ret;
	}

	public Map<String, Object> validateAssessment(List<Map<String, Object>> questions) {
		try {
			Integer correct = 0;
			Integer blank = 0;
			Integer inCorrect = 0;
			Double result;
			Map<String, Object> resultMap = new HashMap<>();
			Map<String, Object> answers = getAnswers(questions);
			for (Map<String, Object> question : questions) {
				List<String> marked = new ArrayList<>();
				if (question.containsKey(QUESTION_TYPE)) {
					String questionType = (String) question.get(QUESTION_TYPE);
					switch (questionType) {
					case "mtf":
						for (Map<String, Object> options : (List<Map<String, Object>>) question.get(OPTIONS)) {
							if (options.containsKey(RESPONSE) && !options.get(RESPONSE).toString().isEmpty())
								marked.add(options.get(OPTION_ID).toString() + "-"
										+ options.get("text").toString().toLowerCase() + "-"
										+ options.get(RESPONSE).toString().toLowerCase());
						}
						break;
					case "fitb":
						for (Map<String, Object> options : (List<Map<String, Object>>) question.get(OPTIONS)) {
							if (options.containsKey(RESPONSE) && !options.get(RESPONSE).toString().isEmpty())
								marked.add(options.get(OPTION_ID).toString() + "-"
										+ options.get(RESPONSE).toString().toLowerCase());
						}
						break;
					case MCQ_SCA:
					case MCQ_MCA:
						for (Map<String, Object> options : (List<Map<String, Object>>) question.get(OPTIONS)) {
							if ((boolean) options.get(USER_SELECTED))
								marked.add(options.get(OPTION_ID).toString());
						}
						break;
					default:
						break;
					}
				} else {
					for (Map<String, Object> options : (List<Map<String, Object>>) question.get(OPTIONS)) {
						if ((boolean) options.get(USER_SELECTED))
							marked.add(options.get(OPTION_ID).toString());
					}
				}

				if (CollectionUtils.isEmpty(marked))
					blank++;
				else {
					List<String> answer = (List<String>) answers.get(question.get(QUESTION_ID));
					if (answer.size() > 1)
						Collections.sort(answer);
					if (marked.size() > 1)
						Collections.sort(marked);
					if (answer.equals(marked))
						correct++;
					else
						inCorrect++;
				}
			}
			result = ((correct * 100d) / (correct + blank + inCorrect));
			resultMap.put("result", result);
			resultMap.put("incorrect", inCorrect);
			resultMap.put("blank", blank);
			resultMap.put("correct", correct);
			return resultMap;

		} catch (Exception ex) {
			throw new ApplicationLogicError("Error when verifying assessment. Error : " + ex.getMessage(), ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> validateAssessment(List<Map<String, Object>> questions, Map<String, Object> answers) {
		try {
			Integer correct = 0;
			Integer blank = 0;
			Integer inCorrect = 0;
			Double result;
			Map<String, Object> resultMap = new HashMap<>();
			for (Map<String, Object> question : questions) {
				List<String> marked = new ArrayList<>();
				if (question.containsKey(QUESTION_TYPE)) {
					if (question.get(QUESTION_TYPE).toString().equalsIgnoreCase("mtf")) {
						for (Map<String, Object> options : (List<Map<String, Object>>) question.get(OPTIONS)) {
							if (options.containsKey(RESPONSE) && !options.get(RESPONSE).toString().isEmpty())
								marked.add(options.get(OPTION_ID).toString() + "-"
										+ options.get("text").toString().toLowerCase() + "-"
										+ options.get(RESPONSE).toString().toLowerCase());
						}
					} else if (question.get(QUESTION_TYPE).toString().equalsIgnoreCase("fitb")) {
						for (Map<String, Object> options : (List<Map<String, Object>>) question.get(OPTIONS)) {
							if (options.containsKey(RESPONSE) && !options.get(RESPONSE).toString().isEmpty())
								marked.add(options.get(OPTION_ID).toString() + "-"
										+ options.get(RESPONSE).toString().toLowerCase());
						}
					} else if (question.get(QUESTION_TYPE).toString().equalsIgnoreCase((MCQ_SCA))
							|| question.get(QUESTION_TYPE).toString().equalsIgnoreCase(MCQ_MCA)) {
						for (Map<String, Object> options : (List<Map<String, Object>>) question.get(OPTIONS)) {
							if ((boolean) options.get(USER_SELECTED))
								marked.add(options.get(OPTION_ID).toString());
						}
					}
				} else {
					for (Map<String, Object> options : (List<Map<String, Object>>) question.get(OPTIONS)) {
						if ((boolean) options.get(USER_SELECTED))
							marked.add(options.get(OPTION_ID).toString());
					}
				}

				if (CollectionUtils.isEmpty(marked))
					blank++;
				else {
					List<String> answer = (List<String>) answers.get(question.get(QUESTION_ID));
					if (answer.size() > 1)
						Collections.sort(answer);
					if (marked.size() > 1)
						Collections.sort(marked);
					if (answer.equals(marked))
						correct++;
					else
						inCorrect++;
				}
			}
			result = ((correct * 100d) / (correct + blank + inCorrect));
			resultMap.put("result", result);
			resultMap.put("incorrect", inCorrect);
			resultMap.put("blank", blank);
			resultMap.put("correct", correct);
			return resultMap;
		} catch (Exception ex) {
			throw new ApplicationLogicError("Error when verifying assessment. Error : " + ex.getMessage(), ex);
		}

	}

	/*
	 * This method fetches the answer key for assessment
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getAnswerKeyForAssessmentAuthoringPreview(Map<String, Object> contentMeta) {
		return null;
	}

	/**
	 * To remove answers from the assessment question sets
	 * 
	 * @param assessmentContent
	 *            Object
	 * @return QuestionSet
	 */
	@Override
	public QuestionSet removeAssessmentAnsKey(Object assessmentContent) {
		QuestionSet questionSet = new ObjectMapper().convertValue(assessmentContent, QuestionSet.class);
		List<String> qnsTypes = Arrays.asList(MCQ_MCA, MCQ_SCA, FITB, MTF);
		for (Questions question : questionSet.getQuestions()) {
			if (qnsTypes.contains(question.getQuestionType()) && !ObjectUtils.isEmpty(question.getOptions())) {
				for (Map<String, Object> option : question.getOptions()) {
					option.remove(IS_CORRECT);
				}
			}
		}
		return questionSet;
	}
}
