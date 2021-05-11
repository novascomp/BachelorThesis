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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "scope", catalog = "BAKALARKA", schema = "nvflat")
@NamedQueries({
    @NamedQuery(name = "Scope.findAll", query = "SELECT s FROM Scope s"),
    @NamedQuery(name = "Scope.findByScopeId", query = "SELECT s FROM Scope s WHERE s.scopeId = :scopeId"),
    @NamedQuery(name = "Scope.findByScope", query = "SELECT s FROM Scope s WHERE s.scope = :scope")})
public class Scope implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @Column(name = "scope_id", nullable = false, length = 10485760)
    private String scopeId;

    @Column(name = "scope", length = 10485760)
    private String scope;

    @JsonIgnore
    @ManyToMany(mappedBy = "scopeList")
    @Fetch(FetchMode.SUBSELECT)
    private List<Token> tokenList;

    @JsonIgnore
    @ManyToMany(mappedBy = "scopeList")
    @Fetch(FetchMode.SUBSELECT)
    private List<MessageRecordNvm> messageRecordNvmList;

    @JsonIgnore
    @JoinColumn(name = "scope_id", referencedColumnName = "general_id", nullable = false, insertable = false, updatable = false)
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private General general;

    public Scope() {
    }

    public Scope(String scopeId) {
        this.scopeId = scopeId;
    }

    public String getScopeId() {
        return scopeId;
    }

    public void setScopeId(String scopeId) {
        this.scopeId = scopeId;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public List<Token> getTokenList() {
        return tokenList;
    }

    public void setTokenList(List<Token> tokenList) {
        this.tokenList = tokenList;
    }

    public List<MessageRecordNvm> getMessageRecordNvmList() {
        return messageRecordNvmList;
    }

    public void setMessageRecordNvmList(List<MessageRecordNvm> messageRecordNvmList) {
        this.messageRecordNvmList = messageRecordNvmList;
    }

    public General getGeneral() {
        return general;
    }

    public void setGeneral(General general) {
        this.general = general;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (scopeId != null ? scopeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Scope)) {
            return false;
        }
        Scope other = (Scope) object;
        if ((this.scopeId == null && other.scopeId != null) || (this.scopeId != null && !this.scopeId.equals(other.scopeId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "me.novascomp.flat.model.Scope[ scopeId=" + scopeId + " ]";
    }

}
