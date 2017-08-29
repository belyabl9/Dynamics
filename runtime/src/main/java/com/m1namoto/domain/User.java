package com.m1namoto.domain;

import com.google.gson.annotations.Expose;
import com.m1namoto.service.FeatureService;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "Users")
public class User extends DomainSuperClass implements Serializable {
    private static final long serialVersionUID = 1L;

    final static Logger logger = Logger.getLogger(User.class);

    public enum Type {
        ADMIN(0),
        REGULAR(1),
        ;

        private final int value;

        Type(int value) {
            this.value = value;
        }

        public static Type fromInt(int value) {
            for (Type type : values()) {
                if (type.getValue() == value) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unsupported user type.");
        }

        public int getValue() {
            return value;
        }
    }

    @Expose
	@Column(name = "name")
	private String name;

    @Expose
	@Column(name = "login", unique = true)
    private String login;
    
    @Column(name = "password")
    private String password;

    @Column(name = "userType", nullable=false)
    @Enumerated(EnumType.ORDINAL)
    private Type userType;

    @Column(name = "authenticatedCnt", columnDefinition = "int default 0", nullable=false)
    private int authenticatedCnt;

    public User() {}

    public User(@NotNull User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.login = user.getLogin();
        this.password = user.getPassword();
        this.userType = user.getUserType();
        this.authenticatedCnt = user.getAuthenticatedCnt();
    }

    public int getAuthenticatedCnt() {
        return authenticatedCnt;
    }

    public void setAuthenticatedCnt(int authenticatedCnt) {
        this.authenticatedCnt = authenticatedCnt;
    }
    
    public String getFirstName() {
    	return name.split(" +")[0];
    }
    
    public String getSurname() {
    	return name.split(" +")[1];
    }
    
    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

    public Type getUserType() {
        return userType;
    }

    public void setUserType(Type userType) {
        this.userType = userType;
    }

    public List<HoldFeature> getHoldFeatures() {
        return FeatureService.getInstance().getHoldFeatures(this);
    }

    public List<ReleasePressFeature> getReleasePressFeatures() {
        return FeatureService.getInstance().getReleasePressFeatures(this);
    }

    public Double getMeanKeypressTime() {
        return FeatureService.getInstance().getMeanKeypressTime(this).orNull();
    }

    public Double getMeanTimeBetweenKeys() {
        return FeatureService.getInstance().getMeanReleasePressTime(this).orNull();
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", userType=" + userType +
                ", authenticatedCnt=" + authenticatedCnt +
                '}';
    }
}