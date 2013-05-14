package models;

import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import play.db.ebean.Model;

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
    public User getCreatedBy()
    {
	return createdBy;
    }
    public void setCreatedBy(User createdBy)
    {
	this.createdBy = createdBy;
    }
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
	    createdAt = utils.DateTimeHelper.getCurrentTime();
	}
	if (createdBy == null) {
	    createdBy = User.getCurrentUser();
	}
    }
    @PreUpdate
    public void doPreUpdate()
    {
	updatedAt = utils.DateTimeHelper.getCurrentTime();
	updatedBy = User.getCurrentUser();
    }
}
