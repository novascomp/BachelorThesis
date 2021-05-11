package me.novascomp.messages.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import me.novascomp.messages.config.NVFUtils;

@Entity
@Table(name = "message", catalog = "BAKALARKA", schema = "nvm")
@NamedQueries({
    @NamedQuery(name = "Message.findAll", query = "SELECT m FROM Message m"),
    @NamedQuery(name = "Message.findByMessageId", query = "SELECT m FROM Message m WHERE m.messageId = :messageId"),
    @NamedQuery(name = "Message.findByHeading", query = "SELECT m FROM Message m WHERE m.heading = :heading"),
    @NamedQuery(name = "Message.findByBody", query = "SELECT m FROM Message m WHERE m.body = :body")})
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    @Version
    private Integer version;

    @Id
    @Basic(optional = false)
    @Column(name = "message_id", nullable = false, length = 10485760)
    private String messageId;

    @Column(name = "heading", length = 10485760)
    private String heading;

    @Column(name = "body", length = 10485760)
    private String body;

    @Column(name = "creator_key", length = 10485760)
    private String creatorKey;

    @JsonProperty(access = Access.WRITE_ONLY)
    @JoinTable(name = "messagefile_nvf", joinColumns = {
        @JoinColumn(name = "message_id", referencedColumnName = "message_id")}, inverseJoinColumns = {
        @JoinColumn(name = "file_nvf_id", referencedColumnName = "file_nvf_id")})
    @ManyToMany(fetch = FetchType.LAZY)
    private List<FileNvf> fileNvfList;

    @JsonProperty(access = Access.WRITE_ONLY)
    @JoinTable(name = "messagepriority", joinColumns = {
        @JoinColumn(name = "message_id", referencedColumnName = "message_id")}, inverseJoinColumns = {
        @JoinColumn(name = "priority_id", referencedColumnName = "priority_id")})
    @ManyToMany(fetch = FetchType.LAZY)
    private List<Priority> priorityList;

    @JsonProperty(access = Access.WRITE_ONLY)
    @JoinTable(name = "categorymessage", joinColumns = {
        @JoinColumn(name = "message_id", referencedColumnName = "message_id")}, inverseJoinColumns = {
        @JoinColumn(name = "category_id", referencedColumnName = "category_id")})
    @ManyToMany
    private List<Category> categoryList;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "messageId")
    private List<Re> reList;

    @JsonIgnore
    @JoinColumn(name = "message_id", referencedColumnName = "general_id", nullable = false, insertable = false, updatable = false)
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private General general;

    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @ManyToOne
    private User userId;

    public Message() {
    }

    public Message(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<FileNvf> getFileNvfList() {
        return fileNvfList;
    }

    public void setFileNvfList(List<FileNvf> fileNvfList) {
        this.fileNvfList = fileNvfList;
    }

    public List<Priority> getPriorityList() {
        return priorityList;
    }

    public void setPriorityList(List<Priority> priorityList) {
        this.priorityList = priorityList;
    }

    public List<Category> getCategoryList() {
        return categoryList;
    }

    public void setCategoryList(List<Category> categoryList) {
        this.categoryList = categoryList;
    }

    public List<Re> getReList() {
        return reList;
    }

    public void setReList(List<Re> reList) {
        this.reList = reList;
    }

    public General getGeneral() {
        return general;
    }

    public void setGeneral(General general) {
        this.general = general;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    public String getCreatorKey() {
        return creatorKey;
    }

    public void setCreatorKey(String creatorKey) {
        this.creatorKey = creatorKey;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (messageId != null ? messageId.hashCode() : 0);
        return hash;
    }

    public Integer getVersion() {
        return version;//
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Message)) {
            return false;
        }
        Message other = (Message) object;
        if ((this.messageId == null && other.messageId != null) || (this.messageId != null && !this.messageId.equals(other.messageId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "me.novascmp.messages.model.Message[ messageId=" + messageId + " ]";
    }

    public String getFilesLink() {
        String currentUrl = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURL().toString();
        return NVFUtils.getBaseUrl(currentUrl) + "NVM/messages/" + messageId + "/" + "files";
    }

    public String getREsLink() {
        String currentUrl = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURL().toString();
        return NVFUtils.getBaseUrl(currentUrl) + "NVM/messages/" + messageId + "/" + "REs";
    }

    public String getPrioritiesLink() {
        String currentUrl = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURL().toString();
        return NVFUtils.getBaseUrl(currentUrl) + "NVM/messages/" + messageId + "/" + "priorities";
    }

    public String getCategoriesLink() {
        String currentUrl = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURL().toString();
        return NVFUtils.getBaseUrl(currentUrl) + "NVM/messages/" + messageId + "/" + "categories";
    }

}
