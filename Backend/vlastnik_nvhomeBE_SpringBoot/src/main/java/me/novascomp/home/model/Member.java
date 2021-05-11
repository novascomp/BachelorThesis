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
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "member", catalog = "BAKALARKA", schema = "nvhome")
@NamedQueries({
    @NamedQuery(name = "Member.findAll", query = "SELECT m FROM Member m"),
    @NamedQuery(name = "Member.findByMemberId", query = "SELECT m FROM Member m WHERE m.memberId = :memberId"),
    @NamedQuery(name = "Member.findByFirstName", query = "SELECT m FROM Member m WHERE m.firstName = :firstName"),
    @NamedQuery(name = "Member.findByLastName", query = "SELECT m FROM Member m WHERE m.lastName = :lastName"),
    @NamedQuery(name = "Member.findByEmail", query = "SELECT m FROM Member m WHERE m.email = :email"),
    @NamedQuery(name = "Member.findByPhone", query = "SELECT m FROM Member m WHERE m.phone = :phone"),
    @NamedQuery(name = "Member.findByDateOfBirth", query = "SELECT m FROM Member m WHERE m.dateOfBirth = :dateOfBirth")})
public class Member implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @Column(name = "member_id", nullable = false, length = 10485760)
    private String memberId;

    @Column(name = "first_name", length = 10485760)
    private String firstName;

    @Column(name = "last_name", length = 10485760)
    private String lastName;

    @Column(name = "email", length = 10485760)
    private String email;

    @Column(name = "phone", length = 10485760)
    private String phone;

    @Column(name = "date_of_birth", length = 10485760)
    private String dateOfBirth;

    @JsonIgnore
    @ManyToMany(mappedBy = "memberList")
    @Fetch(FetchMode.SUBSELECT)
    private List<Committee> committeeList;

    @JsonIgnore
    @JoinColumn(name = "member_id", referencedColumnName = "general_id", nullable = false, insertable = false, updatable = false)
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private General general;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Transient
    private String requiredCommittee;

    public Member() {
    }

    public Member(String memberId) {
        this.memberId = memberId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public List<Committee> getCommitteeList() {
        return committeeList;
    }

    public void setCommitteeList(List<Committee> committeeList) {
        this.committeeList = committeeList;
    }

    public General getGeneral() {
        return general;
    }

    public void setGeneral(General general) {
        this.general = general;
    }

    public String getRequiredCommittee() {
        return requiredCommittee;
    }

    public void setRequiredCommittee(String requiredCommittee) {
        this.requiredCommittee = requiredCommittee;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (memberId != null ? memberId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Member)) {
            return false;
        }
        Member other = (Member) object;
        if ((this.memberId == null && other.memberId != null) || (this.memberId != null && !this.memberId.equals(other.memberId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "me.novascomp.home.model.Member[ memberId=" + memberId + " ]";
    }

}
