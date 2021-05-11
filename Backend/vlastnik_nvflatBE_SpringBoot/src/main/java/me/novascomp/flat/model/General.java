/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.novascomp.flat.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Paul
 */
@Entity
@Table(name = "general", catalog = "BAKALARKA", schema = "nvflat")
@NamedQueries({
    @NamedQuery(name = "General.findAll", query = "SELECT g FROM General g"),
    @NamedQuery(name = "General.findByGeneralId", query = "SELECT g FROM General g WHERE g.generalId = :generalId"),
    @NamedQuery(name = "General.findByDate", query = "SELECT g FROM General g WHERE g.date = :date"),
    @NamedQuery(name = "General.findByTime", query = "SELECT g FROM General g WHERE g.time = :time"),
    @NamedQuery(name = "General.findBySwBuild", query = "SELECT g FROM General g WHERE g.swBuild = :swBuild")})
public class General implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "general_id", nullable = false, length = 10485760)
    private String generalId;
    @Column(name = "date")
    @Temporal(TemporalType.DATE)
    private Date date;
    @Column(name = "time")
    @Temporal(TemporalType.TIME)
    private Date time;
    @Column(name = "sw_build", length = 10485760)
    private String swBuild;
    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "general")
    private Flat flat;
    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "general")
    private Organization organization;
    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "general")
    private Scope scope;
    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "general")
    private Detail detail;
    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "general")
    private User user;
    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "general")
    private MessageRecordNvm messageRecordNvm;
    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "general")
    private Token token;
    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "general")
    private Resident resident;

    public General() {
    }

    public General(String generalId) {
        this.generalId = generalId;
    }

    public String getGeneralId() {
        return generalId;
    }

    public void setGeneralId(String generalId) {
        this.generalId = generalId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getSwBuild() {
        return swBuild;
    }

    public void setSwBuild(String swBuild) {
        this.swBuild = swBuild;
    }

    public Flat getFlat() {
        return flat;
    }

    public void setFlat(Flat flat) {
        this.flat = flat;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public Detail getDetail() {
        return detail;
    }

    public void setDetail(Detail detail) {
        this.detail = detail;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public MessageRecordNvm getMessageRecordNvm() {
        return messageRecordNvm;
    }

    public void setMessageRecordNvm(MessageRecordNvm messageRecordNvm) {
        this.messageRecordNvm = messageRecordNvm;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public Resident getResident() {
        return resident;
    }

    public void setResident(Resident resident) {
        this.resident = resident;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (generalId != null ? generalId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof General)) {
            return false;
        }
        General other = (General) object;
        if ((this.generalId == null && other.generalId != null) || (this.generalId != null && !this.generalId.equals(other.generalId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "me.novascomp.flat.model.General[ generalId=" + generalId + " ]";
    }

}
