package org.sunbird.digilocker.model.certificateMetaData;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class ParticipationCertificate {
    private String name;
    private String representingDistric;
    private String tournamentName;

    private Participant participant;

    private Tournament tournament;

    public ParticipationCertificate() {
        this.name = "";
        this.representingDistric = "";
        this. tournamentName = "";
        this.participant = new Participant();
        this.tournament = new Tournament();
    }
    @JacksonXmlProperty(isAttribute = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String getRepresentingDistric() {
        return representingDistric;
    }

    public void setRepresentingDistric(String representingDistric) {
        this.representingDistric = representingDistric;
    }

    @JacksonXmlProperty(localName = "tournamentname" ,isAttribute = true)
    public String getTournamentName() {
        return tournamentName;
    }

    public void setTournamentName(String tournamentName) {
        this.tournamentName = tournamentName;
    }

    @JacksonXmlProperty(localName = "Participant",  namespace = "http://tempuri.org/")
    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }

    @JacksonXmlProperty(localName = "Tournament",  namespace = "http://tempuri.org/")
    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }
}
