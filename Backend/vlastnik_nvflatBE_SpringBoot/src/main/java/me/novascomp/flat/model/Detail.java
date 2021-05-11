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
import javax.persistence.Version;
import me.novascomp.utils.rest.RestUtils;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Entity
@Table(name = "detail", catalog = "BAKALARKA", schema = "nvflat")
@NamedQueries({
    @NamedQuery(name = "Detail.findAll", query = "SELECT d FROM Detail d"),
    @NamedQuery(name = "Detail.findByDetailId", query = "SELECT d FROM Detail d WHERE d.detailId = :detailId"),
    @NamedQuery(name = "Detail.findBySize", query = "SELECT d FROM Detail d WHERE d.size = :size"),
    @NamedQuery(name = "Detail.findByCommonShareSize", query = "SELECT d FROM Detail d WHERE d.commonShareSize = :commonShareSize")})
public class Detail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Version
    private Integer version;

    @Id
    @Basic(optional = false)
    @Column(name = "detail_id", nullable = false, length = 10485760)
    private String detailId;

    @Column(name = "size", length = 10485760)
    private String size;

    @Column(name = "common_share_size", length = 10485760)
    private String commonShareSize;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JoinColumn(name = "flat_id", referencedColumnName = "flat_id", nullable = false)
    @ManyToOne(optional = false)
    private Flat flat;

    @JsonIgnore
    @JoinTable(name = "detailmessage_record_nvm", joinColumns = {
        @JoinColumn(name = "detail_id", referencedColumnName = "detail_id")}, inverseJoinColumns = {
        @JoinColumn(name = "message_record_nvm_id", referencedColumnName = "message_record_nvm_id")})
    @ManyToMany
    @Fetch(FetchMode.SUBSELECT)
    private List<MessageRecordNvm> messageRecordNvmList;

    @JsonIgnore
    @JoinTable(name = "residentdetail", joinColumns = {
        @JoinColumn(name = "detail_id", referencedColumnName = "detail_id")}, inverseJoinColumns = {
        @JoinColumn(name = "resident_id", referencedColumnName = "resident_id")})
    @ManyToMany
    @Fetch(FetchMode.SUBSELECT)
    private List<Resident> residentList;

    @JsonIgnore
    @JoinColumn(name = "detail_id", referencedColumnName = "general_id", nullable = false, insertable = false, updatable = false)
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private General general;

    public Detail() {
    }

    public Detail(String detailId) {
        this.detailId = detailId;
    }

    public String getDetailId() {
        return detailId;
    }

    public void setDetailId(String detailId) {
        this.detailId = detailId;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getCommonShareSize() {
        return commonShareSize;
    }

    public void setCommonShareSize(String commonShareSize) {
        this.commonShareSize = commonShareSize;
    }

    public List<MessageRecordNvm> getMessageRecordNvmList() {
        return messageRecordNvmList;
    }

    public void setMessageRecordNvmList(List<MessageRecordNvm> messageRecordNvmList) {
        this.messageRecordNvmList = messageRecordNvmList;
    }

    public List<Resident> getResidentList() {
        return residentList;
    }

    public void setResidentList(List<Resident> residentList) {
        this.residentList = residentList;
    }

    public Flat getFlat() {
        return flat;
    }

    public void setFlat(Flat fat) {
        this.flat = fat;
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
        hash += (detailId != null ? detailId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Detail)) {
            return false;
        }
        Detail other = (Detail) object;
        if ((this.detailId == null && other.detailId != null) || (this.detailId != null && !this.detailId.equals(other.detailId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "me.novascomp.flat.model.Detail[ detailId=" + detailId + " ]";
    }

    public String getFlatLink() {
        String currentUrl = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURL().toString();
        return RestUtils.getBaseUrl(currentUrl) + RestUtils.getRoot() + "details/" + detailId + "/" + "flat";
    }

    public String getMessagesLink() {
        String currentUrl = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURL().toString();
        return RestUtils.getBaseUrl(currentUrl) + RestUtils.getRoot() + "details/" + detailId + "/" + "messages";
    }

    public String getResidentsLink() {
        String currentUrl = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURL().toString();
        return RestUtils.getBaseUrl(currentUrl) + RestUtils.getRoot() + "details/" + detailId + "/" + "residents";
    }

}
