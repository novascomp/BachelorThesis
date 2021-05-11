package me.novascomp.home.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import me.novascomp.utils.rest.RestUtils;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Entity
@Table(name = "committee", catalog = "BAKALARKA", schema = "nvhome")
@NamedQueries({
    @NamedQuery(name = "Committee.findAll", query = "SELECT c FROM Committee c"),
    @NamedQuery(name = "Committee.findByCommitteeId", query = "SELECT c FROM Committee c WHERE c.committeeId = :committeeId"),
    @NamedQuery(name = "Committee.findByEmail", query = "SELECT c FROM Committee c WHERE c.email = :email"),
    @NamedQuery(name = "Committee.findByPhone", query = "SELECT c FROM Committee c WHERE c.phone = :phone")})
public class Committee implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @Column(name = "committee_id", nullable = false, length = 10485760)
    private String committeeId;

    @Column(name = "email", length = 10485760)
    private String email;

    @Column(name = "phone", length = 10485760)
    private String phone;

    @JsonIgnore
    @JoinTable(name = "committeemember", joinColumns = {
        @JoinColumn(name = "committee_id", referencedColumnName = "committee_id")}, inverseJoinColumns = {
        @JoinColumn(name = "member_id", referencedColumnName = "member_id")})
    @ManyToMany
    @Fetch(FetchMode.SUBSELECT)
    private List<Member> memberList;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JoinColumn(name = "organization_id", referencedColumnName = "organization_id", nullable = false)
    @ManyToOne(optional = false)
    private Organization organization;

    @JsonIgnore
    @JoinColumn(name = "committee_id", referencedColumnName = "general_id", nullable = false, insertable = false, updatable = false)
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private General general;

    public Committee() {
    }

    public Committee(String committeeId) {
        this.committeeId = committeeId;
    }

    public String getCommitteeId() {
        return committeeId;
    }

    public void setCommitteeId(String committeeId) {
        this.committeeId = committeeId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<Member> getMemberList() {
        return memberList;
    }

    public void setMemberList(List<Member> memberList) {
        this.memberList = memberList;
    }

    public General getGeneral() {
        return general;
    }

    public void setGeneral(General general) {
        this.general = general;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (committeeId != null ? committeeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Committee)) {
            return false;
        }
        Committee other = (Committee) object;
        if ((this.committeeId == null && other.committeeId != null) || (this.committeeId != null && !this.committeeId.equals(other.committeeId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "me.novascomp.home.model.Committee[ committeeId=" + committeeId + " ]";
    }

    public String getOrganizationLink() {
        String currentUrl = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURL().toString();
        return RestUtils.getBaseUrl(currentUrl) + RestUtils.getRoot() + "organizations/" + organization.getOrganizationId();
    }

    public String getCommitteesMembers() {
        String currentUrl = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURL().toString();
        return RestUtils.getBaseUrl(currentUrl) + RestUtils.getRoot() + "committees/" + committeeId + "/members";
    }
}
