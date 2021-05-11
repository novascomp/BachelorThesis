package me.novascomp.home.model;

import ares.vr.fe.AresVrForFEPruposes;
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
import javax.persistence.Transient;
import javax.persistence.Version;
import me.novascomp.utils.rest.RestUtils;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Entity
@Table(name = "organization", catalog = "BAKALARKA", schema = "nvhome")
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

    @Column(name = "ico", length = 10485760)
    private String ico;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "organization")
    @Fetch(FetchMode.SUBSELECT)
    private List<Committee> committeeList;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "organization")
    @Fetch(FetchMode.SUBSELECT)
    private List<File> fileList;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "organization")
    @Fetch(FetchMode.SUBSELECT)
    private List<Token> tokenList;

    @JsonIgnore
    @JoinColumn(name = "organization_id", referencedColumnName = "general_id", nullable = false, insertable = false, updatable = false)
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private General general;

    @Transient
    private AresVrForFEPruposes aresVrForFEPruposes;

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

    public List<Committee> getCommitteeList() {
        return committeeList;
    }

    public void setCommitteeList(List<Committee> committeeList) {
        this.committeeList = committeeList;
    }

    public List<File> getFileList() {
        return fileList;
    }

    public void setFileList(List<File> fileList) {
        this.fileList = fileList;
    }

    public General getGeneral() {
        return general;
    }

    public void setGeneral(General general) {
        this.general = general;
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
        if ((this.organizationId == null && other.organizationId != null) || (this.organizationId != null && !this.organizationId.equals(other.organizationId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "me.novascomp.home.model.Organization[ organizationId=" + organizationId + " ]";
    }

    public String getTokensLink() {
        String currentUrl = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURL().toString();
        return RestUtils.getBaseUrl(currentUrl) + RestUtils.getRoot() + "organizations/" + organizationId + "/tokens/";
    }

    public String getFilesLink() {
        String currentUrl = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURL().toString();
        return RestUtils.getBaseUrl(currentUrl) + RestUtils.getRoot() + "organizations/" + organizationId + "/files/";
    }

    public AresVrForFEPruposes getAresVrForFEPruposes() {
        return aresVrForFEPruposes;
    }

    public void setAresVrForFEPruposes(AresVrForFEPruposes aresVrForFEPruposes) {
        this.aresVrForFEPruposes = aresVrForFEPruposes;
    }

}
