package org.sunbird.assessment.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.sunbird.core.exception.ApplicationLogicError;

@Service
public class AssessmentUtilServiceImpl implements AssessmentUtilService {

	private Map<String, Object> getAnswers(List<Map<String, Object>> questions) {
		Map<String, Object> ret = new HashMap<>();

		for (Map<String, Object> question : questions) {
			List<String> correctOption = new ArrayList<String>();

			if (question.containsKey("questionType")) {
				String questionType = (String) question.get("questionType");
				switch (questionType) {
				case "mtf":
					for (Map<String, Object> options : (List<Map<String, Object>>) question.get("options")) {
						if ((boolean) options.get("isCorrect"))
							correctOption.add(options.get("optionId").toString() + "-"
									+ options.get("text").toString().toLowerCase() + "-"
									+ options.get("match").toString().toLowerCase());
					}
					break;
				case "fitb":
					for (Map<String, Object> options : (List<Map<String, Object>>) question.get("options")) {
						if ((boolean) options.get("isCorrect"))
							correctOption.add(options.get("optionId").toString() + "-"
									+ options.get("text").toString().toLowerCase());
					}
					break;
				case "mcq-sca":
				case "mcq-mca":
					for (Map<String, Object> options : (List<Map<String, Object>>) question.get("options")) {
						if ((boolean) options.get("isCorrect"))
							correctOption.add(options.get("optionId").toString());
					}
					break;
				}
			} else {
				for (Map<String, Object> options : (List<Map<String, Object>>) question.get("options")) {
					if ((boolean) options.get("isCorrect"))
						correctOption.add(options.get("optionId").toString());
				}
			}
			ret.put(question.get("questionId").toString(), correctOption);
		}

		return ret;
	}

	public Map<String, Object> validateAssessment(List<Map<String, Object>> questions) {
		try {
			Integer correct = 0;
			Integer blank = 0;
			Integer inCorrect = 0;
			Double result = 0d;
			Map<String, Object> resultMap = new HashMap<String, Object>();
			Map<String, Object> answers = getAnswers(questions);
			for (Map<String, Object> question : questions) {
				List<String> marked = new ArrayList<String>();
				if (question.containsKey("questionType")) {
					String questionType = (String) question.get("questionType");
					switch (questionType) {
					case "mtf":
						for (Map<String, Object> options : (List<Map<String, Object>>) question.get("options")) {
							if (options.containsKey("response") && !options.get("response").toString().isEmpty())
								marked.add(options.get("optionId").toString() + "-"
										+ options.get("text").toString().toLowerCase() + "-"
										+ options.get("response").toString().toLowerCase());
						}
						break;
					case "fitb":
						for (Map<String, Object> options : (List<Map<String, Object>>) question.get("options")) {
							if (options.containsKey("response") && !options.get("response").toString().isEmpty())
								marked.add(options.get("optionId").toString() + "-"
										+ options.get("response").toString().toLowerCase());
						}
						break;
					case "mcq-sca":
					case "mcq-mca":
						for (Map<String, Object> options : (List<Map<String, Object>>) question.get("options")) {
							if ((boolean) options.get("userSelected"))
								marked.add(options.get("optionId").toString());
						}
						break;
					}
				} else {
					for (Map<String, Object> options : (List<Map<String, Object>>) question.get("options")) {
						if ((boolean) options.get("userSelected"))
							marked.add(options.get("optionId").toString());
					}
				}

				if (marked.size() == 0)
					blank++;
				else {
					List<String> answer = (List<String>) answers.get(question.get("questionId"));
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
			Double result = 0d;
			Map<String, Object> resultMap = new HashMap<String, Object>();
			for (Map<String, Object> question : questions) {
				List<String> marked = new ArrayList<String>();
				if (question.containsKey("questionType")) {
					if (question.get("questionType").toString().toLowerCase().equals("mtf")) {
						for (Map<String, Object> options : (List<Map<String, Object>>) question.get("options")) {
							if (options.containsKey("response") && !options.get("response").toString().isEmpty())
								marked.add(options.get("optionId").toString() + "-"
										+ options.get("text").toString().toLowerCase() + "-"
										+ options.get("response").toString().toLowerCase());
						}
					} else if (question.get("questionType").toString().toLowerCase().equals("fitb")) {
						for (Map<String, Object> options : (List<Map<String, Object>>) question.get("options")) {
							if (options.containsKey("response") && !options.get("response").toString().isEmpty())
								marked.add(options.get("optionId").toString() + "-"
										+ options.get("response").toString().toLowerCase());
						}
					} else if (question.get("questionType").toString().toLowerCase().equals("mcq-sca")
							|| question.get("questionType").toString().toLowerCase().equals("mcq-mca")) {
						for (Map<String, Object> options : (List<Map<String, Object>>) question.get("options")) {
							if ((boolean) options.get("userSelected"))
								marked.add(options.get("optionId").toString());
						}
					}
				} else {
					for (Map<String, Object> options : (List<Map<String, Object>>) question.get("options")) {
						if ((boolean) options.get("userSelected"))
							marked.add(options.get("optionId").toString());
					}
				}

				if (marked.size() == 0)
					blank++;
				else {
					List<String> answer = (List<String>) answers.get(question.get("questionId"));
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
}
