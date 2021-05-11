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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "server", catalog = "postgres", schema = "nvf", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"base_url"})})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Server.findAll", query = "SELECT s FROM Server s"),
    @NamedQuery(name = "Server.findByServerId", query = "SELECT s FROM Server s WHERE s.serverId = :serverId"),
    @NamedQuery(name = "Server.findByBaseUrl", query = "SELECT s FROM Server s WHERE s.baseUrl = :baseUrl"),
    @NamedQuery(name = "Server.findByPassword", query = "SELECT s FROM Server s WHERE s.password = :password"),
    @NamedQuery(name = "Server.findByUsername", query = "SELECT s FROM Server s WHERE s.username = :username"),
    @NamedQuery(name = "Server.findByWebdavPath", query = "SELECT s FROM Server s WHERE s.webdavPath = :webdavPath"),
    @NamedQuery(name = "Server.findByOcsPath", query = "SELECT s FROM Server s WHERE s.ocsPath = :ocsPath")})
public class Server implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "server_id", nullable = false, length = 10485760)

    private String serverId;
    @Basic(optional = false)
    @Column(name = "base_url", nullable = false, length = 10485760)
    private String baseUrl;

    @Basic(optional = false)
    @Column(name = "password", nullable = false, length = 10485760)
    private String password;

    @Basic(optional = false)
    @Column(name = "username", nullable = false, length = 10485760)
    private String username;

    @Basic(optional = false)
    @Column(name = "webdav_path", nullable = false, length = 10485760)
    private String webdavPath;

    @Basic(optional = false)
    @Column(name = "ocs_path", nullable = false, length = 10485760)
    private String ocsPath;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "serverId")
    @Fetch(FetchMode.SUBSELECT)
    private List<File> fileList;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "serverId")
    @Fetch(FetchMode.SUBSELECT)
    private List<Folder> folderList;

    @JsonIgnore
    @JoinColumn(name = "server_id", referencedColumnName = "general_id", nullable = false, insertable = false, updatable = false)
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private General general;

    public Server() {
    }

    public Server(String serverId) {
        this.serverId = serverId;
    }

    public Server(String serverId, String baseUrl, String password, String username, String webdavPath, String ocsPath) {
        this.serverId = serverId;
        this.baseUrl = baseUrl;
        this.password = password;
        this.username = username;
        this.webdavPath = webdavPath;
        this.ocsPath = ocsPath;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getWebdavPath() {
        return webdavPath;
    }

    public void setWebdavPath(String webdavPath) {
        this.webdavPath = webdavPath;
    }

    public String getOcsPath() {
        return ocsPath;
    }

    public void setOcsPath(String ocsPath) {
        this.ocsPath = ocsPath;
    }

    public General getGeneral() {
        return general;
    }

    public void setGeneral(General general) {
        this.general = general;
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

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (serverId != null ? serverId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Server)) {
            return false;
        }
        Server other = (Server) object;
        if ((this.serverId == null && other.serverId != null) || (this.serverId != null && !this.serverId.equals(other.serverId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "me.novascomp.files.model.Server[ serverId=" + serverId + " ]";
    }

}
