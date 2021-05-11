package me.novascomp.files.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name = "file_share", catalog = "postgres", schema = "nvf", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"link"})})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "FileShare.findAll", query = "SELECT f FROM FileShare f"),
    @NamedQuery(name = "FileShare.findByFileShareId", query = "SELECT f FROM FileShare f WHERE f.fileShareId = :fileShareId"),
    @NamedQuery(name = "FileShare.findByLink", query = "SELECT f FROM FileShare f WHERE f.link = :link")})
public class FileShare implements Serializable {

    private static final long serialVersionUID = 1L;
   
    @Id
    @Basic(optional = false)
    @Column(name = "file_share_id", nullable = false, length = 10485760)
    private String fileShareId;
    
    @Basic(optional = false)
    @Column(name = "link", nullable = false, length = 10485760)
    private String link;
    
    @JsonIgnore
    @JoinColumn(name = "file_id", referencedColumnName = "file_id", nullable = false)
    @ManyToOne(optional = false)
    private File fileId;
   
    @JoinColumn(name = "file_share_id", referencedColumnName = "general_id", nullable = false, insertable = false, updatable = false)
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private General general;

    public FileShare() {
    }

    public FileShare(String fileShareId) {
        this.fileShareId = fileShareId;
    }

    public FileShare(String fileShareId, String link) {
        this.fileShareId = fileShareId;
        this.link = link;
    }

    public String getFileShareId() {
        return fileShareId;
    }

    public void setFileShareId(String fileShareId) {
        this.fileShareId = fileShareId;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @XmlTransient
    public File getFileId() {
        return fileId;
    }

    public void setFileId(File fileId) {
        this.fileId = fileId;
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
        hash += (fileShareId != null ? fileShareId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof FileShare)) {
            return false;
        }
        FileShare other = (FileShare) object;
        if ((this.fileShareId == null && other.fileShareId != null) || (this.fileShareId != null && !this.fileShareId.equals(other.fileShareId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FileShare{" + "fileShareId=" + fileShareId + ", link=" + link + ", fileId=" + fileId + ", general=" + general + '}';
    }

    public String getFileName() {
        if (this.fileId != null) {
            return this.fileId.getName();
        }
        return null;
    }
}
