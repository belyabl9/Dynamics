package domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "Events")
public class Event extends DomainSuperClass implements Serializable {

	public static int ACTION_DOWN = 0;
	public static int ACTION_UP = 1;
	
	public static int SESSION_START = 10000;
	public static int SESSION_END = 10001;
	
	public static int BACKSPACE_CODE = -5;
	
	@Column(name = "code")
	private int code;
    
    @Column(name = "action")
    private int action;
    
    @Column(name = "device_id")
    private String deviceId;
    
    @Column(name = "time")
    private long time;
    
    @Column(name = "orientation")
    private short orientation;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Event() {}

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public short getOrientation() {
        return orientation;
    }

    public void setOrientation(short orientation) {
        this.orientation = orientation;
    }

}