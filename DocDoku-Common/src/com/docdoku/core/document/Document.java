/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.  
 */

package com.docdoku.core.document;

import com.docdoku.core.common.User;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.FileHolder;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlTransient;

/**
 * This <code>Document</code> class represents the iterated part of a document.
 * The iteration attribute indicates the order in which the modifications
 * have been made on the document.
 * 
 * @author Florent GARIN
 * @version 1.0, 02/06/08
 * @since   V1.0
 */
@javax.persistence.IdClass(com.docdoku.core.document.DocumentKey.class)
@javax.persistence.Entity
public class Document implements Serializable, FileHolder, Comparable<Document>, Cloneable {
    
    
    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name="MASTERDOCUMENT_ID", referencedColumnName="ID"),
        @JoinColumn(name="MASTERDOCUMENT_VERSION", referencedColumnName="VERSION"),
        @JoinColumn(name="WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    private MasterDocument masterDocument;
    
    @javax.persistence.Id
    private int iteration;
    
    @javax.persistence.Column(name = "MASTERDOCUMENT_ID", length=50, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String masterDocumentId="";
    
    @javax.persistence.Column(name = "MASTERDOCUMENT_VERSION", length=10, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String masterDocumentVersion="";
    
    @javax.persistence.Column(name = "WORKSPACE_ID", length=50, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String workspaceId="";
    
    
    @OneToMany(cascade={CascadeType.REMOVE,CascadeType.REFRESH}, fetch=FetchType.EAGER)
    @JoinTable(
    inverseJoinColumns={
        @JoinColumn(name="ATTACHEDFILES_FULLNAME", referencedColumnName="FULLNAME")
    },
    joinColumns={
        @JoinColumn(name="DOCUMENT_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID"),
        @JoinColumn(name="DOCUMENT_MASTERDOCUMENT_ID", referencedColumnName="MASTERDOCUMENT_ID"),
        @JoinColumn(name="DOCUMENT_MASTERDOCUMENT_VERSION", referencedColumnName="MASTERDOCUMENT_VERSION"),
        @JoinColumn(name="DOCUMENT_ITERATION", referencedColumnName="ITERATION")
    })
    private Set<BinaryResource> attachedFiles = new HashSet<BinaryResource>();
    
    private String revisionNote;
    
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name="AUTHOR_LOGIN", referencedColumnName="LOGIN"),
        @JoinColumn(name="AUTHOR_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    private User author;
    
    @javax.persistence.Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date creationDate;
    
    @OneToMany(mappedBy = "fromDocument", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    private Set<DocumentToDocumentLink> linkedDocuments=new HashSet<DocumentToDocumentLink>();
    
    @OneToMany(mappedBy = "document", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    @MapKey(name="name")
    private Map<String, InstanceAttribute> instanceAttributes=new HashMap<String, InstanceAttribute>();

    public Document() {
    }
    
    
    public Document(MasterDocument pMasterDocument, int pIteration, User pAuthor) {
        setMasterDocument(pMasterDocument);
        iteration = pIteration;
        author = pAuthor;
    }
    
    public void setMasterDocument(MasterDocument pMasterDocument) {
        masterDocument = pMasterDocument;
        masterDocumentId=pMasterDocument.getId();
        masterDocumentVersion=pMasterDocument.getVersion();
        workspaceId=pMasterDocument.getWorkspaceId();
    }
    
    public int getIteration() {
        return iteration;
    }
    
    public void setRevisionNote(String pRevisionNote) {
        revisionNote = pRevisionNote;
    }
    
    public String getRevisionNote() {
        return revisionNote;
    }

    public void setAttachedFiles(Set<BinaryResource> attachedFiles) {
        this.attachedFiles = attachedFiles;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    
    
    public boolean removeFile(BinaryResource pBinaryResource){
        return attachedFiles.remove(pBinaryResource);
    }
    
    public void addFile(BinaryResource pBinaryResource){
        attachedFiles.add(pBinaryResource);
    }
    
    @Override
    public Set<BinaryResource> getAttachedFiles() {
        return attachedFiles;
    }
    
    public DocumentKey getKey() {
        return new DocumentKey(workspaceId, masterDocumentId, masterDocumentVersion, iteration);
    }
    
    public String getMasterDocumentId() {
        return masterDocumentId;
    }
    
    public String getMasterDocumentVersion() {
        return masterDocumentVersion;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }
    
    public void setAuthor(User pAuthor) {
        author = pAuthor;
    }
    
    public User getAuthor() {
        return author;
    }
    
    @XmlTransient
    public MasterDocument getMasterDocument() {
        return masterDocument;
    }
    
    public void setCreationDate(Date pCreationDate) {
        creationDate = pCreationDate;
    }
    
    public Date getCreationDate() {
        return creationDate;
    }
     
    public Set<DocumentToDocumentLink> getLinkedDocuments() {
        return linkedDocuments;
    }
    
    public void setLinkedDocuments(Set<DocumentToDocumentLink> pLinkedDocuments) {
        linkedDocuments.retainAll(pLinkedDocuments);
        pLinkedDocuments.removeAll(linkedDocuments);
        linkedDocuments.addAll(pLinkedDocuments);
    }
    
    public Map<String, InstanceAttribute> getInstanceAttributes() {
        return instanceAttributes;
    }


    

    public void setInstanceAttributes(Map<String, InstanceAttribute> pInstanceAttributes) {
        for (InstanceAttribute attr : pInstanceAttributes.values()) {
            InstanceAttribute attrToUpdate = instanceAttributes.get(attr.getName());
            if (attrToUpdate != null) {
                attrToUpdate.setValue(attr.getValue());
            }
        }

        instanceAttributes.values().retainAll(pInstanceAttributes.values());
        pInstanceAttributes.values().removeAll(instanceAttributes.values());
        instanceAttributes.putAll(pInstanceAttributes);
    }
    
    public void setInstanceAttributes(Collection<InstanceAttribute> pInstanceAttributes) {
        for (InstanceAttribute attr : pInstanceAttributes) {
            InstanceAttribute attrToUpdate = instanceAttributes.get(attr.getName());
            if (attrToUpdate != null) {
                attrToUpdate.setValue(attr.getValue());
            }
        }

        instanceAttributes.values().retainAll(pInstanceAttributes);
        pInstanceAttributes.removeAll(instanceAttributes.values());
        
        for(InstanceAttribute attr:pInstanceAttributes)
            instanceAttributes.put(attr.getName(),attr);
    }
    
    @Override
    public String toString() {
        return masterDocumentId + "-" + masterDocumentVersion + "-" + iteration;
    }
    
    @Override
    public int hashCode() {
        int hash = 1;
	hash = 31 * hash + workspaceId.hashCode();
	hash = 31 * hash + masterDocumentId.hashCode();
        hash = 31 * hash + masterDocumentVersion.hashCode();
        hash = 31 * hash + iteration;
	return hash;
    }
    
    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof Document))
            return false;
        Document document = (Document) pObj;
        return ((document.masterDocumentId.equals(masterDocumentId)) && (document.workspaceId.equals(workspaceId))  && (document.masterDocumentVersion.equals(masterDocumentVersion)) && (document.iteration==iteration));
    }
    
    public int compareTo(Document pDoc) {
        
        int wksComp = workspaceId.compareTo(pDoc.workspaceId);
        if (wksComp != 0)
            return wksComp;
        int mdocIdComp = masterDocumentId.compareTo(pDoc.masterDocumentId);
        if (mdocIdComp != 0)
            return mdocIdComp;
        int mdocVersionComp = masterDocumentVersion.compareTo(pDoc.masterDocumentVersion);
        if (mdocVersionComp != 0)
            return mdocVersionComp;
        else
            return iteration-pDoc.iteration;
    }
    
    /**
     * perform a deep clone operation
     */
    @Override
    public Document clone() {
        Document clone = null;
        try {
            clone = (Document) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        //perform a deep copy
        clone.attachedFiles = new HashSet<BinaryResource>(attachedFiles);
        
        Set<DocumentToDocumentLink> clonedLinks=new HashSet<DocumentToDocumentLink>();
        for (DocumentToDocumentLink link : linkedDocuments) {
            DocumentToDocumentLink clonedLink=link.clone();
            clonedLink.setFromDocument(clone);
            clonedLinks.add(clonedLink);
        }
        clone.linkedDocuments=clonedLinks;
        
        //perform a deep copy
        Map<String, InstanceAttribute> clonedInstanceAttributes = new HashMap<String, InstanceAttribute>();
        for (InstanceAttribute attribute : instanceAttributes.values()) {
            InstanceAttribute clonedAttribute=attribute.clone();
            clonedAttribute.setDocument(clone);
            clonedInstanceAttributes.put(clonedAttribute.getName(),clonedAttribute);
        }
        clone.instanceAttributes = clonedInstanceAttributes;
        
        if(creationDate!=null)
            clone.creationDate = (Date) creationDate.clone();
        return clone;
    }
}