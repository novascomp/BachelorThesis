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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import me.novascomp.utils.rest.RestUtils;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Entity
@Table(name = "token", catalog = "BAKALARKA", schema = "nvflat")
@NamedQueries({
    @NamedQuery(name = "Token.findAll", query = "SELECT t FROM Token t"),
    @NamedQuery(name = "Token.findByTokenId", query = "SELECT t FROM Token t WHERE t.tokenId = :tokenId"),
    @NamedQuery(name = "Token.findByKey", query = "SELECT t FROM Token t WHERE t.key = :key")})
public class Token implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @Column(name = "token_id", nullable = false, length = 10485760)
    private String tokenId;

    @Column(name = "key", length = 10485760)
    private String key;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JoinColumn(name = "flat_id", referencedColumnName = "flat_id")
    @ManyToOne
    private Flat flat;

    @JsonIgnore
    @JoinColumn(name = "resident_id", referencedColumnName = "resident_id")
    @ManyToOne
    private Resident residentId;

    @JsonIgnore
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @ManyToOne
    private User userId;

    @JsonIgnore
    @JoinTable(name = "tokenscope", joinColumns = {
        @JoinColumn(name = "token_id", referencedColumnName = "token_id")}, inverseJoinColumns = {
        @JoinColumn(name = "scope_id", referencedColumnName = "scope_id")})
    @ManyToMany
    @Fetch(FetchMode.SUBSELECT)
    private List<Scope> scopeList;

    @JoinColumn(name = "token_id", referencedColumnName = "general_id", nullable = false, insertable = false, updatable = false)
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private General general;

    @Transient
    private boolean mapped;

    @Transient
    private String flatId;

    public Token() {
    }

    public Token(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<Scope> getScopeList() {
        return scopeList;
    }

    public void setScopeList(List<Scope> scopeList) {
        this.scopeList = scopeList;
    }

    public Flat getFlat() {
        return flat;
    }

    public void setFlat(Flat flat) {
        this.flat = flat;
    }

    public General getGeneral() {
        return general;
    }

    public void setGeneral(General general) {
        this.general = general;
    }

    public Resident getResidentId() {
        return residentId;
    }

    public void setResidentId(Resident residentId) {
        this.residentId = residentId;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    public boolean isMapped() {
        if (userId != null) {
            mapped = true;
        }
        return mapped;
    }

    public void setMapped(boolean mapped) {
        this.mapped = mapped;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (tokenId != null ? tokenId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Token)) {
            return false;
        }
        Token other = (Token) object;
        if ((this.tokenId == null && other.tokenId != null) || (this.tokenId != null && !this.tokenId.equals(other.tokenId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "me.novascomp.flat.model.Token[ tokenId=" + tokenId + " ]";
    }

    public String getScopesLink() {
        String currentUrl = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURL().toString();
        return RestUtils.getBaseUrl(currentUrl) + RestUtils.getRoot() + "tokens/" + tokenId + "/" + "scopes";
    }

    public String getResidentLink() {
        String currentUrl = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURL().toString();
        return RestUtils.getBaseUrl(currentUrl) + RestUtils.getRoot() + "tokens/" + tokenId + "/resident";
    }

    public String getUserLink() {
        String currentUrl = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURL().toString();
        return RestUtils.getBaseUrl(currentUrl) + RestUtils.getRoot() + "tokens/" + tokenId + "/user";
    }

    public String getFlatId() {
        if (flat != null) {
            return flat.getFlatId();
        }
        return null;
    }

    public void setFlatId(String flatId) {
        this.flatId = flatId;
    }

}
