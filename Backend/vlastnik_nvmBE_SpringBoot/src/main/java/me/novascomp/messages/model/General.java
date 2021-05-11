/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.novascomp.messages.model;

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
@Table(name = "general", catalog = "BAKALARKA", schema = "nvm")
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
    private Re re;
    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "general")
    private FileNvf fileNvf;
    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "general")
    private Category category;
    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "general")
    private Message message;
    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "general")
    private Priority priority;
    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "general")
    private User user;

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

    public Re getRe() {
        return re;
    }

    public void setRe(Re re) {
        this.re = re;
    }

    public FileNvf getFileNvf() {
        return fileNvf;
    }

    public void setFileNvf(FileNvf fileNvf) {
        this.fileNvf = fileNvf;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
        return "me.novascmp.messages.model.General[ generalId=" + generalId + " ]";
    }
    
}
