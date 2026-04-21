package com.src.main.auth.util;

import java.util.Date;
import java.util.List;

public class JwtClaims {
	private String sub;
	private String typ;
	private String rid;
	private List<String> roles;
	private List<String> permissions;
	private Date expiration;

	public JwtClaims() {}

	public JwtClaims(String sub, String typ, String rid, List<String> roles, List<String> permissions) {
		this.sub = sub;
		this.typ = typ;
		this.rid = rid;
		this.roles = roles;
		this.permissions = permissions;
	}

	public JwtClaims(String sub, String typ, String rid, List<String> roles, List<String> permissions, Date expiration) {
		this.sub = sub;
		this.typ = typ;
		this.rid = rid;
		this.roles = roles;
		this.permissions = permissions;
		this.expiration = expiration;
	}

	public String getSub() {
		return sub;
	}

	public void setSub(String sub) {
		this.sub = sub;
	}

	public String getTyp() {
		return typ;
	}

	public void setTyp(String typ) {
		this.typ = typ;
	}

	public String getRid() {
		return rid;
	}

	public void setRid(String rid) {
		this.rid = rid;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public List<String> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<String> permissions) {
		this.permissions = permissions;
	}

	public Date getExpiration() {
		return expiration;
	}

	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}
}
