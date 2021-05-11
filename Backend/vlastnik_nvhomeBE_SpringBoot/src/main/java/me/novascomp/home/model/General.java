package me.novascomp.home.model;

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

@Entity
@Table(name = "general", catalog = "BAKALARKA", schema = "nvhome")
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
    private Committee committee;
    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "general")
    private File file;
    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "general")
    private Organization organization;
    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "general")
    private Member member1;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "general")
    @JsonIgnore
    private User user;
    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "general")
    private Token token;

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

    public Committee getCommittee() {
        return committee;
    }

    public void setCommittee(Committee committee) {
        this.committee = committee;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Member getMember1() {
        return member1;
    }

    public void setMember1(Member member1) {
        this.member1 = member1;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
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
        return "me.novascomp.home.model.General[ generalId=" + generalId + " ]";
    }

}
