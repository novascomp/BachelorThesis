package me.novascomp.messages.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import javax.persistence.Version;

@Entity
@Table(name = "category", catalog = "BAKALARKA", schema = "nvm")
@NamedQueries({
    @NamedQuery(name = "Category.findAll", query = "SELECT c FROM Category c"),
    @NamedQuery(name = "Category.findByText", query = "SELECT c FROM Category c WHERE c.text = :text"),
    @NamedQuery(name = "Category.findByCategoryId", query = "SELECT c FROM Category c WHERE c.categoryId = :categoryId")})
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    @Version
    private Integer version;

    @Id
    @Basic(optional = false)
    @Column(name = "category_id", nullable = false, length = 10485760)
    private String categoryId;

    @Column(name = "text", length = 10485760)
    private String text;

    @Column(name = "creator_key", length = 10485760)
    private String creatorKey;

    @JsonIgnore
    @ManyToMany(mappedBy = "categoryList")
    private List<Message> messageList;

    @JsonIgnore
    @JoinColumn(name = "category_id", referencedColumnName = "general_id", nullable = false, insertable = false, updatable = false)
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private General general;

    public Category() {
    }

    public Category(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public List<Message> getMessageList() {
        return messageList;
    }

    public void setMessageList(List<Message> messageList) {
        this.messageList = messageList;
    }

    public General getGeneral() {
        return general;
    }

    public void setGeneral(General general) {
        this.general = general;
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
        hash += (categoryId != null ? categoryId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Category)) {
            return false;
        }
        Category other = (Category) object;
        if ((this.categoryId == null && other.categoryId != null) || (this.categoryId != null && !this.categoryId.equals(other.categoryId))) {
            return false;
        }
        return true;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "me.novascmp.messages.model.Category[ categoryId=" + categoryId + " ]";
    }

}
