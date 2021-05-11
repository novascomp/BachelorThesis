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

/**
 *
 * @author Paul
 */
@Entity
@Table(name = "message_record_nvm", catalog = "BAKALARKA", schema = "nvflat")
@NamedQueries({
    @NamedQuery(name = "MessageRecordNvm.findAll", query = "SELECT m FROM MessageRecordNvm m"),
    @NamedQuery(name = "MessageRecordNvm.findByIdInNvm", query = "SELECT m FROM MessageRecordNvm m WHERE m.idInNvm = :idInNvm"),
    @NamedQuery(name = "MessageRecordNvm.findByMessageRecordNvmId", query = "SELECT m FROM MessageRecordNvm m WHERE m.messageRecordNvmId = :messageRecordNvmId")})
public class MessageRecordNvm implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @Column(name = "message_record_nvm_id", nullable = false, length = 10485760)
    private String messageRecordNvmId;

    @Column(name = "id_in_nvm", length = 10485760)
    private String idInNvm;

    @JsonIgnore
    @JoinTable(name = "message_record_nvmscope", joinColumns = {
        @JoinColumn(name = "message_record_nvm_id", referencedColumnName = "message_record_nvm_id")}, inverseJoinColumns = {
        @JoinColumn(name = "scope_id", referencedColumnName = "scope_id")})
    @ManyToMany
    @Fetch(FetchMode.SUBSELECT)
    private List<Scope> scopeList;

    @JsonIgnore
    @ManyToMany(mappedBy = "messageRecordNvmList")
    @Fetch(FetchMode.SUBSELECT)
    private List<Detail> detailList;

    @JoinColumn(name = "message_record_nvm_id", referencedColumnName = "general_id", nullable = false, insertable = false, updatable = false)
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private General general;

    public MessageRecordNvm() {
    }

    public MessageRecordNvm(String messageRecordNvmId) {
        this.messageRecordNvmId = messageRecordNvmId;
    }

    public String getIdInNvm() {
        return idInNvm;
    }

    public void setIdInNvm(String idInNvm) {
        this.idInNvm = idInNvm;
    }

    public String getMessageRecordNvmId() {
        return messageRecordNvmId;
    }

    public void setMessageRecordNvmId(String messageRecordNvmId) {
        this.messageRecordNvmId = messageRecordNvmId;
    }

    public List<Scope> getScopeList() {
        return scopeList;
    }

    public void setScopeList(List<Scope> scopeList) {
        this.scopeList = scopeList;
    }

    public List<Detail> getDetailList() {
        return detailList;
    }

    public void setDetailList(List<Detail> detailList) {
        this.detailList = detailList;
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
        hash += (messageRecordNvmId != null ? messageRecordNvmId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MessageRecordNvm)) {
            return false;
        }
        MessageRecordNvm other = (MessageRecordNvm) object;
        if ((this.messageRecordNvmId == null && other.messageRecordNvmId != null) || (this.messageRecordNvmId != null && !this.messageRecordNvmId.equals(other.messageRecordNvmId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "me.novascomp.flat.model.MessageRecordNvm[ messageRecordNvmId=" + messageRecordNvmId + " ]";
    }

}
