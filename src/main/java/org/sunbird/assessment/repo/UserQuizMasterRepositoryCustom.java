package org.sunbird.assessment.repo;

public interface UserQuizMasterRepositoryCustom {
	/**
	 * update quiz and quiz summary
	 * 
	 * @param quiz
	 * @param quizSummary
	 * @return
	 */
	public UserQuizMasterModel updateQuiz(UserQuizMasterModel quiz, UserQuizSummaryModel quizSummary);
}
