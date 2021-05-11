/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 *
 * @author Paul
 */
@Entity
@Table(name = "file_nvf", catalog = "BAKALARKA", schema = "nvm")
@NamedQueries({
    @NamedQuery(name = "FileNvf.findAll", query = "SELECT f FROM FileNvf f"),
    @NamedQuery(name = "FileNvf.findByFileIdInNvf", query = "SELECT f FROM FileNvf f WHERE f.fileIdInNvf = :fileIdInNvf"),
    @NamedQuery(name = "FileNvf.findByFileNvfId", query = "SELECT f FROM FileNvf f WHERE f.fileNvfId = :fileNvfId")})
public class FileNvf implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "file_id_in_nvf", length = 10485760)
    private String fileIdInNvf;

    @Id
    @Basic(optional = false)
    @Column(name = "file_nvf_id", nullable = false, length = 10485760)
    private String fileNvfId;

    @Column(name = "creator_key", length = 10485760)
    private String creatorKey;

    @JsonIgnore
    @ManyToMany(mappedBy = "fileNvfList")
    private List<Message> messageList;

    @JsonIgnore
    @JoinColumn(name = "file_nvf_id", referencedColumnName = "general_id", nullable = false, insertable = false, updatable = false)
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private General general;

    public FileNvf() {
    }

    public FileNvf(String fileNvfId) {
        this.fileNvfId = fileNvfId;
    }

    public String getFileIdInNvf() {
        return fileIdInNvf;
    }

    public void setFileIdInNvf(String fileIdInNvf) {
        this.fileIdInNvf = fileIdInNvf;
    }

    public String getFileNvfId() {
        return fileNvfId;
    }

    public void setFileNvfId(String fileNvfId) {
        this.fileNvfId = fileNvfId;
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
        hash += (fileNvfId != null ? fileNvfId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof FileNvf)) {
            return false;
        }
        FileNvf other = (FileNvf) object;
        if ((this.fileNvfId == null && other.fileNvfId != null) || (this.fileNvfId != null && !this.fileNvfId.equals(other.fileNvfId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "me.novascmp.messages.model.FileNvf[ fileNvfId=" + fileNvfId + " ]";
    }

}
