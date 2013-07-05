/*******************************************************************************
 * Copyright (c) 2013 by Beligum b.v.b.a. (http://www.beligum.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * Contributors:
 *     Beligum - initial implementation
 *******************************************************************************/
package com.beligum.core.models;

import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;


import org.codehaus.jackson.annotate.JsonIgnore;

import play.db.ebean.Model;

import com.beligum.core.utils.DateTimeHelper;

@MappedSuperclass
public abstract class BasicModel extends Model
{
    // -----VARIABLES-----
    @Column(name = "created_at")
    protected Calendar createdAt;
    @Column(name = "updated_at")
    protected Calendar updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    protected User createdBy;

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    protected User updatedBy;

    // -----PUBLIC GETTERS/SETTERS-----
    public Calendar getCreatedAt()
    {
	return createdAt;
    }
    public void setCreatedAt(Calendar created_at)
    {
	this.createdAt = created_at;
    }
    public Calendar getUpdatedAt()
    {
	return updatedAt;
    }
    public void setUpdatedAt(Calendar updatedAt)
    {
	this.updatedAt = updatedAt;
    }
    
    @JsonIgnore
    public User getCreatedBy()
    {
	return createdBy;
    }
    public void setCreatedBy(User createdBy)
    {
	this.createdBy = createdBy;
    }
    
    @JsonIgnore
    public User getUpdatedBy()
    {
	return updatedBy;
    }
    public void setUpdatedBy(User updatedBy)
    {
	this.updatedBy = updatedBy;
    }

    // -----MANAGEMENT FUNCTIONS-----
    @PrePersist
    public void doPrePersist()
    {
	if (createdAt == null) {
	    createdAt = DateTimeHelper.getCurrentTime();
	}
	if (createdBy == null) {
	    createdBy = User.getCurrentUser();
	}
    }
    @PreUpdate
    public void doPreUpdate()
    {
	updatedAt = DateTimeHelper.getCurrentTime();
	updatedBy = User.getCurrentUser();
    }
}
