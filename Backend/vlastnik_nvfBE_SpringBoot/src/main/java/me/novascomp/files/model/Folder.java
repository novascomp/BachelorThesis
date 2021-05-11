package me.novascomp.files.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "folder", catalog = "postgres", schema = "nvf")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Folder.findAll", query = "SELECT f FROM Folder f"),
    @NamedQuery(name = "Folder.findByFolderId", query = "SELECT f FROM Folder f WHERE f.folderId = :folderId"),
    @NamedQuery(name = "Folder.findByName", query = "SELECT f FROM Folder f WHERE f.name = :name")})
public class Folder implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @Basic(optional = false)
    @Column(name = "folder_id", nullable = false, length = 10485760)
    private String folderId;
    
    @Basic(optional = false)
    @Column(name = "name", nullable = false, length = 10485760)
    private String name;

    @JoinColumn(name = "previous_folder_id", referencedColumnName = "folder_id")
    @ManyToOne
    private Folder previousFolderId;

    @JsonIgnore
    @JoinColumn(name = "server_id", referencedColumnName = "server_id", nullable = false)
    @ManyToOne(optional = false)
    private Server serverId;

    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    @ManyToOne(optional = false)
    private User userId;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "folderId")
    @Fetch(FetchMode.SUBSELECT)
    private List<File> fileList;

    @JsonIgnore
    @OneToMany(mappedBy = "previousFolderId")
    @Fetch(FetchMode.SUBSELECT)
    private List<Folder> folderList;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "folderId")
    @Fetch(FetchMode.SUBSELECT)
    private List<FolderShare> folderShareList;

    @JoinColumn(name = "folder_id", referencedColumnName = "general_id", nullable = false, insertable = false, updatable = false)
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private General general;

    public Folder() {
    }

    public Folder(String folderId) {
        this.folderId = folderId;
    }

    public Folder(String folderId, String name) {
        this.folderId = folderId;
        this.name = name;
    }

    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlTransient
    public List<File> getFileList() {
        return fileList;
    }

    public void setFileList(List<File> fileList) {
        this.fileList = fileList;
    }

    @XmlTransient
    public List<Folder> getFolderList() {
        return folderList;
    }

    public void setFolderList(List<Folder> folderList) {
        this.folderList = folderList;
    }

    public Folder getPreviousFolderId() {
        return previousFolderId;
    }

    public void setPreviousFolderId(Folder previousFolderId) {
        this.previousFolderId = previousFolderId;
    }

    @XmlTransient
    public General getGeneral() {
        return general;
    }

    public void setGeneral(General general) {
        this.general = general;
    }

    public Server getServerId() {
        return serverId;
    }

    public void setServerId(Server serverId) {
        this.serverId = serverId;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    @XmlTransient
    public List<FolderShare> getFolderShareList() {
        return folderShareList;
    }

    public void setFolderShareList(List<FolderShare> folderShareList) {
        this.folderShareList = folderShareList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (folderId != null ? folderId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Folder)) {
            return false;
        }
        Folder other = (Folder) object;
        if ((this.folderId == null && other.folderId != null) || (this.folderId != null && !this.folderId.equals(other.folderId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "me.novascomp.files.model.Folder[ folderId=" + folderId + " ]";
    }

}
