
package org.sunbird.assessment.model;

import java.io.Serializable;

import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "questionSet"
})

public class Result extends JdkSerializationRedisSerializer implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@JsonProperty("questionSet")
    private QuestionSet questionSet;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Result() {
    }

    /**
     * 
     * @param questionSet
     */
    public Result(QuestionSet questionSet) {
        super();
        this.questionSet = questionSet;
    }

    @JsonProperty("questionSet")
    public QuestionSet getQuestionSet() {
        return questionSet;
    }

    @JsonProperty("questionSet")
    public void setQuestionSet(QuestionSet questionSet) {
        this.questionSet = questionSet;
    }

    public Result withQuestionSet(QuestionSet questionSet) {
        this.questionSet = questionSet;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Result.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("questionSet");
        sb.append('=');
        sb.append(((this.questionSet == null)?"<null>":this.questionSet));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}
