package me.novascomp.files.model;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "folder_share", catalog = "postgres", schema = "nvf", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"link"})})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "FolderShare.findAll", query = "SELECT f FROM FolderShare f"),
    @NamedQuery(name = "FolderShare.findByFolderShareId", query = "SELECT f FROM FolderShare f WHERE f.folderShareId = :folderShareId"),
    @NamedQuery(name = "FolderShare.findByLink", query = "SELECT f FROM FolderShare f WHERE f.link = :link")})
public class FolderShare implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @Column(name = "folder_share_id", nullable = false, length = 10485760)
    private String folderShareId;

    @Basic(optional = false)
    @Column(name = "link", nullable = false, length = 10485760)
    private String link;

    @JoinColumn(name = "folder_id", referencedColumnName = "folder_id", nullable = false)
    @ManyToOne(optional = false)
    private Folder folderId;

    @JoinColumn(name = "folder_share_id", referencedColumnName = "general_id", nullable = false, insertable = false, updatable = false)
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private General general;

    public FolderShare() {
    }

    public FolderShare(String folderShareId) {
        this.folderShareId = folderShareId;
    }

    public FolderShare(String folderShareId, String link) {
        this.folderShareId = folderShareId;
        this.link = link;
    }

    public String getFolderShareId() {
        return folderShareId;
    }

    public void setFolderShareId(String folderShareId) {
        this.folderShareId = folderShareId;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Folder getFolderId() {
        return folderId;
    }

    public void setFolderId(Folder folderId) {
        this.folderId = folderId;
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
        hash += (folderShareId != null ? folderShareId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof FolderShare)) {
            return false;
        }
        FolderShare other = (FolderShare) object;
        if ((this.folderShareId == null && other.folderShareId != null) || (this.folderShareId != null && !this.folderShareId.equals(other.folderShareId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "me.novascomp.files.model.FolderShare[ folderShareId=" + folderShareId + " ]";
    }

}
