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
@Table(name = "file", catalog = "postgres", schema = "nvf")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "File.findAll", query = "SELECT f FROM File f"),
    @NamedQuery(name = "File.findByFileId", query = "SELECT f FROM File f WHERE f.fileId = :fileId"),
    @NamedQuery(name = "File.findByName", query = "SELECT f FROM File f WHERE f.name = :name")})
public class File implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "file_id", nullable = false, length = 10485760)
    private String fileId;

    @Basic(optional = false)
    @Column(name = "name", nullable = false, length = 10485760)
    private String name;

    @JsonIgnore
    @JoinColumn(name = "folder_id", referencedColumnName = "folder_id", nullable = false)
    @ManyToOne(optional = false)
    private Folder folderId;

    @JoinColumn(name = "file_id", referencedColumnName = "general_id", nullable = false, insertable = false, updatable = false)
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private General general;

    @JsonIgnore
    @JoinColumn(name = "server_id", referencedColumnName = "server_id", nullable = false)
    @ManyToOne(optional = false)
    private Server serverId;

    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    @ManyToOne(optional = false)
    private User userId;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fileId")
    @Fetch(FetchMode.SUBSELECT)
    private List<FileShare> fileShareList;

    public File() {
    }

    public File(String fileId) {
        this.fileId = fileId;
    }

    public File(String fileId, String name) {
        this.fileId = fileId;
        this.name = name;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
    public List<FileShare> getFileShareList() {
        return fileShareList;
    }

    public void setFileShareList(List<FileShare> fileShareList) {
        this.fileShareList = fileShareList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (fileId != null ? fileId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof File)) {
            return false;
        }
        File other = (File) object;
        if ((this.fileId == null && other.fileId != null) || (this.fileId != null && !this.fileId.equals(other.fileId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "File{" + "fileId=" + fileId + ", name=" + name + '}';
    }

}
