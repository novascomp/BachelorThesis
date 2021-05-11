/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.novascomp.messages.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
import me.novascomp.messages.config.NVFUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 *
 * @author Paul
 */
@Entity
@Table(name = "re", catalog = "BAKALARKA", schema = "nvm")
@NamedQueries({
    @NamedQuery(name = "Re.findAll", query = "SELECT r FROM Re r"),
    @NamedQuery(name = "Re.findByReId", query = "SELECT r FROM Re r WHERE r.reId = :reId"),
    @NamedQuery(name = "Re.findByHeading", query = "SELECT r FROM Re r WHERE r.heading = :heading"),
    @NamedQuery(name = "Re.findByBody", query = "SELECT r FROM Re r WHERE r.body = :body")})
public class Re implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @Column(name = "re_id", nullable = false, length = 10485760)
    private String reId;

    @JsonIgnore
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @ManyToOne
    private User userId;

    @Column(name = "heading", length = 10485760)
    private String heading;

    @Column(name = "body", length = 10485760)
    private String body;

    @Column(name = "creator_key", length = 10485760)
    private String creatorKey;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JoinColumn(name = "message_id", referencedColumnName = "message_id")
    @ManyToOne
    private Message messageId;

    @JsonIgnore
    @JoinColumn(name = "re_id", referencedColumnName = "general_id", nullable = false, insertable = false, updatable = false)
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private General general;

    public Re() {
    }

    public Re(String reId) {
        this.reId = reId;
    }

    public String getReId() {
        return reId;
    }

    public void setReId(String reId) {
        this.reId = reId;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    public General getGeneral() {
        return general;
    }

    public void setMessageId(Message messageId) {
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

    public void setGeneral(General general) {
        this.general = general;
    }

    public Message getMessageId() {
        return messageId;
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
        hash += (reId != null ? reId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Re)) {
            return false;
        }
        Re other = (Re) object;
        if ((this.reId == null && other.reId != null) || (this.reId != null && !this.reId.equals(other.reId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "me.novascmp.messages.model.Re[ reId=" + reId + " ]";
    }

    public String getMessageLink() {
        String currentUrl = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURL().toString();
        return NVFUtils.getBaseUrl(currentUrl) + "NVM/messages/" + messageId.getMessageId();
    }
}
