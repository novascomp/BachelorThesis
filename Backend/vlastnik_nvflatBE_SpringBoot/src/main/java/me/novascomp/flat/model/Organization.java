package me.novascomp.flat.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;
import me.novascomp.utils.rest.RestUtils;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Entity
@Table(name = "organization", catalog = "BAKALARKA", schema = "nvflat")
@NamedQueries({
    @NamedQuery(name = "Organization.findAll", query = "SELECT o FROM Organization o"),
    @NamedQuery(name = "Organization.findByOrganizationId", query = "SELECT o FROM Organization o WHERE o.organizationId = :organizationId"),
    @NamedQuery(name = "Organization.findByIco", query = "SELECT o FROM Organization o WHERE o.ico = :ico")})
public class Organization implements Serializable {

    private static final long serialVersionUID = 1L;

    @Version
    private Integer version;

    @Id
    @Basic(optional = false)
    @Column(name = "organization_id", nullable = false, length = 10485760)
    private String organizationId;

    @Column(name = "ico", nullable = false, unique = true, length = 10485760)
    private String ico;

    @JsonIgnore
    @OneToMany(mappedBy = "organization")
    @Fetch(FetchMode.SUBSELECT)
    private List<Flat> flatList;

    @JsonIgnore
    @JoinColumn(name = "organization_id", referencedColumnName = "general_id", nullable = false, insertable = false, updatable = false)
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private General general;

    public Organization() {
    }

    public Organization(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getIco() {
        return ico;
    }

    public void setIco(String ico) {
        this.ico = ico;
    }

    public List<Flat> getFlatList() {
        return flatList;
    }

    public void setFlatList(List<Flat> flatList) {
        this.flatList = flatList;
    }

    public General getGeneral() {
        return general;
    }

    public void setGeneral(General general) {
        this.general = general;
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
        hash += (organizationId != null ? organizationId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Organization)) {
            return false;
        }
        Organization other = (Organization) object;
        return !((this.organizationId == null && other.organizationId != null) || (this.organizationId != null && !this.organizationId.equals(other.organizationId)));
    }

    @Override
    public String toString() {
        return "me.novascomp.flat.model.Organization[ organizationId=" + organizationId + " ]";
    }

    public String getFlatsLink() {
        String currentUrl = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURL().toString();
        return RestUtils.getBaseUrl(currentUrl) + RestUtils.getRoot() + "organizations/" + organizationId + "/flats/";
    }

}
