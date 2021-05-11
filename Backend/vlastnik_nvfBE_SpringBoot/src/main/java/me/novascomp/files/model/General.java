package me.novascomp.files.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name = "general", catalog = "postgres", schema = "nvf")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "General.findAll", query = "SELECT g FROM General g"),
    @NamedQuery(name = "General.findByGeneralId", query = "SELECT g FROM General g WHERE g.generalId = :generalId"),
    @NamedQuery(name = "General.findByTime", query = "SELECT g FROM General g WHERE g.time = :time"),
    @NamedQuery(name = "General.findByDate", query = "SELECT g FROM General g WHERE g.date = :date"),
    @NamedQuery(name = "General.findBySwBuild", query = "SELECT g FROM General g WHERE g.swBuild = :swBuild")})
public class General implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "general_id", nullable = false, length = 10485760)
    private String generalId;
    @Basic(optional = false)
    @Column(name = "time", nullable = false)
    @Temporal(TemporalType.TIME)
    private Date time;
    @Basic(optional = false)
    @Column(name = "date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date date;
    @Basic(optional = false)
    @Column(name = "sw_build", nullable = false, length = 10485760)
    private String swBuild;
    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "general")
    private Server server;
    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "general")
    private File file;
    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "general")
    private Folder folder;
    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "general")
    private FileShare fileShare;
    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "general")
    private FolderShare folderShare;
    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "general")
    private User user;

    public General() {
    }

    public General(String generalId) {
        this.generalId = generalId;
    }

    public General(String generalId, Date time, Date date, String swBuild) {
        this.generalId = generalId;
        this.time = time;
        this.date = date;
        this.swBuild = swBuild;
    }

    public String getGeneralId() {
        return generalId;
    }

    public void setGeneralId(String generalId) {
        this.generalId = generalId;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getSwBuild() {
        return swBuild;
    }

    public void setSwBuild(String swBuild) {
        this.swBuild = swBuild;
    }

    @XmlTransient
    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    @XmlTransient
    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @XmlTransient
    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    @XmlTransient
    public FileShare getFileShare() {
        return fileShare;
    }

    public void setFileShare(FileShare fileShare) {
        this.fileShare = fileShare;
    }

    @XmlTransient
    public FolderShare getFolderShare() {
        return folderShare;
    }

    public void setFolderShare(FolderShare folderShare) {
        this.folderShare = folderShare;
    }

    @XmlTransient
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (generalId != null ? generalId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof General)) {
            return false;
        }
        General other = (General) object;
        if ((this.generalId == null && other.generalId != null) || (this.generalId != null && !this.generalId.equals(other.generalId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "me.novascomp.files.model.General[ generalId=" + generalId + " ]";
    }

}
