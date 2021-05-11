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
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import me.novascomp.utils.rest.RestUtils;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Entity
@Table(name = "flat", catalog = "BAKALARKA", schema = "nvflat")
@NamedQueries({
    @NamedQuery(name = "Flat.findAll", query = "SELECT f FROM Flat f"),
    @NamedQuery(name = "Flat.findByFlatId", query = "SELECT f FROM Flat f WHERE f.flatId = :flatId"),
    @NamedQuery(name = "Flat.findByIdentifier", query = "SELECT f FROM Flat f WHERE f.identifier = :identifier")})
public class Flat implements Serializable {

    private static final long serialVersionUID = 1L;

    @Version
    private Integer version;

    @Id
    @Basic(optional = false)
    @Column(name = "flat_id", nullable = false, length = 10485760)
    private String flatId;

    @Column(name = "identifier", length = 10485760)
    private String identifier;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JoinColumn(name = "organization_id", referencedColumnName = "organization_id")
    @ManyToOne
    private Organization organization;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "flat")
    @Fetch(FetchMode.SUBSELECT)
    private List<Detail> detailList;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "flat")
    @Fetch(FetchMode.SUBSELECT)
    private List<Token> tokenList;

    @JsonIgnore
    @JoinColumn(name = "flat_id", referencedColumnName = "general_id", nullable = false, insertable = false, updatable = false)
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private General general;

    @Transient
    private String organizationId;

    public Flat() {
    }

    public Flat(String flatId) {
        this.flatId = flatId;
    }

    public String getFlatId() {
        return flatId;
    }

    public void setFlatId(String flatId) {
        this.flatId = flatId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (flatId != null ? flatId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Flat)) {
            return false;
        }
        Flat other = (Flat) object;
        if ((this.flatId == null && other.flatId != null) || (this.flatId != null && !this.flatId.equals(other.flatId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "me.novascomp.flat.model.Flat[ flatId=" + flatId + " ]";
    }

    public String getFlatOrganizationLink() {
        String currentUrl = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURL().toString();
        return RestUtils.getBaseUrl(currentUrl) + RestUtils.getRoot() + "flats/" + flatId + "/" + "organization";
    }

    public String getFlatDetailLink() {
        String currentUrl = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURL().toString();
        return RestUtils.getBaseUrl(currentUrl) + RestUtils.getRoot() + "flats/" + flatId + "/" + "detail";
    }

    public String getFlatTokensLink() {
        String currentUrl = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURL().toString();
        return RestUtils.getBaseUrl(currentUrl) + RestUtils.getRoot() + "flats/" + flatId + "/" + "tokens";
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getOrganizationId() {
        if (this.organization != null) {
            return organization.getOrganizationId();
        }
        return null;
    }
}
