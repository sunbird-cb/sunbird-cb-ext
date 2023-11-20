package org.sunbird.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = { "classpath:application.properties" })
public class PropertiesConfig {

    @Value("${lms.service.host}")
    private String lmsServiceHost;

    @Value("${lms.user.search}")
    private String lmsUserSearchEndPoint;

    @Value("${hub.notification.rootOrg}")
    private String hubRootOrg;

    @Value("${notify.service.host}")
    private String notifyServiceHost;

    @Value("${notify.service.path}")
    private String notifyServicePath;

    @Value("${notify.email.template}")
    private String notificationEmailTemplate;

    @Value("${notification.support.mail}")
    private String supportEmail;

    @Value("${notification.mail.body.for.attendance}")
    private String attendanceNotificationMailBody;

    public String getInsightsLabelCertificatesAcross() {
        return insightsLabelCertificatesAcross;
    }

    public void setInsightsLabelCertificatesAcross(String insightsLabelCertificatesAcross) {
        this.insightsLabelCertificatesAcross = insightsLabelCertificatesAcross;
    }

    public String getInsightsLabelLearningHoursAcross() {
        return insightsLabelLearningHoursAcross;
    }

    public void setInsightsLabelLearningHoursAcross(String insightsLabelLearningHoursAcross) {
        this.insightsLabelLearningHoursAcross = insightsLabelLearningHoursAcross;
    }

    public String getInsightsLabelCertificatesYourDepartment() {
        return insightsLabelCertificatesYourDepartment;
    }

    public void setInsightsLabelCertificatesYourDepartment(String insightsLabelCertificatesYourDepartment) {
        this.insightsLabelCertificatesYourDepartment = insightsLabelCertificatesYourDepartment;
    }

    public String getInsightsLabelLearningHoursYourDepartment() {
        return insightsLabelLearningHoursYourDepartment;
    }

    public void setInsightsLabelLearningHoursYourDepartment(String insightsLabelLearningHoursYourDepartment) {
        this.insightsLabelLearningHoursYourDepartment = insightsLabelLearningHoursYourDepartment;
    }

    @Value("${insights.label.certificates.across}")
    private String insightsLabelCertificatesAcross;
    @Value("${insights.label.learning.hours.across}")
    private String insightsLabelLearningHoursAcross;
    @Value("${insights.label.certificates.your.department}")
    private String insightsLabelCertificatesYourDepartment;
    @Value("${insights.label.learning.hours.your.department}")
    private String insightsLabelLearningHoursYourDepartment;

    public String getLmsServiceHost() {
        return lmsServiceHost;
    }

    public void setLmsServiceHost(String lmsServiceHost) {
        this.lmsServiceHost = lmsServiceHost;
    }

    public String getLmsUserSearchEndPoint() {
        return lmsUserSearchEndPoint;
    }

    public void setLmsUserSearchEndPoint(String lmsUserSearchEndPoint) {
        this.lmsUserSearchEndPoint = lmsUserSearchEndPoint;
    }

    public String getHubRootOrg() {
        return hubRootOrg;
    }

    public void setHubRootOrg(String hubRootOrg) {
        this.hubRootOrg = hubRootOrg;
    }

    public String getNotifyServiceHost() {
        return notifyServiceHost;
    }

    public void setNotifyServiceHost(String notifyServiceHost) {
        this.notifyServiceHost = notifyServiceHost;
    }

    public String getNotifyServicePath() {
        return notifyServicePath;
    }

    public void setNotifyServicePath(String notifyServicePath) {
        this.notifyServicePath = notifyServicePath;
    }

    public String getNotificationEmailTemplate() {
        return notificationEmailTemplate;
    }

    public void setNotificationEmailTemplate(String notificationEmailTemplate) {
        this.notificationEmailTemplate = notificationEmailTemplate;
    }

    public String getSupportEmail() {
        return supportEmail;
    }

    public void setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
    }

    public String getAttendanceNotificationMailBody() {
        return attendanceNotificationMailBody;
    }

    public void setAttendanceNotificationMailBody(String attendanceNotificationMailBody) {
        this.attendanceNotificationMailBody = attendanceNotificationMailBody;
    }
}
