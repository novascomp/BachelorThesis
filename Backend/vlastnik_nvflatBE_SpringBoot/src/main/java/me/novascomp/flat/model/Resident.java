package me.novascomp.flat.model;

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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "resident", catalog = "BAKALARKA", schema = "nvflat")
@NamedQueries({
    @NamedQuery(name = "Resident.findAll", query = "SELECT r FROM Resident r"),
    @NamedQuery(name = "Resident.findByResidentId", query = "SELECT r FROM Resident r WHERE r.residentId = :residentId"),
    @NamedQuery(name = "Resident.findByFirstName", query = "SELECT r FROM Resident r WHERE r.firstName = :firstName"),
    @NamedQuery(name = "Resident.findByLastName", query = "SELECT r FROM Resident r WHERE r.lastName = :lastName"),
    @NamedQuery(name = "Resident.findByEmail", query = "SELECT r FROM Resident r WHERE r.email = :email"),
    @NamedQuery(name = "Resident.findByPhone", query = "SELECT r FROM Resident r WHERE r.phone = :phone"),
    @NamedQuery(name = "Resident.findByDateOfBirth", query = "SELECT r FROM Resident r WHERE r.dateOfBirth = :dateOfBirth")})
public class Resident implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @Column(name = "resident_id", nullable = false, length = 10485760)
    private String residentId;

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
    @ManyToMany(mappedBy = "residentList")
    @Fetch(FetchMode.SUBSELECT)
    private List<Detail> detailList;

    @JsonIgnore
    @OneToMany(mappedBy = "residentId")
    @Fetch(FetchMode.SUBSELECT)
    private List<Token> tokenList;

    @JsonIgnore
    @JoinColumn(name = "resident_id", referencedColumnName = "general_id", nullable = false, insertable = false, updatable = false)
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private General general;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Transient
    private String requiredFlatDetail;

    public Resident() {
    }

    public Resident(String residentId) {
        this.residentId = residentId;
    }

    public String getResidentId() {
        return residentId;
    }

    public void setResidentId(String residentId) {
        this.residentId = residentId;
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

    public List<Detail> getDetailList() {
        return detailList;
    }

    public void setDetailList(List<Detail> detailList) {
        this.detailList = detailList;
    }

    public List<Token> getTokenList() {
        return tokenList;
    }

    public void setTokenList(List<Token> tokenList) {
        this.tokenList = tokenList;
    }

    public General getGeneral() {
        return general;
    }

    public void setGeneral(General general) {
        this.general = general;
    }

    public String getRequiredFlatDetail() {
        return requiredFlatDetail;
    }

    public void setRequiredFlatDetail(String requiredFlatDetail) {
        this.requiredFlatDetail = requiredFlatDetail;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (residentId != null ? residentId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Resident)) {
            return false;
        }
        Resident other = (Resident) object;
        if ((this.residentId == null && other.residentId != null) || (this.residentId != null && !this.residentId.equals(other.residentId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "me.novascomp.flat.model.Resident[ residentId=" + residentId + " ]";
    }

}
