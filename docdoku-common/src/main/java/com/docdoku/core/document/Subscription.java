/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU Affero General Public License for more details.  
 *  
 * You should have received a copy of the GNU Affero General Public License  
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.  
 */

package com.docdoku.core.document;

import com.docdoku.core.common.User;
import java.io.Serializable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;

/**
 * Abstract class for defining subscription made by users on documents.  
 * 
 * @author Florent Garin
 * @version 1.0, 02/06/08
 * @since   V1.0
 */
@javax.persistence.IdClass(com.docdoku.core.document.SubscriptionKey.class)
@javax.persistence.MappedSuperclass
public abstract class Subscription implements Serializable{
    

    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name="SUBSCRIBER_LOGIN", referencedColumnName="LOGIN"),
        @JoinColumn(name="SUBSCRIBER_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    protected User subscriber;
    
    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name="DOCUMENTMASTER_ID", referencedColumnName="ID"),
        @JoinColumn(name="DOCUMENTMASTER_VERSION", referencedColumnName="VERSION"),
        @JoinColumn(name="DOCUMENTMASTER_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    protected DocumentMaster observedDocumentMaster;
    
    @javax.persistence.Column(name = "SUBSCRIBER_WORKSPACE_ID", length=50, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String subscriberWorkspaceId="";
    
    @javax.persistence.Column(name = "SUBSCRIBER_LOGIN", nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String subscriberLogin="";
    
    
    @javax.persistence.Column(name = "DOCUMENTMASTER_WORKSPACE_ID", length=50, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String observedDocumentMasterWorkspaceId="";
    
    @javax.persistence.Column(name = "DOCUMENTMASTER_VERSION", length=10, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String observedDocumentMasterVersion="";
    
    @javax.persistence.Column(name = "DOCUMENTMASTER_ID", length=50, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String observedDocumentMasterId="";
    
    
    public Subscription() {
    }
    
    public Subscription (User pSubscriber, DocumentMaster pObservedElement){
        setSubscriber(pSubscriber);
        setObservedDocumentMaster(pObservedElement);
    }

    public SubscriptionKey getKey(){
    return new SubscriptionKey(subscriberWorkspaceId, subscriberLogin, observedDocumentMasterWorkspaceId, observedDocumentMasterId, observedDocumentMasterVersion);
    }
    
    public DocumentMaster getObservedDocumentMaster() {
        return observedDocumentMaster;
    }

    public User getSubscriber() {
        return subscriber;
    }

    public String getSubscriberLogin() {
        return subscriberLogin;
    }


    public String getSubscriberWorkspaceId() {
        return subscriberWorkspaceId;
    }

    public String getObservedDocumentMasterId() {
        return observedDocumentMasterId;
    }

    public String getObservedDocumentMasterVersion() {
        return observedDocumentMasterVersion;
    }

    public String getObservedDocumentMasterWorkspaceId() {
        return observedDocumentMasterWorkspaceId;
    }


    public void setObservedDocumentMaster(DocumentMaster pObservedDocumentMaster) {
        observedDocumentMaster = pObservedDocumentMaster;
        observedDocumentMasterId=observedDocumentMaster.getId();
        observedDocumentMasterVersion=observedDocumentMaster.getVersion();
        observedDocumentMasterWorkspaceId=observedDocumentMaster.getWorkspaceId();
        
    }

    public void setSubscriber(User pSubscriber) {
        subscriber = pSubscriber;
        subscriberLogin=subscriber.getLogin();
        subscriberWorkspaceId=subscriber.getWorkspaceId();
    }
    

}
