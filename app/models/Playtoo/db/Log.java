package models.Playtoo.db;

import java.util.Date;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Embeddable
public class Log {

	@ManyToOne
	public UserID createdBy;

	@ManyToOne
	public UserID updatedBy;

	@Temporal(TemporalType.TIMESTAMP)
	public Date createdAt;

	@Temporal(TemporalType.TIMESTAMP)
	public Date updatedAt;

	public Log() {
		this(null);
	}

	public Log(UserID createdBy) {
		this(createdBy, new Date());
	}

	public Log(UserID createdBy, Date createdAt) {
		if (createdAt == null)
			createdAt = new Date();
		this.createdBy = createdBy;
		this.createdAt = createdAt;
		this.updatedBy = createdBy;
		this.updatedAt = createdAt;
	}

	public void updatedBy(UserID updatedBy) {
		this.updatedBy = updatedBy;
		this.updatedAt = new Date();
	}

}
